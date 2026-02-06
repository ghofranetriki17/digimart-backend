package com.nexashop.application.usecase;

import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.service.TenantProvisioningService;
import java.util.List;

public class AdminProvisionUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public AdminProvisionUseCase(
            CurrentUserProvider currentUserProvider,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    public void provisionAllTenants() {
        currentUserProvider.requireSuperAdmin();
        List<Long> tenantIds = tenantRepository.findAll().stream()
                .map(t -> t.getId())
                .toList();
        tenantIds.forEach(provisioningService::provisionTenant);
    }
}

