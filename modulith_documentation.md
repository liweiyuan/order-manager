# 订单管理系统 - 项目文档

## 1. 项目概述

订单管理系统是一个Spring Boot应用程序，旨在管理客户、库存和订单相关的操作。它使用Spring Modulith演示了模块化单体架构。

*   **目的：** 提供一个健壮且可维护的系统来处理订单、客户和库存。
*   **技术栈：**
    *   **语言：** Java 17
    *   **框架：** Spring Boot 3.5.4
    *   **模块化：** Spring Modulith 1.4.6
    *   **数据持久化：** Spring Data JPA with Hibernate
    *   **数据库：** H2 (开发用内存数据库)
    *   **构建工具：** Apache Maven
    *   **API：** RESTful Web Services

## 2. 基于Spring Modulith的模块化架构

Spring Modulith用于将应用程序构建成定义良好、相互独立的模块。这种方法有助于管理复杂性、提高可维护性，并在不同的业务能力之间强制执行清晰的边界，即使它们部署在同一个单元中。

### 2.1. 定义的模块

应用程序被分解为以下模块：

*   **`customer` (客户模块)**：管理客户相关的信息和操作。
*   **`inventory` (库存模块)**：处理库存物品、库存水平和相关流程。
*   **`order` (订单模块)**：管理客户订单的创建、处理和状态。
*   **`events` (事件模块)**：一个专门用于定义共享领域事件的模块，旨在实现其他模块之间的松散耦合。

### 2.2. 模块定义 (`package-info.java`)

每个模块都使用其根包中的 `package-info.java` 文件通过 `@ApplicationModule` 注解明确定义。

**示例：`com.example.ordermanager.customer.package-info.java`**
```java
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = "events"
)
package com.example.ordermanager.customer;
```

### 2.3. 模块依赖和关系

Spring Modulith强制执行模块之间明确的依赖规则，防止意外的耦合。依赖关系使用 `@ApplicationModule` 中的 `allowedDependencies` 属性声明。

*   **`order` 模块的依赖：**
    *   依赖于 `customer` (获取客户详细信息)。
    *   依赖于 `inventory` (检查/管理库存物品)。
    *   依赖于 `events` (发布 `OrderCreated` 事件和相关DTO)。
    ```java
    @org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"customer", "inventory", "events"}
    )
    package com.example.ordermanager.order;
    ```

*   **`customer` 模块的依赖：**
    *   仅依赖于 `events` (消费 `OrderCreated` 事件)。
    ```java
    @org.springframework.modulith.ApplicationModule(
        allowedDependencies = "events"
    )
    package com.example.ordermanager.customer;
    ```

*   **`inventory` 模块的依赖：**
    *   仅依赖于 `events` (消费 `OrderCreated` 事件)。
    ```java
    @org.springframework.modulith.ApplicationModule(
        allowedDependencies = "events"
    )
    package com.example.ordermanager.inventory;
    ```

*   **`events` 模块的依赖：**
    *   此模块定义共享事件DTO，没有出站依赖。
    ```java
    @org.springframework.modulith.ApplicationModule
    package com.example.ordermanager.events;
    ```

## 4. 事件驱动通信

为了实现松散耦合，尤其是在模块响应其他模块中的操作的场景中，项目利用了Spring Modulith的事件发布和订阅机制。

*   **`events` 模块：** 此模块对于定义共享事件对象至关重要。
    *   `LineItemData`：一个简单的记录 (DTO)，表示行项目详细信息，用于事件中。
    ```java
    package com.example.ordermanager.events;
    import java.math.BigDecimal;
    public record LineItemData(Long inventoryItemId, int quantity, BigDecimal price) {}
    ```
    *   `OrderCreated`：一个记录 (DTO)，表示创建新订单时的事件，包含 `orderId` 和 `LineItemData` 列表。
    ```java
    package com.example.ordermanager.events;
    import java.util.List;
    import org.springframework.util.Assert;
    public record OrderCreated(Long orderId, List<LineItemData> lineItemsData) {
        public OrderCreated {
            Assert.notNull(orderId, "OrderId must not be null!");
            Assert.notNull(lineItemsData, "LineItemsData must not be null!");
        }
    }
    ```

*   **事件发布：** `OrderController` 在成功保存订单后发布 `OrderCreated` 事件。它将JPA实体 (`LineItem`) 转换为事件特定的DTO (`LineItemData`)，以避免跨模块边界泄露持久化关注点。
    ```java
    // 在 OrderController.java 中
    var result = orderRepository.save(order);
    List<LineItemData> lineItemDataList = result.getLineItems().stream()
            .map(item -> new LineItemData(item.getInventoryItemId(), item.getQuantity(), item.getPrice()))
            .collect(Collectors.toList());
    events.publishEvent(new OrderCreated(result.getId(), lineItemDataList));
    ```

*   **异步事件消费：** 对 `OrderCreated` 事件感兴趣的模块 (如 `customer` 和 `inventory`) 在其 `internal` 服务中的方法上使用 `@ApplicationModuleListener` 注解。关键是，这些监听器被标记为 `@Async` 以异步处理事件。这有助于：
    *   **解耦执行：** 发布者不等待监听器完成。
    *   **打破静态循环：** 从Spring Modulith的静态分析角度来看，异步监听器不会在消费模块和发布模块之间创建直接的依赖循环 (因为依赖是事件DTO，它位于独立的 `events` 模块中)。

    **示例：`com.example.ordermanager.inventory.internal.InventoryService.java`**
    ```java
    package com.example.ordermanager.inventory.internal;
    // ... 导入，包括 com.example.ordermanager.events.OrderCreated
    import org.springframework.scheduling.annotation.Async; // 重要!

    @Service
    @Transactional
    class InventoryService {
        // ... 构造函数和其他方法

        @Async // 异步处理事件
        @ApplicationModuleListener
        void on(OrderCreated event) {
            LOGGER.info("收到订单确认，订单ID为 {}.", event.orderId());
            event.lineItemsData().forEach(item -> {
                // 根据 item.inventoryItemId() 和 item.quantity() 更新库存的逻辑
            });
        }
    }
    ```
    为了启用 `@Async`，主应用程序类 `OrderManagerApplication` 使用 `@EnableAsync` 进行注解。

## 5. 模块化验证和文档

Spring Modulith提供了强大的测试能力，以确保模块化架构随着时间的推移保持不变。

*   **`ApplicationModularityTest.java`**：这个位于 `src/test/java` 中的测试类验证了模块结构。
    *   `verifiesModuleStructure()`：使用 `ApplicationModules.of(OrderManagerApplication.class).verify()` 静态分析代码库，查找模块边界冲突和非法依赖。此测试现在已通过，确认了正确的模块化。
    *   `createDocumentation()`：生成模块结构的视觉文档。此文档包含PlantUML图表和其他工件，在 `target/spring-modulith-docs` 目录中提供了应用程序设计的清晰概述。

## 6. 总结

通过采用Spring Modulith，订单管理系统已从一个简单的单体Spring Boot应用程序转变为一个结构化的模块化单体。此次重构已：
*   **改进了代码组织：** 将关注点清晰地分离到不同的模块中。
*   **增强了可维护性：** 更容易理解、测试和演进单个业务能力。
*   **强制执行了边界：** 静态分析可防止意外的直接依赖，促进更健康的 codebase。
*   **促进了解耦：** 事件驱动的通信减少了模块之间的紧密耦合，使它们更加独立。

这种方法为未来的开发奠定了坚实的基础，使应用程序能够更有效地扩展和适应。
