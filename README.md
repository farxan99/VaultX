# VaultX Bank Management System

A comprehensive Java-based banking management system with role-based access control, secure authentication, and modern GUI design.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [User Guide](#user-guide)
- [Project Structure](#project-structure)
- [Security Features](#security-features)
- [Credits](#credits)

## 🎯 Overview

VaultX Bank is a desktop banking management application built with Java Swing. It provides a complete banking solution with separate interfaces for administrators and customers, featuring secure authentication, account management, transaction processing, and comprehensive reporting.

## ✨ Features

### Authentication & Security
- **Multi-identifier Login**: Users can login using Account ID, Email, or Username
- **Strong Password Policy**: Enforces password requirements (8+ chars, uppercase, lowercase, digit, special char)
- **Password Hashing**: BCrypt encryption for secure password storage
- **Role-Based Access Control**: Separate dashboards for Admins and Customers
- **CAPTCHA Protection**: Security verification on login and registration

### Admin Features
- **Full Dashboard**: View total customers, accounts, balance, and activity metrics
- **Account Management**: Open, close, edit, and delete bank accounts
- **Customer Management**: Create, update, and remove customer records
- **Transaction Monitoring**: Complete audit trail of all transactions
- **Financial Overview**: Real-time bank performance metrics

### Customer Features
- **Personal Dashboard**: View own account balance and activity
- **Transaction History**: Filter and view personal transaction records
- **Account Overview**: See all personal accounts and balances

### User Management
- **Auto-Generated Account IDs**: 6-digit unique account numbers
- **User Registration**: Self-service account creation with validation
- **Password Recovery**: Forgot password functionality

## 🛠 Technology Stack

- **Language**: Java 22
- **GUI Framework**: Java Swing
- **Database**: MySQL (via XAMPP)
- **Build Tool**: Maven
- **Dependencies**:
  - MySQL Connector/J 8.0.33
  - BCrypt (jbcrypt 0.4) - Password hashing
  - JFreeChart 1.5.0 - Charts and analytics

## 📦 Prerequisites

Before running the application, ensure you have:

1. **Java Development Kit (JDK) 22** or higher
2. **XAMPP** (or MySQL Server) installed and running
3. **Maven** 3.6+ (for dependency management)
4. **NetBeans IDE** (recommended) or any Java IDE

## 🗄 Database Setup

### Step 1: Start MySQL Server
- Open XAMPP Control Panel
- Start MySQL service

### Step 2: Create Database
The application will automatically create the database and tables on first run. However, you can also create it manually:

```sql
CREATE DATABASE IF NOT EXISTS vaultx;
USE vaultx;
```

### Step 3: Database Schema
The application automatically creates the following tables:

**admins** table:
- `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
- `username` (VARCHAR(64), UNIQUE, NOT NULL)
- `password` (VARCHAR(255), NOT NULL) - BCrypt hashed

**customers** table:
- `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
- `account_id` (VARCHAR(6), UNIQUE, NOT NULL) - 6-digit account number
- `username` (VARCHAR(64), OPTIONAL)
- `name` (VARCHAR(120), NOT NULL)
- `email` (VARCHAR(120), UNIQUE, NOT NULL)
- `phone` (VARCHAR(40))
- `address` (VARCHAR(255))
- `password` (VARCHAR(255), NOT NULL) - BCrypt hashed

## 🚀 Installation & Setup

### 1. Clone or Download the Project
```bash
cd vaultX2
```

### 2. Configure Database Connection
Edit `src/main/java/com/bank/brewdreamwelcome/DatabaseUtil.java` if needed:
- Default: `localhost:3306`
- Database: `vaultx`
- User: `root`
- Password: (empty)

### 3. Build the Project
```bash
mvn clean install
```

Or use your IDE's build function.

### 4. Run the Application
```bash
mvn exec:java
```

Or run `WelcomeApp.java` from your IDE.

## 📖 User Guide

### For Administrators

**Login Credentials** (see `VAULTX_CREDENTIALS.txt`):
- Username: `admin` / Password: `Admin@123!Secure`
- Username: `Farxan` / Password: `Farxan@2024#Bank`
- Username: `Hasnain` / Password: `Hasnain$Bank2024!`

**Admin Dashboard Features**:
1. **Dashboard**: View bank-wide metrics and KPIs
2. **Accounts**: Manage all bank accounts (open, close, deposit, withdraw, transfer)
3. **Customers**: View and manage customer records
4. **Transactions**: Complete transaction history with filtering

### For Customers

**Login Options**:
- Use your 6-digit Account ID
- Use your registered Email
- Use your Username (if set)

**Customer Dashboard Features**:
1. **Dashboard**: Personal account overview
2. **Transactions**: View your transaction history

### Registration

1. Click "Sign Up" on the login screen
2. Fill in:
   - Full Name (minimum 3 characters)
   - Email Address (valid format required)
   - Username (optional)
   - Password (must meet strength requirements)
   - Confirm Password
3. Complete CAPTCHA verification
4. Your 6-digit Account ID will be auto-generated

### Password Requirements

- **Minimum 8 characters**
- **At least 1 uppercase letter** (A-Z)
- **At least 1 lowercase letter** (a-z)
- **At least 1 digit** (0-9)
- **At least 1 special character**: `!@#$%^&*()_+-=[]{}|;:,.<>?`

## 📁 Project Structure

```
vaultX2/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/bank/brewdreamwelcome/
│   │           ├── WelcomeApp.java          # Main entry point & Splash screen
│   │           ├── LoginFrame.java          # Login interface
│   │           ├── SignupFrame.java        # Registration interface
│   │           ├── ForgotPasswordFrame.java # Password recovery
│   │           ├── AdminDashboardFrame.java # Admin dashboard
│   │           ├── CustomerDashboardFrame.java # Customer dashboard
│   │           ├── AuthService.java        # Authentication logic
│   │           ├── PasswordValidator.java  # Password strength validation
│   │           ├── DatabaseUtil.java       # Database connection
│   │           ├── BankService.java       # Business logic
│   │           ├── BankAccount.java       # Account model
│   │           ├── BankTransaction.java   # Transaction model
│   │           ├── Customer.java          # Customer model
│   │           └── VaultXTheme.java       # UI theme utilities
│   └── test/
│       └── java/                           # Unit tests (to be added)
├── pom.xml                                 # Maven configuration
├── VAULTX_CREDENTIALS.txt                 # Default credentials
└── README.md                               # This file
```

## 🔒 Security Features

1. **Password Hashing**: All passwords stored using BCrypt (industry standard)
2. **Strong Password Policy**: Enforced at registration
3. **Role-Based Access**: Admins and Customers have separate interfaces
4. **Data Isolation**: Customers can only view their own transactions
5. **CAPTCHA Protection**: Prevents automated attacks
6. **SQL Injection Prevention**: Prepared statements used throughout
7. **Input Validation**: Comprehensive validation on all user inputs

## 🎨 GUI Features

- **Modern Design**: Dark theme with red/blue accent colors
- **Smooth Animations**: Fade-in effects and transitions
- **Responsive Layout**: Clean, professional interface
- **Visual Feedback**: Clear error messages and success notifications
- **Intuitive Navigation**: Easy-to-use sidebar navigation

## 📊 Default Accounts

See `VAULTX_CREDENTIALS.txt` for complete list of:
- 5 Admin accounts with strong passwords
- 5 Customer accounts with strong passwords

## 🐛 Troubleshooting

### Database Connection Issues
- Ensure MySQL is running in XAMPP
- Check database credentials in `DatabaseUtil.java`
- Verify database `vaultx` exists

### Password Issues
- Ensure password meets all requirements
- Check password strength validator messages

### Build Issues
- Run `mvn clean install` to refresh dependencies
- Ensure JDK 22 is properly configured

## 📝 Development Notes

- The application uses in-memory `BankService` for demo data
- Database is used for authentication and user management
- All passwords are hashed before storage
- Account IDs are auto-generated as 6-digit numbers

## 👥 Credits

**Project**: VaultX Bank Management System  
**Course**: Java Programming  
**Semester**: [Your Semester]  
**University**: [Your University]

## 📄 License

This is an academic project for educational purposes.

---

**Note**: This is a university project. All credentials in `VAULTX_CREDENTIALS.txt` are for demonstration purposes only.

