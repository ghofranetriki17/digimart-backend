package com.nexashop.api.controller.catalog;

import com.nexashop.api.dto.request.product.CreateProductRequest;
import com.nexashop.api.dto.request.product.ProductInventoryRequest;
import com.nexashop.api.dto.request.product.UpdateProductRequest;
import com.nexashop.api.dto.request.product.UpdateProductImageOrderRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.product.ProductDetailsResponse;
import com.nexashop.api.dto.response.product.ProductImageResponse;
import com.nexashop.api.dto.response.product.ProductInventoryResponse;
import com.nexashop.api.dto.response.product.ProductResponse;
import com.nexashop.api.dto.response.product.ProductStoreRefResponse;
import com.nexashop.api.util.UploadUtil;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.ProductUseCase;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.catalog.entity.ProductStoreInventory;
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
@RequestMapping("/api/products")
public class ProductController {

    private final ProductUseCase productUseCase;
    private final String uploadBaseDir;

    public ProductController(
            ProductUseCase productUseCase,
            @Value("${app.upload.dir:}") String uploadBaseDir
    ) {
        this.productUseCase = productUseCase;
        this.uploadBaseDir = uploadBaseDir;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setInitialPrice(request.getInitialPrice());
        product.setFinalPrice(request.getFinalPrice());
        product.setCostPrice(request.getCostPrice());
        product.setShippingPrice(request.getShippingPrice());
        product.setShippingCostPrice(request.getShippingCostPrice());
        product.setTrackStock(Boolean.TRUE.equals(request.getTrackStock()));
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setStatus(request.getStatus());
        product.setAvailability(request.getAvailability());
        product.setAvailabilityText(request.getAvailabilityText());

        Product saved = productUseCase.createProduct(
                product,
                request.getTenantId(),
                request.getCategoryIds(),
                request.getPrimaryCategoryId(),
                toInventories(request.getInventories())
        );

        return ResponseEntity
                .created(URI.create("/api/products/" + saved.getId()))
                .body(toResponse(saved, null, List.of()));
    }

    @GetMapping("/{id}")
    public ProductDetailsResponse getProduct(@PathVariable Long id) {
        ProductUseCase.ProductDetails details = productUseCase.getProductDetails(id);
        String primaryImageUrl = resolvePrimaryImageUrl(details.images());
        return toDetailsResponse(details, primaryImageUrl);
    }

    @GetMapping
    public List<ProductResponse> listProducts(@RequestParam Long tenantId) {
        return productUseCase.listProducts(tenantId).stream()
                .map(product -> toResponse(
                        product,
                        resolvePrimaryImageUrl(product.getId()),
                        productUseCase.listActiveStores(product.getId())
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public PageResponse<ProductResponse> listProductsPaged(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                productUseCase.listProducts(request, tenantId),
                product -> toResponse(
                        product,
                        resolvePrimaryImageUrl(product.getId()),
                        productUseCase.listActiveStores(product.getId())
                )
        );
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Product updates = new Product();
        updates.setName(request.getName());
        updates.setDescription(request.getDescription());
        updates.setInitialPrice(request.getInitialPrice());
        updates.setFinalPrice(request.getFinalPrice());
        updates.setCostPrice(request.getCostPrice());
        updates.setShippingPrice(request.getShippingPrice());
        updates.setShippingCostPrice(request.getShippingCostPrice());
        updates.setTrackStock(Boolean.TRUE.equals(request.getTrackStock()));
        updates.setStockQuantity(request.getStockQuantity());
        updates.setLowStockThreshold(request.getLowStockThreshold());
        updates.setStatus(request.getStatus());
        updates.setAvailability(request.getAvailability());
        updates.setAvailabilityText(request.getAvailabilityText());

        Product saved = productUseCase.updateProduct(
                id,
                request.getSlug(),
                request.getSku(),
                updates,
                request.getCategoryIds(),
                request.getPrimaryCategoryId(),
                toInventories(request.getInventories())
        );
        return toResponse(
                saved,
                resolvePrimaryImageUrl(saved.getId()),
                productUseCase.listActiveStores(saved.getId())
        );
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageResponse uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "primary", defaultValue = "false") boolean primary
    ) throws IOException {
        productUseCase.getProduct(id);
        UploadUtil.StoredFile stored = UploadUtil.storeImage(file, uploadBaseDir, "products");
        ProductImage image = new ProductImage();
        image.setImageUrl(stored.relativeUrl());
        image.setAltText(altText);
        image.setDisplayOrder(displayOrder);
        image.setPrimary(primary);
        ProductImage saved = productUseCase.addProductImage(id, image);
        return toImageResponse(saved);
    }

    @GetMapping("/{id}/images")
    public List<ProductImageResponse> listProductImages(@PathVariable Long id) {
        return productUseCase.listProductImages(id).stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/images/order")
    public List<ProductImageResponse> reorderProductImages(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductImageOrderRequest request
    ) {
        return productUseCase.reorderProductImages(id, request.getImageIds()).stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        productUseCase.deleteProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productUseCase.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductResponse toResponse(
            Product product,
            String imageUrl,
            List<ProductUseCase.StoreRef> storeRefs
    ) {
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

    private ProductDetailsResponse toDetailsResponse(ProductUseCase.ProductDetails details, String imageUrl) {
        Product product = details.product();
        return ProductDetailsResponse.builder()
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
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .categoryIds(details.categoryIds())
                .primaryCategoryId(details.primaryCategoryId())
                .images(details.images().stream().map(this::toImageResponse).toList())
                .inventories(details.inventories().stream().map(this::toInventoryResponse).toList())
                .build();
    }

    private ProductImageResponse toImageResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .productId(image.getProductId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .displayOrder(image.getDisplayOrder())
                .primary(image.isPrimary())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private ProductInventoryResponse toInventoryResponse(ProductStoreInventory inventory) {
        return ProductInventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .storeId(inventory.getStoreId())
                .quantity(inventory.getQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .activeInStore(inventory.isActiveInStore())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private List<ProductStoreInventory> toInventories(List<ProductInventoryRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return null;
        }
        return requests.stream()
                .map((request) -> {
                    ProductStoreInventory inventory = new ProductStoreInventory();
                    inventory.setStoreId(request.getStoreId());
                    inventory.setQuantity(request.getQuantity());
                    inventory.setLowStockThreshold(request.getLowStockThreshold());
                    inventory.setActiveInStore(request.getActiveInStore() == null || request.getActiveInStore());
                    return inventory;
                })
                .collect(Collectors.toList());
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
