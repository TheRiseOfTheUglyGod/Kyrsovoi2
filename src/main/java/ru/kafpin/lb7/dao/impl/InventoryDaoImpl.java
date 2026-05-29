package ru.kafpin.lb7.dao.impl;

import ru.kafpin.lb7.dao.InventoryDao;
import ru.kafpin.lb7.model.Inventory;
import ru.kafpin.lb7.model.InventoryItem;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDaoImpl implements InventoryDao {

    @Override
    public void createInventory(Inventory inventory, List<InventoryItem> items) {
        String invSql = SqlQueries.get("inventories.insert");
        String itemSql = SqlQueries.get("inventories.insertItem");
        String getIdsSql = SqlQueries.get("inventories.getItemIds");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long invId;
            try (PreparedStatement ps = conn.prepareStatement(invSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(inventory.getInventoryDate()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) invId = keys.getLong(1);
                    else throw new SQLException("Не получен ID инвентаризации");
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                for (InventoryItem item : items) {
                    psItem.setLong(1, invId);
                    psItem.setLong(2, item.getProductId());
                    psItem.setLong(3, item.getCellId());
                    psItem.setInt(4, item.getBookQuantity());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            // Получаем сгенерированные ID позиций
            try (PreparedStatement psGetIds = conn.prepareStatement(getIdsSql)) {
                psGetIds.setLong(1, invId);
                try (ResultSet rs = psGetIds.executeQuery()) {
                    int i = 0;
                    while (rs.next() && i < items.size()) {
                        items.get(i).setInventoryItemId(rs.getLong("inventory_item_id"));
                        i++;
                    }
                }
            }

            conn.commit();
            inventory.setInventoryId(invId);
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException(e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void updateActualQuantity(Long inventoryItemId, int actualQuantity) {
        String sql = SqlQueries.get("inventories.updateActual");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, actualQuantity);
            ps.setLong(2, inventoryItemId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void completeInventory(Long inventoryId) {
        String sql = SqlQueries.get("inventories.complete");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, inventoryId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        String sql = SqlQueries.get("inventories.findById");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Inventory inv = new Inventory();
                    inv.setInventoryId(rs.getLong("inventory_id"));
                    inv.setInventoryDate(rs.getDate("inventory_date").toLocalDate());
                    inv.setStatus(rs.getString("status"));
                    return Optional.of(inv);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<Inventory> findAll() {
        List<Inventory> list = new ArrayList<>();
        String sql = SqlQueries.get("inventories.findAll");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setInventoryId(rs.getLong("inventory_id"));
                inv.setInventoryDate(rs.getDate("inventory_date").toLocalDate());
                inv.setStatus(rs.getString("status"));
                list.add(inv);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}