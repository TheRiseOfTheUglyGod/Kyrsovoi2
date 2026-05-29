package ru.kafpin.lb7.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream is = DBConnection.class.getResourceAsStream("/config.properties")) {
            Properties props = new Properties();
            props.load(is);
            url = props.getProperty("db.url");
        } catch (IOException e) {
            logger.error("Не удалось загрузить config.properties", e);
            throw new RuntimeException("Не удалось загрузить config.properties", e);
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL Driver не найден", e);
            throw new RuntimeException("PostgreSQL Driver не найден", e);
        }
    }

    public static void setCredentials(String username, String password) {
        DBConnection.user = username;
        DBConnection.password = password;
        logger.debug("Учётные данные установлены для пользователя '{}'", username);
    }

    public static Connection getConnection() throws SQLException {
        if (user == null || password == null) {
            logger.error("Попытка подключения без установленных учётных данных");
            throw new SQLException("Учётные данные не установлены. Сначала выполните аутентификацию.");
        }
        logger.info("Подключение к БД: {}", url);
        return DriverManager.getConnection(url, user, password);
    }
}