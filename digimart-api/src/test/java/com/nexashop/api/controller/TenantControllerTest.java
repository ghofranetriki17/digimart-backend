package com.nexashop.api.controller;

import com.nexashop.api.controller.tenant.TenantController;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.tenant.entity.TenantStatus;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TenantControllerTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getTenantForbiddenWithoutOwnerOrAdmin() {
        TenantJpaRepository tenantRepo = Mockito.mock(TenantJpaRepository.class);
        TenantController controller = new TenantController(tenantRepo);

        Tenant tenant = new Tenant();
        tenant.setId(3L);
        tenant.setName("T1");
        tenant.setSubdomain("t1");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setDefaultLocale(Locale.FR);
        when(tenantRepo.findById(3L)).thenReturn(Optional.of(tenant));

        setAuth(3L, "USER");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getTenant(3L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void listTenantsRequiresAdminAny() {
        TenantJpaRepository tenantRepo = Mockito.mock(TenantJpaRepository.class);
        TenantController controller = new TenantController(tenantRepo);

        setAuth(1L, "USER");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                controller::listTenants
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
