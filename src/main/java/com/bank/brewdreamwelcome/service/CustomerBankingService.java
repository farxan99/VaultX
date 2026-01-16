package com.bank.brewdreamwelcome.service;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import com.bank.brewdreamwelcome.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for customer banking operations.
 * Handles deposits, withdrawals, transfers with transaction safety.
 */
public class CustomerBankingService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerBankingService.class);
    private static CustomerBankingService instance;
    
    private CustomerBankingService() {}
    
    public static synchronized CustomerBankingService getInstance() {
        if (instance == null) {
            instance = new CustomerBankingService();
        }
        return instance;
    }
    
    public static class AccountInfo {
        public final String accountNumber;
        public final String accountType;
        public final double balance;
        public final String status;
        public final String branchName;
        
        public AccountInfo(String accountNumber, String accountType, double balance, 
                          String status, String branchName) {
            this.accountNumber = accountNumber;
            this.accountType = accountType;
            this.balance = balance;
            this.status = status;
            this.branchName = branchName;
        }
    }
    
    public static class TransactionRecord {
        public final long id;
        public final String fromAccount;
        public final String toAccount;
        public final double amount;
        public final String type;
        public final String description;
        public final String tag;
        public final Timestamp timestamp;
        
        public TransactionRecord(long id, String fromAccount, String toAccount, double amount,
                               String type, String description, String tag, Timestamp timestamp) {
            this.id = id;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.type = type;
            this.description = description;
            this.tag = tag;
            this.timestamp = timestamp;
        }
    }
    
    public List<AccountInfo> getCustomerAccounts(int customerId) {
        List<AccountInfo> accounts = new ArrayList<>();
        String sql = "SELECT account_number, account_type, balance, status, branch_name " +
                    "FROM accounts WHERE customer_id = ? ORDER BY opened_date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new AccountInfo(
                        rs.getString("account_number"),
                        rs.getString("account_type"),
                        rs.getDouble("balance"),
                        rs.getString("status"),
                        rs.getString("branch_name")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching customer accounts", e);
            LoggerUtil.error("Error fetching customer accounts: " + e.getMessage(), e);
        }
        
        return accounts;
    }
    
    public List<TransactionRecord> getAccountTransactions(String accountNumber, int limit) {
        List<TransactionRecord> transactions = new ArrayList<>();
        String sql = "SELECT id, from_account, to_account, amount, transaction_type, " +
                    "description, transaction_tag, timestamp " +
                    "FROM transactions " +
                    "WHERE from_account = ? OR to_account = ? " +
                    "ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            ps.setInt(3, limit);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionRecord(
                        rs.getLong("id"),
                        rs.getString("from_account"),
                        rs.getString("to_account"),
                        rs.getDouble("amount"),
                        rs.getString("transaction_type"),
                        rs.getString("description"),
                        rs.getString("transaction_tag"),
                        rs.getTimestamp("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching transactions", e);
            LoggerUtil.error("Error fetching transactions: " + e.getMessage(), e);
        }
        
        return transactions;
    }
    
    public boolean deposit(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            logger.warn("Invalid deposit amount: {}", amount);
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Check account status
            String checkSql = "SELECT status FROM accounts WHERE account_number = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        logger.error("Account not found: {}", accountNumber);
                        conn.rollback();
                        return false;
                    }
                    String status = rs.getString("status");
                    if (!"ACTIVE".equals(status)) {
                        logger.error("Account not active: {} (status: {})", accountNumber, status);
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            // Update balance
            String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, accountNumber);
                ps.executeUpdate();
            }
            
            // Record transaction
            String txSql = "INSERT INTO transactions (to_account, amount, transaction_type, description, status) " +
                          "VALUES (?, ?, 'DEPOSIT', ?, 'COMPLETED')";
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                ps.setString(1, accountNumber);
                ps.setDouble(2, amount);
                ps.setString(3, description);
                ps.executeUpdate();
            }
            
            conn.commit();
            logger.info("Deposit successful: account={}, amount={}", accountNumber, amount);
            AuditService.log("DEPOSIT", "Deposited " + amount + " to account " + accountNumber);
            return true;
            
        } catch (SQLException e) {
            logger.error("Error processing deposit", e);
            LoggerUtil.error("Error processing deposit: " + e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {}
            }
        }
    }
    
    public boolean withdraw(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            logger.warn("Invalid withdrawal amount: {}", amount);
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Check account status and balance
            String checkSql = "SELECT status, balance FROM accounts WHERE account_number = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        logger.error("Account not found: {}", accountNumber);
                        conn.rollback();
                        return false;
                    }
                    String status = rs.getString("status");
                    double balance = rs.getDouble("balance");
                    
                    if (!"ACTIVE".equals(status)) {
                        logger.error("Account not active: {}", accountNumber);
                        conn.rollback();
                        return false;
                    }
                    if (balance < amount) {
                        logger.error("Insufficient funds: account={}, balance={}, requested={}", 
                                   accountNumber, balance, amount);
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            // Update balance
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, accountNumber);
                ps.executeUpdate();
            }
            
            // Record transaction
            String txSql = "INSERT INTO transactions (from_account, amount, transaction_type, description, status) " +
                          "VALUES (?, ?, 'WITHDRAWAL', ?, 'COMPLETED')";
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                ps.setString(1, accountNumber);
                ps.setDouble(2, amount);
                ps.setString(3, description);
                ps.executeUpdate();
            }
            
            conn.commit();
            logger.info("Withdrawal successful: account={}, amount={}", accountNumber, amount);
            AuditService.log("WITHDRAWAL", "Withdrew " + amount + " from account " + accountNumber);
            return true;
            
        } catch (SQLException e) {
            logger.error("Error processing withdrawal", e);
            LoggerUtil.error("Error processing withdrawal: " + e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {}
            }
        }
    }
    
    public boolean transfer(String fromAccount, String toAccount, double amount, String description) {
        if (amount <= 0) {
            logger.warn("Invalid transfer amount: {}", amount);
            return false;
        }
        
        if (fromAccount.equals(toAccount)) {
            logger.warn("Cannot transfer to same account");
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Lock accounts in consistent order to prevent deadlock
            String firstAcc = fromAccount.compareTo(toAccount) < 0 ? fromAccount : toAccount;
            String secondAcc = fromAccount.compareTo(toAccount) < 0 ? toAccount : fromAccount;
            
            // Check both accounts
            String checkSql = "SELECT account_number, status, balance FROM accounts " +
                            "WHERE account_number IN (?, ?) FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, firstAcc);
                ps.setString(2, secondAcc);
                
                double fromBalance = 0;
                boolean fromFound = false, toFound = false;
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String accNo = rs.getString("account_number");
                        String status = rs.getString("status");
                        
                        if (!status.equals("ACTIVE")) {
                            logger.error("Account not active: {}", accNo);
                            conn.rollback();
                            return false;
                        }
                        
                        if (accNo.equals(fromAccount)) {
                            fromBalance = rs.getDouble("balance");
                            fromFound = true;
                        } else if (accNo.equals(toAccount)) {
                            toFound = true;
                        }
                    }
                }
                
                if (!fromFound || !toFound) {
                    logger.error("One or both accounts not found");
                    conn.rollback();
                    return false;
                }
                
                if (fromBalance < amount) {
                    logger.error("Insufficient funds: account={}, balance={}, requested={}", 
                               fromAccount, fromBalance, amount);
                    conn.rollback();
                    return false;
                }
            }
            
            // Debit from source
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(debitSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, fromAccount);
                ps.executeUpdate();
            }
            
            // Credit to destination
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(creditSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, toAccount);
                ps.executeUpdate();
            }
            
            // Record transaction
            String txSql = "INSERT INTO transactions (from_account, to_account, amount, transaction_type, description, status) " +
                          "VALUES (?, ?, ?, 'TRANSFER', ?, 'COMPLETED')";
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                ps.setString(1, fromAccount);
                ps.setString(2, toAccount);
                ps.setDouble(3, amount);
                ps.setString(4, description);
                ps.executeUpdate();
            }
            
            conn.commit();
            logger.info("Transfer successful: from={}, to={}, amount={}", fromAccount, toAccount, amount);
            AuditService.log("TRANSFER", "Transferred " + amount + " from " + fromAccount + " to " + toAccount);
            return true;
            
        } catch (SQLException e) {
            logger.error("Error processing transfer", e);
            LoggerUtil.error("Error processing transfer: " + e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {}
            }
        }
    }
}
