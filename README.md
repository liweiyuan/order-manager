# Order Manager - Spring Modulith Implementation

This is a modular monolith application built with Spring Boot and Spring Modulith, demonstrating best practices for building scalable applications with clear module boundaries.

## Architecture Overview

The application is structured into 4 distinct modules:

### 1. Order Module (`com.example.ordermanager.order`)
- Core module responsible for order management
- Handles order creation, updates, and lifecycle management
- Publishes events to other modules
- Depends on: customer, inventory, events

### 2. Customer Module (`com.example.ordermanager.customer`)
- Manages customer data and operations
- Listens to order events for customer-related notifications
- Depends on: events

### 3. Inventory Module (`com.example.ordermanager.inventory`)
- Manages inventory items and stock levels
- Processes order events to update inventory
- Depends on: events

### 4. Events Module (`com.example.ordermanager.events`)
- Central module for shared event definitions
- Provides type-safe event contracts between modules
- No dependencies on other modules

## Event-Driven Architecture

The application uses an asynchronous event-driven architecture with the following events:

- `OrderCreated` - Triggered when a new order is created
- `OrderStatusUpdated` - Triggered when an order status changes
- `OrderShipped` - Specific event when an order is shipped
- `OrderDelivered` - Specific event when an order is delivered
- `OrderCancelled` - Specific event when an order is cancelled

## Module Dependencies

```
Order → Customer (via events)
Order → Inventory (via events) 
Customer → Events
Inventory → Events
```

## Key Features

- **Asynchronous Processing**: All events are processed asynchronously using `@Async` and `@ApplicationModuleListener`
- **Loose Coupling**: Modules communicate only through well-defined events
- **Type Safety**: Events are implemented as records with compile-time validation
- **Modularity**: Clear boundaries between modules enforced by Spring Modulith
- **Documentation**: Automatic module documentation generation

## Development Guidelines

### Adding New Events
1. Create the event as a record in the `events` module
2. Add the event to the `OrderEventPublisher` interface
3. Implement the event publishing method in `DefaultOrderEventPublisher`
4. Add listeners in the appropriate modules using `@ApplicationModuleListener`

### Adding New Modules
1. Create a new package for the module
2. Add a `package-info.java` with appropriate `@ApplicationModule` annotation
3. Define dependencies in the annotation
4. Use events for communication with other modules

## Running the Application

```bash
mvn spring-boot:run
```

## Testing

Run all tests:
```bash
mvn test
```

Verify module structure:
```bash
mvn test -Dtest=ApplicationModularityTest
```

Generate module documentation:
```bash
mvn test -Dtest=ApplicationModularityTest#createDocumentation
```

## API Endpoints

- `GET /orders` - Get all orders
- `GET /orders/{id}` - Get order by ID
- `POST /orders` - Create new order
- `PUT /orders/{id}/status` - Update order status
- `DELETE /orders/{id}` - Delete order

## Testing

Run all tests:
```bash
mvn test
```

Verify module structure:
```bash
mvn test -Dtest=ApplicationModularityTest
```

Generate module documentation:
```bash
mvn test -Dtest=ApplicationModularityTest#createDocumentation
```

### API Integration Testing

The project includes a comprehensive test script that tests all API endpoints:

```bash
# Make sure the application is running first:
mvn spring-boot:run

# In another terminal, run the test script:
./test.sh
```

The test script will:
1. Verify the server is running
2. Create test customer and inventory items
3. Create test orders
4. Test all CRUD operations
5. Test order status updates
6. Clean up by deleting test data