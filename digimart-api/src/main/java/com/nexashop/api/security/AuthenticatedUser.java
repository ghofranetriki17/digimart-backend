package com.nexashop.api.security;

import java.util.Collections;
import java.util.Set;

public class AuthenticatedUser {

    private final Long userId;
    private final Long tenantId;
    private final Set<String> roles;

    public AuthenticatedUser(Long userId, Long tenantId, Set<String> roles) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
