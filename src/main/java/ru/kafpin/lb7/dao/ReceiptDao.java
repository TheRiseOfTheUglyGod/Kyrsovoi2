package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Receipt;
import ru.kafpin.lb7.model.ReceiptItem;
import java.util.List;
import java.util.Optional;

/**
 * Работа с приходными накладными.
 */
public interface ReceiptDao {
    /**
     * Создать накладную и все её позиции в одной транзакции.
     * Также обновляет остатки через StockDao.
     */
    void save(Receipt receipt, List<ReceiptItem> items);

    Optional<Receipt> findById(Long id);
    List<Receipt> findAll();
}