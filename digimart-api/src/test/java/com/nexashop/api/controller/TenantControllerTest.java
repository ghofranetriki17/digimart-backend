package com.nexashop.api.controller;

import com.nexashop.api.controller.tenant.TenantController;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.application.usecase.TenantUseCase;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.tenant.entity.TenantStatus;
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
        TenantUseCase tenantUseCase = Mockito.mock(TenantUseCase.class);
        TenantController controller = new TenantController(tenantUseCase);

        Tenant tenant = new Tenant();
        tenant.setId(3L);
        tenant.setName("T1");
        tenant.setSubdomain("t1");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setDefaultLocale(Locale.FR);
        when(tenantUseCase.getTenant(3L)).thenReturn(tenant);

        setAuth(3L, "USER");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getTenant(3L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void listTenantsRequiresAdminAny() {
        TenantUseCase tenantUseCase = Mockito.mock(TenantUseCase.class);
        TenantController controller = new TenantController(tenantUseCase);

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
