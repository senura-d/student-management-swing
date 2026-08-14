package com.university.sms.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides the single shared JDBC connection used throughout the app.
 * Connection details come from db.properties on the classpath instead
 * of being hardcoded, so they can be changed without touching Java code.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    private static Connection connection;

    private DBConnection() {
        // utility class - not meant to be instantiated
    }

    /**
     * Returns the shared connection, opening it on first use.
     * The connection is reused for the lifetime of the application,
     * so callers must not close it themselves.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = loadProperties();
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    private static Properties loadProperties() throws SQLException {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new SQLException("Could not find " + CONFIG_FILE + " on the classpath.");
            }
            props.load(input);
        } catch (IOException e) {
            throw new SQLException("Failed to read " + CONFIG_FILE, e);
        }
        return props;
    }
}
