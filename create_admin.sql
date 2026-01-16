-- Quick fix: Ensure admin account exists
-- Run this in phpMyAdmin SQL tab

-- Delete existing admin if any
DELETE FROM admins WHERE username = 'admin';

-- Create admin account
-- Username: admin
-- Password: admin123
INSERT INTO admins (username, password, created_at) VALUES 
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYq0Oi', NOW());

-- Verify it was created
SELECT id, username, created_at FROM admins WHERE username = 'admin';
