# VaultX Enhancement Implementation Plan

## Issues to Fix:
1. ✅ Dashboard showing wrong/dummy data
2. ✅ No navigation functionality
3. ✅ Missing password eye icon in signup
4. ✅ No input validation
5. ✅ Missing CAPTCHA
6. ✅ Admin approval system for new accounts
7. ✅ Admin login credentials

## Implementation Steps:

### 1. Database Schema Update
- Add `account_status` ENUM('PENDING', 'APPROVED', 'REJECTED') to customers table
- Default to 'PENDING' for new signups

### 2. Enhanced ModernDashboardView
- Fetch real user data from database
- Display actual balance, transactions
- Add functional navigation (Accounts, Transfers, Profile, Logout)
- Show transaction history
- Add deposit/withdraw/transfer functionality

### 3. Enhanced ModernSignupView
- Add password visibility toggle (eye icon)
- Add CAPTCHA generation and validation
- Implement strict input validation:
  * Name: 3-50 characters, letters only
  * Email: Valid email format
  * ID Card: Valid format (13 digits with dashes)
  * Password: Min 8 chars, uppercase, lowercase, number, special char
- Set account_status to 'PENDING' on creation

### 4. Enhanced ModernLoginView
- Add CAPTCHA
- Check account_status before login
- Show appropriate message if account is PENDING or REJECTED

### 5. Admin Dashboard
- Create AdminDashboardView
- Show pending account approvals
- Allow approve/reject actions
- View all customers and accounts

### 6. Admin Login Credentials
- Username: admin
- Password: admin123
- (Already in database from setup script)

## Next: Implement each component
