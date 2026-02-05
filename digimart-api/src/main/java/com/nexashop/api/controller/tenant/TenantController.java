package com.nexashop.api.controller.tenant;

import com.nexashop.api.dto.request.tenant.CreateTenantRequest;
import com.nexashop.api.dto.request.tenant.UpdateTenantRequest;
import com.nexashop.api.dto.response.tenant.TenantResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.api.service.TenantProvisioningService;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.infrastructure.persistence.jpa.ActivitySectorJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantJpaRepository tenantRepository;
    private final ActivitySectorJpaRepository sectorRepository;
    private final TenantProvisioningService provisioningService;

    public TenantController(
            TenantJpaRepository tenantRepository,
            ActivitySectorJpaRepository sectorRepository,
            TenantProvisioningService provisioningService
    ) {
        this.tenantRepository = tenantRepository;
        this.sectorRepository = sectorRepository;
        this.provisioningService = provisioningService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new ResponseStatusException(CONFLICT, "Subdomain already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setSubdomain(request.getSubdomain());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setLogoUrl(request.getLogoUrl());
        tenant.setStatus(request.getStatus());
        tenant.setDefaultLocale(request.getDefaultLocale());
        tenant.setSectorId(resolveSectorId(request.getSectorId()));

        Tenant saved = tenantRepository.save(tenant);
        provisioningService.provisionTenant(saved.getId());
        return ResponseEntity
                .created(URI.create("/api/tenants/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
        SecurityContextUtil.requireOwnerOrAdmin(tenant.getId());
        return toResponse(tenant);
    }

    @GetMapping
    public List<TenantResponse> listTenants() {
        SecurityContextUtil.requireAdminAny();
        return tenantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public TenantResponse updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
        SecurityContextUtil.requireOwnerOrAdmin(tenant.getId());

        tenant.setName(request.getName());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setLogoUrl(request.getLogoUrl());
        tenant.setStatus(request.getStatus());
        tenant.setDefaultLocale(request.getDefaultLocale());
        tenant.setSectorId(resolveSectorId(request.getSectorId()));

        Tenant saved = tenantRepository.save(tenant);
        return toResponse(saved);
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TenantResponse uploadTenantLogo(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Logo file is required");
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
        SecurityContextUtil.requireOwnerOrAdmin(tenant.getId());

        Path uploadDir = Paths.get("uploads", "tenants");
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename() == null ? "logo" : file.getOriginalFilename();
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > -1) {
            ext = originalName.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());

        tenant.setLogoUrl("/uploads/tenants/" + filename);
        Tenant saved = tenantRepository.save(tenant);
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
