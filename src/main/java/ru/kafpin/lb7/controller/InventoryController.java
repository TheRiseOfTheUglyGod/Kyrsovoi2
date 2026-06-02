package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;

import java.time.LocalDate;
import java.util.*;

public class InventoryController {

    @FXML private DatePicker inventoryDate;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, String> colCell;
    @FXML private TableColumn<InventoryItem, Integer> colBookQty;
    @FXML private TableColumn<InventoryItem, Integer> colActualQty;
    @FXML private TableColumn<InventoryItem, Integer> colDiff;

    private final ProductDao productDao;
    private final StockDao stockDao;
    private final InventoryDao inventoryDao;
    private final StorageCellDao cellDao;

    private Inventory currentInventory;
    private Map<Long, String> productNames = new HashMap<>();
    private Map<Long, String> cellDescriptions = new HashMap<>();

    public InventoryController(ProductDao productDao, StockDao stockDao,
                               InventoryDao inventoryDao, StorageCellDao cellDao) {
        this.productDao = productDao;
        this.stockDao = stockDao;
        this.inventoryDao = inventoryDao;
        this.cellDao = cellDao;
    }

    @FXML
    private void initialize() {
        inventoryDate.setValue(LocalDate.now());

        colProduct.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        productNames.getOrDefault(cellData.getValue().getProductId(), "Товар ?")));
        colCell.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cellDescriptions.getOrDefault(cellData.getValue().getCellId(), "Ячейка ?")));
        colBookQty.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getBookQuantity()));
        colActualQty.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getActualQuantity()));
        colActualQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colActualQty.setOnEditCommit(event -> {
            InventoryItem item = event.getRowValue();
            item.setActualQuantity(event.getNewValue());
            item.setDifference(item.getBookQuantity() - event.getNewValue());
            inventoryTable.refresh();
        });
        colDiff.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getDifference()));
        inventoryTable.setEditable(true);
    }

    @FXML
    private void generateList() {
        try {
            Inventory inv = new Inventory();
            inv.setInventoryDate(inventoryDate.getValue());
            inv.setStatus("in_progress");

            List<Product> products = productDao.findAll();
            List<StorageCell> cells = cellDao.findAll();

            productNames.clear();
            for (Product p : products) productNames.put(p.getProductId(), p.getName());
            cellDescriptions.clear();
            for (StorageCell c : cells) cellDescriptions.put(c.getCellId(), c.toString());

            List<InventoryItem> items = new ArrayList<>();
            for (Product p : products) {
                List<Stock> stocks = stockDao.findByProduct(p.getProductId());
                for (Stock s : stocks) {
                    InventoryItem item = new InventoryItem();
                    item.setProductId(p.getProductId());
                    item.setCellId(s.getCellId());
                    item.setBookQuantity(s.getQuantity());
                    item.setActualQuantity(s.getQuantity());
                    item.setDifference(0);
                    items.add(item);
                }
            }

            inventoryDao.createInventory(inv, items);
            this.currentInventory = inv;
            inventoryTable.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void finishInventory() {
        if (currentInventory == null) {
            new Alert(Alert.AlertType.WARNING, "Сначала сформируйте список").showAndWait();
            return;
        }
        try {
            inventoryDao.completeInventory(currentInventory.getInventoryId());
            new Alert(Alert.AlertType.INFORMATION, "Инвентаризация завершена").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void applyAdjustments() {
        if (currentInventory == null) {
            new Alert(Alert.AlertType.WARNING, "Сначала сформируйте список").showAndWait();
            return;
        }

        List<InventoryItem> items = inventoryTable.getItems();
        StringBuilder report = new StringBuilder("Отчёт о расхождениях:\n");
        int discrepancies = 0;

        try {
            for (InventoryItem item : items) {
                inventoryDao.updateActualQuantity(item.getInventoryItemId(), item.getActualQuantity());

                int diff = item.getBookQuantity() - item.getActualQuantity();
                if (diff != 0) {
                    discrepancies++;
                    report.append(String.format("Товар: %s, Ячейка: %s, учёт: %d, факт: %d, разница: %d\n",
                            productNames.getOrDefault(item.getProductId(), "?"),
                            cellDescriptions.getOrDefault(item.getCellId(), "?"),
                            item.getBookQuantity(), item.getActualQuantity(), diff));
                    stockDao.setQuantity(item.getProductId(), item.getCellId(), item.getActualQuantity());
                }
            }

            if (discrepancies == 0) {
                report.append("Расхождений нет.");
            }

            TextArea textArea = new TextArea(report.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(400);
            textArea.setPrefHeight(200);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Результат корректировки");
            alert.setHeaderText("Корректировки применены. Расхождения:");
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();

            inventoryTable.refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка применения корректировок: " + e.getMessage()).showAndWait();
        }
    }
}