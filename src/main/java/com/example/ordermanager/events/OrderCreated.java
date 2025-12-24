package com.example.ordermanager.events;

import org.springframework.util.Assert;

import java.util.List;

public record OrderCreated(Long orderId, List<LineItemData> lineItemsData) {
  public OrderCreated {
    Assert.notNull(orderId, "OrderId must not be null!");
    Assert.notNull(lineItemsData, "LineItemsData must not be null!");
  }
}
