package ru.kafpin.lb7.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.lb7.auth.AuthManager;
import ru.kafpin.lb7.dao.*;
import ru.kafpin.lb7.dao.impl.*;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            logger.warn("Попытка входа с пустыми полями");
            new Alert(Alert.AlertType.ERROR, "Введите логин и пароль").showAndWait();
            return;
        }

        logger.info("Попытка входа с логином '{}'", username);

        if (AuthManager.authenticate(username, password)) {
            logger.info("Аутентификация успешна, открывается главное окно");
            ProductDao productDao = new ProductDaoImpl();
            SupplierDao supplierDao = new SupplierDaoImpl();
            StorageCellDao storageCellDao = new StorageCellDaoImpl();
            StockDao stockDao = new StockDaoImpl();
            ReceiptDao receiptDao = new ReceiptDaoImpl();
            ShipmentDao shipmentDao = new ShipmentDaoImpl();
            InventoryDao inventoryDao = new InventoryDaoImpl();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
                MainController mainController = new MainController(productDao, supplierDao,
                        storageCellDao, stockDao, receiptDao, shipmentDao, inventoryDao);
                loader.setController(mainController);
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("АРМ менеджера склада");
            } catch (Exception e) {
                logger.error("Ошибка загрузки главного окна", e);
                new Alert(Alert.AlertType.ERROR, "Ошибка запуска главного окна").showAndWait();
            }
        } else {
            logger.warn("Аутентификация отклонена для '{}'", username);
            new Alert(Alert.AlertType.ERROR, "Неверный логин или пароль").showAndWait();
        }
    }
}