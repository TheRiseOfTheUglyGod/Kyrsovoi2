package ru.kafpin.lb7.dao.impl;

import ru.kafpin.lb7.dao.ShipmentDao;
import ru.kafpin.lb7.dao.StockDao;
import ru.kafpin.lb7.model.Shipment;
import ru.kafpin.lb7.model.ShipmentItem;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipmentDaoImpl implements ShipmentDao {

    private final StockDao stockDao = new StockDaoImpl();

    @Override
    public void save(Shipment shipment, List<ShipmentItem> items) {
        String shipmentSql = SqlQueries.get("shipments.insert");
        String itemSql = SqlQueries.get("shipments.insertItem");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long shipmentId;
            try (PreparedStatement ps = conn.prepareStatement(shipmentSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(shipment.getShipmentDate()));
                ps.setString(2, shipment.getCustomer());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) shipmentId = keys.getLong(1);
                    else throw new SQLException("Не получен ID расходной накладной");
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                for (ShipmentItem item : items) {
                    psItem.setLong(1, shipmentId);
                    psItem.setLong(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setLong(4, item.getCellId());
                    psItem.addBatch();

                    stockDao.decrease(item.getProductId(), item.getCellId(), item.getQuantity());
                }
                psItem.executeBatch();
            }

            conn.commit();
            shipment.setShipmentId(shipmentId);
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("Ошибка сохранения расхода", e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public Optional<Shipment> findById(Long id) {
        String sql = SqlQueries.get("shipments.findById");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shipment s = new Shipment();
                    s.setShipmentId(rs.getLong("shipment_id"));
                    s.setShipmentDate(rs.getDate("shipment_date").toLocalDate());
                    s.setCustomer(rs.getString("customer"));
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<Shipment> findAll() {
        List<Shipment> list = new ArrayList<>();
        String sql = SqlQueries.get("shipments.findAll");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Shipment s = new Shipment();
                s.setShipmentId(rs.getLong("shipment_id"));
                s.setShipmentDate(rs.getDate("shipment_date").toLocalDate());
                s.setCustomer(rs.getString("customer"));
                list.add(s);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}