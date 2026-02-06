package com.nexashop.application.security;

import java.util.Set;

public record CurrentUser(Long userId, Long tenantId, Set<String> roles) {
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
