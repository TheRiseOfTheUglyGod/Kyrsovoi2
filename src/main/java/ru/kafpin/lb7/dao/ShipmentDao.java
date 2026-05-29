package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Shipment;
import ru.kafpin.lb7.model.ShipmentItem;
import java.util.List;
import java.util.Optional;

public interface ShipmentDao {
    void save(Shipment shipment, List<ShipmentItem> items);
    Optional<Shipment> findById(Long id);
    List<Shipment> findAll();
}