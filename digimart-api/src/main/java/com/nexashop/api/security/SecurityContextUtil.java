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
        AuthenticatedUser user = requireUser();
        if (user.hasRole("SUPER_ADMIN")) {
            return;
        }
        if (!tenantId.equals(user.getTenantId()) || !user.hasRole("ADMIN")) {
            throw new ResponseStatusException(FORBIDDEN, "Admin access required");
        }
    }

    public static void requireAdminAny() {
        AuthenticatedUser user = requireUser();
        if (!user.hasRole("ADMIN") && !user.hasRole("SUPER_ADMIN")) {
            throw new ResponseStatusException(FORBIDDEN, "Admin access required");
        }
    }

    public static void requireOwnerOrAdmin(Long tenantId) {
        AuthenticatedUser user = requireUser();
        if (user.hasRole("SUPER_ADMIN")) {
            return;
        }
        boolean allowed = tenantId.equals(user.getTenantId())
                && (user.hasRole("OWNER") || user.hasRole("ADMIN"));
        if (!allowed) {
            throw new ResponseStatusException(FORBIDDEN, "Owner or admin access required");
        }
    }

    public static void requireSuperAdmin() {
        AuthenticatedUser user = requireUser();
        if (!user.hasRole("SUPER_ADMIN")) {
            throw new ResponseStatusException(FORBIDDEN, "Super admin access required");
        }
    }
}
