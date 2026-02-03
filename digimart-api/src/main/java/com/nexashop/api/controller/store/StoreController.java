package com.nexashop.api.controller.store;

import com.nexashop.api.dto.request.store.CreateStoreRequest;
import com.nexashop.api.dto.request.store.UpdateStoreRequest;
import com.nexashop.api.dto.response.store.StoreResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.store.entity.Store;
import com.nexashop.infrastructure.persistence.jpa.StoreJpaRepository;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreJpaRepository storeRepository;
    private final TenantJpaRepository tenantRepository;

    public StoreController(
            StoreJpaRepository storeRepository,
            TenantJpaRepository tenantRepository
    ) {
        this.storeRepository = storeRepository;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody CreateStoreRequest request
    ) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Long tenantId = request.getTenantId();
        if (tenantId == null) {
            tenantId = user.getTenantId();
        }
        if (!user.hasRole("SUPER_ADMIN") && !tenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        if (storeRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new ResponseStatusException(CONFLICT, "Store code already exists");
        }

        Store store = new Store();
        store.setTenantId(tenantId);
        store.setName(request.getName());
        store.setCode(request.getCode());
        store.setAddress(request.getAddress());
        store.setCity(request.getCity());
        store.setPostalCode(request.getPostalCode());
        store.setCountry(request.getCountry());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            store.setImageUrl(request.getImageUrl());
        }
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());

        Store saved = storeRepository.save(store);
        return ResponseEntity
                .created(URI.create("/api/stores/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public StoreResponse getStore(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return toResponse(store);
    }

    @GetMapping
    public List<StoreResponse> listStores(@RequestParam Long tenantId) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !tenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return storeRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public StoreResponse updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }

        if (request.getCode() != null && !request.getCode().isBlank()) {
            if (!request.getCode().equals(store.getCode())
                    && storeRepository.existsByTenantIdAndCode(
                            store.getTenantId(),
                            request.getCode()
                    )) {
                throw new ResponseStatusException(CONFLICT, "Store code already exists");
            }
            store.setCode(request.getCode());
        }
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setCity(request.getCity());
        store.setPostalCode(request.getPostalCode());
        store.setCountry(request.getCountry());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setImageUrl(request.getImageUrl());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        if (request.getActive() != null) {
            store.setActive(request.getActive());
        }

        Store saved = storeRepository.save(store);
        return toResponse(saved);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoreResponse uploadStoreImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Image file is required");
        }

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }

        Path uploadDir = Paths.get("uploads", "stores");
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > -1) {
            ext = originalName.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());

        store.setImageUrl("/uploads/stores/" + filename);
        return toResponse(storeRepository.save(store));
    }

    @PostMapping("/{id}/activate")
    public StoreResponse activateStore(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        store.setActive(true);
        return toResponse(storeRepository.save(store));
    }

    @PostMapping("/{id}/deactivate")
    public StoreResponse deactivateStore(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        store.setActive(false);
        return toResponse(storeRepository.save(store));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        if (!user.hasRole("SUPER_ADMIN") && !store.getTenantId().equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        storeRepository.delete(store);
        return ResponseEntity.noContent().build();
    }


    private StoreResponse toResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .tenantId(store.getTenantId())
                .name(store.getName())
                .code(store.getCode())
                .address(store.getAddress())
                .city(store.getCity())
                .postalCode(store.getPostalCode())
                .country(store.getCountry())
                .phone(store.getPhone())
                .email(store.getEmail())
                .imageUrl(store.getImageUrl())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .active(store.isActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
