package ru.kafpin.lb7.dao.impl;

import ru.kafpin.lb7.dao.StorageCellDao;
import ru.kafpin.lb7.model.StorageCell;
import ru.kafpin.lb7.util.DBConnection;
import ru.kafpin.lb7.util.SqlQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageCellDaoImpl implements StorageCellDao {

    @Override
    public Optional<StorageCell> findById(Long id) {
        String sql = SqlQueries.get("cells.findById");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<StorageCell> findAll() {
        List<StorageCell> list = new ArrayList<>();
        String sql = SqlQueries.get("cells.findAll");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public void create(StorageCell cell) {
        String sql = SqlQueries.get("cells.create");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cell.getZone());
            ps.setInt(2, cell.getRowNum());
            ps.setString(3, cell.getRack());
            ps.setInt(4, cell.getCellNumber());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cell.setCellId(keys.getLong(1));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(StorageCell cell) {
        String sql = SqlQueries.get("cells.update");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cell.getZone());
            ps.setInt(2, cell.getRowNum());
            ps.setString(3, cell.getRack());
            ps.setInt(4, cell.getCellNumber());
            ps.setLong(5, cell.getCellId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(Long id) {
        String sql = SqlQueries.get("cells.delete");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private StorageCell map(ResultSet rs) throws SQLException {
        StorageCell c = new StorageCell();
        c.setCellId(rs.getLong("cell_id"));
        c.setZone(rs.getString("zone"));
        c.setRowNum(rs.getInt("row_num"));
        c.setRack(rs.getString("rack"));
        c.setCellNumber(rs.getInt("cell_number"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}