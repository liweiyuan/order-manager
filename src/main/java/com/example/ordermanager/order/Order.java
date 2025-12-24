package com.example.ordermanager.order;

import com.example.ordermanager.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.Assert;

@Entity
@Table(name = "orders") // Renamed to avoid conflicts with SQL keyword ORDER
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private Customer customer;

  private LocalDateTime orderDate;
  private String status; // e.g., PENDING, SHIPPED, DELIVERED

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LineItem> lineItems = new ArrayList<>();

  public Order(Customer customer) {
    Assert.notNull(customer, "Customer must not be null!");
    this.customer = customer;
    this.orderDate = LocalDateTime.now();
    this.status = "PENDING";
  }

  protected Order() {}

  public void addLineItem(Long inventoryItemId, int quantity, BigDecimal price) {
    this.lineItems.add(new LineItem(inventoryItemId, quantity, price));
  }

  public BigDecimal getTotalAmount() {
    return lineItems.stream().map(LineItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Getters and basic Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Customer getCustomer() {
    return customer;
  }

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<LineItem> getLineItems() {
    return lineItems;
  }

  @Override
  public String toString() {
    return "Order{"
        + "id="
        + id
        + ", customer="
        + customer.getName()
        + ", orderDate="
        + orderDate
        + ", status='"
        + status
        + "'"
        + ", totalAmount="
        + getTotalAmount()
        + '}';
  }
}