package com.josephken.roors.common.util;

public class LogCategory {
    // Main category prefixes for Restaurant System
    public static final String USER = "[USER]";           // User operations (login, register, profile)
    public static final String MENU = "[MENU]";           // Menu operations (add, update, delete dishes)
    public static final String ORDER = "[ORDER]";         // Order operations (create, update, cancel)
    public static final String RESERVATION = "[RESERVATION]"; // Reservation operations (book, cancel, modify)
    public static final String TABLE = "[TABLE]";       // Dining table operations
    public static final String PAYMENT = "[PAYMENT]";     // Payment operations
    public static final String ADMIN = "[ADMIN]";         // Admin operations
    public static final String SYSTEM = "[SYSTEM]";       // System events
    public static final String ERROR = "[ERROR]";         // Error events
    
    // Helper methods for formatted logging
    public static String format(String category, String message) {
        return String.format("%s %s", category, message);
    }
    
    public static String user(String message) {
        return format(USER, message);
    }
    
    public static String menu(String message) {
        return format(MENU, message);
    }
    
    public static String order(String message) {
        return format(ORDER, message);
    }
    
    public static String reservation(String message) {
        return format(RESERVATION, message);
    }

    public static String table(String message) { return format(TABLE, message); }

    public static String payment(String message) {
        return format(PAYMENT, message);
    }
    
    public static String admin(String message) {
        return format(ADMIN, message);
    }
    
    public static String system(String message) {
        return format(SYSTEM, message);
    }
    
    public static String error(String message) {
        return format(ERROR, message);
    }
}
