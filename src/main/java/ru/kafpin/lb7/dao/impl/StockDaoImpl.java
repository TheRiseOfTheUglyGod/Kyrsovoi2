package ru.kafpin.lb7.dao.impl;

import ru.kafpin.lb7.dao.StockDao;
import ru.kafpin.lb7.model.Stock;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockDaoImpl implements StockDao {

    @Override
    public Optional<Integer> getQuantity(Long productId, Long cellId) {
        String sql = SqlQueries.get("stocks.getQuantity");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, cellId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getInt("quantity"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public void increase(Long productId, Long cellId, int quantity) {
        String sql = "CALL public.sp_increase_stock(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, cellId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка увеличения остатка", e);
        }
    }

    @Override
    public void decrease(Long productId, Long cellId, int quantity) {
        String sql = "CALL public.sp_decrease_stock(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, cellId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка уменьшения остатка", e);
        }
    }

    @Override
    public void setQuantity(Long productId, Long cellId, int quantity) {
        String sql = SqlQueries.get("stocks.setQuantity");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setLong(3, cellId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка установки количества", e);
        }
    }

    @Override
    public List<Stock> findByProduct(Long productId) {
        List<Stock> list = new ArrayList<>();
        String sql = SqlQueries.get("stocks.findByProduct");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stock s = new Stock();
                    s.setStockId(rs.getLong("stock_id"));
                    s.setProductId(rs.getLong("product_id"));
                    s.setCellId(rs.getLong("cell_id"));
                    s.setQuantity(rs.getInt("quantity"));
                    s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    list.add(s);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}