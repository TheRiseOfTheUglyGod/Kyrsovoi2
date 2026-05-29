package ru.kafpin.lb7.dao;

import ru.kafpin.lb7.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с товарами.
 * Все методы могут выбросить {@link RuntimeException} при ошибках SQL.
 */
public interface ProductDao {
    /**
     * Найти товар по идентификатору.
     * @param id уникальный ID товара
     * @return товар или Optional.empty()
     */
    Optional<Product> findById(Long id);

    /** @return все товары */
    List<Product> findAll();

    /**
     * Поиск товаров по части названия или артикулу.
     * @param keyword поисковый запрос
     * @return список подходящих товаров
     */
    List<Product> search(String keyword);

    void create(Product product);
    void update(Product product);
    void delete(Long id);
}