package com.velora.app.infrastructure.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

public final class DatabaseConfig {
    private static final String PROPERTIES_FILE = "/application.properties";
    private static final String URL_PROPERTY = "db.url";
    private static final String USER_PROPERTY = "db.user";
    private static final String PASSWORD_PROPERTY = "db.password";

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();

        // Load properties from application.properties on the classpath
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            SQLException se = new SQLException("Failed to load database configuration from " + PROPERTIES_FILE);
            se.initCause(e);
            throw se;
        }

        // Allow environment variables to override properties if present
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        if (envUser != null && !envUser.isEmpty()) {
            props.setProperty("user", envUser);
        } else if (props.containsKey(USER_PROPERTY)) {
            props.setProperty("user", props.getProperty(USER_PROPERTY));
        }

        if (envPassword != null && !envPassword.isEmpty()) {
            props.setProperty("password", envPassword);
        } else if (props.containsKey(PASSWORD_PROPERTY)) {
            props.setProperty("password", props.getProperty(PASSWORD_PROPERTY));
        }

        String url;
        if (envUrl != null && !envUrl.isEmpty()) {
            url = envUrl;
        } else {
            url = props.getProperty(URL_PROPERTY);
        }

        if (url == null || url.isEmpty()) {
            throw new SQLException("Database URL is not configured. Set " + URL_PROPERTY + " in " + PROPERTIES_FILE + " or DB_URL environment variable.");
        }

        return DriverManager.getConnection(url, props);
    }
}
