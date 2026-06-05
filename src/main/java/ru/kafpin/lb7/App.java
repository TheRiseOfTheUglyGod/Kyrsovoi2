package ru.kafpin.lb7;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kafpin.lb7.controller.LoginController;
import ru.kafpin.lb7.controller.MainController;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.dao.impl.*;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    public static ResourceBundle bundle;
    private static Stage primaryStage;

    // DAO создаются один раз, чтобы не было утечек соединений при переключении языка
    private static final ProductDao productDao = new ProductDaoImpl();
    private static final SupplierDao supplierDao = new SupplierDaoImpl();
    private static final StorageCellDao storageCellDao = new StorageCellDaoImpl();
    private static final StockDao stockDao = new StockDaoImpl();
    private static final ReceiptDao receiptDao = new ReceiptDaoImpl();
    private static final ShipmentDao shipmentDao = new ShipmentDaoImpl();
    private static final InventoryDao inventoryDao = new InventoryDaoImpl();

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.primaryStage = primaryStage;
        // Устанавливаем локаль по умолчанию (русский)
        Locale.setDefault(new Locale("ru", "RU"));
        bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        showLoginScreen();
    }

    public static void showLoginScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/login.fxml"), bundle);
        Scene scene = new Scene(loader.load());
        LoginController loginController = loader.getController();
        loginController.setStage(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.show();
    }

    public static void switchLanguage(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("messages", locale);
        try {
            // Перезагружаем главное окно с новым bundle
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/main.fxml"), bundle);
            MainController mainController = new MainController(productDao, supplierDao,
                    storageCellDao, stockDao, receiptDao, shipmentDao, inventoryDao);
            loader.setController(mainController);
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle(bundle.getString("app.title"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Геттеры для DAO (чтобы LoginController мог их использовать)
    public static ProductDao getProductDao() { return productDao; }
    public static SupplierDao getSupplierDao() { return supplierDao; }
    public static StorageCellDao getStorageCellDao() { return storageCellDao; }
    public static StockDao getStockDao() { return stockDao; }
    public static ReceiptDao getReceiptDao() { return receiptDao; }
    public static ShipmentDao getShipmentDao() { return shipmentDao; }
    public static InventoryDao getInventoryDao() { return inventoryDao; }

    public static void main(String[] args) {
        launch(args);
    }
}