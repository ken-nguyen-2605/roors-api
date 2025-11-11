package com.josephken.roors.auth.util;

import com.josephken.roors.auth.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuthenticationHelper {

    private AuthenticationHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the currently authenticated user from the security context.
     * 
     * @return Optional containing the authenticated User, or empty if not authenticated
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        return Optional.of((User) authentication.getPrincipal());
    }

    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Gets the username of the currently authenticated user.
     * 
     * @return Optional containing the username, or empty if not authenticated
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(User::getUsername);
    }
}
