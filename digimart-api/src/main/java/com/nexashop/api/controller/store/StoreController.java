package com.nexashop.api.controller.store;

import com.nexashop.api.dto.request.store.CreateStoreRequest;
import com.nexashop.api.dto.request.store.UpdateStoreRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.product.ProductResponse;
import com.nexashop.api.dto.response.product.ProductStoreRefResponse;
import com.nexashop.api.dto.response.store.StoreResponse;
import com.nexashop.api.util.UploadUtil;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.ProductUseCase;
import com.nexashop.application.usecase.StoreUseCase;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.store.entity.Store;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreUseCase storeUseCase;
    private final ProductUseCase productUseCase;
    private final String uploadBaseDir;

    public StoreController(
            StoreUseCase storeUseCase,
            ProductUseCase productUseCase,
            @Value("${app.upload.dir:}") String uploadBaseDir
    ) {
        this.storeUseCase = storeUseCase;
        this.productUseCase = productUseCase;
        this.uploadBaseDir = uploadBaseDir;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody CreateStoreRequest request
    ) {
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
                request.getTenantId()
        );

        return ResponseEntity
                .created(URI.create("/api/stores/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public StoreResponse getStore(@PathVariable Long id) {
        Store store = storeUseCase.getStore(id);
        return toResponse(store);
    }

    @GetMapping("/{id}/products")
    public List<ProductResponse> listStoreProducts(@PathVariable Long id) {
        return productUseCase.listProductsForStore(id).stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<StoreResponse> listStores(@RequestParam Long tenantId) {
        return storeUseCase.listStores(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public PageResponse<StoreResponse> listStoresPaged(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                storeUseCase.listStores(request, tenantId),
                this::toResponse
        );
    }

    @PutMapping("/{id}")
    public StoreResponse updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
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
                request.getActive()
        );
        return toResponse(saved);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoreResponse uploadStoreImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        storeUseCase.getStore(id);
        UploadUtil.StoredFile stored = UploadUtil.storeImage(file, uploadBaseDir, "stores");
        Store saved = storeUseCase.updateStoreImage(
                id,
                stored.relativeUrl()
        );
        return toResponse(saved);
    }

    @PostMapping("/{id}/activate")
    public StoreResponse activateStore(@PathVariable Long id) {
        Store saved = storeUseCase.setStoreActive(id, true);
        return toResponse(saved);
    }

    @PostMapping("/{id}/deactivate")
    public StoreResponse deactivateStore(@PathVariable Long id) {
        Store saved = storeUseCase.setStoreActive(id, false);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeUseCase.deleteStore(id);
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

    private ProductResponse toProductResponse(Product product) {
        String imageUrl = resolvePrimaryImageUrl(product.getId());
        List<ProductUseCase.StoreRef> storeRefs = productUseCase.listActiveStores(product.getId());
        List<ProductStoreRefResponse> storeResponses = new ArrayList<>();
        List<String> storeNames = new ArrayList<>();
        if (storeRefs != null) {
            for (ProductUseCase.StoreRef ref : storeRefs) {
                if (ref == null || ref.name() == null) {
                    continue;
                }
                storeResponses.add(ProductStoreRefResponse.builder()
                        .id(ref.id())
                        .name(ref.name())
                        .build());
                storeNames.add(ref.name());
            }
        }
        return ProductResponse.builder()
                .id(product.getId())
                .tenantId(product.getTenantId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .initialPrice(product.getInitialPrice())
                .finalPrice(product.getFinalPrice())
                .costPrice(product.getCostPrice())
                .shippingPrice(product.getShippingPrice())
                .shippingCostPrice(product.getShippingCostPrice())
                .trackStock(product.isTrackStock())
                .stockQuantity(product.getStockQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .status(product.getStatus())
                .availability(product.getAvailability())
                .availabilityText(product.getAvailabilityText())
                .imageUrl(imageUrl)
                .stores(storeResponses)
                .storeNames(storeNames)
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String resolvePrimaryImageUrl(Long productId) {
        if (productId == null) {
            return null;
        }
        return resolvePrimaryImageUrl(productUseCase.listProductImages(productId));
    }

    private String resolvePrimaryImageUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        for (ProductImage image : images) {
            if (image.isPrimary()) {
                return image.getImageUrl();
            }
        }
        return images.get(0).getImageUrl();
    }
}
