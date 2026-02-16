package com.nexashop.api.controller.catalog;

import com.nexashop.api.dto.request.product.CreateProductRequest;
import com.nexashop.api.dto.request.product.ProductBulkDeleteRequest;
import com.nexashop.api.dto.request.product.ProductBulkPriceUpdateRequest;
import com.nexashop.api.dto.request.product.ProductBulkStatusUpdateRequest;
import com.nexashop.api.dto.request.product.ProductDescriptionAiRequest;
import com.nexashop.api.dto.request.product.ProductInventoryRequest;
import com.nexashop.api.dto.request.product.ProductOptionRequest;
import com.nexashop.api.dto.request.product.ProductOptionValueRequest;
import com.nexashop.api.dto.request.product.ProductVariantRequest;
import com.nexashop.api.dto.request.product.UpdateProductRequest;
import com.nexashop.api.dto.request.product.UpdateProductImageOrderRequest;
import com.nexashop.api.dto.request.product.UpdateProductOptionsRequest;
import com.nexashop.api.dto.request.product.UpdateProductVariantsRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.product.ProductDescriptionAiResponse;
import com.nexashop.api.dto.response.product.ProductDetailsResponse;
import com.nexashop.api.dto.response.product.ProductBulkActionResponse;
import com.nexashop.api.dto.response.product.ProductImportErrorResponse;
import com.nexashop.api.dto.response.product.ProductImportResponse;
import com.nexashop.api.dto.response.product.ProductImageResponse;
import com.nexashop.api.dto.response.product.ProductInventoryResponse;
import com.nexashop.api.dto.response.product.ProductOptionResponse;
import com.nexashop.api.dto.response.product.ProductOptionValueResponse;
import com.nexashop.api.dto.response.product.ProductPriceHistoryResponse;
import com.nexashop.api.dto.response.product.ProductResponse;
import com.nexashop.api.dto.response.product.ProductStoreRefResponse;
import com.nexashop.api.dto.response.product.ProductVariantResponse;
import com.nexashop.api.util.UploadUtil;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.ProductUseCase;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.catalog.entity.ProductOption;
import com.nexashop.domain.catalog.entity.ProductOptionValue;
import com.nexashop.domain.catalog.entity.ProductStoreInventory;
import com.nexashop.domain.catalog.entity.ProductVariant;
import com.nexashop.domain.catalog.entity.ProductStatus;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    private static final List<String> CSV_HEADERS = List.of(
            "name",
            "description",
            "initial_price",
            "final_price",
            "cost_price",
            "shipping_price",
            "shipping_cost_price"
    );

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
        product.setContinueSelling(Boolean.TRUE.equals(request.getContinueSelling()));
        product.setShowLowestPrice(Boolean.TRUE.equals(request.getShowLowestPrice()));

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

    @GetMapping("/{id}/price-history")
    public PageResponse<ProductPriceHistoryResponse> listPriceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                productUseCase.listPriceHistory(request, id),
                history -> ProductPriceHistoryResponse.builder()
                        .initialPrice(history.getInitialPrice())
                        .finalPrice(history.getFinalPrice())
                        .changedAt(history.getChangedAt())
                        .changedBy(history.getChangedBy())
                        .build()
        );
    }

    @PostMapping("/{id}/ai-description")
    public ProductDescriptionAiResponse suggestProductDescription(
            @PathVariable Long id,
            @RequestBody(required = false) ProductDescriptionAiRequest request
    ) {
        ProductDescriptionAiRequest resolvedRequest = request == null
                ? new ProductDescriptionAiRequest()
                : request;
        String suggestion = productUseCase.suggestProductDescription(
                id,
                resolvedRequest.getLanguage(),
                resolvedRequest.getMaxSentences(),
                resolvedRequest.getTone()
        );
        return ProductDescriptionAiResponse.builder()
                .productId(id)
                .suggestion(suggestion)
                .build();
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
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) ProductAvailability availability,
            @RequestParam(required = false) Boolean stockLow,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                productUseCase.listProducts(
                        request,
                        tenantId,
                        status,
                        availability,
                        stockLow,
                        minPrice,
                        maxPrice,
                        search,
                        categoryId
                ),
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
        updates.setContinueSelling(Boolean.TRUE.equals(request.getContinueSelling()));
        updates.setShowLowestPrice(Boolean.TRUE.equals(request.getShowLowestPrice()));

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

    @GetMapping("/{id}/options")
    public List<ProductOptionResponse> listProductOptions(@PathVariable Long id) {
        return productUseCase.listProductOptions(id).stream()
                .map(this::toOptionResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/options")
    public List<ProductOptionResponse> replaceProductOptions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductOptionsRequest request
    ) {
        List<ProductUseCase.ProductOptionGroup> groups = toOptionGroups(request == null ? null : request.getOptions());
        return productUseCase.replaceProductOptions(id, groups).stream()
                .map(this::toOptionResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/variants")
    public List<ProductVariantResponse> listProductVariants(@PathVariable Long id) {
        return mapVariantResponses(id, productUseCase.listProductVariants(id));
    }

    @PutMapping("/{id}/variants")
    public List<ProductVariantResponse> replaceProductVariants(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductVariantsRequest request
    ) {
        List<ProductUseCase.ProductVariantGroup> groups = toVariantGroups(request == null ? null : request.getVariants());
        List<ProductUseCase.ProductVariantGroup> saved = productUseCase.replaceProductVariants(id, groups);
        return mapVariantResponses(id, saved);
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

    @PutMapping("/bulk/pricing")
    public ProductBulkActionResponse bulkUpdatePricing(
            @Valid @RequestBody ProductBulkPriceUpdateRequest request
    ) {
        int updated = productUseCase.bulkUpdatePricing(
                request.getProductIds(),
                request.getInitialPrice(),
                request.getFinalPrice(),
                request.getShippingPrice(),
                request.getShippingCostPrice()
        );
        return ProductBulkActionResponse.builder().affected(updated).build();
    }

    @PutMapping("/bulk/status")
    public ProductBulkActionResponse bulkUpdateStatus(
            @Valid @RequestBody ProductBulkStatusUpdateRequest request
    ) {
        int updated = productUseCase.bulkUpdateStatus(
                request.getProductIds(),
                request.getStatus()
        );
        return ProductBulkActionResponse.builder().affected(updated).build();
    }

    @DeleteMapping("/bulk")
    public ProductBulkActionResponse bulkDeleteProducts(
            @Valid @RequestBody ProductBulkDeleteRequest request
    ) {
        int deleted = productUseCase.bulkDeleteProducts(request.getProductIds());
        return ProductBulkActionResponse.builder().affected(deleted).build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportProducts(@RequestParam Long tenantId) throws IOException {
        List<Product> products = productUseCase.listProducts(tenantId).stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(CSV_HEADERS.toArray(String[]::new)))) {
            for (Product product : products) {
                printer.printRecord(
                        safe(product.getName()),
                        safe(product.getDescription()),
                        toPlain(product.getInitialPrice()),
                        toPlain(product.getFinalPrice()),
                        toPlain(product.getCostPrice()),
                        toPlain(product.getShippingPrice()),
                        toPlain(product.getShippingCostPrice())
                );
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out.toString());
    }

    @GetMapping(value = "/export/template", produces = "text/csv")
    public ResponseEntity<String> exportProductsTemplate() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(CSV_HEADERS.toArray(String[]::new)))) {
            // header only
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out.toString());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImportResponse importProducts(
            @RequestParam Long tenantId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ProductImportResponse.builder()
                    .totalRows(0)
                    .imported(0)
                    .failed(0)
                    .errors(List.of(ProductImportErrorResponse.builder()
                            .row(0)
                            .message("CSV file is empty")
                            .build()))
                    .build();
        }

        int imported = 0;
        List<ProductImportErrorResponse> errors = new ArrayList<>();
        int totalRows = 0;

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            List<String> missingHeaders = CSV_HEADERS.stream()
                    .filter((header) -> !parser.getHeaderMap().containsKey(header))
                    .toList();
            if (!missingHeaders.isEmpty()) {
                return ProductImportResponse.builder()
                        .totalRows(0)
                        .imported(0)
                        .failed(1)
                        .errors(List.of(ProductImportErrorResponse.builder()
                                .row(0)
                                .message("CSV non conforme. Exportez le template et remplissez-le. " +
                                        "Colonnes manquantes: " + String.join(", ", missingHeaders))
                                .build()))
                        .build();
            }
            for (CSVRecord record : parser) {
                totalRows += 1;
                int rowNumber = (int) record.getRecordNumber() + 1;
                try {
                    String name = read(record, "name");
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("name is required");
                    }
                    BigDecimal initialPrice = parseRequiredDecimal(read(record, "initial_price"), "initial_price");
                    BigDecimal shippingPrice = parseRequiredDecimal(read(record, "shipping_price"), "shipping_price");

                    Product product = new Product();
                    product.setName(name.trim());
                    product.setDescription(read(record, "description"));
                    product.setInitialPrice(initialPrice);
                    product.setFinalPrice(parseDecimal(read(record, "final_price")));
                    product.setCostPrice(parseDecimal(read(record, "cost_price")));
                    product.setShippingPrice(shippingPrice);
                    product.setShippingCostPrice(parseDecimal(read(record, "shipping_cost_price")));
                    product.setStatus(parseStatus(read(record, "status")));
                    product.setAvailability(parseAvailability(read(record, "availability")));
                    product.setAvailabilityText(read(record, "availability_text"));
                    product.setSku(read(record, "sku"));

                    productUseCase.createProduct(product, tenantId, null, null, null);
                    imported += 1;
                } catch (Exception ex) {
                    errors.add(ProductImportErrorResponse.builder()
                            .row(rowNumber)
                            .message(ex.getMessage())
                            .build());
                }
            }
        }

        return ProductImportResponse.builder()
                .totalRows(totalRows)
                .imported(imported)
                .failed(errors.size())
                .errors(errors)
                .build();
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
        BigDecimal lowestPrice = product.isShowLowestPrice()
                ? productUseCase.getLowestPrice(product.getId())
                : null;
        boolean lowStock = productUseCase.isLowStock(product);
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
                .continueSelling(product.isContinueSelling())
                .showLowestPrice(product.isShowLowestPrice())
                .lowestPrice(lowestPrice)
                .lowStock(lowStock)
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
        BigDecimal lowestPrice = product.isShowLowestPrice()
                ? productUseCase.getLowestPrice(product.getId())
                : null;
        boolean lowStock = productUseCase.isLowStock(product);
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
                .continueSelling(product.isContinueSelling())
                .showLowestPrice(product.isShowLowestPrice())
                .lowestPrice(lowestPrice)
                .lowStock(lowStock)
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

    private ProductOptionResponse toOptionResponse(ProductUseCase.ProductOptionGroup group) {
        ProductOption option = group.option();
        return ProductOptionResponse.builder()
                .id(option.getId())
                .productId(option.getProductId())
                .name(option.getName())
                .type(option.getType())
                .required(option.isRequired())
                .usedForVariants(option.isUsedForVariants())
                .displayOrder(option.getDisplayOrder())
                .createdBy(option.getCreatedBy())
                .createdAt(option.getCreatedAt())
                .values(group.values().stream().map(this::toOptionValueResponse).toList())
                .build();
    }

    private ProductOptionValueResponse toOptionValueResponse(ProductOptionValue value) {
        return ProductOptionValueResponse.builder()
                .id(value.getId())
                .optionId(value.getOptionId())
                .value(value.getValue())
                .hexColor(value.getHexColor())
                .displayOrder(value.getDisplayOrder())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .build();
    }

    private List<ProductUseCase.ProductOptionGroup> toOptionGroups(List<ProductOptionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<ProductUseCase.ProductOptionGroup> groups = new ArrayList<>();
        for (ProductOptionRequest request : requests) {
            if (request == null) {
                continue;
            }
            ProductOption option = new ProductOption();
            option.setName(request.getName());
            option.setType(request.getType());
            option.setRequired(Boolean.TRUE.equals(request.getRequired()));
            option.setUsedForVariants(request.getUsedForVariants() == null || request.getUsedForVariants());
            option.setDisplayOrder(request.getDisplayOrder());

            List<ProductOptionValue> values = new ArrayList<>();
            if (request.getValues() != null) {
                for (ProductOptionValueRequest valueRequest : request.getValues()) {
                    if (valueRequest == null) {
                        continue;
                    }
                    ProductOptionValue value = new ProductOptionValue();
                    value.setValue(valueRequest.getValue());
                    value.setHexColor(valueRequest.getHexColor());
                    value.setDisplayOrder(valueRequest.getDisplayOrder());
                    values.add(value);
                }
            }
            groups.add(new ProductUseCase.ProductOptionGroup(option, values));
        }
        return groups;
    }

    private List<ProductUseCase.ProductVariantGroup> toVariantGroups(List<ProductVariantRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<ProductUseCase.ProductVariantGroup> groups = new ArrayList<>();
        for (ProductVariantRequest request : requests) {
            if (request == null) {
                continue;
            }
            ProductVariant variant = new ProductVariant();
            variant.setSku(request.getSku());
            variant.setPriceOverride(request.getPriceOverride());
            variant.setStockQuantity(request.getStockQuantity());
            variant.setLowStockThreshold(request.getLowStockThreshold());
            variant.setStatus(request.getStatus());
            variant.setDefaultVariant(Boolean.TRUE.equals(request.getIsDefault()));
            variant.setContinueSellingOverride(request.getContinueSellingOverride());
            variant.setProductImageId(request.getProductImageId());
            groups.add(new ProductUseCase.ProductVariantGroup(variant, request.getOptionValueIds()));
        }
        return groups;
    }

    private List<ProductVariantResponse> mapVariantResponses(
            Long productId,
            List<ProductUseCase.ProductVariantGroup> groups
    ) {
        List<ProductUseCase.ProductOptionGroup> optionGroups = productUseCase.listProductOptions(productId);
        List<ProductImage> images = productUseCase.listProductImages(productId);

        var optionOrder = optionGroups.stream()
                .collect(Collectors.toMap(
                        group -> group.option().getId(),
                        group -> group.option().getDisplayOrder()
                ));
        var valueById = new java.util.HashMap<Long, ProductOptionValue>();
        var optionByValueId = new java.util.HashMap<Long, Long>();
        for (ProductUseCase.ProductOptionGroup group : optionGroups) {
            for (ProductOptionValue value : group.values()) {
                valueById.put(value.getId(), value);
                optionByValueId.put(value.getId(), group.option().getId());
            }
        }
        var imageUrlById = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, ProductImage::getImageUrl));

        List<ProductVariantResponse> responses = new ArrayList<>();
        for (ProductUseCase.ProductVariantGroup group : groups) {
            ProductVariant variant = group.variant();
            List<Long> optionValueIds = group.optionValueIds() == null ? List.of() : group.optionValueIds();
            List<Long> sortedIds = optionValueIds.stream()
                    .sorted((a, b) -> {
                        Integer orderA = optionOrder.getOrDefault(optionByValueId.get(a), 0);
                        Integer orderB = optionOrder.getOrDefault(optionByValueId.get(b), 0);
                        return Integer.compare(orderA, orderB);
                    })
                    .toList();
            String displayName = sortedIds.stream()
                    .map(valueById::get)
                    .filter(value -> value != null && value.getValue() != null)
                    .map(ProductOptionValue::getValue)
                    .collect(Collectors.joining(" / "));
            String imageUrl = variant.getProductImageId() == null
                    ? null
                    : imageUrlById.get(variant.getProductImageId());

            responses.add(ProductVariantResponse.builder()
                    .id(variant.getId())
                    .productId(variant.getProductId())
                    .sku(variant.getSku())
                    .priceOverride(variant.getPriceOverride())
                    .stockQuantity(variant.getStockQuantity())
                    .lowStockThreshold(variant.getLowStockThreshold())
                    .status(variant.getStatus())
                    .isDefault(variant.isDefaultVariant())
                    .continueSellingOverride(variant.getContinueSellingOverride())
                    .productImageId(variant.getProductImageId())
                    .productImageUrl(imageUrl)
                    .optionValueIds(sortedIds)
                    .displayName(displayName)
                    .createdBy(variant.getCreatedBy())
                    .updatedBy(variant.getUpdatedBy())
                    .createdAt(variant.getCreatedAt())
                    .updatedAt(variant.getUpdatedAt())
                    .build());
        }
        return responses;
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String toPlain(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String read(CSVRecord record, String key) {
        if (record.isMapped(key)) {
            String value = record.get(key);
            return value == null || value.isBlank() ? null : value.trim();
        }
        return null;
    }

    private static BigDecimal parseRequiredDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return parseDecimal(value);
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: " + value);
        }
    }

    private static ProductStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return ProductStatus.ACTIVE;
        }
        return ProductStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static ProductAvailability parseAvailability(String value) {
        if (value == null || value.isBlank()) {
            return ProductAvailability.IN_STOCK;
        }
        return ProductAvailability.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
