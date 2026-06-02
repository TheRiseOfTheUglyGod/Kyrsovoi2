package ru.kafpin.lb7;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kafpin.lb7.controller.LoginController;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    public static ResourceBundle bundle;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        // "ru", "RU" русский
        // "en", "US" английский
        // "de", "DE" немецкий

        bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"), bundle);
        Scene scene = new Scene(loader.load());
        LoginController loginController = loader.getController();
        loginController.setStage(primaryStage);
        primaryStage.setScene(scene);

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}