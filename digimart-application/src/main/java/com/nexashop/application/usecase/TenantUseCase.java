package com.nexashop.application.usecase;

import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.service.TenantProvisioningService;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TenantUseCase {

    private final TenantRepository tenantRepository;
    private final ActivitySectorRepository sectorRepository;
    private final TenantProvisioningService provisioningService;

    public TenantUseCase(
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            TenantProvisioningService provisioningService
    ) {
        this.tenantRepository = tenantRepository;
        this.sectorRepository = sectorRepository;
        this.provisioningService = provisioningService;
    }

    @Transactional
    public Tenant createTenant(Tenant tenant) {
        if (tenantRepository.existsBySubdomain(tenant.getSubdomain())) {
            throw new ResponseStatusException(CONFLICT, "Subdomain already exists");
        }
        tenant.setSectorId(resolveSectorId(tenant.getSectorId()));
        Tenant saved = tenantRepository.save(tenant);
        provisioningService.provisionTenant(saved.getId());
        return saved;
    }

    public Tenant getTenant(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
    }

    public List<Tenant> listTenants() {
        return tenantRepository.findAll();
    }

    public Tenant updateTenant(Long id, Tenant updates) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
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
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
        tenant.setLogoUrl(logoUrl);
        return tenantRepository.save(tenant);
    }

    private Long resolveSectorId(Long sectorId) {
        if (sectorId == null) {
            return null;
        }
        ActivitySector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Activity sector not found"));
        if (!sector.isActive()) {
            throw new ResponseStatusException(BAD_REQUEST, "Activity sector is inactive");
        }
        return sector.getId();
    }
}
