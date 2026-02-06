package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.service.TenantProvisioningService;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import java.util.List;


public class TenantUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final TenantRepository tenantRepository;
    private final ActivitySectorRepository sectorRepository;
    private final TenantProvisioningService provisioningService;

    public TenantUseCase(
            CurrentUserProvider currentUserProvider,
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            TenantProvisioningService provisioningService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.tenantRepository = tenantRepository;
        this.sectorRepository = sectorRepository;
        this.provisioningService = provisioningService;
    }

    public Tenant createTenant(Tenant tenant) {
        if (tenantRepository.existsBySubdomain(tenant.getSubdomain())) {
            throw new ConflictException("Subdomain already exists");
        }
        tenant.setSectorId(resolveSectorId(tenant.getSectorId()));
        Tenant saved = tenantRepository.save(tenant);
        provisioningService.provisionTenant(saved.getId());
        return saved;
    }

    public Tenant getTenant(Long id) {
        currentUserProvider.requireOwnerOrAdmin(id);
        return tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }

    public List<Tenant> listTenants() {
        currentUserProvider.requireAdminAny();
        return tenantRepository.findAll();
    }

    public Tenant updateTenant(Long id, Tenant updates) {
        currentUserProvider.requireOwnerOrAdmin(id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.setName(updates.getName());
        tenant.setContactEmail(updates.getContactEmail());
        tenant.setContactPhone(updates.getContactPhone());
        tenant.setLogoUrl(updates.getLogoUrl());
        tenant.setStatus(updates.getStatus());
        tenant.setDefaultLocale(updates.getDefaultLocale());
        tenant.setSectorId(resolveSectorId(updates.getSectorId()));
        return tenantRepository.save(tenant);
    }

    public Tenant updateLogo(Long id, String logoUrl) {
        currentUserProvider.requireOwnerOrAdmin(id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.setLogoUrl(logoUrl);
        return tenantRepository.save(tenant);
    }

    private Long resolveSectorId(Long sectorId) {
        if (sectorId == null) {
            return null;
        }
        ActivitySector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new NotFoundException("Activity sector not found"));
        if (!sector.isActive()) {
            throw new BadRequestException("Activity sector is inactive");
        }
        return sector.getId();
    }
}


