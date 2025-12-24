package com.example.ordermanager.inventory.internal;

import com.example.ordermanager.inventory.InventoryItem;
import com.example.ordermanager.inventory.InventoryManagement;
import com.example.ordermanager.inventory.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class InventoryManagementImpl implements InventoryManagement {

  private final InventoryRepository repository;

  InventoryManagementImpl(InventoryRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<InventoryItem> findById(Long id) {
    return repository.findById(id);
  }
}
