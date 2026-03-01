package com.velora.app.infrastructure.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/velora";

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        // In a real app, read from application.properties
        props.setProperty("user", "velora_user");
        props.setProperty("password", "changeme");
        return DriverManager.getConnection(DEFAULT_URL, props);
    }
}
