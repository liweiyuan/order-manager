package com.example.ordermanager.inventory;

import com.example.ordermanager.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
class InventoryController {

  private final InventoryRepository inventory;

  InventoryController(InventoryRepository inventory) {
    this.inventory = inventory;
  }

  @GetMapping
  ApiResponse<Iterable<InventoryItem>> getAllInventoryItems() {
    return ApiResponse.success(inventory.findAll());
  }

  @PostMapping
  ApiResponse<InventoryItem> createInventoryItem(@RequestBody InventoryItem inventoryItem) {
    if (inventoryItem == null) {
      return ApiResponse.error("Inventory item cannot be null");
    }
    return ApiResponse.success(inventory.save(inventoryItem));
  }
}
