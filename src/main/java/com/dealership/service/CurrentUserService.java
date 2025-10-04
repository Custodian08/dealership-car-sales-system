package com.dealership.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return auth.getName();
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        String needle = role != null && role.startsWith("ROLE_") ? role : ("ROLE_" + role);
        return auth.getAuthorities().stream().anyMatch(a -> needle.equalsIgnoreCase(a.getAuthority()));
    }

    public boolean isAdmin() { return hasRole("ADMIN"); }
}
