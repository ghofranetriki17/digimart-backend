package com.nexashop.application.usecase;

import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ActivitySectorUseCase {

    public record ActivitySectorSummary(ActivitySector sector, long tenantCount) {}

    private final ActivitySectorRepository sectorRepository;
    private final TenantRepository tenantRepository;

    public ActivitySectorUseCase(
            ActivitySectorRepository sectorRepository,
            TenantRepository tenantRepository
    ) {
        this.sectorRepository = sectorRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<ActivitySectorSummary> listSectors(boolean includeInactive) {
        List<ActivitySector> sectors = includeInactive
                ? sectorRepository.findAll()
                : sectorRepository.findByActiveTrue();
        Map<Long, Long> tenantCounts = buildTenantCounts(sectors);
        return sectors.stream()
                .sorted(Comparator.comparing(ActivitySector::getLabel))
                .map(sector -> new ActivitySectorSummary(
                        sector,
                        tenantCounts.getOrDefault(sector.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    public ActivitySector createSector(String label, String description, Boolean active) {
        if (sectorRepository.findByLabelIgnoreCase(label).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Sector label already exists");
        }
        ActivitySector sector = new ActivitySector();
        sector.setLabel(label);
        sector.setDescription(description);
        sector.setActive(active == null || active);
        return sectorRepository.save(sector);
    }

    public ActivitySector updateSector(Long id, String label, String description, Boolean active) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        sectorRepository.findByLabelIgnoreCase(label)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Sector label already exists");
                });
        sector.setLabel(label);
        sector.setDescription(description);
        if (active != null) {
            sector.setActive(active);
        }
        return sectorRepository.save(sector);
    }

    public List<Tenant> listSectorTenants(Long id) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        return tenantRepository.findAll().stream()
                .filter(tenant -> sector.getId().equals(tenant.getSectorId()))
                .sorted(Comparator.comparing(Tenant::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    public void deleteSector(Long id) {
        ActivitySector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sector not found"));
        sectorRepository.delete(sector);
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
