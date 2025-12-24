package com.example.ordermanager.order.internal;

import com.example.ordermanager.events.OrderCancelled;
import com.example.ordermanager.events.OrderCreated;
import com.example.ordermanager.events.OrderDelivered;
import com.example.ordermanager.events.OrderShipped;
import com.example.ordermanager.events.OrderStatusUpdated;
import com.example.ordermanager.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventProcessor.class);

    public OrderEventProcessor(OrderRepository orderRepository) {
    }

    @Async("applicationEventExecutor")
    @EventListener
    public void onOrderCreated(OrderCreated event) {
        LOGGER.info("Processing OrderCreated event for order ID: {}", event.orderId());
        // Additional business logic can be implemented here
    }

    @Async("applicationEventExecutor")
    @EventListener
    public void onOrderStatusUpdated(OrderStatusUpdated event) {
        LOGGER.info("Processing OrderStatusUpdated event for order ID: {} ({} -> {})",
                event.orderId(), event.oldStatus(), event.newStatus());
        // Additional business logic can be implemented here
    }

    @Async("applicationEventExecutor")
    @EventListener
    public void onOrderShipped(OrderShipped event) {
        LOGGER.info("Processing OrderShipped event for order ID: {}", event.orderId());
        // Additional business logic can be implemented here
    }

    @Async("applicationEventExecutor")
    @EventListener
    public void onOrderDelivered(OrderDelivered event) {
        LOGGER.info("Processing OrderDelivered event for order ID: {}", event.orderId());
        // Additional business logic can be implemented here
    }

    @Async("applicationEventExecutor")
    @EventListener
    public void onOrderCancelled(OrderCancelled event) {
        LOGGER.info("Processing OrderCancelled event for order ID: {} (Reason: {})",
                event.orderId(), event.reason());
        // Additional business logic can be implemented here
    }
}