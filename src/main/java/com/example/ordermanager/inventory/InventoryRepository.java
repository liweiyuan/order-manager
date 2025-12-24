package com.example.ordermanager.inventory;

import org.springframework.data.repository.CrudRepository;

public interface InventoryRepository extends CrudRepository<InventoryItem, Long> {
}
