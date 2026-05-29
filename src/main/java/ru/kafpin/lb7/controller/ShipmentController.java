package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private List<Product> allProducts;

    public ShipmentController(ProductDao productDao, StockDao stockDao, ShipmentDao shipmentDao) {
        this.productDao = productDao;
        this.stockDao = stockDao;
        this.shipmentDao = shipmentDao;
    }

    @FXML
    private void initialize() {
        shipmentDate.setValue(LocalDate.now());

        try {
            allProducts = productDao.findAll();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки товаров: " + e.getMessage());
            allProducts = new ArrayList<>();
        }

        productCombo.setItems(FXCollections.observableArrayList(
                allProducts.stream().map(Product::getName).collect(Collectors.toList())
        ));

        colCellAddress.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper("Ячейка ID " + cellData.getValue().getCellId()));
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
            System.err.println("Ошибка загрузки остатков: " + e.getMessage());
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

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Отгрузка товара");
        dialog.setHeaderText("Введите ID ячейки и количество через запятую");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                try {
                    Long cellId = Long.parseLong(parts[0].trim());
                    int qty = Integer.parseInt(parts[1].trim());

                    Shipment shipment = new Shipment();
                    shipment.setShipmentDate(shipmentDate.getValue());
                    shipment.setCustomer(customer);

                    ShipmentItem item = new ShipmentItem();
                    item.setProductId(product.getProductId());
                    item.setCellId(cellId);
                    item.setQuantity(qty);
                    List<ShipmentItem> items = new ArrayList<>();
                    items.add(item);

                    shipmentDao.save(shipment, items);
                    new Alert(Alert.AlertType.INFORMATION, "Отгрузка выполнена").showAndWait();
                    updateStockTable();
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Некорректные числа").showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Введите два значения").showAndWait();
            }
        });
    }
}