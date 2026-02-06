package com.nexashop.application.usecase;

import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.service.TenantProvisioningService;
import java.util.List;

public class AdminProvisionUseCase {

    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public AdminProvisionUseCase(
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    public void provisionAllTenants() {
        List<Long> tenantIds = tenantRepository.findAll().stream()
                .map(t -> t.getId())
                .toList();
        tenantIds.forEach(provisioningService::provisionTenant);
    }
}

