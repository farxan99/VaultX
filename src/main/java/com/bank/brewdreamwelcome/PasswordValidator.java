package com.bank.brewdreamwelcome;

/**
 * Password strength validator for VaultX Bank.
 * Enforces strong password requirements to ensure security.
 */
public final class PasswordValidator {
    
    private PasswordValidator() {
    }
    
    /**
     * Validates password strength.
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
     * 
     * @param password The password to validate
     * @return ValidationResult with isValid flag and error message if invalid
     */
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, 
                "Password must be at least 8 characters long");
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (specialChars.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }
        }
        
        StringBuilder error = new StringBuilder();
        if (!hasUpperCase) {
            error.append("Password must contain at least one uppercase letter. ");
        }
        if (!hasLowerCase) {
            error.append("Password must contain at least one lowercase letter. ");
        }
        if (!hasDigit) {
            error.append("Password must contain at least one digit. ");
        }
        if (!hasSpecialChar) {
            error.append("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?). ");
        }
        
        if (error.length() > 0) {
            return new ValidationResult(false, error.toString().trim());
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Result of password validation.
     */
    public static final class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        private ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

