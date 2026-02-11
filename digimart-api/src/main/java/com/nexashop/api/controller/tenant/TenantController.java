package com.nexashop.api.controller.tenant;

import com.nexashop.api.dto.request.tenant.CreateTenantRequest;
import com.nexashop.api.dto.request.tenant.UpdateTenantRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.tenant.TenantResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.usecase.TenantUseCase;
import com.nexashop.domain.tenant.entity.Tenant;
import jakarta.validation.Valid;
import com.nexashop.api.util.UploadUtil;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantUseCase tenantUseCase;
    private final String uploadBaseDir;

    public TenantController(
            TenantUseCase tenantUseCase,
            @Value("${app.upload.dir:}") String uploadBaseDir
    ) {
        this.tenantUseCase = tenantUseCase;
        this.uploadBaseDir = uploadBaseDir;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setSubdomain(request.getSubdomain());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setLogoUrl(request.getLogoUrl());
        tenant.setStatus(request.getStatus());
        tenant.setDefaultLocale(request.getDefaultLocale());
        tenant.setSectorId(request.getSectorId());

        Tenant saved = tenantUseCase.createTenant(tenant);
        return ResponseEntity
                .created(URI.create("/api/tenants/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable Long id) {
        Tenant tenant = tenantUseCase.getTenant(id);
        return toResponse(tenant);
    }

    @GetMapping
    public List<TenantResponse> listTenants() {
        return tenantUseCase.listTenants().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public PageResponse<TenantResponse> listTenantsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageResult<Tenant> result = tenantUseCase.listTenants(PageRequest.of(page, size));
        return PageResponse.from(result, this::toResponse);
    }

    @PutMapping("/{id}")
    public TenantResponse updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        Tenant updates = new Tenant();
        updates.setName(request.getName());
        updates.setContactEmail(request.getContactEmail());
        updates.setContactPhone(request.getContactPhone());
        updates.setLogoUrl(request.getLogoUrl());
        updates.setStatus(request.getStatus());
        updates.setDefaultLocale(request.getDefaultLocale());
        updates.setSectorId(request.getSectorId());

        Tenant saved = tenantUseCase.updateTenant(id, updates);
        return toResponse(saved);
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TenantResponse uploadTenantLogo(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadUtil.StoredFile stored = UploadUtil.storeImage(file, uploadBaseDir, "tenants");
        Tenant saved = tenantUseCase.updateLogo(id, stored.relativeUrl());
        return toResponse(saved);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .contactEmail(tenant.getContactEmail())
                .contactPhone(tenant.getContactPhone())
                .logoUrl(tenant.getLogoUrl())
                .status(tenant.getStatus())
                .defaultLocale(tenant.getDefaultLocale())
                .sectorId(tenant.getSectorId())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
