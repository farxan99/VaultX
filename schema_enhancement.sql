-- VaultX Production Database Schema Enhancement
-- Adds account approval workflow, multi-account support, transaction tagging, and audit trails

USE vaultx;

-- 1. Add account approval status to customers
ALTER TABLE customers 
ADD COLUMN account_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' AFTER kyc_status,
ADD COLUMN approved_by INT NULL AFTER account_status,
ADD COLUMN approved_at TIMESTAMP NULL AFTER approved_by,
ADD COLUMN rejection_reason TEXT NULL AFTER approved_at;

-- 2. Enhance transactions table with tagging and notes
ALTER TABLE transactions
ADD COLUMN transaction_note TEXT NULL AFTER description,
ADD COLUMN transaction_tag VARCHAR(50) NULL AFTER transaction_note,
ADD COLUMN status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REVERSED') DEFAULT 'COMPLETED' AFTER transaction_tag,
ADD COLUMN processed_by INT NULL AFTER status;

-- 3. Create transaction categories/tags table
CREATE TABLE IF NOT EXISTS transaction_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(50) UNIQUE NOT NULL,
    tag_color VARCHAR(7) DEFAULT '#3B82F6',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Insert default transaction tags
INSERT INTO transaction_tags (tag_name, tag_color) VALUES
('Salary', '#10B981'),
('Rent', '#EF4444'),
('Utilities', '#F59E0B'),
('Groceries', '#8B5CF6'),
('Entertainment', '#EC4899'),
('Healthcare', '#06B6D4'),
('Investment', '#14B8A6'),
('Other', '#6B7280');

-- 5. Create account freeze/unfreeze log
CREATE TABLE IF NOT EXISTS account_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    previous_status ENUM('ACTIVE', 'FROZEN', 'CLOSED'),
    new_status ENUM('ACTIVE', 'FROZEN', 'CLOSED'),
    changed_by INT NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    FOREIGN KEY (changed_by) REFERENCES admins(id)
);

-- 6. Create KYC documents table
CREATE TABLE IF NOT EXISTS kyc_documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    document_type ENUM('ID_CARD', 'PASSPORT', 'UTILITY_BILL', 'BANK_STATEMENT') NOT NULL,
    document_path VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_by INT NULL,
    verified_at TIMESTAMP NULL,
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES admins(id)
);

-- 7. Create fraud detection alerts table
CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    alert_type ENUM('UNUSUAL_AMOUNT', 'RAPID_TRANSACTIONS', 'LOCATION_MISMATCH', 'PATTERN_ANOMALY') NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    description TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_by INT NULL,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    FOREIGN KEY (resolved_by) REFERENCES admins(id)
);

-- 8. Create system health monitoring table
CREATE TABLE IF NOT EXISTS system_health_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(18,4) NOT NULL,
    status ENUM('NORMAL', 'WARNING', 'CRITICAL') DEFAULT 'NORMAL',
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_metric_time (metric_name, recorded_at)
);

-- 9. Enhance scheduled_transfers with more options
ALTER TABLE scheduled_transfers
ADD COLUMN start_date DATE NOT NULL AFTER amount,
ADD COLUMN end_date DATE NULL AFTER next_execution_date,
ADD COLUMN execution_count INT DEFAULT 0 AFTER is_active,
ADD COLUMN last_executed_at TIMESTAMP NULL AFTER execution_count,
ADD COLUMN created_by INT NOT NULL AFTER last_executed_at,
ADD FOREIGN KEY (created_by) REFERENCES customers(id);

-- 10. Create account statements generation log
CREATE TABLE IF NOT EXISTS statement_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    requested_by INT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    file_path VARCHAR(255) NULL,
    status ENUM('PENDING', 'GENERATED', 'FAILED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_at TIMESTAMP NULL,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    FOREIGN KEY (requested_by) REFERENCES customers(id)
);

-- 11. Add indexes for performance optimization
CREATE INDEX idx_customer_status ON customers(account_status);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_timestamp ON transactions(timestamp);
CREATE INDEX idx_account_status ON accounts(status);
CREATE INDEX idx_fraud_unresolved ON fraud_alerts(is_resolved, severity);

-- 12. Update existing test customer to APPROVED status
UPDATE customers SET account_status = 'APPROVED' WHERE email = 'test@vaultx.com';

-- 13. Create view for customer account summary
CREATE OR REPLACE VIEW customer_account_summary AS
SELECT 
    c.id AS customer_id,
    c.name,
    c.email,
    c.account_status,
    COUNT(a.account_number) AS total_accounts,
    SUM(a.balance) AS total_balance,
    GROUP_CONCAT(a.account_number) AS account_numbers
FROM customers c
LEFT JOIN accounts a ON c.id = a.customer_id
GROUP BY c.id;

-- Success message
SELECT 'VaultX Production Schema Enhancement Complete!' AS Status;
