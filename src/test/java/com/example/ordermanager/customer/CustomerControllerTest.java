package com.example.ordermanager.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CustomerRepository customerRepository;

  // We also need to mock CustomerManagement because it is a dependency of
  // OrderController
  // and if CustomerManagement is not mocked, the context will fail to load all
  // required beans
  @MockitoBean
  private CustomerManagement customerManagement;

  @Test
  void getAllCustomersShouldReturnCustomers() throws Exception {
    Customer customer1 = new Customer("John Doe", "john.doe@example.com");
    customer1.setId(1L);
    Customer customer2 = new Customer("Jane Smith", "jane.smith@example.com");
    customer2.setId(2L);
    List<Customer> allCustomers = Arrays.asList(customer1, customer2);

    when(customerRepository.findAll()).thenReturn(allCustomers);

    mockMvc
        .perform(get("/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(1L))
        .andExpect(jsonPath("$.data[0].name").value("John Doe"))
        .andExpect(jsonPath("$.data[1].id").value(2L))
        .andExpect(jsonPath("$.data[1].name").value("Jane Smith"));
  }
}
