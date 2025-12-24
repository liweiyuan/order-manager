package com.example.ordermanager.customer.internal;

import com.example.ordermanager.customer.Customer;
import com.example.ordermanager.customer.CustomerManagement;
import com.example.ordermanager.customer.CustomerRepository;
import com.example.ordermanager.events.OrderCancelled;
import com.example.ordermanager.events.OrderCreated;
import com.example.ordermanager.events.OrderDelivered;
import com.example.ordermanager.events.OrderShipped;
import com.example.ordermanager.events.OrderStatusUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class CustomerManagementImpl implements CustomerManagement {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerManagementImpl.class);
  private final CustomerRepository repository;

  CustomerManagementImpl(CustomerRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Customer> findById(Long id) {
    return repository.findById(id);
  }

  @Async("applicationEventExecutor")
  @EventListener
  void on(OrderCreated event) {
    LOGGER.info("Received order confirmation for {}.", event.orderId());
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
  }
}