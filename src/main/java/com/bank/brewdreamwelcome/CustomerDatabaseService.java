package com.bank.brewdreamwelcome;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Database service for customer management operations.
 * Provides CRUD operations for customers accessible by administrators.
 * 
 * This service handles:
 * - Retrieving all customers
 * - Creating new customers (and their primary account)
 * - Updating customer information
 * - Deleting customers (with cascade to accounts)
 * - Searching customers
 */
public class CustomerDatabaseService {

    private static CustomerDatabaseService instance;

    private CustomerDatabaseService() {
        // Private constructor for singleton
    }

    public static synchronized CustomerDatabaseService getInstance() {
        if (instance == null) {
            instance = new CustomerDatabaseService();
        }
        return instance;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, account_id, id_card_number, name, email, phone, address FROM customers ORDER BY id";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("id"),
                        rs.getString("account_id"),
                        rs.getString("id_card_number"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"));
                customers.add(customer);
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error retrieving customers: " + ex.getMessage(), ex);
        }

        return customers;
    }

    /**
     * Creates a new customer AND their primary bank account.
     * Uses transaction to ensure both are created or neither.
     */
    public Customer createCustomer(String username, String name, String email, String phone, String address,
            String password,
            String idCardNumber, String accountType) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()
                || idCardNumber == null || idCardNumber.isBlank()) {
            LoggerUtil.error("Cannot create customer: missing required fields", null);
            return null;
        }

        // Validate password strength (basic check, detailed check in UI)
        if (password.length() < 6) {
            return null;
        }

        Connection con = null;
        try {
            con = DatabaseUtil.getConnection();
            con.setAutoCommit(false); // Start Transaction

            // 1. Generate unique 6-digit Account ID
            String accountId = generateAccountId(con);

            // 2. Insert Customer
            String customerSql = "INSERT INTO customers(account_id, username, id_card_number, name, email, phone, address, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            int customerId = -1;

            try (PreparedStatement ps = con.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, accountId); // Customer account_id is their login ID
                ps.setString(2, username);
                ps.setString(3, idCardNumber);
                ps.setString(4, name);
                ps.setString(5, email);
                ps.setString(6, phone);
                ps.setString(7, address);
                ps.setString(8, BCrypt.hashpw(password, BCrypt.gensalt()));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customerId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated customer ID");
                    }
                }
            }

            // 3. Insert Bank Account
            // For now, the 'account_number' in 'accounts' table will be same as
            // 'account_id' (6-digit)
            // Or we could make them different. User said "useraccount as the auto increment
            // of a 6 digit account".
            // Let's use the 6-digit accountId as the account_number for simplicity and
            // consistency.
            String accountSql = "INSERT INTO accounts(account_number, customer_id, account_type, balance, branch_name, is_active) VALUES (?, ?, ?, 0.00, 'Main Branch', TRUE)";

            try (PreparedStatement ps = con.prepareStatement(accountSql)) {
                ps.setString(1, accountId);
                ps.setInt(2, customerId);
                ps.setString(3, accountType != null ? accountType : "SAVINGS");
                ps.executeUpdate();
            }

            con.commit(); // Commit Transaction

            LoggerUtil.info("Customer and Account created successfully: " + customerId + " / " + accountId);

            return new Customer(customerId, accountId, idCardNumber, name, email, phone, address);

        } catch (SQLException ex) {
            LoggerUtil.error("Error creating customer/account: " + ex.getMessage(), ex);
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e) {
                    // ignore
                }
            }
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    public boolean updateCustomer(int customerId, String name, String email, String phone, String address) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setInt(5, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LoggerUtil.error("Error updating customer: " + ex.getMessage(), ex);
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LoggerUtil.error("Error deleting customer: " + ex.getMessage(), ex);
            return false;
        }
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT id, account_id, id_card_number, name, email, phone, address FROM customers WHERE id = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("account_id"),
                            rs.getString("id_card_number"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"));
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error retrieving customer: " + ex.getMessage(), ex);
        }
        return null;
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            return getAllCustomers();
        }
        String sql = "SELECT id, account_id, id_card_number, name, email, phone, address FROM customers " +
                "WHERE name LIKE ? OR email LIKE ? OR account_id LIKE ? ORDER BY id";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("id"),
                            rs.getString("account_id"),
                            rs.getString("id_card_number"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"));
                    customers.add(customer);
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error searching customers: " + ex.getMessage(), ex);
        }
        return customers;
    }

    private String generateAccountId(Connection con) throws SQLException {
        java.util.Random random = new java.util.Random();
        String accountId;
        int attempts = 0;
        do {
            accountId = String.format("%06d", 100000 + random.nextInt(900000));
            attempts++;
            if (attempts > 100) {
                throw new SQLException("Unable to generate unique account ID");
            }
        } while (isAccountIdExists(con, accountId));
        return accountId;
    }

    private boolean isAccountIdExists(Connection con, String accountId) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE account_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Promote a customer to admin by copying credentials.
     */
    public boolean promoteCustomerToAdmin(int customerId) {
        String selectSql = "SELECT username, password FROM customers WHERE id = ?";
        String insertSql = "INSERT INTO admins (username, password) VALUES (?, ?)";

        Connection con = null;
        try {
            con = DatabaseUtil.getConnection();
            con.setAutoCommit(false);

            String username = null;
            String password = null;

            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        username = rs.getString("username");
                        password = rs.getString("password");
                    }
                }
            }

            if (password == null) {
                con.rollback();
                return false;
            }

            // Generate a username if null (optional field)
            if (username == null) {
                username = "admin_u" + customerId;
                // Maybe prompt user? But logic here is auto-promote.
                // Assuming we use email part or generated.
            }

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException ex) {
            LoggerUtil.error("Error promoting customer to admin: " + ex.getMessage(), ex);
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e) {
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}
