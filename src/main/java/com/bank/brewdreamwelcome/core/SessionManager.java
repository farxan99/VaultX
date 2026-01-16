package com.bank.brewdreamwelcome.core;

/**
 * Session Manager for tracking logged-in user state.
 */
public class SessionManager {
    private static Integer currentCustomerId = null;
    private static Integer currentAdminId = null;
    private static String currentRole = null;
    
    public static void setCustomerSession(int customerId) {
        currentCustomerId = customerId;
        currentAdminId = null;
        currentRole = "CUSTOMER";
    }
    
    public static void setAdminSession(int adminId) {
        currentAdminId = adminId;
        currentCustomerId = null;
        currentRole = "ADMIN";
    }
    
    public static void clearSession() {
        currentCustomerId = null;
        currentAdminId = null;
        currentRole = null;
    }
    
    public static Integer getCurrentCustomerId() {
        return currentCustomerId;
    }
    
    public static Integer getCurrentAdminId() {
        return currentAdminId;
    }
    
    public static String getCurrentRole() {
        return currentRole;
    }
    
    public static boolean isLoggedIn() {
        return currentRole != null;
    }
    
    public static boolean isCustomer() {
        return "CUSTOMER".equals(currentRole);
    }
    
    public static boolean isAdmin() {
        return "ADMIN".equals(currentRole);
    }
}
