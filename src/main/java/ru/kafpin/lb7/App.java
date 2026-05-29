package ru.kafpin.lb7;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kafpin.lb7.controller.LoginController;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Устанавливаем нужную локаль (для демонстрации можно менять)
        Locale.setDefault(new Locale("ru", "RU"));  // русский
        // Locale.setDefault(new Locale("en", "US")); // английский
        // Locale.setDefault(new Locale("de", "DE")); // немецкий

        // 2. Загружаем соответствующий файл локализации
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        // 3. Передаём bundle в загрузчик FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"), bundle);
        Scene scene = new Scene(loader.load());
        LoginController loginController = loader.getController();
        loginController.setStage(primaryStage);
        primaryStage.setScene(scene);

        // 4. Заголовок окна тоже берём из локализации
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}