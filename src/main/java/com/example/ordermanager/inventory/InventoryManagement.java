package com.example.ordermanager.inventory;

import java.util.Optional;

public interface InventoryManagement {
  Optional<InventoryItem> findById(Long id);
}
