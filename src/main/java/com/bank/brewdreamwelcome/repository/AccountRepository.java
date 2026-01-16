package com.bank.brewdreamwelcome.repository;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import java.sql.*;
import java.util.Optional;

/**
 * Enterprise Repository with Row-Level Locking capabilities.
 */
public interface AccountRepository {
    
    /**
     * Finds and LOCKS an account for a safe transaction.
     * Uses SELECT ... FOR UPDATE
     */
    default Optional<AccountRecord> findAndLockByAccountNumber(Connection conn, String accNo) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AccountRecord(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    void updateBalance(Connection conn, String accNo, double newBalance) throws SQLException;
    
    record AccountRecord(String number, double balance, String status) {}
}
