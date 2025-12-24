package com.example.ordermanager.order.internal;

import com.example.ordermanager.events.LineItemData;
import com.example.ordermanager.events.OrderCancelled;
import com.example.ordermanager.events.OrderCreated;
import com.example.ordermanager.events.OrderDelivered;
import com.example.ordermanager.events.OrderShipped;
import com.example.ordermanager.events.OrderStatusUpdated;
import com.example.ordermanager.order.OrderEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class DefaultOrderEventPublisher implements OrderEventPublisher {
  private final ApplicationEventPublisher events;

  DefaultOrderEventPublisher(ApplicationEventPublisher events) {
    this.events = events;
  }

  @Override
  public void publishOrderCreated(Long orderId, List<LineItemData> lineItemDataList) {
    events.publishEvent(new OrderCreated(orderId, lineItemDataList));
  }

  @Override
  public void publishOrderStatusUpdated(Long orderId, String oldStatus, String newStatus) {
    events.publishEvent(new OrderStatusUpdated(orderId, oldStatus, newStatus));
  }

  @Override
  public void publishOrderShipped(Long orderId) {
    events.publishEvent(new OrderShipped(orderId));
  }

  @Override
  public void publishOrderDelivered(Long orderId) {
    events.publishEvent(new OrderDelivered(orderId));
  }

  @Override
  public void publishOrderCancelled(Long orderId, String reason) {
    events.publishEvent(new OrderCancelled(orderId, reason));
  }
}