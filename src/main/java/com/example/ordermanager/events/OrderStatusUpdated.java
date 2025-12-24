package com.example.ordermanager.events;

import org.springframework.util.Assert;

public record OrderStatusUpdated(Long orderId, String oldStatus, String newStatus) {
    public OrderStatusUpdated {
        Assert.notNull(orderId, "OrderId must not be null!");
        Assert.hasText(oldStatus, "Old status must not be blank!");
        Assert.hasText(newStatus, "New status must not be blank!");
    }
}