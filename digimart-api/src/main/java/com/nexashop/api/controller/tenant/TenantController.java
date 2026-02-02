package com.nexashop.api.controller.tenant;

import com.nexashop.api.dto.request.tenant.CreateTenantRequest;
import com.nexashop.api.dto.response.tenant.TenantResponse;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantJpaRepository tenantRepository;

    public TenantController(TenantJpaRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
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
        tenant.setStatus(request.getStatus());
        tenant.setDefaultLocale(request.getDefaultLocale());

        Tenant saved = tenantRepository.save(tenant);
        return ResponseEntity
                .created(URI.create("/api/tenants/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .contactEmail(tenant.getContactEmail())
                .contactPhone(tenant.getContactPhone())
                .status(tenant.getStatus())
                .defaultLocale(tenant.getDefaultLocale())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
