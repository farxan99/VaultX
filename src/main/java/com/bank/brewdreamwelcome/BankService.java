package com.bank.brewdreamwelcome;

import java.util.List;

/**
 * Bank Service Facade.
 * Delegates operations to persistent database services (BankDatabaseService,
 * CustomerDatabaseService).
 * Replaces previous in-memory implementation.
 */
public final class BankService {

    private static final BankService INSTANCE = new BankService();

    private final BankDatabaseService bankDb = BankDatabaseService.getInstance();
    private final CustomerDatabaseService customerDb = CustomerDatabaseService.getInstance();

    public static BankService getInstance() {
        return INSTANCE;
    }

    private BankService() {
        // No in-memory seeding. Database persistence handles data.
    }

    /* ===================== CUSTOMER CRUD ===================== */

    public synchronized Customer createCustomer(String name, String email, String phone, String address) {
        // Provide defaults for fields not present in this simplified signature
        // This method is likely used by legacy code or simple tests
        String generatedUsername = email.split("@")[0];
        String defaultPassword = "password123";
        String generatedIdCard = String.valueOf(System.currentTimeMillis() % 1000000000);

        return customerDb.createCustomer(
                generatedUsername,
                name,
                email,
                phone,
                address,
                defaultPassword,
                generatedIdCard,
                "SAVINGS");
    }

    public synchronized void updateCustomer(Customer customer, String name, String email, String phone,
            String address) {
        customerDb.updateCustomer(customer.getId(), name, email, phone, address);
        // Optimize: Update local object if needed, but fetching fresh from DB is better
        // practice
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
    }

    public synchronized void deleteCustomer(Customer customer) {
        // Delete customer and their accounts
        customerDb.deleteCustomer(customer.getId());
    }

    public synchronized List<Customer> getCustomers() {
        return customerDb.getAllCustomers();
    }

    /* ===================== ACCOUNT CRUD ===================== */

    public synchronized BankAccount openAccount(Customer customer,
            AccountType type,
            double initialDeposit,
            String branchName) {
        return bankDb.openAccount(customer.getId(), type, initialDeposit, branchName);
    }

    public synchronized boolean closeAccount(BankAccount account) {
        if (account == null)
            return false;
        return bankDb.closeAccount(account.getAccountNumber());
    }

    public synchronized List<BankAccount> getAccounts() {
        return bankDb.getAllAccounts();
    }

    public synchronized void updateAccountBranch(BankAccount account, String branchName) {
        if (account == null)
            return;
        boolean success = bankDb.updateAccountBranch(account.getAccountNumber(), branchName);
        if (success) {
            account.setBranchName(branchName);
        }
    }

    public synchronized boolean deleteAccount(BankAccount account) {
        if (account == null)
            return false;
        return bankDb.deleteAccount(account.getAccountNumber());
    }

    public synchronized BankAccount findAccount(String accountNumber) {
        return bankDb.findAccount(accountNumber);
    }

    /* ===================== MONEY OPERATIONS ===================== */

    public synchronized boolean deposit(BankAccount account, double amount, String description) {
        if (account == null)
            return false;
        return bankDb.deposit(account.getAccountNumber(), amount, description);
    }

    public synchronized boolean withdraw(BankAccount account, double amount, String description) {
        if (account == null)
            return false;
        return bankDb.withdraw(account.getAccountNumber(), amount, description);
    }

    public synchronized boolean transfer(BankAccount from, BankAccount to, double amount, String description) {
        if (from == null || to == null)
            return false;
        return bankDb.transfer(from.getAccountNumber(), to.getAccountNumber(), amount, description);
    }

    /* ===================== TRANSACTIONS & METRICS ===================== */

    public synchronized List<BankTransaction> getTransactions() {
        return bankDb.getAllTransactions();
    }

    public synchronized double getTotalBalance() {
        return bankDb.getTotalBalance();
    }

    public synchronized int getTotalAccounts() {
        return bankDb.getTotalAccounts();
    }

    public synchronized int getTotalActiveAccounts() {
        return bankDb.getTotalActiveAccounts();
    }

    public synchronized int getTotalCustomers() {
        return bankDb.getTotalCustomers();
    }

    public synchronized long getTransactionsTodayCount() {
        return bankDb.getTransactionsTodayCount();
    }

    public synchronized double getTodayNetFlow() {
        return bankDb.getTodayNetFlow();
    }
}
