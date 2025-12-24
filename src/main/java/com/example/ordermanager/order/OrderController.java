package com.example.ordermanager.order;

import com.example.ordermanager.api.ApiResponse;
import com.example.ordermanager.customer.Customer;
import com.example.ordermanager.customer.CustomerManagement;
import com.example.ordermanager.events.LineItemData;
import com.example.ordermanager.inventory.InventoryItem;
import com.example.ordermanager.inventory.InventoryManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
  private final OrderRepository orderRepository;
  private final CustomerManagement customerManagement;
  private final InventoryManagement inventoryManagement;
  private final OrderEventPublisher orderEventPublisher;

  public OrderController(
      OrderRepository orderRepository,
      CustomerManagement customerManagement,
      InventoryManagement inventoryManagement,
      OrderEventPublisher orderEventPublisher) {
    this.orderRepository = orderRepository;
    this.customerManagement = customerManagement;
    this.inventoryManagement = inventoryManagement;
    this.orderEventPublisher = orderEventPublisher;
  }

  @GetMapping
  public ApiResponse<List<Order>> getAllOrders() {
    return ApiResponse.success(orderRepository.findAll());
  }

  @GetMapping("/{id}")
  public ApiResponse<Order> getOrderById(@PathVariable Long id) {
    Optional<Order> orderOpt = orderRepository.findById(id);
    return orderOpt
        .map(ApiResponse::success)
        .orElseGet(() -> ApiResponse.notFound("Order not found with id: " + id));
  }

  @PostMapping
  public ApiResponse<Order> createOrder(@RequestBody OrderRequest orderRequest) {
    try {
      Optional<Customer> customerOpt = customerManagement.findById(orderRequest.customerId());
      if (customerOpt.isEmpty()) {
        return ApiResponse.badRequest("Customer not found with id: " + orderRequest.customerId());
      }

      Order order = new Order(customerOpt.get());

      for (LineItemRequest itemRequest : orderRequest.items()) {
        Optional<InventoryItem> inventoryItemOpt =
            inventoryManagement.findById(itemRequest.inventoryItemId());
        if (inventoryItemOpt.isEmpty()) {
          return ApiResponse.badRequest(
              "Inventory item not found with id: " + itemRequest.inventoryItemId());
        }
        // For simplicity, we are not checking stock here, but we should in a real app
        // Also, we'll just use a fixed price for now
        order.addLineItem(
            itemRequest.inventoryItemId(), itemRequest.quantity(), new BigDecimal("10.00"));
      }

      var result = orderRepository.save(order);

      List<LineItemData> lineItemDataList =
          result.getLineItems().stream()
              .map(
                  item ->
                      new LineItemData(
                          item.getInventoryItemId(), item.getQuantity(), item.getPrice()))
              .collect(Collectors.toList());

      orderEventPublisher.publishOrderCreated(result.getId(), lineItemDataList);

      return ApiResponse.success("Order created successfully", result);
    } catch (Exception e) {
      LOGGER.error("Error creating order: ", e);
      return ApiResponse.serverError("Failed to create order: " + e.getMessage());
    }
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Order> updateOrderStatus(
      @PathVariable Long id, @RequestBody StatusUpdateRequest statusUpdate) {
    try {
      Optional<Order> orderOpt = orderRepository.findById(id);
      if (orderOpt.isEmpty()) {
        return ApiResponse.notFound("Order not found with id: " + id);
      }
      Order order = orderOpt.get();
      String oldStatus = order.getStatus();
      String newStatus = statusUpdate.status();

      order.setStatus(newStatus);
      Order savedOrder = orderRepository.save(order);

      // Publish status updated event
      orderEventPublisher.publishOrderStatusUpdated(savedOrder.getId(), oldStatus, newStatus);

      // Publish specific events based on status
      if ("SHIPPED".equalsIgnoreCase(newStatus)) {
        orderEventPublisher.publishOrderShipped(savedOrder.getId());
      } else if ("DELIVERED".equalsIgnoreCase(newStatus)) {
        orderEventPublisher.publishOrderDelivered(savedOrder.getId());
      } else if ("CANCELLED".equalsIgnoreCase(newStatus)) {
        orderEventPublisher.publishOrderCancelled(
            savedOrder.getId(), "Order status updated to cancelled");
      }

      return ApiResponse.success("Order status updated successfully", savedOrder);
    } catch (Exception e) {
      LOGGER.error("Error updating order status: ", e);
      return ApiResponse.serverError("Failed to update order status: " + e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
    if (orderRepository.existsById(id)) {
      orderRepository.deleteById(id);
      return ApiResponse.success(204, "Order deleted successfully", null);
    } else {
      return ApiResponse.notFound("Order not found with id: " + id);
    }
  }
}

record OrderRequest(Long customerId, List<LineItemRequest> items) {}

record LineItemRequest(Long inventoryItemId, int quantity) {}

record StatusUpdateRequest(String status) {}
