package com.bank.brewdreamwelcome;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple JDBC helper for connecting to the XAMPP MySQL database.
 *
 * Default connection (you can change these to match your local XAMPP setup):
 *   host:     localhost
 *   port:     3306
 *   database: vaultx
 *   user:     root
 *   password: (empty)
 *
 * Make sure you have created the database and tables in phpMyAdmin.
 */
public final class DatabaseUtil {

    // Hardcoded credentials and driver loading are now handled by DatabaseConfig

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return com.bank.brewdreamwelcome.config.DatabaseConfig.getConnection();
    }
}


