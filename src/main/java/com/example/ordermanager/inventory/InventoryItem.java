package com.example.ordermanager.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.util.Assert;

@Entity
public class InventoryItem {

  @Id
  @GeneratedValue
  private Long id;

  private String name;
  private String description;
  private int stock;

  protected InventoryItem() {
    // JPA an-construction
  }

  public InventoryItem(String name, String description, int stock) {
    Assert.hasText(name, "Name must not be null or empty!");
    Assert.hasText(description, "Description must not be null or empty!");
    Assert.isTrue(stock >= 0, "Stock must be greater than or equal to 0!");

    this.name = name;
    this.description = description;
    this.stock = stock;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }
}
