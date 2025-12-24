package com.example.ordermanager.customer;

import com.example.ordermanager.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
class CustomerController {

  private final CustomerRepository customers;

  CustomerController(CustomerRepository customers) {
    this.customers = customers;
  }

  @GetMapping
  ApiResponse<Iterable<Customer>> getAllCustomers() {
    return ApiResponse.success(customers.findAll());
  }

  @PostMapping
  ApiResponse<Customer> createCustomer(@RequestBody Customer customer) {
    return ApiResponse.success(customers.save(customer));
  }
}