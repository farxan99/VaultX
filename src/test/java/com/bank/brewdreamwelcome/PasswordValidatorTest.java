package com.bank.brewdreamwelcome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordValidator.
 * Tests password strength validation requirements.
 */
public class PasswordValidatorTest {

    @Test
    public void testValidPassword() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Secure@123");
        assertTrue(result.isValid(), "Valid password should pass validation");
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testPasswordTooShort() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Pass1!");
        assertFalse(result.isValid(), "Password less than 8 characters should fail");
        assertTrue(result.getErrorMessage().contains("8 characters"));
    }

    @Test
    public void testPasswordMissingUppercase() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("password123!");
        assertFalse(result.isValid(), "Password without uppercase should fail");
        assertTrue(result.getErrorMessage().contains("uppercase"));
    }

    @Test
    public void testPasswordMissingLowercase() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("PASSWORD123!");
        assertFalse(result.isValid(), "Password without lowercase should fail");
        assertTrue(result.getErrorMessage().contains("lowercase"));
    }

    @Test
    public void testPasswordMissingDigit() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Password!");
        assertFalse(result.isValid(), "Password without digit should fail");
        assertTrue(result.getErrorMessage().contains("digit"));
    }

    @Test
    public void testPasswordMissingSpecialChar() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Password123");
        assertFalse(result.isValid(), "Password without special character should fail");
        assertTrue(result.getErrorMessage().contains("special character"));
    }

    @Test
    public void testEmptyPassword() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("");
        assertFalse(result.isValid(), "Empty password should fail");
        assertTrue(result.getErrorMessage().contains("required"));
    }

    @Test
    public void testNullPassword() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(null);
        assertFalse(result.isValid(), "Null password should fail");
    }

    @Test
    public void testAllSpecialCharacters() {
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        for (char c : specialChars.toCharArray()) {
            String password = "Password" + c + "123";
            PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
            assertTrue(result.isValid(), 
                "Password with special char '" + c + "' should be valid");
        }
    }
}

