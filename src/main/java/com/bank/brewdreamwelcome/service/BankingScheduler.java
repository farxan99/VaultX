package com.bank.brewdreamwelcome.service;

import com.bank.brewdreamwelcome.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automates recurring payments and interest simulations.
 */
public class BankingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BankingScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ModernBankService bankService;

    public BankingScheduler(ModernBankService bankService) {
        this.bankService = bankService;
    }

    public void start() {
        // Runs every day
        scheduler.scheduleAtFixedRate(this::processRecurringTransfers, 0, 1, TimeUnit.DAYS);
        logger.info("Recurring transaction engine started.");
    }

    private void processRecurringTransfers() {
        String query = "SELECT * FROM scheduled_transfers WHERE next_execution_date <= CURDATE() AND is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String from = rs.getString("sender_account_no");
                String to = rs.getString("receiver_account_no");
                double amount = rs.getDouble("amount");
                
                boolean success = bankService.transferFunds(from, to, amount);
                if (success) {
                    updateNextExecutionDate(rs.getInt("id"), rs.getString("frequency"));
                    AuditService.log("SCHEDULED_TRANSFER", "Transfer of " + amount + " from " + from + " processed.");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing scheduled transfers", e);
        }
    }

    private void updateNextExecutionDate(int id, String frequency) {
        // Logic to increment date based on frequency (DAILY/WEEKLY/MONTHLY)
    }
}
