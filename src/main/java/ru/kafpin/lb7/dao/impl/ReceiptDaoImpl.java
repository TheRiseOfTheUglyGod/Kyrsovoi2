package ru.kafpin.lb7.dao.impl;

import ru.kafpin.lb7.dao.ReceiptDao;
import ru.kafpin.lb7.dao.StockDao;
import ru.kafpin.lb7.model.Receipt;
import ru.kafpin.lb7.model.ReceiptItem;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReceiptDaoImpl implements ReceiptDao {

    private final StockDao stockDao = new StockDaoImpl();

    @Override
    public void save(Receipt receipt, List<ReceiptItem> items) {
        String receiptSql = SqlQueries.get("receipts.insert");
        String itemSql = SqlQueries.get("receipts.insertItem");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long receiptId;
            try (PreparedStatement ps = conn.prepareStatement(receiptSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(receipt.getReceiptDate()));
                ps.setLong(2, receipt.getSupplierId());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) receiptId = keys.getLong(1);
                    else throw new SQLException("Не получен ID накладной");
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                for (ReceiptItem item : items) {
                    psItem.setLong(1, receiptId);
                    psItem.setLong(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getPurchasePrice());
                    psItem.setLong(5, item.getCellId());
                    psItem.addBatch();

                    stockDao.increase(item.getProductId(), item.getCellId(), item.getQuantity());
                }
                psItem.executeBatch();
            }

            conn.commit();
            receipt.setReceiptId(receiptId);
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("Ошибка сохранения прихода", e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public Optional<Receipt> findById(Long id) {
        String sql = SqlQueries.get("receipts.findById");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Receipt r = new Receipt();
                    r.setReceiptId(rs.getLong("receipt_id"));
                    r.setReceiptDate(rs.getDate("receipt_date").toLocalDate());
                    r.setSupplierId(rs.getLong("supplier_id"));
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<Receipt> findAll() {
        List<Receipt> list = new ArrayList<>();
        String sql = SqlQueries.get("receipts.findAll");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Receipt r = new Receipt();
                r.setReceiptId(rs.getLong("receipt_id"));
                r.setReceiptDate(rs.getDate("receipt_date").toLocalDate());
                r.setSupplierId(rs.getLong("supplier_id"));
                list.add(r);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }
}