package com.bank.brewdreamwelcome.service;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import com.bank.brewdreamwelcome.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Production-ready Bank Service with ACID transaction guarantees.
 */
public class ModernBankService {
    private static final Logger logger = LoggerFactory.getLogger(ModernBankService.class);
    private final AccountRepository accountRepo;

    public ModernBankService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public boolean transferFunds(String fromAcc, String toAcc, double amount) {
        if (amount <= 0) return false;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try {
                // 1. Lock rows in alphabetical order to PREVENT DEADLOCKS
                String first = fromAcc.compareTo(toAcc) < 0 ? fromAcc : toAcc;
                String second = first.equals(fromAcc) ? toAcc : fromAcc;

                var acc1 = accountRepo.findAndLockByAccountNumber(conn, first);
                var acc2 = accountRepo.findAndLockByAccountNumber(conn, second);

                if (acc1.isEmpty() || acc2.isEmpty()) {
                    conn.rollback();
                    return false;
                }

                // 2. Validate Business Rules
                AccountRepository.AccountRecord from = fromAcc.equals(first) ? acc1.get() : acc2.get();
                AccountRepository.AccountRecord to = toAcc.equals(first) ? acc1.get() : acc2.get();

                if (!"ACTIVE".equals(from.status()) || from.balance() < amount) {
                    conn.rollback();
                    return false;
                }

                // 3. Perform atomic updates
                accountRepo.updateBalance(conn, fromAcc, from.balance() - amount);
                accountRepo.updateBalance(conn, toAcc, to.balance() + amount);

                conn.commit(); // Finalize
                logger.info("Transfer successful: {} -> {} (Amount: {})", fromAcc, toAcc, amount);
                return true;

            } catch (Exception e) {
                conn.rollback();
                logger.error("Transfer failed, rolling back", e);
                throw e;
            }
        } catch (SQLException e) {
            logger.error("DB Error during transfer", e);
            return false;
        }
    }
}
