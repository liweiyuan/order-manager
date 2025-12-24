package com.example.ordermanager.order;

import com.example.ordermanager.events.LineItemData;
import java.util.List;

public interface OrderEventPublisher {
  void publishOrderCreated(Long orderId, List<LineItemData> lineItemDataList);
  void publishOrderStatusUpdated(Long orderId, String oldStatus, String newStatus);
  void publishOrderShipped(Long orderId);
  void publishOrderDelivered(Long orderId);
  void publishOrderCancelled(Long orderId, String reason);
}