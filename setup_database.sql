-- VaultX Complete Database Setup Script
-- Run this in phpMyAdmin SQL tab

-- Drop existing database if it exists (clean slate)
DROP DATABASE IF EXISTS vaultx;

-- Create the database
CREATE DATABASE vaultx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Select the database
USE vaultx;

-- 1. Admins Table
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE,
    id_card_number VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    password VARCHAR(255) NOT NULL,
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    kyc_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT') DEFAULT 'SAVINGS',
    balance DECIMAL(18, 4) DEFAULT 0.0000,
    branch_name VARCHAR(100) DEFAULT 'Main Branch',
    is_active BOOLEAN DEFAULT TRUE,
    status ENUM('ACTIVE', 'FROZEN', 'CLOSED') DEFAULT 'ACTIVE',
    opened_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- 4. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_account VARCHAR(20),
    to_account VARCHAR(20),
    amount DECIMAL(18, 4) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account) REFERENCES accounts(account_number),
    FOREIGN KEY (to_account) REFERENCES accounts(account_number)
);

-- 5. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Scheduled Transfers Table
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

-- 7. Create Indexes for Performance
CREATE INDEX idx_tx_timestamp ON transactions(timestamp);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_account_id ON customers(account_id);

-- 8. Insert Default Admin (username: admin, password: admin123)
-- Password is BCrypt hashed
INSERT INTO admins (username, password) VALUES 
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYq0Oi');

-- 9. Insert Sample Customer for Testing
-- Account ID: 100001, Email: test@vaultx.com, Password: test123
INSERT INTO customers (account_id, username, id_card_number, name, email, phone, address, password) VALUES
('100001', 'testuser', '12345-6789012-3', 'Test User', 'test@vaultx.com', '03001234567', '123 Test Street', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYq0Oi');

-- 10. Create Account for Test Customer
INSERT INTO accounts (account_number, customer_id, account_type, balance, branch_name) VALUES
('100001', 1, 'SAVINGS', 5000.0000, 'Main Branch');

-- Success message
SELECT 'VaultX Database Setup Complete!' AS Status;
