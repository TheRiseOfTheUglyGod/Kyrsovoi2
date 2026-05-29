package ru.kafpin.lb7.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.lb7.dao.ProductDao;
import ru.kafpin.lb7.model.Product;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductDaoImpl.class);

    @Override
    public Optional<Product> findById(Long id) {
        String sql = SqlQueries.get("products.findById");
        logger.debug("Поиск товара по ID: {}", id);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска товара по ID {}", id, e);
            throw new RuntimeException("Ошибка поиска товара по ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        String sql = SqlQueries.get("products.findAll");
        logger.debug("Получение всех товаров");
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
            logger.debug("Найдено {} товаров", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка товаров", e);
            throw new RuntimeException("Ошибка получения списка товаров", e);
        }
        return list;
    }

    @Override
    public List<Product> search(String keyword) {
        String sql = SqlQueries.get("products.search");
        logger.debug("Поиск товаров по ключевому слову: {}", keyword);
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            logger.debug("Найдено {} товаров по запросу '{}'", list.size(), keyword);
        } catch (SQLException e) {
            logger.error("Ошибка поиска товаров по ключу '{}'", keyword, e);
            throw new RuntimeException("Ошибка поиска товаров", e);
        }
        return list;
    }

    @Override
    public void create(Product product) {
        String sql = SqlQueries.get("products.create");
        logger.info("Создание товара: {}", product.getName());
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getArticle());
            ps.setString(3, product.getUnit());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getPhotoPath());
            ps.setInt(6, product.getMinStock());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setProductId(keys.getLong(1));
                }
            }
            logger.info("Товар '{}' создан с ID {}", product.getName(), product.getProductId());
        } catch (SQLException e) {
            logger.error("Ошибка создания товара", e);
            throw new RuntimeException("Ошибка создания товара", e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = SqlQueries.get("products.update");
        logger.info("Обновление товара ID {}", product.getProductId());
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getArticle());
            ps.setString(3, product.getUnit());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getPhotoPath());
            ps.setInt(6, product.getMinStock());
            ps.setLong(7, product.getProductId());
            ps.executeUpdate();
            logger.info("Товар ID {} обновлён", product.getProductId());
        } catch (SQLException e) {
            logger.error("Ошибка обновления товара ID {}", product.getProductId(), e);
            throw new RuntimeException("Ошибка обновления товара", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = SqlQueries.get("products.delete");
        logger.info("Удаление товара ID {}", id);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            logger.info("Товар ID {} удалён", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления товара ID {}", id, e);
            throw new RuntimeException("Ошибка удаления товара", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getLong("product_id"));
        p.setName(rs.getString("name"));
        p.setArticle(rs.getString("article"));
        p.setUnit(rs.getString("unit"));
        p.setDescription(rs.getString("description"));
        p.setPhotoPath(rs.getString("photo_path"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return p;
    }
}