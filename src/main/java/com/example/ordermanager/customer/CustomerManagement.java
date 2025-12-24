package com.example.ordermanager.customer;

import java.util.Optional;

public interface CustomerManagement {
  Optional<Customer> findById(Long id);
}
