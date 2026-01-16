-- VaultX Production Evolution Script

-- 1. Create Audit Logs for Security Compliance
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Enhance Accounts with Precision and Security
ALTER TABLE accounts 
    MODIFY balance DECIMAL(18, 4) DEFAULT 0.0000,
    ADD COLUMN last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN status ENUM('ACTIVE', 'FROZEN', 'CLOSED') DEFAULT 'ACTIVE';

-- 3. Enhance Customers with Security Metadata
ALTER TABLE customers
    ADD COLUMN failed_attempts INT DEFAULT 0,
    ADD COLUMN locked_until TIMESTAMP NULL,
    ADD COLUMN kyc_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING';

-- 4. Recurring Transactions Support
CREATE TABLE IF NOT EXISTS scheduled_transfers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_account_no VARCHAR(20),
    receiver_account_no VARCHAR(20),
    amount DECIMAL(18,4),
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY'),
    next_execution_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (sender_account_no) REFERENCES accounts(account_number)
);

-- 5. Indexes for Scalability
CREATE INDEX idx_tx_timestamp ON transactions(timestamp);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
