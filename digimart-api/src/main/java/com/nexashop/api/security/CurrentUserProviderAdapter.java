package com.nexashop.api.security;

import com.nexashop.application.exception.UnauthorizedException;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProviderAdapter implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return new CurrentUser(user.getUserId(), user.getTenantId(), user.getRoles());
    }

    @Override
    public CurrentUser requireUser() {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return user;
    }

    @Override
    public void requireAdminAny() {
        // Temporary: open access for dev
    }

    @Override
    public void requireOwnerOrAdmin(Long tenantId) {
        // Temporary: open access for dev
    }

    @Override
    public void requireSuperAdmin() {
        // Temporary: open access for dev
    }
}
