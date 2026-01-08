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

    private static final String URL =
            "jdbc:mysql://localhost:3306/vaultx?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LoggerUtil.error("MySQL JDBC driver not found. Add mysql-connector-java to the classpath.", e);
        }
    }

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}


