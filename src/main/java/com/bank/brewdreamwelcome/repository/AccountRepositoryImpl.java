package com.bank.brewdreamwelcome.repository;

import java.sql.*;
import java.util.Optional;

/**
 * Concrete implementation of AccountRepository for production use.
 */
public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public void updateBalance(Connection conn, String accNo, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accNo);
            ps.executeUpdate();
        }
    }
    
    // Default methods from Interface are inherited
}
