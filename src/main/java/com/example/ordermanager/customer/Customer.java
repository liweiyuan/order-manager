package com.example.ordermanager.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.util.Assert;

@Entity
public class Customer {

  @Id @GeneratedValue private Long id;

  private String name;
  private String email;

  protected Customer() {
    // JPA an-construction
  }

  public Customer(String name, String email) {
    Assert.hasText(name, "Name must not be null or empty!");
    Assert.hasText(email, "Email must not be null or empty!");

    this.name = name;
    this.email = email;
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

  public String getEmail() {
    return email;
  }
}
