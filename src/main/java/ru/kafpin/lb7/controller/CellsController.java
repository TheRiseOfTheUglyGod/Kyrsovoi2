package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.StorageCellDao;
import ru.kafpin.lb7.model.StorageCell;
import java.util.List;
import java.util.Optional;

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
        Dialog<StorageCell> dialog = createCellDialog(null);
        Optional<StorageCell> result = dialog.showAndWait();
        result.ifPresent(cell -> {
            try {
                storageCellDao.create(cell);
                refresh();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
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
        Dialog<StorageCell> dialog = createCellDialog(selected);
        Optional<StorageCell> result = dialog.showAndWait();
        result.ifPresent(cell -> {
            selected.setZone(cell.getZone());
            selected.setRowNum(cell.getRowNum());
            selected.setRack(cell.getRack());
            selected.setCellNumber(cell.getCellNumber());
            try {
                storageCellDao.update(selected);
                refresh();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
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

    private Dialog<StorageCell> createCellDialog(StorageCell existing) {
        Dialog<StorageCell> dialog = new Dialog<>();
        dialog.setTitle(existing == null ?
                App.bundle.getString("cell.dialog.new") :
                App.bundle.getString("cell.dialog.edit"));
        dialog.setHeaderText(App.bundle.getString("cell.dialog.header"));

        ButtonType okButtonType = new ButtonType(
                App.bundle.getString("button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                App.bundle.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField zoneField = new TextField();
        TextField rowField = new TextField();
        TextField rackField = new TextField();
        TextField cellNumField = new TextField();

        if (existing != null) {
            zoneField.setText(existing.getZone());
            rowField.setText(String.valueOf(existing.getRowNum()));
            rackField.setText(existing.getRack());
            cellNumField.setText(String.valueOf(existing.getCellNumber()));
        }

        grid.add(new Label(App.bundle.getString("cell.field.zone")), 0, 0);
        grid.add(zoneField, 1, 0);
        grid.add(new Label(App.bundle.getString("cell.field.row")), 0, 1);
        grid.add(rowField, 1, 1);
        grid.add(new Label(App.bundle.getString("cell.field.rack")), 0, 2);
        grid.add(rackField, 1, 2);
        grid.add(new Label(App.bundle.getString("cell.field.number")), 0, 3);
        grid.add(cellNumField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                StorageCell cell = new StorageCell();
                cell.setZone(zoneField.getText().trim());
                try {
                    cell.setRowNum(Integer.parseInt(rowField.getText().trim()));
                } catch (NumberFormatException e) { cell.setRowNum(0); }
                cell.setRack(rackField.getText().trim());
                try {
                    cell.setCellNumber(Integer.parseInt(cellNumField.getText().trim()));
                } catch (NumberFormatException e) { cell.setCellNumber(0); }
                return cell;
            }
            return null;
        });
        return dialog;
    }
}