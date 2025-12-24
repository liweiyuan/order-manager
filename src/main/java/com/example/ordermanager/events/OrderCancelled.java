package com.example.ordermanager.events;

import org.springframework.util.Assert;

public record OrderCancelled(Long orderId, String reason) {
  public OrderCancelled {
    Assert.notNull(orderId, "OrderId must not be null!");
    Assert.hasText(reason, "Reason must not be blank!");
  }
}
