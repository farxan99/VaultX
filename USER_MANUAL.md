# VaultX Bank - User Manual

## Table of Contents
1. [Getting Started](#getting-started)
2. [Registration](#registration)
3. [Login](#login)
4. [Customer Dashboard](#customer-dashboard)
5. [Admin Dashboard](#admin-dashboard)
6. [Troubleshooting](#troubleshooting)

---

## Getting Started

### System Requirements
- Java 22 or higher
- MySQL Database (XAMPP recommended)
- 2GB RAM minimum
- Windows/Linux/Mac OS

### First Time Setup
1. Ensure MySQL is running (via XAMPP)
2. Run the application
3. Database tables will be created automatically
4. Default admin accounts are created automatically

---

## Registration

### Step 1: Access Registration
- Click "Sign Up" on the login screen

### Step 2: Fill Registration Form
- **Full Name**: Enter your complete name (minimum 3 characters)
- **Email Address**: Enter a valid email address
- **Username** (Optional): Choose a username for login
- **Password**: Create a strong password (see requirements below)
- **Confirm Password**: Re-enter your password

### Step 3: Complete CAPTCHA
- Enter the CAPTCHA code shown (case-sensitive)
- Click refresh button (↻) if code is unclear

### Step 4: Submit
- Click "Sign Up →" button
- Your account will be created
- A 6-digit Account ID will be auto-generated
- You'll be redirected to login screen

### Password Requirements
Your password must contain:
- ✅ At least 8 characters
- ✅ At least 1 uppercase letter (A-Z)
- ✅ At least 1 lowercase letter (a-z)
- ✅ At least 1 digit (0-9)
- ✅ At least 1 special character: `!@#$%^&*()_+-=[]{}|;:,.<>?`

**Example Valid Passwords**:
- `MyBank@2024`
- `Secure$Pass1`
- `VaultX#2024!`

---

## Login

### Login Methods
You can login using any of these:
1. **Account ID**: Your 6-digit account number
2. **Email**: Your registered email address
3. **Username**: Your username (if you set one)

### Login Steps
1. Enter your identifier (Account ID/Email/Username)
2. Enter your password
3. Complete CAPTCHA verification
4. Click "Login →"

### Show/Hide Password
- Click the eye icon (👁) to show your password
- Click again (🙈) to hide it

### Forgot Password
- Click "Forgot password?" link
- Enter your email address
- Follow the reset instructions

---

## Customer Dashboard

### Dashboard Overview
- **My Balance**: Total balance across all your accounts
- **My Accounts**: Number of active accounts
- **Today's Activity**: Number of transactions today

### Transactions Tab
- View all your transaction history
- See deposits, withdrawals, and transfers
- Filter by date range (if implemented)
- View transaction details

### Navigation
- **Dashboard**: Overview of your accounts
- **Transactions**: Your transaction history
- **← Logout**: Exit and return to login

---

## Admin Dashboard

### Dashboard Overview
- **Customers**: Total registered customers
- **Accounts**: Total and active accounts
- **Total Balance**: Sum of all account balances
- **Today's Activity**: Transaction count and net flow

### Accounts Tab
**Available Actions**:
- **Open Account**: Create new bank account
- **Edit Account**: Modify account details
- **Close Account**: Close an account (balance must be 0)
- **Delete Account**: Remove account record
- **Deposit**: Add money to account
- **Withdraw**: Remove money from account
- **Transfer**: Transfer between accounts

### Customers Tab
**Available Actions**:
- **Add Customer**: Create new customer record
- **Edit Customer**: Update customer information
- **Delete Customer**: Remove customer (closes all accounts)

### Transactions Tab
- View all transactions in the system
- Filter by transaction type
- Filter by account number
- Complete audit trail

---

## Troubleshooting

### Cannot Connect to Database
**Problem**: "Error connecting to database"

**Solutions**:
1. Check if MySQL is running in XAMPP
2. Verify database credentials in `DatabaseUtil.java`
3. Ensure database `vaultx` exists

### Password Not Accepted
**Problem**: "Password validation failed"

**Solutions**:
1. Check password meets all requirements:
   - 8+ characters
   - Has uppercase letter
   - Has lowercase letter
   - Has digit
   - Has special character
2. Use the password requirements guide above

### Login Fails
**Problem**: "Invalid credentials"

**Solutions**:
1. Verify you're using correct identifier (Account ID/Email/Username)
2. Check password is correct (case-sensitive)
3. Ensure CAPTCHA is entered correctly
4. Try refreshing CAPTCHA if unclear

### Account ID Not Found
**Problem**: Cannot login with Account ID

**Solutions**:
1. Use your 6-digit account ID (check email confirmation)
2. Try logging in with email instead
3. Contact admin if account ID is lost

### Application Won't Start
**Problem**: Application doesn't launch

**Solutions**:
1. Verify Java 22 is installed: `java -version`
2. Check Maven dependencies: `mvn clean install`
3. Ensure MySQL driver is in classpath
4. Check console for error messages

---

## Tips & Best Practices

1. **Keep Your Account ID Safe**: Write it down when you register
2. **Use Strong Passwords**: Follow all password requirements
3. **Remember Your Login Method**: You can use Account ID, Email, or Username
4. **Check CAPTCHA Carefully**: It's case-sensitive
5. **Logout When Done**: Always logout for security

---

## Support

For technical issues:
- Check the README.md for setup instructions
- Review DATABASE_SCHEMA.md for database information
- Check application logs for error details

---

**Version**: 1.0  
**Last Updated**: 2024

