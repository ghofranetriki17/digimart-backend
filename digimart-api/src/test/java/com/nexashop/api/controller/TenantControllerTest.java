package com.nexashop.api.controller;

import com.nexashop.api.controller.tenant.TenantController;
import com.nexashop.api.dto.request.tenant.UpdateTenantRequest;
import com.nexashop.api.dto.response.tenant.TenantResponse;
import com.nexashop.application.usecase.TenantUseCase;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.tenant.entity.TenantStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TenantControllerTest {

    @Test
    void getTenantReturnsResponse() {
        TenantUseCase tenantUseCase = Mockito.mock(TenantUseCase.class);
        TenantController controller = new TenantController(tenantUseCase);

        Tenant tenant = new Tenant();
        tenant.setId(3L);
        tenant.setName("T1");
        tenant.setSubdomain("t1");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setDefaultLocale(Locale.FR);
        when(tenantUseCase.getTenant(3L)).thenReturn(tenant);

        TenantResponse response = controller.getTenant(3L);
        assertEquals(3L, response.getId());
        assertEquals("T1", response.getName());
    }

    @Test
    void listTenantsReturnsResponses() {
        TenantUseCase tenantUseCase = Mockito.mock(TenantUseCase.class);
        TenantController controller = new TenantController(tenantUseCase);

        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("Alpha");
        when(tenantUseCase.listTenants()).thenReturn(List.of(tenant));

        List<TenantResponse> responses = controller.listTenants();
        assertEquals(1, responses.size());
    }

    @Test
    void updateTenantReturnsResponse() {
        TenantUseCase tenantUseCase = Mockito.mock(TenantUseCase.class);
        TenantController controller = new TenantController(tenantUseCase);

        UpdateTenantRequest request = new UpdateTenantRequest();
        request.setName("New Name");

        Tenant updated = new Tenant();
        updated.setId(5L);
        updated.setName("New Name");
        when(tenantUseCase.updateTenant(Mockito.eq(5L), Mockito.any(Tenant.class))).thenReturn(updated);

        TenantResponse response = controller.updateTenant(5L, request);
        assertEquals("New Name", response.getName());
    }
}
