package com.bank.brewdreamwelcome.service;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import com.bank.brewdreamwelcome.core.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Compliance service for recording all system activity.
 */
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public static void log(String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            Integer userId = SessionManager.getCurrentCustomerId();
            if (userId == null) {
                userId = SessionManager.getCurrentAdminId();
            }
            ps.setObject(1, userId);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
            
            logger.info("AUDIT: {} - {}", action, details);
        } catch (Exception e) {
            logger.error("Critical: Failed to write to audit log", e);
        }
    }
}
