package com.bank.brewdreamwelcome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logging utility for VaultX Bank.
 * Provides consistent logging across all components.
 */
public final class LoggerUtil {
    
    private static final Logger logger = LoggerFactory.getLogger("VaultXBank");
    
    private LoggerUtil() {
    }
    
    /**
     * Gets the application logger.
     * @return The SLF4J Logger instance
     */
    public static Logger getLogger() {
        return logger;
    }
    
    /**
     * Logs an info message.
     * @param message The message to log
     */
    public static void info(String message) {
        logger.info(message);
    }
    
    /**
     * Logs a warning message.
     * @param message The message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }
    
    /**
     * Logs an error message.
     * @param message The message to log
     * @param throwable The exception (can be null)
     */
    public static void error(String message, Throwable throwable) {
        if (throwable != null) {
            logger.error(message, throwable);
        } else {
            logger.error(message);
        }
    }
    
    /**
     * Logs a debug message.
     * @param message The message to log
     */
    public static void debug(String message) {
        logger.debug(message);
    }
}

