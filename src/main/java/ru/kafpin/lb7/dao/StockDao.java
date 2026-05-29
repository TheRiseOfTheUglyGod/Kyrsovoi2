package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Stock;
import java.util.List;
import java.util.Optional;

/**
 * Управление остатками товаров в ячейках.
 */
public interface StockDao {
    /**
     * Получить текущий остаток товара в конкретной ячейке.
     * @return количество или Optional.empty(), если записи нет
     */
    Optional<Integer> getQuantity(Long productId, Long cellId);

    /**
     * Увеличить остаток (поступление).
     */
    void increase(Long productId, Long cellId, int quantity);

    /**
     * Уменьшить остаток (отгрузка). Бросает RuntimeException, если остатка недостаточно.
     */
    void decrease(Long productId, Long cellId, int quantity);

    /**
     * Установить точное количество (используется при корректировках инвентаризации).
     */
    void setQuantity(Long productId, Long cellId, int quantity);

    /**
     * Все ячейки, где хранится товар, с количеством.
     */
    List<Stock> findByProduct(Long productId);
}