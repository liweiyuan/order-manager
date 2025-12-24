package com.example.ordermanager.events;

import org.springframework.util.Assert;

public record OrderShipped(Long orderId) {
    public OrderShipped {
        Assert.notNull(orderId, "OrderId must not be null!");
    }
}