package com.nexashop.api.security;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserTest {

    @Test
    void hasRoleUsesProvidedRoles() {
        AuthenticatedUser user = new AuthenticatedUser(1L, 2L, Set.of("ADMIN", "OWNER"));
        assertTrue(user.hasRole("ADMIN"));
        assertFalse(user.hasRole("SUPER_ADMIN"));
    }

    @Test
    void rolesAreUnmodifiable() {
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        AuthenticatedUser user = new AuthenticatedUser(1L, 2L, roles);
        assertThrows(UnsupportedOperationException.class, () -> user.getRoles().add("OWNER"));
    }

    @Test
    void nullRolesBecomeEmptySet() {
        AuthenticatedUser user = new AuthenticatedUser(1L, 2L, null);
        assertEquals(0, user.getRoles().size());
    }
}
