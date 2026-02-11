package com.nexashop.api.controller.sector;

import com.nexashop.api.dto.request.sector.CreateActivitySectorRequest;
import com.nexashop.api.dto.request.sector.UpdateActivitySectorRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.sector.ActivitySectorResponse;
import com.nexashop.api.dto.response.sector.ActivitySectorTenantResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.ActivitySectorUseCase;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import jakarta.validation.Valid;
import java.util.List;
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

@RestController
@RequestMapping("/api/activity-sectors")
public class ActivitySectorController {

    private final ActivitySectorUseCase sectorUseCase;

    public ActivitySectorController(ActivitySectorUseCase sectorUseCase) {
        this.sectorUseCase = sectorUseCase;
    }

    @GetMapping
    public List<ActivitySectorResponse> listSectors(
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive
    ) {
        return sectorUseCase.listSectors(includeInactive).stream()
                .map(summary -> toResponse(summary.sector(), summary.tenantCount()))
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public PageResponse<ActivitySectorResponse> listSectorsPaged(
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                sectorUseCase.listSectors(request, includeInactive),
                summary -> toResponse(summary.sector(), summary.tenantCount())
        );
    }

    @PostMapping
    public ActivitySectorResponse createSector(
            @Valid @RequestBody CreateActivitySectorRequest request
    ) {
        ActivitySector sector = sectorUseCase.createSector(
                request.getLabel(),
                request.getDescription(),
                request.getActive()
        );
        return toResponse(sector, 0L);
    }

    @PutMapping("/{id}")
    public ActivitySectorResponse updateSector(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivitySectorRequest request
    ) {
        ActivitySector sector = sectorUseCase.updateSector(
                id,
                request.getLabel(),
                request.getDescription(),
                request.getActive()
        );
        return toResponse(sector, 0L);
    }

    @GetMapping("/{id}/tenants")
    public List<ActivitySectorTenantResponse> listSectorTenants(@PathVariable Long id) {
        return sectorUseCase.listSectorTenants(id).stream()
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
        sectorUseCase.deleteSector(id);
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
}
