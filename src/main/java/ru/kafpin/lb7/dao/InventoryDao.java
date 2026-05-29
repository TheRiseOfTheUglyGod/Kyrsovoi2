package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Inventory;
import ru.kafpin.lb7.model.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryDao {
    void createInventory(Inventory inventory, List<InventoryItem> items);
    void updateActualQuantity(Long inventoryItemId, int actualQuantity);
    void completeInventory(Long inventoryId);
    Optional<Inventory> findById(Long id);
    List<Inventory> findAll();
}