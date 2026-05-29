package ru.kafpin.lb7.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SqlQueries {
    public static final Properties QUERIES = new Properties();

    static {
        try (InputStream is = SqlQueries.class.getResourceAsStream("/sql.properties")) {
            QUERIES.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить sql.properties", e);
        }
    }

    public static String get(String key) {
        return QUERIES.getProperty(key);
    }
}