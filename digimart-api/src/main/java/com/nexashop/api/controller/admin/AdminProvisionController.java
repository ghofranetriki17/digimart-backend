package com.nexashop.api.controller.admin;

import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.api.service.TenantProvisioningService;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/provision")
public class AdminProvisionController {

    private final TenantJpaRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public AdminProvisionController(
            TenantJpaRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    @PostMapping("/tenants")
    public void provisionAllTenants() {
        SecurityContextUtil.requireSuperAdmin();
        List<Long> tenantIds = tenantRepository.findAll().stream()
                .map(t -> t.getId())
                .toList();
        tenantIds.forEach(provisioningService::provisionTenant);
    }
}
