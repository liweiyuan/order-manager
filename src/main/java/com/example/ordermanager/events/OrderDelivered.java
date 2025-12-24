package com.example.ordermanager.events;

import org.springframework.util.Assert;

public record OrderDelivered(Long orderId) {
    public OrderDelivered {
        Assert.notNull(orderId, "OrderId must not be null!");
    }
}