package com.example.ordermanager.inventory.internal;

import com.example.ordermanager.events.OrderCancelled;
import com.example.ordermanager.events.OrderCreated;
import com.example.ordermanager.events.OrderDelivered;
import com.example.ordermanager.events.OrderShipped;
import com.example.ordermanager.events.OrderStatusUpdated;
import com.example.ordermanager.inventory.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class InventoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

  private final InventoryRepository inventory;

  InventoryService(InventoryRepository inventory) {
    this.inventory = inventory;
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderCreated event) {

    LOGGER.info("Received order confirmation for {}.", event.orderId());

    event
        .lineItemsData()
        .forEach(
            item -> {
              inventory
                  .findById(item.inventoryItemId())
                  .ifPresent(
                      it -> {
                        it.setStock(it.getStock() - item.quantity());
                        inventory.save(it);
                        LOGGER.info(
                            "Updated stock for inventory item {}: {}.",
                            item.inventoryItemId(),
                            it.getStock());
                      });
            });
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderStatusUpdated event) {
    LOGGER.info("Order {} status updated from {} to {}.", event.orderId(), event.oldStatus(), event.newStatus());
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderShipped event) {
    LOGGER.info("Order {} has been shipped.", event.orderId());
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderDelivered event) {
    LOGGER.info("Order {} has been delivered.", event.orderId());
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderCancelled event) {
    LOGGER.info("Order {} has been cancelled. Reason: {}", event.orderId(), event.reason());
    // When an order is cancelled, we might want to restore inventory
    LOGGER.warn("TODO: Implement inventory restoration logic for cancelled orders");
  }
}