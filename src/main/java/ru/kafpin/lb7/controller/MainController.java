package ru.kafpin.lb7.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.*;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label statusLabel;

    private final ProductDao productDao;
    private final SupplierDao supplierDao;
    private final StorageCellDao storageCellDao;
    private final StockDao stockDao;
    private final ReceiptDao receiptDao;
    private final ShipmentDao shipmentDao;
    private final InventoryDao inventoryDao;

    public MainController(ProductDao productDao, SupplierDao supplierDao,
                          StorageCellDao storageCellDao, StockDao stockDao,
                          ReceiptDao receiptDao, ShipmentDao shipmentDao,
                          InventoryDao inventoryDao) {
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.storageCellDao = storageCellDao;
        this.stockDao = stockDao;
        this.receiptDao = receiptDao;
        this.shipmentDao = shipmentDao;
        this.inventoryDao = inventoryDao;
    }

    @FXML
    private void handleReceipt() {
        loadForm("/view/receipt.fxml",
                new ReceiptController(productDao, supplierDao, stockDao, receiptDao));
    }

    @FXML
    private void handleShipment() {
        loadForm("/view/shipment.fxml",
                new ShipmentController(productDao, stockDao, shipmentDao));
    }

    @FXML
    private void handleInventory() {
        loadForm("/view/inventory.fxml",
                new InventoryController(productDao, stockDao, inventoryDao));
    }

    @FXML
    private void handleProducts() {
        loadForm("/view/products.fxml",
                new ProductsController(productDao));
    }

    @FXML
    private void handleSuppliers() {
        loadForm("/view/suppliers.fxml",
                new SuppliersController(supplierDao));
    }

    @FXML
    private void handleCells() {
        loadForm("/view/cells.fxml",
                new CellsController(storageCellDao));
    }

    private void loadForm(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), App.bundle);
            loader.setController(controller);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(loader.load());
            statusLabel.setText(App.bundle.getString("label.status.ready"));
        } catch (IOException e) {
            statusLabel.setText("Ошибка загрузки формы");
            e.printStackTrace();
        }
    }
}