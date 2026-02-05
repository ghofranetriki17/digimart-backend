package com.nexashop.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static AuthenticatedUser requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    public static void requireAdmin(Long tenantId) {
        // Temporary: open access for dev
    }

    public static void requireAdminAny() {
        // Temporary: open access for dev
    }

    public static void requireOwnerOrAdmin(Long tenantId) {
        // Temporary: open access for dev
    }

    public static void requireSuperAdmin() {
        // Temporary: open access for dev
    }
}
