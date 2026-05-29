package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface SupplierDao {
    Optional<Supplier> findById(Long id);
    List<Supplier> findAll();
    void create(Supplier supplier);
    void update(Supplier supplier);
    void delete(Long id);
}