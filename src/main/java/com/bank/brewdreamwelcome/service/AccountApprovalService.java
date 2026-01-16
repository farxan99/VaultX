package com.bank.brewdreamwelcome.service;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import com.bank.brewdreamwelcome.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing customer account approvals.
 * Handles PENDING -> APPROVED/REJECTED workflow.
 */
public class AccountApprovalService {
    private static final Logger logger = LoggerFactory.getLogger(AccountApprovalService.class);
    private static AccountApprovalService instance;
    
    private AccountApprovalService() {}
    
    public static synchronized AccountApprovalService getInstance() {
        if (instance == null) {
            instance = new AccountApprovalService();
        }
        return instance;
    }
    
    public static class PendingAccount {
        public final int customerId;
        public final String name;
        public final String email;
        public final String idCard;
        public final String accountId;
        public final Timestamp createdAt;
        
        public PendingAccount(int customerId, String name, String email, String idCard, 
                            String accountId, Timestamp createdAt) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.idCard = idCard;
            this.accountId = accountId;
            this.createdAt = createdAt;
        }
    }
    
    public List<PendingAccount> getPendingAccounts() {
        List<PendingAccount> pending = new ArrayList<>();
        String sql = "SELECT id, name, email, id_card_number, account_id, created_at " +
                    "FROM customers WHERE account_status = 'PENDING' ORDER BY created_at ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                pending.add(new PendingAccount(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("id_card_number"),
                    rs.getString("account_id"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error fetching pending accounts", e);
            LoggerUtil.error("Error fetching pending accounts: " + e.getMessage(), e);
        }
        
        return pending;
    }
    
    public boolean approveAccount(int customerId, int adminId) {
        String sql = "UPDATE customers SET account_status = 'APPROVED', " +
                    "approved_by = ?, approved_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND account_status = 'PENDING'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            ps.setInt(2, customerId);
            
            int updated = ps.executeUpdate();
            if (updated > 0) {
                logger.info("Account approved: customerId={}, adminId={}", customerId, adminId);
                AuditService.log("ACCOUNT_APPROVED", 
                    "Admin " + adminId + " approved customer " + customerId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error approving account", e);
            LoggerUtil.error("Error approving account: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    public boolean rejectAccount(int customerId, int adminId, String reason) {
        String sql = "UPDATE customers SET account_status = 'REJECTED', " +
                    "approved_by = ?, approved_at = CURRENT_TIMESTAMP, " +
                    "rejection_reason = ? " +
                    "WHERE id = ? AND account_status = 'PENDING'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            ps.setString(2, reason);
            ps.setInt(3, customerId);
            
            int updated = ps.executeUpdate();
            if (updated > 0) {
                logger.info("Account rejected: customerId={}, adminId={}, reason={}", 
                           customerId, adminId, reason);
                AuditService.log("ACCOUNT_REJECTED", 
                    "Admin " + adminId + " rejected customer " + customerId + ": " + reason);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error rejecting account", e);
            LoggerUtil.error("Error rejecting account: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    public String getAccountStatus(int customerId) {
        String sql = "SELECT account_status FROM customers WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("account_status");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting account status", e);
        }
        
        return null;
    }
}
