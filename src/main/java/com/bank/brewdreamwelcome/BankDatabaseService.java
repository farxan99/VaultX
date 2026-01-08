package com.bank.brewdreamwelcome;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database-backed service for bank accounts and transactions.
 * Handles all account and transaction operations with MySQL database.
 */
public class BankDatabaseService {

    private static final BankDatabaseService INSTANCE = new BankDatabaseService();

    public static BankDatabaseService getInstance() {
        return INSTANCE;
    }

    private BankDatabaseService() {
    }

    /**
     * Gets all accounts for a specific customer.
     */
    public List<BankAccount> getCustomerAccounts(Integer customerId) {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.name, c.email, c.id_card_number FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id " +
                "WHERE a.customer_id = ? AND a.is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String accountNumber = rs.getString("account_number");
                    AccountType type = AccountType.valueOf(rs.getString("account_type"));
                    double balance = rs.getDouble("balance");
                    String branchName = rs.getString("branch_name");
                    boolean isActive = rs.getBoolean("is_active");

                    // Create customer object for the account
                    Customer customer = new Customer(
                            customerId,
                            String.valueOf(customerId), // Account ID not strictly needed here or same as ID
                            rs.getString("id_card_number"),
                            rs.getString("name"),
                            rs.getString("email"),
                            null, // Phone not fetched
                            null); // Address not fetched

                    BankAccount account = new BankAccount(accountNumber, customer, type, balance, branchName);
                    account.setActive(isActive);

                    accounts.add(account);
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting customer accounts: " + ex.getMessage(), ex);
        }
        return accounts;
    }

    /**
     * Gets the total balance for a customer across all their accounts.
     */
    public double getCustomerTotalBalance(Integer customerId) {
        String sql = "SELECT COALESCE(SUM(balance), 0) as total FROM accounts " +
                "WHERE customer_id = ? AND is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting customer balance: " + ex.getMessage(), ex);
        }
        return 0.0;
    }

    /**
     * Gets account types for a customer (e.g., "Savings, Current").
     */
    public String getCustomerAccountTypes(Integer customerId) {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT account_type FROM accounts " +
                "WHERE customer_id = ? AND is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("account_type");
                    AccountType accountType = AccountType.valueOf(type);
                    types.add(accountType.getDisplayName());
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting account types: " + ex.getMessage(), ex);
        }

        if (types.isEmpty()) {
            return "No accounts";
        }
        return String.join(", ", types);
    }

    /**
     * Gets all transactions for a customer's accounts.
     */
    public List<BankTransaction> getCustomerTransactions(Integer customerId) {
        List<BankTransaction> transactions = new ArrayList<>();

        // First get all account numbers for this customer
        List<String> accountNumbers = new ArrayList<>();
        String accountSql = "SELECT account_number FROM accounts WHERE customer_id = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(accountSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accountNumbers.add(rs.getString("account_number"));
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting account numbers: " + ex.getMessage(), ex);
            return transactions;
        }

        if (accountNumbers.isEmpty()) {
            return transactions;
        }

        // Build query with placeholders for account numbers
        String placeholders = String.join(",", Collections.nCopies(accountNumbers.size(), "?"));
        String sql = "SELECT * FROM transactions " +
                "WHERE from_account IN (" + placeholders + ") OR to_account IN (" + placeholders + ") " +
                "ORDER BY timestamp DESC";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String accNum : accountNumbers) {
                ps.setString(paramIndex++, accNum);
            }
            for (String accNum : accountNumbers) {
                ps.setString(paramIndex++, accNum);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String txId = rs.getString("transaction_id");
                    TransactionType type = TransactionType.valueOf(rs.getString("transaction_type"));
                    String fromAccount = rs.getString("from_account");
                    String toAccount = rs.getString("to_account");
                    double amount = rs.getDouble("amount");
                    String description = rs.getString("description");

                    // Create transaction object
                    BankTransaction tx = new BankTransaction(
                            txId, type, fromAccount, toAccount, amount, description);
                    // Note: BankTransaction uses LocalDateTime.now() in constructor
                    // We'll need to handle this differently or modify BankTransaction
                    transactions.add(tx);
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting customer transactions: " + ex.getMessage(), ex);
        }

        return transactions;
    }

    /**
     * Gets count of transactions made today by the customer.
     */
    public long getCustomerTransactionsTodayCount(Integer customerId) {
        List<String> accountNumbers = new ArrayList<>();
        String accountSql = "SELECT account_number FROM accounts WHERE customer_id = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(accountSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accountNumbers.add(rs.getString("account_number"));
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting account numbers: " + ex.getMessage(), ex);
            return 0;
        }

        if (accountNumbers.isEmpty()) {
            return 0;
        }

        String placeholders = String.join(",", Collections.nCopies(accountNumbers.size(), "?"));
        String sql = "SELECT COUNT(*) as count FROM transactions " +
                "WHERE (from_account IN (" + placeholders + ") OR to_account IN (" + placeholders + ")) " +
                "AND DATE(timestamp) = CURDATE()";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String accNum : accountNumbers) {
                ps.setString(paramIndex++, accNum);
            }
            for (String accNum : accountNumbers) {
                ps.setString(paramIndex++, accNum);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("count");
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting today's transaction count: " + ex.getMessage(), ex);
        }
        return 0;
    }

    /**
     * Finds an account by account number.
     */
    public BankAccount findAccount(String accountNumber) {
        String sql = "SELECT a.*, c.id as customer_id, c.account_id, c.id_card_number, c.name, c.email FROM accounts a "
                +
                "JOIN customers c ON a.customer_id = c.id " +
                "WHERE a.account_number = ? AND a.is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountType type = AccountType.valueOf(rs.getString("account_type"));
                    double balance = rs.getDouble("balance");
                    String branchName = rs.getString("branch_name");
                    boolean isActive = rs.getBoolean("is_active");

                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("account_id"),
                            rs.getString("id_card_number"),
                            rs.getString("name"),
                            rs.getString("email"),
                            null, // Phone not fetched
                            null); // Address not fetched

                    BankAccount account = new BankAccount(accountNumber, customer, type, balance, branchName);
                    account.setActive(isActive);
                    return account;
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error finding account: " + ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Deposits money into an account.
     */
    public boolean deposit(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            return false;
        }

        String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ? AND is_active = TRUE";
        String insertSql = "INSERT INTO transactions(transaction_id, transaction_type, to_account, amount, description) "
                +
                "VALUES (?, 'DEPOSIT', ?, ?, ?)";

        try (Connection con = DatabaseUtil.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement updatePs = con.prepareStatement(updateSql);
                    PreparedStatement insertPs = con.prepareStatement(insertSql)) {

                // Update balance
                updatePs.setDouble(1, amount);
                updatePs.setString(2, accountNumber);
                int rowsUpdated = updatePs.executeUpdate();

                if (rowsUpdated == 0) {
                    con.rollback();
                    return false;
                }

                // Record transaction
                String txId = generateTransactionId(con);
                insertPs.setString(1, txId);
                insertPs.setString(2, accountNumber);
                insertPs.setDouble(3, amount);
                insertPs.setString(4, description != null ? description : "Deposit");
                insertPs.executeUpdate();

                con.commit();
                LoggerUtil.info("Deposit of PKR " + amount + " to account " + accountNumber);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error depositing money: " + ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Withdraws money from an account.
     */
    public boolean withdraw(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            return false;
        }

        // Check balance first
        String checkSql = "SELECT balance FROM accounts WHERE account_number = ? AND is_active = TRUE";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setString(1, accountNumber);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next() || rs.getDouble("balance") < amount) {
                    return false; // Insufficient balance or account not found
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error checking balance: " + ex.getMessage(), ex);
            return false;
        }

        String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ? AND is_active = TRUE";
        String insertSql = "INSERT INTO transactions(transaction_id, transaction_type, from_account, amount, description) "
                +
                "VALUES (?, 'WITHDRAW', ?, ?, ?)";

        try (Connection con = DatabaseUtil.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement updatePs = con.prepareStatement(updateSql);
                    PreparedStatement insertPs = con.prepareStatement(insertSql)) {

                // Update balance
                updatePs.setDouble(1, amount);
                updatePs.setString(2, accountNumber);
                int rowsUpdated = updatePs.executeUpdate();

                if (rowsUpdated == 0) {
                    con.rollback();
                    return false;
                }

                // Record transaction
                String txId = generateTransactionId(con);
                insertPs.setString(1, txId);
                insertPs.setString(2, accountNumber);
                insertPs.setDouble(3, amount);
                insertPs.setString(4, description != null ? description : "Withdrawal");
                insertPs.executeUpdate();

                con.commit();
                LoggerUtil.info("Withdrawal of PKR " + amount + " from account " + accountNumber);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error withdrawing money: " + ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Transfers money between accounts.
     */
    public boolean transfer(String fromAccountNumber, String toAccountNumber, double amount, String description) {
        if (amount <= 0 || fromAccountNumber.equals(toAccountNumber)) {
            return false;
        }

        // Check balance
        String checkSql = "SELECT balance FROM accounts WHERE account_number = ? AND is_active = TRUE";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setString(1, fromAccountNumber);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next() || rs.getDouble("balance") < amount) {
                    return false; // Insufficient balance
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error checking balance: " + ex.getMessage(), ex);
            return false;
        }

        String updateFromSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ? AND is_active = TRUE";
        String updateToSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ? AND is_active = TRUE";
        String insertOutSql = "INSERT INTO transactions(transaction_id, transaction_type, from_account, to_account, amount, description) "
                +
                "VALUES (?, 'TRANSFER_OUT', ?, ?, ?, ?)";
        String insertInSql = "INSERT INTO transactions(transaction_id, transaction_type, from_account, to_account, amount, description) "
                +
                "VALUES (?, 'TRANSFER_IN', ?, ?, ?, ?)";

        try (Connection con = DatabaseUtil.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement updateFromPs = con.prepareStatement(updateFromSql);
                    PreparedStatement updateToPs = con.prepareStatement(updateToSql);
                    PreparedStatement insertOutPs = con.prepareStatement(insertOutSql);
                    PreparedStatement insertInPs = con.prepareStatement(insertInSql)) {

                // Update from account
                updateFromPs.setDouble(1, amount);
                updateFromPs.setString(2, fromAccountNumber);
                int rowsUpdated1 = updateFromPs.executeUpdate();

                // Update to account
                updateToPs.setDouble(1, amount);
                updateToPs.setString(2, toAccountNumber);
                int rowsUpdated2 = updateToPs.executeUpdate();

                if (rowsUpdated1 == 0 || rowsUpdated2 == 0) {
                    con.rollback();
                    return false;
                }

                // Record transactions
                String txIdOut = generateTransactionId(con);
                String txIdIn = generateTransactionId(con);
                String desc = description != null ? description : "Transfer";

                insertOutPs.setString(1, txIdOut);
                insertOutPs.setString(2, fromAccountNumber);
                insertOutPs.setString(3, toAccountNumber);
                insertOutPs.setDouble(4, amount);
                insertOutPs.setString(5, desc + " (debit)");
                insertOutPs.executeUpdate();

                insertInPs.setString(1, txIdIn);
                insertInPs.setString(2, fromAccountNumber);
                insertInPs.setString(3, toAccountNumber);
                insertInPs.setDouble(4, amount);
                insertInPs.setString(5, desc + " (credit)");
                insertInPs.executeUpdate();

                con.commit();
                LoggerUtil.info("Transfer of PKR " + amount + " from " + fromAccountNumber + " to " + toAccountNumber);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error transferring money: " + ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Opens a new account for a customer.
     */
    public BankAccount openAccount(Integer customerId, AccountType type, double initialDeposit, String branchName) {
        String accountNumber = generateAccountNumber();
        String sql = "INSERT INTO accounts(account_number, customer_id, account_type, balance, branch_name, opened_at) "
                +
                "VALUES (?, ?, ?, ?, ?, CURDATE())";

        try (Connection con = DatabaseUtil.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accountNumber);
                ps.setInt(2, customerId);
                ps.setString(3, type.name());
                ps.setDouble(4, initialDeposit);
                ps.setString(5, branchName != null ? branchName : "Main Branch");
                ps.executeUpdate();

                // Record open account transaction
                String txId = generateTransactionId(con);
                String insertTxSql = "INSERT INTO transactions(transaction_id, transaction_type, to_account, amount, description) "
                        +
                        "VALUES (?, 'OPEN_ACCOUNT', ?, 0, ?)";
                try (PreparedStatement txPs = con.prepareStatement(insertTxSql)) {
                    txPs.setString(1, txId);
                    txPs.setString(2, accountNumber);
                    txPs.setString(3, "Opened " + type.getDisplayName());
                    txPs.executeUpdate();
                }

                // If initial deposit, record it
                if (initialDeposit > 0) {
                    deposit(accountNumber, initialDeposit, "Initial deposit");
                }

                con.commit();

                // Return the created account
                return findAccount(accountNumber);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error opening account: " + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Generates a unique account number.
     */
    private String generateAccountNumber() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(account_number, 4) AS UNSIGNED)), 0) + 1 as next_id FROM accounts";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return String.format("AC-%05d", nextId);
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error generating account number: " + ex.getMessage(), ex);
        }
        return "AC-10001";
    }

    /**
     * Generates a unique transaction ID.
     */
    private String generateTransactionId(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_id, 3) AS UNSIGNED)), 0) + 1 as next_id FROM transactions";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return String.format("T-%05d", nextId);
            }
        }
        return "T-00001";
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Gets all bank accounts (for admin view).
     * Includes both active and inactive accounts.
     * 
     * @return list of all bank accounts
     */
    public List<BankAccount> getAllAccounts() {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.id as customer_id, c.account_id, c.id_card_number, c.name, c.email, c.phone, c.address FROM accounts a "
                +
                "JOIN customers c ON a.customer_id = c.id " +
                "ORDER BY a.id DESC";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String accountNumber = rs.getString("account_number");
                AccountType type = AccountType.valueOf(rs.getString("account_type"));
                double balance = rs.getDouble("balance");
                String branchName = rs.getString("branch_name");
                boolean isActive = rs.getBoolean("is_active");

                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("account_id"), // Fetching account_id
                        rs.getString("id_card_number"), // Fetching id_card_number
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"));

                BankAccount account = new BankAccount(accountNumber, customer, type, balance, branchName);
                account.setActive(isActive);
                accounts.add(account);
            }

            LoggerUtil.info("Retrieved " + accounts.size() + " accounts for admin view");
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting all accounts: " + ex.getMessage(), ex);
        }

        return accounts;
    }

    /**
     * Gets all transactions (for admin view).
     * Returns all transactions in the system ordered by timestamp.
     * 
     * @return list of all transactions
     */
    public List<BankTransaction> getAllTransactions() {
        List<BankTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 1000";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String txId = rs.getString("transaction_id");
                TransactionType type = TransactionType.valueOf(rs.getString("transaction_type"));
                String fromAccount = rs.getString("from_account");
                String toAccount = rs.getString("to_account");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");

                BankTransaction tx = new BankTransaction(
                        txId, type, fromAccount, toAccount, amount, description);
                transactions.add(tx);
            }

            LoggerUtil.info("Retrieved " + transactions.size() + " transactions for admin view");
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting all transactions: " + ex.getMessage(), ex);
        }

        return transactions;
    }

    /**
     * Closes an account (sets it as inactive).
     * Account balance must be zero before closing.
     * 
     * @param accountNumber the account number to close
     * @return true if successfully closed, false otherwise
     */
    public boolean closeAccount(String accountNumber) {
        // Check balance first
        String checkSql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    LoggerUtil.warn("Account not found: " + accountNumber);
                    return false;
                }
                if (rs.getDouble("balance") != 0.0) {
                    LoggerUtil.warn("Cannot close account with non-zero balance: " + accountNumber);
                    return false;
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error checking account balance: " + ex.getMessage(), ex);
            return false;
        }

        String sql = "UPDATE accounts SET is_active = FALSE, closed_at = CURDATE() WHERE account_number = ?";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                LoggerUtil.info("Account closed: " + accountNumber);

                // Record transaction
                String txSql = "INSERT INTO transactions(transaction_id, transaction_type, from_account, amount, description) "
                        +
                        "VALUES (?, 'CLOSE_ACCOUNT', ?, 0, ?)";
                try (PreparedStatement txPs = con.prepareStatement(txSql)) {
                    String txId = generateTransactionId(con);
                    txPs.setString(1, txId);
                    txPs.setString(2, accountNumber);
                    txPs.setString(3, "Account closed");
                    txPs.executeUpdate();
                }

                return true;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error closing account: " + ex.getMessage(), ex);
        }

        return false;
    }

    /**
     * Deletes an account permanently.
     * Account must be closed and have zero balance.
     * 
     * @param accountNumber the account number to delete
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteAccount(String accountNumber) {
        // Check if account can be deleted
        String checkSql = "SELECT balance, is_active FROM accounts WHERE account_number = ?";
        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    LoggerUtil.warn("Account not found: " + accountNumber);
                    return false;
                }
                if (rs.getDouble("balance") != 0.0) {
                    LoggerUtil.warn("Cannot delete account with non-zero balance: " + accountNumber);
                    return false;
                }
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error checking account: " + ex.getMessage(), ex);
            return false;
        }

        String sql = "DELETE FROM accounts WHERE account_number = ?";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                LoggerUtil.info("Account deleted: " + accountNumber);
                return true;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error deleting account: " + ex.getMessage(), ex);
        }

        return false;
    }

    /**
     * Updates the branch name of an account.
     * 
     * @param accountNumber the account number
     * @param branchName    the new branch name
     * @return true if successfully updated, false otherwise
     */
    public boolean updateAccountBranch(String accountNumber, String branchName) {
        if (branchName == null || branchName.isBlank()) {
            LoggerUtil.error("Branch name cannot be empty", null);
            return false;
        }

        String sql = "UPDATE accounts SET branch_name = ? WHERE account_number = ?";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, branchName);
            ps.setString(2, accountNumber);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                LoggerUtil.info("Account branch updated: " + accountNumber + " -> " + branchName);
                return true;
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error updating account branch: " + ex.getMessage(), ex);
        }

        return false;
    }

    /**
     * Gets the total balance across all active accounts.
     * 
     * @return total balance
     */
    public double getTotalBalance() {
        String sql = "SELECT COALESCE(SUM(balance), 0) as total FROM accounts WHERE is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting total balance: " + ex.getMessage(), ex);
        }

        return 0.0;
    }

    /**
     * Gets the total number of accounts.
     * 
     * @return total account count
     */
    public int getTotalAccounts() {
        String sql = "SELECT COUNT(*) as count FROM accounts";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting total accounts: " + ex.getMessage(), ex);
        }

        return 0;
    }

    /**
     * Gets the number of active accounts.
     * 
     * @return active account count
     */
    public int getTotalActiveAccounts() {
        String sql = "SELECT COUNT(*) as count FROM accounts WHERE is_active = TRUE";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting active accounts: " + ex.getMessage(), ex);
        }

        return 0;
    }

    /**
     * Gets the total number of customers.
     * 
     * @return total customer count
     */
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) as count FROM customers";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting total customers: " + ex.getMessage(), ex);
        }

        return 0;
    }

    /**
     * Gets the count of transactions made today.
     * 
     * @return today's transaction count
     */
    public int getTransactionsTodayCount() {
        String sql = "SELECT COUNT(*) as count FROM transactions WHERE DATE(timestamp) = CURDATE()";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting today's transactions: " + ex.getMessage(), ex);
        }

        return 0;
    }

    /**
     * Gets the net flow (deposits - withdrawals) for today.
     * 
     * @return today's net flow
     */
    public double getTodayNetFlow() {
        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN amount ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN transaction_type IN ('WITHDRAW', 'TRANSFER_OUT') THEN amount ELSE 0 END), 0) as net_flow "
                +
                "FROM transactions WHERE DATE(timestamp) = CURDATE()";

        try (Connection con = DatabaseUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("net_flow");
            }
        } catch (SQLException ex) {
            LoggerUtil.error("Error getting today's net flow: " + ex.getMessage(), ex);
        }

        return 0.0;
    }
}
