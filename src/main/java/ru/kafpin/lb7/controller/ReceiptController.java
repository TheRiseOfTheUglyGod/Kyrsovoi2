package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReceiptController {

    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private DatePicker receiptDate;
    @FXML private TableView<ReceiptItem> itemsTable;
    @FXML private TableColumn<ReceiptItem, Long> colProduct;
    @FXML private TableColumn<ReceiptItem, Integer> colQuantity;
    @FXML private TableColumn<ReceiptItem, BigDecimal> colPrice;
    @FXML private TableColumn<ReceiptItem, Long> colCell;

    private final ProductDao productDao;
    private final SupplierDao supplierDao;
    private final StockDao stockDao;
    private final ReceiptDao receiptDao;

    private final ObservableList<ReceiptItem> itemList = FXCollections.observableArrayList();

    public ReceiptController(ProductDao productDao, SupplierDao supplierDao,
                             StockDao stockDao, ReceiptDao receiptDao) {
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.stockDao = stockDao;
        this.receiptDao = receiptDao;
    }

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getProductId()));
        colQuantity.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getPurchasePrice()));
        colCell.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getCellId()));
        itemsTable.setItems(itemList);

        try {
            supplierCombo.setItems(FXCollections.observableArrayList(supplierDao.findAll()));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить список поставщиков: " + e.getMessage());
        }
        receiptDate.setValue(LocalDate.now());
    }

    @FXML
    private void addItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить позицию");
        dialog.setHeaderText("Введите ID товара, количество, цену, ID ячейки через запятую");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 4) {
                try {
                    Long productId = Long.parseLong(parts[0].trim());
                    int qty = Integer.parseInt(parts[1].trim());
                    BigDecimal price = new BigDecimal(parts[2].trim());
                    Long cellId = Long.parseLong(parts[3].trim());

                    if (productDao.findById(productId).isEmpty()) {
                        showError("Товар не найден");
                        return;
                    }

                    ReceiptItem item = new ReceiptItem();
                    item.setProductId(productId);
                    item.setQuantity(qty);
                    item.setPurchasePrice(price);
                    item.setCellId(cellId);
                    itemList.add(item);
                } catch (NumberFormatException e) {
                    showError("Некорректный формат чисел");
                } catch (Exception e) {
                    showError("Ошибка: " + e.getMessage());
                }
            } else {
                showError("Введите 4 значения через запятую");
            }
        });
    }

    @FXML
    private void removeItem() {
        ReceiptItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            itemList.remove(selected);
        }
    }

    @FXML
    private void saveReceipt() {
        Supplier supplier = supplierCombo.getValue();
        if (supplier == null || receiptDate.getValue() == null || itemList.isEmpty()) {
            showError("Заполните все поля и добавьте позиции");
            return;
        }

        Receipt receipt = new Receipt();
        receipt.setReceiptDate(receiptDate.getValue());
        receipt.setSupplierId(supplier.getSupplierId());

        try {
            receiptDao.save(receipt, new ArrayList<>(itemList));
            itemList.clear();
            showInfo("Приход сохранён");
        } catch (Exception e) {
            showError("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}