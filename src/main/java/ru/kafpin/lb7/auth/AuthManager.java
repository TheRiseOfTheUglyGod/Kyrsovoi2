package ru.kafpin.lb7.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.lb7.util.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuthManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthManager.class);

    public static boolean authenticate(String username, String password) {
        String url = "jdbc:postgresql://localhost:5432/warehouse";
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            logger.info("Пользователь '{}' успешно аутентифицирован", username);
            DBConnection.setCredentials(username, password);
            return true;
        } catch (SQLException e) {
            logger.warn("Неудачная попытка входа для пользователя '{}'", username);
            return false;
        }
    }
}