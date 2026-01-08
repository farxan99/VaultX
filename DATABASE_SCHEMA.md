# VaultX Bank - Database Schema Documentation

## Database: `vaultx`

### Table: `admins`

Administrator accounts for system access.

| Column | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique admin identifier |
| `username` | VARCHAR(64) | UNIQUE, NOT NULL | Admin login username |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE on `username`

**Default Admin Accounts**:
- `admin` / `Admin@123!Secure`
- `Farxan` / `Farxan@2024#Bank`
- `Hasnain` / `Hasnain$Bank2024!`
- `Manager` / `Manager@VaultX2024!`
- `SuperAdmin` / `SuperAdmin#Secure2024!`

---

### Table: `customers`

Customer accounts for banking services.

| Column | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique customer identifier |
| `account_id` | VARCHAR(6) | UNIQUE, NOT NULL | 6-digit account number (auto-generated) |
| `username` | VARCHAR(64) | NULL | Optional username for login |
| `name` | VARCHAR(120) | NOT NULL | Customer full name |
| `email` | VARCHAR(120) | UNIQUE, NOT NULL | Customer email address |
| `phone` | VARCHAR(40) | NULL | Contact phone number |
| `address` | VARCHAR(255) | NULL | Customer address |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE on `account_id`
- UNIQUE on `email`

**Login Methods**:
Customers can login using:
1. `account_id` (6-digit number)
2. `email` (email address)
3. `username` (if provided)

**Password Requirements**:
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character: `!@#$%^&*()_+-=[]{}|;:,.<>?`

---

## SQL Schema Creation

```sql
CREATE DATABASE IF NOT EXISTS vaultx;
USE vaultx;

-- Admins table
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(6) NOT NULL UNIQUE,
    username VARCHAR(64),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(40),
    address VARCHAR(255),
    password VARCHAR(255) NOT NULL
);
```

---

## Security Notes

1. **Password Storage**: All passwords are hashed using BCrypt before storage
2. **No Plain Text**: Passwords are never stored in plain text
3. **SQL Injection Prevention**: All queries use PreparedStatements
4. **Case Sensitivity**: Usernames and passwords are case-sensitive

---

## Data Flow

### Registration Flow
1. User submits registration form
2. Password validated for strength
3. Unique 6-digit `account_id` generated
4. Password hashed with BCrypt
5. Customer record inserted into database

### Authentication Flow
1. User enters identifier (account_id/email/username) and password
2. System checks `admins` table first
3. If not admin, checks `customers` table
4. Password verified using BCrypt
5. Returns role and customer_id (if customer)

---

## Maintenance

### Backup
Regular database backups recommended:
```bash
mysqldump -u root vaultx > vaultx_backup.sql
```

### Reset Database
To reset all data (drops and recreates tables):
- Delete database and restart application
- Or manually run DROP TABLE commands

---

**Last Updated**: 2024

