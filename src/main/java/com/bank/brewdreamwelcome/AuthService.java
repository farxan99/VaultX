package com.bank.brewdreamwelcome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.mindrot.jbcrypt.BCrypt;


public final class AuthService {

    public enum Role {
        ADMIN,
        CUSTOMER
    }

    public static final class AuthResult {
        public final Role role;
        public final Integer customerId; // only set for customers

        private AuthResult(Role role, Integer customerId) {
            this.role = role;
            this.customerId = customerId;
        }
    }

    static {
        // Initialize database schema and create default accounts
        initializeDatabase();
    }

    private AuthService() {
    }

    /**
     * Try to authenticate the user as admin or customer.
     * Supports login by:
     * - For admins: username
     * - For customers: account_id (6-digit), email, username, or id_card_number
     */
    public static AuthResult authenticate(String identifier, String password) {
        if (identifier == null || identifier.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        // Try admin authentication first (by username)
        if (isValidAdmin(identifier, password)) {
            return new AuthResult(Role.ADMIN, null);
        }

        // Try customer authentication (by account_id, email, username, or
        // id_card_number)
        Integer customerId = findCustomerId(identifier, password);
        if (customerId != null) {
            return new AuthResult(Role.CUSTOMER, customerId);
        }

        return null;
    }

    private static boolean isValidAdmin(String username, String password) {
        String sql = "SELECT password FROM admins WHERE BINARY username = ? LIMIT 1";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error validating admin login: " + ex.getMessage(), ex);
            return false;
        }
        return false;
    }

    /**
     * Initializes the database schema by dropping old tables and creating new ones.
     * Then populates with default admin accounts.
     */
    public static void initializeDatabase() {
        try (Connection con = DatabaseUtil.getConnection();
                Statement stmt = con.createStatement()) {

            // Drop tables removed to persist data
            // stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
            // stmt.executeUpdate("DROP TABLE IF EXISTS accounts");
            // stmt.executeUpdate("DROP TABLE IF EXISTS customers");
            // stmt.executeUpdate("DROP TABLE IF EXISTS admins");

            // Create admins table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS admins ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL)");

            // Create customers table (added id_card_number)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "account_id VARCHAR(20) UNIQUE NOT NULL, " // This is the 6-digit ID
                    + "id_card_number VARCHAR(20) UNIQUE NOT NULL, " // New ID Card field
                    + "username VARCHAR(50), " // Optional, can be null
                    + "name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) UNIQUE NOT NULL, "
                    + "phone VARCHAR(20), "
                    + "address TEXT, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create accounts table (added account_type)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS accounts ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "account_number VARCHAR(20) UNIQUE NOT NULL, "
                    + "customer_id INT NOT NULL, "
                    + "account_type VARCHAR(20) NOT NULL, " // e.g., SAVINGS, CURRENT
                    + "balance DECIMAL(15,2) DEFAULT 0.00, "
                    + "branch_name VARCHAR(100) DEFAULT 'Main Branch', "
                    + "is_active BOOLEAN DEFAULT TRUE, "
                    + "opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "closed_at DATE, "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE)");

            // Create transactions table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id VARCHAR(50) PRIMARY KEY, "
                    + "transaction_type VARCHAR(20) NOT NULL, "
                    + "from_account VARCHAR(20), "
                    + "to_account VARCHAR(20), "
                    + "amount DECIMAL(15,2) NOT NULL, "
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "description TEXT)");

            // Insert specific default admins if not exist
            createDefaultAdmin(con, "Farxan11", "F@rxan11");
            createDefaultAdmin(con, "Hasnain22", "H@snain22");
            createDefaultAdmin(con, "SampleAdmin33", "S@mpleAdmin33");

            LoggerUtil.info("Database initialized successfully with new schema.");

        } catch (SQLException ex) {
            LoggerUtil.error("Error initializing database: " + ex.getMessage(), ex);
        }
    }

    private static void createDefaultAdmin(Connection con, String username, String password) throws SQLException {
        // Check if admin exists
        try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM admins WHERE username = ?")) {
            check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    // Create admin
                    String hash = BCrypt.hashpw(password, BCrypt.gensalt());
                    try (PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO admins(username, password) VALUES (?, ?)")) {
                        insert.setString(1, username);
                        insert.setString(2, hash);
                        insert.executeUpdate();
                        LoggerUtil.info("Created default admin: " + username);
                    }
                }
            }
        }
    }

    private static Integer findCustomerId(String identifier, String password) {
        // Updated to check id_card_number as well
        String sql = "SELECT id, password FROM customers WHERE account_id = ? OR email = ? OR username = ? OR id_card_number = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ps.setString(4, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error finding customer: " + ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Promote a customer to an admin.
     * Effectively creates a new admin record with the same username and hashed
     * password.
     */
    public static boolean promoteToAdmin(String username, String hashedPassword) {
        String sql = "INSERT INTO admins (username, password) VALUES (?, ?)";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            LoggerUtil.error("Error promoting user to admin: " + ex.getMessage(), ex);
            return false;
        }
    }
}
