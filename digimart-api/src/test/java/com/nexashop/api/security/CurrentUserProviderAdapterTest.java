package com.nexashop.api.security;

import com.nexashop.application.exception.UnauthorizedException;
import com.nexashop.application.security.CurrentUser;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserProviderAdapterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsNullWhenNoAuth() {
        CurrentUserProviderAdapter adapter = new CurrentUserProviderAdapter();
        CurrentUser user = adapter.getCurrentUser();
        assertNull(user);
    }

    @Test
    void requireUserThrowsWhenNoAuth() {
        CurrentUserProviderAdapter adapter = new CurrentUserProviderAdapter();
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, adapter::requireUser);
        assertEquals("Authentication required", ex.getMessage());
    }

    @Test
    void requireUserReturnsCurrentUser() {
        AuthenticatedUser principal = new AuthenticatedUser(1L, 2L, Set.of("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null)
        );

        CurrentUserProviderAdapter adapter = new CurrentUserProviderAdapter();
        CurrentUser user = adapter.requireUser();
        assertEquals(1L, user.userId());
        assertEquals(2L, user.tenantId());
    }
}
