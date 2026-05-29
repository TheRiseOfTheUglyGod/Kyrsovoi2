package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.StorageCell;
import java.util.List;
import java.util.Optional;

public interface StorageCellDao {
    Optional<StorageCell> findById(Long id);
    List<StorageCell> findAll();
    void create(StorageCell cell);
    void update(StorageCell cell);
    void delete(Long id);
}