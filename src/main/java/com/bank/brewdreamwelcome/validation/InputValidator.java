package com.bank.brewdreamwelcome.validation;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized input validation utility for banking operations.
 * Implements strict validation rules for all user inputs.
 */
public class InputValidator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^\\d{5}-\\d{7}-\\d{1}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^03\\d{9}$"
    );
    
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
        "^\\d{6}$"
    );
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public String getFirstError() { return errors.isEmpty() ? "" : errors.get(0); }
    }
    
    public static ValidationResult validateName(String name) {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Name is required");
        } else if (name.trim().length() < 3) {
            errors.add("Name must be at least 3 characters");
        } else if (name.trim().length() > 50) {
            errors.add("Name must not exceed 50 characters");
        } else if (!name.matches("^[a-zA-Z\\s]+$")) {
            errors.add("Name must contain only letters and spaces");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        
        if (email == null || email.trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("Invalid email format");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateIdCard(String idCard) {
        List<String> errors = new ArrayList<>();
        
        if (idCard == null || idCard.trim().isEmpty()) {
            errors.add("ID Card number is required");
        } else if (!ID_CARD_PATTERN.matcher(idCard.trim()).matches()) {
            errors.add("ID Card must be in format: 12345-6789012-3");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validatePhone(String phone) {
        List<String> errors = new ArrayList<>();
        
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                errors.add("Phone must be in format: 03XXXXXXXXX (11 digits)");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
        } else {
            if (password.length() < 8) {
                errors.add("Password must be at least 8 characters");
            }
            if (!password.matches(".*[A-Z].*")) {
                errors.add("Password must contain at least one uppercase letter");
            }
            if (!password.matches(".*[a-z].*")) {
                errors.add("Password must contain at least one lowercase letter");
            }
            if (!password.matches(".*\\d.*")) {
                errors.add("Password must contain at least one digit");
            }
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                errors.add("Password must contain at least one special character");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateAmount(String amountStr) {
        List<String> errors = new ArrayList<>();
        
        if (amountStr == null || amountStr.trim().isEmpty()) {
            errors.add("Amount is required");
            return new ValidationResult(false, errors);
        }
        
        try {
            double amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                errors.add("Amount must be greater than zero");
            }
            if (amount > 10000000) {
                errors.add("Amount exceeds maximum limit (10,000,000)");
            }
            // Check for more than 2 decimal places
            if (amountStr.contains(".") && amountStr.split("\\.")[1].length() > 2) {
                errors.add("Amount cannot have more than 2 decimal places");
            }
        } catch (NumberFormatException e) {
            errors.add("Amount must be a valid number");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateAccountNumber(String accountNumber) {
        List<String> errors = new ArrayList<>();
        
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            errors.add("Account number is required");
        } else if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches()) {
            errors.add("Account number must be 6 digits");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateUsername(String username) {
        List<String> errors = new ArrayList<>();
        
        if (username != null && !username.trim().isEmpty()) {
            if (username.trim().length() < 3) {
                errors.add("Username must be at least 3 characters");
            } else if (username.trim().length() > 20) {
                errors.add("Username must not exceed 20 characters");
            } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
                errors.add("Username must contain only letters, numbers, and underscores");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static ValidationResult validateNotEmpty(String value, String fieldName) {
        List<String> errors = new ArrayList<>();
        
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " is required");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
