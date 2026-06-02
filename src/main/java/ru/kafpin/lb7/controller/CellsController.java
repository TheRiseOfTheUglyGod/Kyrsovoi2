package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kafpin.lb7.dao.StorageCellDao;
import ru.kafpin.lb7.model.StorageCell;
import java.util.List;

public class CellsController {

    @FXML private TableView<StorageCell> cellsTable;
    @FXML private TableColumn<StorageCell, String> colZone;
    @FXML private TableColumn<StorageCell, Integer> colRow;
    @FXML private TableColumn<StorageCell, String> colRack;
    @FXML private TableColumn<StorageCell, Integer> colNumber;

    private final StorageCellDao storageCellDao;

    public CellsController(StorageCellDao storageCellDao) {
        this.storageCellDao = storageCellDao;
    }

    @FXML
    private void initialize() {
        colZone.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getZone()));
        colRow.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getRowNum()));
        colRack.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getRack()));
        colNumber.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getCellNumber()));
        refresh();
    }

    @FXML
    private void addCell() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новая ячейка");
        dialog.setHeaderText("Введите: зона, ряд, стеллаж, номер ячейки через запятую");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 4) {
                StorageCell cell = new StorageCell();
                cell.setZone(parts[0].trim());
                cell.setRowNum(Integer.parseInt(parts[1].trim()));
                cell.setRack(parts[2].trim());
                cell.setCellNumber(Integer.parseInt(parts[3].trim()));
                try {
                    storageCellDao.create(cell);
                    refresh();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void editCell() {
        StorageCell selected = cellsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите ячейку").showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog(
                selected.getZone() + "," + selected.getRowNum() + "," + selected.getRack() + "," + selected.getCellNumber()
        );
        dialog.setTitle("Редактирование ячейки");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 4) {
                selected.setZone(parts[0].trim());
                selected.setRowNum(Integer.parseInt(parts[1].trim()));
                selected.setRack(parts[2].trim());
                selected.setCellNumber(Integer.parseInt(parts[3].trim()));
                try {
                    storageCellDao.update(selected);
                    refresh();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void deleteCell() {
        StorageCell selected = cellsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите ячейку").showAndWait();
            return;
        }
        try {
            storageCellDao.delete(selected.getCellId());
            refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + e.getMessage()).showAndWait();
        }
    }

    private void refresh() {
        try {
            List<StorageCell> cells = storageCellDao.findAll();
            cellsTable.setItems(FXCollections.observableArrayList(cells));
        } catch (Exception e) {
            System.err.println("Ошибка загрузки ячеек: " + e.getMessage());
            cellsTable.setItems(FXCollections.observableArrayList());
        }
    }
}