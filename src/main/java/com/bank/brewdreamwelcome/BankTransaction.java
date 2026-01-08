package com.bank.brewdreamwelcome;

import java.time.LocalDateTime;

/**
 * Simple transaction model representing money movement.
 */
public class BankTransaction {
    private final String id; // e.g. T-0001
    private final TransactionType type;
    private final String fromAccount; // may be null
    private final String toAccount;   // may be null
    private final double amount;
    private final LocalDateTime timestamp;
    private final String description;

    public BankTransaction(String id,
                           TransactionType type,
                           String fromAccount,
                           String toAccount,
                           double amount,
                           String description) {
        this.id = id;
        this.type = type;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}


