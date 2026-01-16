-- CRITICAL FIX: Ensure admin account exists
-- Copy and paste this ENTIRE script into phpMyAdmin SQL tab

-- Step 1: Check if admin exists
SELECT 'Checking for existing admin account...' AS Status;
SELECT * FROM admins WHERE username = 'admin';

-- Step 2: Delete any existing admin (clean slate)
DELETE FROM admins WHERE username = 'admin';

-- Step 3: Create fresh admin account
-- Username: admin
-- Password: admin123 (BCrypt hashed)
INSERT INTO admins (username, password, created_at) VALUES 
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVvMpYq0Oi', NOW());

-- Step 4: Verify admin was created
SELECT 'Admin account created successfully!' AS Status;
SELECT id, username, created_at FROM admins WHERE username = 'admin';

-- Step 5: Show all admins in database
SELECT 'All admin accounts:' AS Status;
SELECT id, username, created_at FROM admins;
