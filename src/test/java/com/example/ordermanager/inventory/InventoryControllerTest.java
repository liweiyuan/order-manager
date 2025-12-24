package com.example.ordermanager.inventory;

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

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private InventoryRepository inventoryRepository;

  // We also need to mock InventoryManagement because it is a dependency of
  // OrderController
  // and if InventoryManagement is not mocked, the context will fail to load all
  // required beans
  @MockitoBean
  private InventoryManagement inventoryManagement;

  @Test
  void getAllInventoryItemsShouldReturnItems() throws Exception {
    InventoryItem item1 = new InventoryItem("Laptop", "Powerful laptop for gaming and work", 10);
    item1.setId(1L);
    InventoryItem item2 = new InventoryItem("Mouse", "Ergonomic wireless mouse", 50);
    item2.setId(2L);
    List<InventoryItem> allItems = Arrays.asList(item1, item2);

    when(inventoryRepository.findAll()).thenReturn(allItems);

    mockMvc
        .perform(get("/inventory"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(1L))
        .andExpect(jsonPath("$.data[0].name").value("Laptop"))
        .andExpect(jsonPath("$.data[1].id").value(2L))
        .andExpect(jsonPath("$.data[1].name").value("Mouse"));
  }
}
