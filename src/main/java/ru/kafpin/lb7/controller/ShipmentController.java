package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ShipmentController {

    @FXML private TextField customerField;
    @FXML private DatePicker shipmentDate;
    @FXML private ComboBox<String> productCombo;
    @FXML private TableView<Stock> stockTable;
    @FXML private TableColumn<Stock, String> colCellAddress;
    @FXML private TableColumn<Stock, Integer> colStockQty;

    private final ProductDao productDao;
    private final StockDao stockDao;
    private final ShipmentDao shipmentDao;
    private final StorageCellDao cellDao;

    private List<Product> allProducts;
    private Map<Long, String> cellDescriptions = new HashMap<>();

    public ShipmentController(ProductDao productDao, StockDao stockDao,
                              ShipmentDao shipmentDao, StorageCellDao cellDao) {
        this.productDao = productDao;
        this.stockDao = stockDao;
        this.shipmentDao = shipmentDao;
        this.cellDao = cellDao;
    }

    @FXML
    private void initialize() {
        shipmentDate.setValue(LocalDate.now());
        try {
            allProducts = productDao.findAll();
            List<StorageCell> cells = cellDao.findAll();
            for (StorageCell c : cells) {
                cellDescriptions.put(c.getCellId(), c.toString());
            }
        } catch (Exception e) {
            allProducts = new ArrayList<>();
        }

        productCombo.setItems(FXCollections.observableArrayList(
                allProducts.stream().map(Product::getName).collect(Collectors.toList())
        ));

        colCellAddress.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cellDescriptions.getOrDefault(cellData.getValue().getCellId(), "Ячейка ?")));
        colStockQty.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        productCombo.setOnAction(event -> updateStockTable());
    }

    private void updateStockTable() {
        String selectedName = productCombo.getValue();
        if (selectedName == null) return;
        Optional<Product> prodOpt = allProducts.stream()
                .filter(p -> p.getName().equals(selectedName)).findFirst();
        if (prodOpt.isEmpty()) return;
        Product product = prodOpt.get();
        try {
            List<Stock> stocks = stockDao.findByProduct(product.getProductId());
            stockTable.setItems(FXCollections.observableArrayList(stocks));
        } catch (Exception e) {
            stockTable.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void performShipment() {
        String customer = customerField.getText();
        if (customer.isEmpty() || shipmentDate.getValue() == null || productCombo.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Заполните все поля").showAndWait();
            return;
        }
        Optional<Product> prodOpt = allProducts.stream()
                .filter(p -> p.getName().equals(productCombo.getValue())).findFirst();
        if (prodOpt.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Товар не выбран").showAndWait();
            return;
        }
        Product product = prodOpt.get();

        if (stockTable.getItems().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Нет остатков для выбранного товара").showAndWait();
            return;
        }

        Dialog<ShipmentItem> dialog = new Dialog<>();
        dialog.setTitle("Отгрузка товара");
        dialog.setHeaderText("Выберите ячейку и укажите количество");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                App.bundle.getString("button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> cellCombo = new ComboBox<>();
        List<String> cellDescriptionsList = stockTable.getItems().stream()
                .map(s -> cellDescriptions.getOrDefault(s.getCellId(), "Ячейка ?") + " (остаток: " + s.getQuantity() + ")")
                .collect(Collectors.toList());
        cellCombo.setItems(FXCollections.observableArrayList(cellDescriptionsList));

        TextField qtyField = new TextField();

        grid.add(new Label("Ячейка:"), 0, 0);
        grid.add(cellCombo, 1, 0);
        grid.add(new Label("Количество:"), 0, 1);
        grid.add(qtyField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                int selectedIndex = cellCombo.getSelectionModel().getSelectedIndex();
                if (selectedIndex < 0) {
                    new Alert(Alert.AlertType.ERROR, "Выберите ячейку").showAndWait();
                    return null;
                }
                Stock selectedStock = stockTable.getItems().get(selectedIndex);
                try {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    if (qty <= 0) throw new NumberFormatException();
                    ShipmentItem item = new ShipmentItem();
                    item.setProductId(product.getProductId());
                    item.setCellId(selectedStock.getCellId());
                    item.setQuantity(qty);
                    return item;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Введите положительное целое число").showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<ShipmentItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            try {
                List<ShipmentItem> items = new ArrayList<>();
                items.add(item);
                Shipment shipment = new Shipment();
                shipment.setShipmentDate(shipmentDate.getValue());
                shipment.setCustomer(customer);
                shipmentDao.save(shipment, items);
                new Alert(Alert.AlertType.INFORMATION, "Отгрузка выполнена").showAndWait();
                updateStockTable();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
            }
        });
    }
}