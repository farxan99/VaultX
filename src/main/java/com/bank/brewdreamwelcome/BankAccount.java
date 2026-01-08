package com.bank.brewdreamwelcome;

import java.time.LocalDate;

/**
 * Simple bank account model.
 */
public class BankAccount {
    private final String accountNumber; // e.g. AC-10001
    private final Customer customer;
    private final AccountType type;
    private double balance;
    private boolean active;
    private final LocalDate openedAt;
    private LocalDate closedAt;
    private String branchName;

    public BankAccount(String accountNumber, Customer customer, AccountType type, double initialBalance, String branchName) {
        this.accountNumber = accountNumber;
        this.customer = customer;
        this.type = type;
        this.balance = initialBalance;
        this.branchName = branchName;
        this.active = true;
        this.openedAt = LocalDate.now();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public AccountType getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return active;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getOpenedAt() {
        return openedAt;
    }

    public LocalDate getClosedAt() {
        return closedAt;
    }

    void setClosedAt(LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return accountNumber + " (" + type.getDisplayName() + ")";
    }
}


