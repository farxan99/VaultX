package com.bank.brewdreamwelcome.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Production-grade Connection Pool Manager using HikariCP.
 * Replaces the old DatabaseUtil to prevent connection leaks.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    private static final Dotenv dotenv = Dotenv.load();

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dotenv.get("DB_URL"));
            config.setUsername(dotenv.get("DB_USER"));
            config.setPassword(dotenv.get("DB_PASS"));

            // Pool Performance Tuning
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("Connection pool initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
