package com.nexashop.api.controller.sector;

import com.nexashop.api.dto.request.sector.CreateActivitySectorRequest;
import com.nexashop.api.dto.request.sector.UpdateActivitySectorRequest;
import com.nexashop.api.dto.response.sector.ActivitySectorResponse;
import com.nexashop.api.dto.response.sector.ActivitySectorTenantResponse;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.infrastructure.persistence.jpa.ActivitySectorJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/activity-sectors")
public class ActivitySectorController {

    private final ActivitySectorJpaRepository sectorRepository;
    private final TenantJpaRepository tenantRepository;

    public ActivitySectorController(
            ActivitySectorJpaRepository sectorRepository,
            TenantJpaRepository tenantRepository
    ) {
        this.sectorRepository = sectorRepository;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    public List<ActivitySectorResponse> listSectors(
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive
    ) {
        List<ActivitySector> sectors = includeInactive
                ? sectorRepository.findAll()
                : sectorRepository.findByActiveTrue();
        Map<Long, Long> tenantCounts = buildTenantCounts(sectors);
        return sectors.stream()
                .sorted(Comparator.comparing(ActivitySector::getLabel))
                .map(sector -> toResponse(sector, tenantCounts.getOrDefault(sector.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ActivitySectorResponse createSector(
            @Valid @RequestBody CreateActivitySectorRequest request
    ) {
        if (sectorRepository.findByLabelIgnoreCase(request.getLabel()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Sector label already exists");
        }
        ActivitySector sector = new ActivitySector();
        sector.setLabel(request.getLabel());
        sector.setDescription(request.getDescription());
        sector.setActive(request.getActive() == null || request.getActive());
        return toResponse(sectorRepository.save(sector), 0L);
    }

    @PutMapping("/{id}")
    public ActivitySectorResponse updateSector(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivitySectorRequest request
    ) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        sectorRepository.findByLabelIgnoreCase(request.getLabel())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Sector label already exists");
                });
        sector.setLabel(request.getLabel());
        sector.setDescription(request.getDescription());
        if (request.getActive() != null) {
            sector.setActive(request.getActive());
        }
        return toResponse(sectorRepository.save(sector), 0L);
    }

    @GetMapping("/{id}/tenants")
    public List<ActivitySectorTenantResponse> listSectorTenants(@PathVariable Long id) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        return tenantRepository.findAll().stream()
                .filter(tenant -> sector.getId().equals(tenant.getSectorId()))
                .sorted(Comparator.comparing(Tenant::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(tenant -> ActivitySectorTenantResponse.builder()
                        .id(tenant.getId())
                        .name(tenant.getName())
                        .subdomain(tenant.getSubdomain())
                        .contactEmail(tenant.getContactEmail())
                        .contactPhone(tenant.getContactPhone())
                        .status(tenant.getStatus())
                        .defaultLocale(tenant.getDefaultLocale())
                        .build())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void deleteSector(@PathVariable Long id) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        sectorRepository.delete(sector);
    }

    private ActivitySectorResponse toResponse(ActivitySector sector, long tenantCount) {
        return ActivitySectorResponse.builder()
                .id(sector.getId())
                .label(sector.getLabel())
                .description(sector.getDescription())
                .active(sector.isActive())
                .tenantCount(tenantCount)
                .createdAt(sector.getCreatedAt())
                .updatedAt(sector.getUpdatedAt())
                .build();
    }

    private Map<Long, Long> buildTenantCounts(List<ActivitySector> sectors) {
        if (sectors == null || sectors.isEmpty()) {
            return Map.of();
        }
        List<Long> sectorIds = sectors.stream()
                .map(ActivitySector::getId)
                .collect(Collectors.toList());
        List<Tenant> tenants = tenantRepository.findAll().stream()
                .filter(tenant -> tenant.getSectorId() != null && sectorIds.contains(tenant.getSectorId()))
                .collect(Collectors.toList());
        if (tenants.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Tenant tenant : tenants) {
            Long sectorId = tenant.getSectorId();
            if (sectorId != null) {
                counts.merge(sectorId, 1L, Long::sum);
            }
        }
        return counts;
    }
}
