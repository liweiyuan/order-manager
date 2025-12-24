package com.example.ordermanager.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import org.springframework.util.Assert;

@Entity
public class LineItem {

  @Id @GeneratedValue private Long id;

  private Long inventoryItemId;
  private int quantity;
  private BigDecimal price; // Price at the time of order

  protected LineItem() {
    // JPA an-construction
  }

  public LineItem(Long inventoryItemId, int quantity, BigDecimal price) {
    Assert.notNull(inventoryItemId, "Inventory Item ID must not be null!");
    Assert.isTrue(quantity > 0, "Quantity must be greater than 0!");
    Assert.notNull(price, "Price must not be null!");

    this.inventoryItemId = inventoryItemId;
    this.quantity = quantity;
    this.price = price;
  }

  public Long getId() {
    return id;
  }

  public Long getInventoryItemId() {
    return inventoryItemId;
  }

  public int getQuantity() {
    return quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getTotal() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }
}
