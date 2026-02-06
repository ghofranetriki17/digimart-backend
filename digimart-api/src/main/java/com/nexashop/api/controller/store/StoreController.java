package com.nexashop.api.controller.store;

import com.nexashop.api.dto.request.store.CreateStoreRequest;
import com.nexashop.api.dto.request.store.UpdateStoreRequest;
import com.nexashop.api.dto.response.store.StoreResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.application.usecase.StoreUseCase;
import com.nexashop.domain.store.entity.Store;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreUseCase storeUseCase;

    public StoreController(StoreUseCase storeUseCase) {
        this.storeUseCase = storeUseCase;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody CreateStoreRequest request
    ) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();

        Store store = new Store();
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

        Store saved = storeUseCase.createStore(
                store,
                request.getTenantId(),
                user.getTenantId(),
                user.hasRole("SUPER_ADMIN")
        );

        return ResponseEntity
                .created(URI.create("/api/stores/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public StoreResponse getStore(@PathVariable Long id) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Store store = storeUseCase.getStore(id, user.getTenantId(), user.hasRole("SUPER_ADMIN"));
        return toResponse(store);
    }

    @GetMapping
    public List<StoreResponse> listStores(@RequestParam Long tenantId) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        return storeUseCase.listStores(tenantId, user.getTenantId(), user.hasRole("SUPER_ADMIN")).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public StoreResponse updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Store updates = new Store();
        updates.setName(request.getName());
        updates.setAddress(request.getAddress());
        updates.setCity(request.getCity());
        updates.setPostalCode(request.getPostalCode());
        updates.setCountry(request.getCountry());
        updates.setPhone(request.getPhone());
        updates.setEmail(request.getEmail());
        updates.setImageUrl(request.getImageUrl());
        updates.setLatitude(request.getLatitude());
        updates.setLongitude(request.getLongitude());

        Store saved = storeUseCase.updateStore(
                id,
                request.getCode(),
                updates,
                request.getActive(),
                user.getTenantId(),
                user.hasRole("SUPER_ADMIN")
        );
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

        AuthenticatedUser user = SecurityContextUtil.requireUser();
        storeUseCase.getStore(id, user.getTenantId(), user.hasRole("SUPER_ADMIN"));

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

        Store saved = storeUseCase.updateStoreImage(
                id,
                "/uploads/stores/" + filename,
                user.getTenantId(),
                user.hasRole("SUPER_ADMIN")
        );
        return toResponse(saved);
    }

    @PostMapping("/{id}/activate")
    public StoreResponse activateStore(@PathVariable Long id) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Store saved = storeUseCase.setStoreActive(id, true, user.getTenantId(), user.hasRole("SUPER_ADMIN"));
        return toResponse(saved);
    }

    @PostMapping("/{id}/deactivate")
    public StoreResponse deactivateStore(@PathVariable Long id) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Store saved = storeUseCase.setStoreActive(id, false, user.getTenantId(), user.hasRole("SUPER_ADMIN"));
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        storeUseCase.deleteStore(id, user.getTenantId(), user.hasRole("SUPER_ADMIN"));
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
