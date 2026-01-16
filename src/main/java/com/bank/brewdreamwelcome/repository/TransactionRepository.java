package com.bank.brewdreamwelcome.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Handles all database interactions for bank transactions.
 */
public class TransactionRepository {

    public record Transaction(
            String id,
            String type,
            String fromAccount,
            String toAccount,
            double amount,
            String description,
            LocalDateTime timestamp
    ) {}

    public List<Transaction> findByAccount(String accNo) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account = ? OR to_account = ? ORDER BY timestamp DESC";
        
        // This would use DatabaseConfig.getConnection() in a real call
        // But for repository methods, we often pass the connection in for transaction support
        return transactions; 
    }

    public void logTransaction(Connection conn, String id, String type, String from, String to, double amount, String desc) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_id, transaction_type, from_account, to_account, amount, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, type);
            ps.setString(3, from);
            ps.setString(4, to);
            ps.setDouble(5, amount);
            ps.setString(6, desc);
            ps.executeUpdate();
        }
    }
}
