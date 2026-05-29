package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    @FXML private DatePicker inventoryDate;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Long> colProductId;
    @FXML private TableColumn<InventoryItem, Long> colCellId;
    @FXML private TableColumn<InventoryItem, Integer> colBookQty;
    @FXML private TableColumn<InventoryItem, Integer> colActualQty;
    @FXML private TableColumn<InventoryItem, Integer> colDiff;

    private final ProductDao productDao;
    private final StockDao stockDao;
    private final InventoryDao inventoryDao;

    private Inventory currentInventory;

    public InventoryController(ProductDao productDao, StockDao stockDao, InventoryDao inventoryDao) {
        this.productDao = productDao;
        this.stockDao = stockDao;
        this.inventoryDao = inventoryDao;
    }

    @FXML
    private void initialize() {
        inventoryDate.setValue(LocalDate.now());

        colProductId.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getProductId()));
        colCellId.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getCellId()));
        colBookQty.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getBookQuantity()));

        // Фактическое количество – редактируемое (по умолчанию равно учётному)
        colActualQty.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getActualQuantity()));
        colActualQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colActualQty.setOnEditCommit(event -> {
            InventoryItem item = event.getRowValue();
            item.setActualQuantity(event.getNewValue());
            item.setDifference(item.getBookQuantity() - event.getNewValue());
            inventoryTable.refresh();
        });

        // Расхождение – вычисляемое, только для чтения
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
            List<InventoryItem> items = new ArrayList<>();
            for (Product p : products) {
                List<Stock> stocks = stockDao.findByProduct(p.getProductId());
                for (Stock s : stocks) {
                    InventoryItem item = new InventoryItem();
                    item.setProductId(p.getProductId());
                    item.setCellId(s.getCellId());
                    item.setBookQuantity(s.getQuantity());
                    // Фактическое количество по умолчанию равно учётному, расхождение = 0
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
                // Сохраняем фактическое количество в БД (даже если совпадает – для истории)
                inventoryDao.updateActualQuantity(item.getInventoryItemId(), item.getActualQuantity());

                int diff = item.getBookQuantity() - item.getActualQuantity();
                if (diff != 0) {
                    discrepancies++;
                    report.append(String.format("Товар ID %d, ячейка ID %d: учёт %d, факт %d, разница %d\n",
                            item.getProductId(), item.getCellId(),
                            item.getBookQuantity(), item.getActualQuantity(), diff));

                    // Корректируем остаток на складе
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

            // Обновляем таблицу
            inventoryTable.refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка применения корректировок: " + e.getMessage()).showAndWait();
        }
    }
}