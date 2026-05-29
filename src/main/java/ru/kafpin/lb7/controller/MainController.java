package ru.kafpin.lb7.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.lb7.dao.*;

import java.io.IOException;

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

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
        logger.info("Открыта форма прихода");
        loadForm("/view/receipt.fxml",
                new ReceiptController(productDao, supplierDao, stockDao, receiptDao));
    }

    @FXML
    private void handleShipment() {
        logger.info("Открыта форма расхода");
        loadForm("/view/shipment.fxml",
                new ShipmentController(productDao, stockDao, shipmentDao));
    }

    @FXML
    private void handleInventory() {
        logger.info("Открыта форма инвентаризации");
        loadForm("/view/inventory.fxml",
                new InventoryController(productDao, stockDao, inventoryDao));
    }

    @FXML
    private void handleProducts() {
        logger.info("Открыт справочник товаров");
        loadForm("/view/products.fxml", new ProductsController(productDao));
    }

    @FXML
    private void handleSuppliers() {
        logger.info("Открыт справочник поставщиков");
        loadForm("/view/suppliers.fxml", new SuppliersController(supplierDao));
    }

    @FXML
    private void handleCells() {
        logger.info("Открыт справочник ячеек");
        loadForm("/view/cells.fxml", new CellsController(storageCellDao));
    }

    private void loadForm(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(controller);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(loader.load());
            statusLabel.setText("Готов");
            logger.debug("Форма {} загружена успешно", fxmlPath);
        } catch (IOException e) {
            statusLabel.setText("Ошибка загрузки формы");
            logger.error("Ошибка загрузки формы {}", fxmlPath, e);
        }
    }
}