package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class ReceiptController {

    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private DatePicker receiptDate;
    @FXML private TableView<ReceiptItem> itemsTable;
    @FXML private TableColumn<ReceiptItem, String> colProduct;
    @FXML private TableColumn<ReceiptItem, Integer> colQuantity;
    @FXML private TableColumn<ReceiptItem, BigDecimal> colPrice;
    @FXML private TableColumn<ReceiptItem, String> colCell;

    private final ProductDao productDao;
    private final SupplierDao supplierDao;
    private final StockDao stockDao;
    private final ReceiptDao receiptDao;
    private final StorageCellDao cellDao;

    private final ObservableList<ReceiptItem> itemList = FXCollections.observableArrayList();
    private Map<Long, String> productNames = new HashMap<>();
    private Map<Long, String> cellDescriptions = new HashMap<>();

    public ReceiptController(ProductDao productDao, SupplierDao supplierDao,
                             StockDao stockDao, ReceiptDao receiptDao, StorageCellDao cellDao) {
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.stockDao = stockDao;
        this.receiptDao = receiptDao;
        this.cellDao = cellDao;
    }

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        productNames.getOrDefault(cellData.getValue().getProductId(), "?")));
        colQuantity.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getPurchasePrice()));
        colCell.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cellDescriptions.getOrDefault(cellData.getValue().getCellId(), "?")));
        itemsTable.setItems(itemList);

        try {
            supplierCombo.setItems(FXCollections.observableArrayList(supplierDao.findAll()));
            List<Product> products = productDao.findAll();
            for (Product p : products) productNames.put(p.getProductId(), p.getName());
            List<StorageCell> cells = cellDao.findAll();
            for (StorageCell c : cells) cellDescriptions.put(c.getCellId(), c.toString());
        } catch (Exception e) {
            System.err.println("Не удалось загрузить справочники: " + e.getMessage());
        }
        receiptDate.setValue(LocalDate.now());
    }

    @FXML
    private void addItem() {
        Dialog<ReceiptItem> dialog = new Dialog<>();
        dialog.setTitle("Добавить позицию");
        dialog.setHeaderText("Выберите товар, ячейку и укажите количество и цену");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                App.bundle.getString("button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Product> productCombo = new ComboBox<>();
        ComboBox<StorageCell> cellCombo = new ComboBox<>();
        TextField qtyField = new TextField();
        TextField priceField = new TextField();

        try {
            productCombo.setItems(FXCollections.observableArrayList(productDao.findAll()));
            cellCombo.setItems(FXCollections.observableArrayList(cellDao.findAll()));
        } catch (Exception e) { /* ignore */ }

        grid.add(new Label("Товар:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Количество:"), 0, 1);
        grid.add(qtyField, 1, 1);
        grid.add(new Label("Цена:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Ячейка:"), 0, 3);
        grid.add(cellCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Product selProduct = productCombo.getValue();
                StorageCell selCell = cellCombo.getValue();
                if (selProduct == null || selCell == null) {
                    new Alert(Alert.AlertType.ERROR, "Выберите товар и ячейку").showAndWait();
                    return null;
                }
                try {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    BigDecimal price = new BigDecimal(priceField.getText().trim());
                    ReceiptItem item = new ReceiptItem();
                    item.setProductId(selProduct.getProductId());
                    item.setCellId(selCell.getCellId());
                    item.setQuantity(qty);
                    item.setPurchasePrice(price);
                    productNames.putIfAbsent(selProduct.getProductId(), selProduct.getName());
                    cellDescriptions.putIfAbsent(selCell.getCellId(), selCell.toString());
                    return item;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Количество и цена должны быть числами").showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<ReceiptItem> result = dialog.showAndWait();
        result.ifPresent(itemList::add);
    }

    @FXML
    private void removeItem() {
        ReceiptItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) itemList.remove(selected);
    }

    @FXML
    private void saveReceipt() {
        Supplier supplier = supplierCombo.getValue();
        if (supplier == null || receiptDate.getValue() == null || itemList.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Заполните все поля и добавьте позиции").showAndWait();
            return;
        }
        Receipt receipt = new Receipt();
        receipt.setReceiptDate(receiptDate.getValue());
        receipt.setSupplierId(supplier.getSupplierId());
        try {
            receiptDao.save(receipt, new ArrayList<>(itemList));
            itemList.clear();
            new Alert(Alert.AlertType.INFORMATION, "Приход сохранён").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage()).showAndWait();
        }
    }
}