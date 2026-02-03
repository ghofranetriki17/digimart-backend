package com.nexashop.api.security;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityContextUtilTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireUserThrowsWhenNoAuthentication() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                SecurityContextUtil::requireUser
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void requireUserThrowsWhenPrincipalIsNotAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null)
        );
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                SecurityContextUtil::requireUser
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void requireAdminAllowsSuperAdminAnyTenant() {
        setAuth(99L, "SUPER_ADMIN");
        assertDoesNotThrow(() -> SecurityContextUtil.requireAdmin(1L));
    }

    @Test
    void requireAdminRejectsWhenTenantMismatchOrMissingRole() {
        setAuth(2L, "ADMIN");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> SecurityContextUtil.requireAdmin(3L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void requireAdminAnyAllowsAdminOrSuperAdmin() {
        setAuth(1L, "ADMIN");
        assertDoesNotThrow(SecurityContextUtil::requireAdminAny);
        setAuth(1L, "SUPER_ADMIN");
        assertDoesNotThrow(SecurityContextUtil::requireAdminAny);
    }

    @Test
    void requireOwnerOrAdminAllowsOwnerOrAdminSameTenant() {
        setAuth(10L, "OWNER");
        assertDoesNotThrow(() -> SecurityContextUtil.requireOwnerOrAdmin(10L));
        setAuth(10L, "ADMIN");
        assertDoesNotThrow(() -> SecurityContextUtil.requireOwnerOrAdmin(10L));
    }

    @Test
    void requireOwnerOrAdminRejectsWhenNotAllowed() {
        setAuth(10L, "USER");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> SecurityContextUtil.requireOwnerOrAdmin(10L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void requireSuperAdminRejectsWhenMissingRole() {
        setAuth(1L, "ADMIN");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                SecurityContextUtil::requireSuperAdmin
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private void setAuth(Long tenantId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(1L, tenantId, Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }
}
