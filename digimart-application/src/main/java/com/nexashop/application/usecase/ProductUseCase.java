package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.BadRequestException;
import com.nexashop.application.exception.ConflictException;
import com.nexashop.application.exception.ForbiddenException;
import com.nexashop.application.exception.NotFoundException;
import com.nexashop.application.port.out.AiTextProvider;
import com.nexashop.application.port.out.CategoryRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.ProductCategoryRepository;
import com.nexashop.application.port.out.ProductImageRepository;
import com.nexashop.application.port.out.ProductOptionRepository;
import com.nexashop.application.port.out.ProductOptionValueRepository;
import com.nexashop.application.port.out.ProductPriceHistoryRepository;
import com.nexashop.application.port.out.ProductRepository;
import com.nexashop.application.port.out.ProductStoreInventoryRepository;
import com.nexashop.application.port.out.ProductVariantRepository;
import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.VariantOptionValueRepository;
import com.nexashop.application.port.out.VariantStoreInventoryRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.catalog.entity.Category;
import com.nexashop.domain.catalog.entity.OptionType;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductCategory;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.catalog.entity.ProductOption;
import com.nexashop.domain.catalog.entity.ProductOptionValue;
import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import com.nexashop.domain.catalog.entity.ProductStatus;
import com.nexashop.domain.catalog.entity.ProductStoreInventory;
import com.nexashop.domain.catalog.entity.ProductVariant;
import com.nexashop.domain.catalog.entity.VariantOptionValue;
import com.nexashop.domain.catalog.entity.VariantStatus;
import com.nexashop.domain.catalog.entity.VariantStoreInventory;
import com.nexashop.domain.store.entity.Store;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ProductUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AiTextProvider aiTextProvider;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final ProductStoreInventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final VariantStoreInventoryRepository variantStoreInventoryRepository;
    private final TenantRepository tenantRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public ProductUseCase(
            CurrentUserProvider currentUserProvider,
            AiTextProvider aiTextProvider,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductImageRepository productImageRepository,
            ProductOptionRepository productOptionRepository,
            ProductOptionValueRepository productOptionValueRepository,
            ProductPriceHistoryRepository priceHistoryRepository,
            ProductStoreInventoryRepository inventoryRepository,
            ProductVariantRepository productVariantRepository,
            VariantOptionValueRepository variantOptionValueRepository,
            VariantStoreInventoryRepository variantStoreInventoryRepository,
            TenantRepository tenantRepository,
            CategoryRepository categoryRepository,
            StoreRepository storeRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.aiTextProvider = aiTextProvider;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productImageRepository = productImageRepository;
        this.productOptionRepository = productOptionRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.variantOptionValueRepository = variantOptionValueRepository;
        this.variantStoreInventoryRepository = variantStoreInventoryRepository;
        this.tenantRepository = tenantRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    public record ProductDetails(
            Product product,
            List<Long> categoryIds,
            Long primaryCategoryId,
            List<ProductImage> images,
            List<ProductStoreInventory> inventories
    ) {
    }

    public record ProductOptionGroup(ProductOption option, List<ProductOptionValue> values) {
    }

    public record ProductVariantGroup(
            ProductVariant variant,
            List<Long> optionValueIds,
            List<VariantStoreInventory> inventories
    ) {
    }

    public record StoreRef(Long id, String name) {
    }

    public Product createProduct(
            Product product,
            Long targetTenantId,
            List<Long> categoryIds,
            Long primaryCategoryId,
            List<ProductStoreInventory> inventories
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;

        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }

        String slugInput = product.getSlug();
        if (slugInput == null || slugInput.isBlank()) {
            slugInput = product.getName();
        }
        String baseSlug = slugify(slugInput);
        if (baseSlug.isBlank()) {
            throw new BadRequestException("Product slug cannot be empty");
        }
        String resolvedSlug = resolveUniqueSlug(tenantId, baseSlug, null);
        product.setSlug(resolvedSlug);

        String sku = product.getSku();
        if (sku != null && !sku.isBlank()) {
            if (productRepository.existsByTenantIdAndSku(tenantId, sku.trim())) {
                throw new ConflictException("Product SKU already exists");
            }
            product.setSku(sku.trim());
        } else {
            product.setSku(null);
        }

        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (product.getAvailability() == null) {
            product.setAvailability(ProductAvailability.IN_STOCK);
        }

        validatePricing(product.getInitialPrice(), product.getFinalPrice());
        validatePreorder(product.getStatus(), product.getAvailability(), product.getAvailabilityText());

        product.setTenantId(tenantId);
        if (product.getCreatedBy() == null) {
            product.setCreatedBy(currentUser.userId());
        }
        product.setUpdatedBy(currentUser.userId());

        Product saved = productRepository.save(product);
        recordPriceHistory(saved, currentUser.userId());

        if (categoryIds != null) {
            saveProductCategories(saved.getId(), tenantId, categoryIds, primaryCategoryId, currentUser.userId());
        }

        if (saved.isTrackStock()) {
            if (inventories != null) {
                replaceInventory(saved.getId(), tenantId, inventories);
            }
        }

        return saved;
    }

    public Product updateProduct(
            Long id,
            String slug,
            String sku,
            Product updates,
            List<Long> categoryIds,
            Long primaryCategoryId,
            List<ProductStoreInventory> inventories
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }

        if (slug != null && !slug.isBlank()) {
            String baseSlug = slugify(slug);
            if (baseSlug.isBlank()) {
                throw new BadRequestException("Product slug cannot be empty");
            }
            if (!baseSlug.equals(product.getSlug())) {
                String resolvedSlug = resolveUniqueSlug(product.getTenantId(), baseSlug, id);
                product.setSlug(resolvedSlug);
            }
        }

        if (sku != null && !sku.isBlank()) {
            String trimmedSku = sku.trim();
            if (!trimmedSku.equals(product.getSku())
                    && productRepository.existsByTenantIdAndSku(product.getTenantId(), trimmedSku)) {
                throw new ConflictException("Product SKU already exists");
            }
            product.setSku(trimmedSku);
        }

        BigDecimal previousInitial = product.getInitialPrice();
        BigDecimal previousFinal = product.getFinalPrice();

        product.setName(updates.getName());
        product.setDescription(updates.getDescription());
        product.setInitialPrice(updates.getInitialPrice());
        product.setFinalPrice(updates.getFinalPrice());
        product.setCostPrice(updates.getCostPrice());
        product.setShippingPrice(updates.getShippingPrice());
        product.setShippingCostPrice(updates.getShippingCostPrice());
        product.setTrackStock(updates.isTrackStock());
        product.setStockQuantity(updates.getStockQuantity());
        product.setLowStockThreshold(updates.getLowStockThreshold());
        if (updates.getStatus() != null) {
            product.setStatus(updates.getStatus());
        }
        if (updates.getAvailability() != null) {
            product.setAvailability(updates.getAvailability());
        }
        product.setAvailabilityText(updates.getAvailabilityText());
        product.setContinueSelling(updates.isContinueSelling());
        product.setShowLowestPrice(updates.isShowLowestPrice());
        product.setUpdatedBy(currentUser.userId());

        validatePricing(product.getInitialPrice(), product.getFinalPrice());
        validatePreorder(product.getStatus(), product.getAvailability(), product.getAvailabilityText());

        Product saved = productRepository.save(product);

        if (priceChanged(previousInitial, saved.getInitialPrice())
                || priceChanged(previousFinal, saved.getFinalPrice())) {
            recordPriceHistory(saved, currentUser.userId());
        }

        if (categoryIds != null) {
            saveProductCategories(saved.getId(), saved.getTenantId(), categoryIds, primaryCategoryId, currentUser.userId());
        }

        if (!saved.isTrackStock()) {
            inventoryRepository.deleteByProductId(saved.getId());
        } else if (inventories != null) {
            replaceInventory(saved.getId(), saved.getTenantId(), inventories);
        }

        return saved;
    }

    public Product getProduct(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return product;
    }

    public ProductDetails getProductDetails(Long id) {
        Product product = getProduct(id);
        List<ProductCategory> categories = productCategoryRepository.findByProductId(product.getId());
        List<Long> categoryIds = categories.stream()
                .map(ProductCategory::getCategoryId)
                .toList();
        Long primaryCategoryId = categories.stream()
                .filter(ProductCategory::isPrimary)
                .map(ProductCategory::getCategoryId)
                .findFirst()
                .orElse(null);
        List<ProductImage> images = productImageRepository.findByProductId(product.getId());
        List<ProductStoreInventory> inventories = inventoryRepository.findByProductId(product.getId());
        return new ProductDetails(product, categoryIds, primaryCategoryId, images, inventories);
    }

    public List<ProductOptionGroup> listProductOptions(Long productId) {
        getProduct(productId);
        List<ProductOption> options = productOptionRepository.findByProductId(productId);
        if (options.isEmpty()) {
            return List.of();
        }
        List<Long> optionIds = options.stream().map(ProductOption::getId).toList();
        List<ProductOptionValue> values = productOptionValueRepository.findByOptionIds(optionIds);
        var valuesByOption = values.stream()
                .collect(Collectors.groupingBy(ProductOptionValue::getOptionId));
        return options.stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(option -> new ProductOptionGroup(
                        option,
                        valuesByOption.getOrDefault(option.getId(), List.of()).stream()
                                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                                .toList()
                ))
                .toList();
    }

    public List<ProductOptionGroup> replaceProductOptions(Long productId, List<ProductOptionGroup> groups) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Product product = getProduct(productId);
        Long tenantId = product.getTenantId();
        List<ProductOptionGroup> requested = groups == null ? List.of() : groups;
        List<ProductOptionGroup> sanitized = new ArrayList<>();
        for (ProductOptionGroup group : requested) {
            if (group == null || group.option() == null) {
                continue;
            }
            ProductOption incoming = group.option();
            if (!incoming.isUsedForVariants()) {
                continue;
            }
            String name = incoming.getName() == null ? "" : incoming.getName().trim();
            if (name.isEmpty()) {
                continue;
            }
            List<ProductOptionValue> incomingValues = group.values() == null ? List.of() : group.values();
            List<ProductOptionValue> cleanedValues = new ArrayList<>();
            int order = 0;
            for (ProductOptionValue value : incomingValues) {
                if (value == null) {
                    continue;
                }
                String val = value.getValue() == null ? "" : value.getValue().trim();
                if (val.isEmpty()) {
                    continue;
                }
                ProductOptionValue cleaned = new ProductOptionValue();
                cleaned.setValue(val);
                cleaned.setHexColor(value.getHexColor());
                cleaned.setDisplayOrder(value.getDisplayOrder() == null ? order : value.getDisplayOrder());
                cleanedValues.add(cleaned);
                order += 1;
            }
            if (cleanedValues.isEmpty()) {
                continue;
            }
            ProductOption cleanedOption = new ProductOption();
            cleanedOption.setName(name);
            cleanedOption.setType(incoming.getType());
            cleanedOption.setRequired(incoming.isRequired());
            cleanedOption.setUsedForVariants(true);
            cleanedOption.setDisplayOrder(incoming.getDisplayOrder());
            sanitized.add(new ProductOptionGroup(cleanedOption, cleanedValues));
        }
        requested = sanitized;

        validateOptionGroups(requested);

        deleteVariantsForProduct(productId);
        deleteOptionsForProduct(productId);

        if (requested.isEmpty()) {
            return List.of();
        }

        List<ProductOption> optionsToSave = new ArrayList<>();
        for (int i = 0; i < requested.size(); i++) {
            ProductOption incoming = requested.get(i).option();
            ProductOption option = new ProductOption();
            option.setTenantId(tenantId);
            option.setProductId(productId);
            option.setName(incoming.getName().trim());
            option.setType(incoming.getType() == null ? OptionType.TEXT : incoming.getType());
            option.setRequired(incoming.isRequired());
            option.setUsedForVariants(incoming.isUsedForVariants());
            option.setDisplayOrder(resolveDisplayOrder(incoming.getDisplayOrder(), i));
            option.setCreatedBy(currentUser.userId());
            optionsToSave.add(option);
        }
        List<ProductOption> savedOptions = productOptionRepository.saveAll(optionsToSave);

        List<ProductOptionValue> valuesToSave = new ArrayList<>();
        for (int i = 0; i < savedOptions.size(); i++) {
            ProductOption savedOption = savedOptions.get(i);
            List<ProductOptionValue> incomingValues = requested.get(i).values();
            int valueOrder = 0;
            for (ProductOptionValue incomingValue : incomingValues) {
                ProductOptionValue value = new ProductOptionValue();
                value.setTenantId(tenantId);
                value.setOptionId(savedOption.getId());
                value.setValue(incomingValue.getValue().trim());
                value.setHexColor(normalizeHex(incomingValue.getHexColor()));
                value.setDisplayOrder(resolveDisplayOrder(incomingValue.getDisplayOrder(), valueOrder));
                value.setCreatedBy(currentUser.userId());
                valuesToSave.add(value);
                valueOrder += 1;
            }
        }
        List<ProductOptionValue> savedValues = valuesToSave.isEmpty()
                ? List.of()
                : productOptionValueRepository.saveAll(valuesToSave);

        var valuesByOption = savedValues.stream()
                .collect(Collectors.groupingBy(ProductOptionValue::getOptionId));

        return savedOptions.stream()
                .map(option -> new ProductOptionGroup(
                        option,
                        valuesByOption.getOrDefault(option.getId(), List.of())
                ))
                .toList();
    }

    public List<ProductVariantGroup> listProductVariants(Long productId) {
        getProduct(productId);
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            return List.of();
        }
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        List<VariantOptionValue> links = variantOptionValueRepository.findByVariantIds(variantIds);
        var idsByVariant = links.stream()
                .collect(Collectors.groupingBy(VariantOptionValue::getVariantId,
                        Collectors.mapping(VariantOptionValue::getOptionValueId, Collectors.toList())));
        List<VariantStoreInventory> inventories = variantStoreInventoryRepository.findByVariantIds(variantIds);
        var inventoriesByVariant = inventories.stream()
                .collect(Collectors.groupingBy(VariantStoreInventory::getVariantId));

        return variants.stream()
                .map(variant -> new ProductVariantGroup(
                        variant,
                        idsByVariant.getOrDefault(variant.getId(), List.of()),
                        inventoriesByVariant.getOrDefault(variant.getId(), List.of())
                ))
                .toList();
    }

    public List<ProductVariantGroup> replaceProductVariants(Long productId, List<ProductVariantGroup> groups) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Product product = getProduct(productId);
        Long tenantId = product.getTenantId();
        List<ProductVariantGroup> requested = groups == null ? List.of() : groups;

        validateVariantGroups(productId, tenantId, requested);

        deleteVariantsForProduct(productId);

        if (requested.isEmpty()) {
            return List.of();
        }

        List<ProductVariant> savedVariants = new ArrayList<>();
        for (ProductVariantGroup group : requested) {
            ProductVariant incoming = group.variant();
            ProductVariant variant = new ProductVariant();
            variant.setTenantId(tenantId);
            variant.setProductId(productId);
            variant.setSku(normalizeSku(incoming.getSku()));
            BigDecimal resolvedFinalOverride = incoming.getFinalPriceOverride() != null
                    ? incoming.getFinalPriceOverride()
                    : incoming.getPriceOverride();
            variant.setPriceOverride(resolvedFinalOverride);
            variant.setInitialPriceOverride(incoming.getInitialPriceOverride());
            variant.setFinalPriceOverride(resolvedFinalOverride);
            variant.setCostPriceOverride(incoming.getCostPriceOverride());
            variant.setShippingPriceOverride(incoming.getShippingPriceOverride());
            variant.setShippingCostPriceOverride(incoming.getShippingCostPriceOverride());
            boolean trackStock = incoming.isTrackStock();
            variant.setTrackStock(trackStock);
            if (trackStock) {
                variant.setStockQuantity(null);
                variant.setLowStockThreshold(null);
            } else {
                variant.setStockQuantity(incoming.getStockQuantity());
                variant.setLowStockThreshold(incoming.getLowStockThreshold());
            }
            variant.setStatus(incoming.getStatus() == null ? VariantStatus.ACTIVE : incoming.getStatus());
            variant.setDefaultVariant(incoming.isDefaultVariant());
            variant.setContinueSellingOverride(incoming.getContinueSellingOverride());
            variant.setProductImageId(incoming.getProductImageId());
            variant.setCreatedBy(currentUser.userId());
            variant.setUpdatedBy(currentUser.userId());
            ProductVariant savedVariant = productVariantRepository.save(variant);
            savedVariants.add(savedVariant);
            if (trackStock) {
                replaceVariantInventory(savedVariant.getId(), tenantId, group.inventories());
            }
        }

        List<VariantOptionValue> linksToSave = new ArrayList<>();
        for (int i = 0; i < savedVariants.size(); i++) {
            ProductVariant savedVariant = savedVariants.get(i);
            List<Long> optionValueIds = requested.get(i).optionValueIds();
            if (optionValueIds == null || optionValueIds.isEmpty()) {
                continue;
            }
            Set<Long> uniqueOptionIds = new LinkedHashSet<>(optionValueIds);
            for (Long optionValueId : uniqueOptionIds) {
                VariantOptionValue link = new VariantOptionValue();
                link.setTenantId(tenantId);
                link.setVariantId(savedVariant.getId());
                link.setOptionValueId(optionValueId);
                linksToSave.add(link);
            }
        }
        if (!linksToSave.isEmpty()) {
            variantOptionValueRepository.saveAll(linksToSave);
        }

        return listProductVariants(productId);
    }

    public String suggestProductDescription(
            Long id,
            String language,
            Integer maxSentences,
            String tone
    ) {
        currentUserProvider.requireUser();
        Product product = getProduct(id);
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Product name is required");
        }
        String resolvedLanguage = language == null || language.isBlank() ? "FR" : language.trim();
        int resolvedMaxSentences = maxSentences == null || maxSentences < 1 ? 2 : Math.min(maxSentences, 4);
        String resolvedTone = tone == null || tone.isBlank() ? "neutre" : tone.trim();
        String categoryContext = resolvePrimaryCategoryContext(product.getId());
        String prompt = buildProductDescriptionPrompt(
                product.getName(),
                product.getDescription(),
                categoryContext,
                resolvedLanguage,
                resolvedMaxSentences,
                resolvedTone
        );
        return aiTextProvider.generateText(prompt);
    }

    public List<Product> listProducts(Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return productRepository.findByTenantId(tenantId);
    }

    public PageResult<Product> listProducts(PageRequest request, Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        return productRepository.findByTenantId(resolved, tenantId);
    }

    public PageResult<Product> listProducts(
            PageRequest request,
            Long tenantId,
            ProductStatus status,
            ProductAvailability availability,
            Boolean stockLow,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Long categoryId
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        String normalizedSearch = search == null
                ? ""
                : search.trim().toLowerCase();
        return productRepository.searchProducts(
                resolved,
                tenantId,
                status,
                availability,
                stockLow,
                minPrice,
                maxPrice,
                normalizedSearch,
                categoryId
        );
    }

    public PageResult<ProductPriceHistory> listPriceHistory(PageRequest request, Long productId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!isSuperAdmin && !product.getTenantId().equals(currentUser.tenantId())) {
            throw new ForbiddenException("Tenant access required");
        }
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        PageResult<ProductPriceHistory> result = priceHistoryRepository.findByProductId(resolved, productId);
        if (!result.items().isEmpty() || resolved.page() > 0) {
            return result;
        }
        if (product.getInitialPrice() == null && product.getFinalPrice() == null) {
            return result;
        }
        ProductPriceHistory fallback = buildPriceHistory(
                product,
                product.getUpdatedBy() != null ? product.getUpdatedBy() : currentUser.userId()
        );
        fallback.setChangedAt(product.getUpdatedAt() != null ? product.getUpdatedAt() : LocalDateTime.now());
        priceHistoryRepository.save(fallback);
        return PageResult.of(List.of(fallback), resolved.page(), resolved.size(), 1);
    }

    public boolean isLowStock(Product product) {
        if (product == null) {
            return false;
        }
        if (productVariantRepository.existsByProductId(product.getId())) {
            return productVariantRepository.existsLowStockByProductId(product.getId());
        }
        if (!product.isTrackStock()) {
            Integer threshold = product.getLowStockThreshold();
            Integer quantity = product.getStockQuantity();
            return threshold != null && quantity != null && quantity <= threshold;
        }
        return inventoryRepository.existsLowStockByProductId(product.getId());
    }

    public void deleteProduct(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        deleteVariantsForProduct(id);
        deleteOptionsForProduct(id);
        productCategoryRepository.deleteByProductId(id);
        productImageRepository.deleteByProductId(id);
        inventoryRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    public ProductImage addProductImage(Long productId, ProductImage image) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Product product = getProduct(productId);
        if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
            throw new BadRequestException("Image URL is required");
        }
        List<ProductImage> existing = productImageRepository.findByProductId(productId);
        int nextOrder = existing.size() + 1;
        if (image.getDisplayOrder() == null) {
            image.setDisplayOrder(nextOrder);
        }
        image.setTenantId(product.getTenantId());
        image.setProductId(productId);

        if (image.isPrimary()) {
            List<ProductImage> toUpdate = new ArrayList<>();
            for (ProductImage current : existing) {
                if (current.isPrimary()) {
                    current.setPrimary(false);
                    toUpdate.add(current);
                }
            }
            if (!toUpdate.isEmpty()) {
                productImageRepository.saveAll(toUpdate);
            }
        } else if (existing.isEmpty()) {
            image.setPrimary(true);
        }

        return productImageRepository.save(image);
    }

    public List<ProductImage> listProductImages(Long productId) {
        getProduct(productId);
        return productImageRepository.findByProductId(productId);
    }

    public List<StoreRef> listActiveStores(Long productId) {
        Product product = getProduct(productId);
        if (productVariantRepository.existsByProductId(productId)) {
            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            List<Long> variantIds = variants.stream()
                    .filter(ProductVariant::isTrackStock)
                    .map(ProductVariant::getId)
                    .toList();
            if (variantIds.isEmpty()) {
                return List.of();
            }
            List<VariantStoreInventory> inventories = variantStoreInventoryRepository.findByVariantIds(variantIds);
            List<StoreRef> stores = new ArrayList<>();
            for (VariantStoreInventory inventory : inventories) {
                if (inventory == null || inventory.getStoreId() == null || !inventory.isActiveInStore()) {
                    continue;
                }
                Store store = storeRepository.findById(inventory.getStoreId()).orElse(null);
                if (store != null) {
                    stores.add(new StoreRef(store.getId(), store.getName()));
                }
            }
            return stores;
        }
        if (!product.isTrackStock()) {
            return List.of();
        }
        List<ProductStoreInventory> inventories = inventoryRepository.findByProductId(productId);
        List<StoreRef> stores = new ArrayList<>();
        for (ProductStoreInventory inventory : inventories) {
            if (inventory == null || inventory.getStoreId() == null || !inventory.isActiveInStore()) {
                continue;
            }
            Store store = storeRepository.findById(inventory.getStoreId()).orElse(null);
            if (store != null && store.getName() != null) {
                stores.add(new StoreRef(store.getId(), store.getName()));
            }
        }
        return stores;
    }

    public void deleteProductImage(Long productId, Long imageId) {
        getProduct(productId);
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        if (!productId.equals(image.getProductId())) {
            throw new ForbiddenException("Image does not belong to product");
        }
        productVariantRepository.clearProductImage(productId, imageId);
        productImageRepository.delete(image);
    }

    public List<ProductImage> reorderProductImages(Long productId, List<Long> imageIds) {
        getProduct(productId);
        if (imageIds == null || imageIds.isEmpty()) {
            throw new BadRequestException("Image order is required");
        }
        List<ProductImage> existing = productImageRepository.findByProductId(productId);
        if (existing.isEmpty()) {
            throw new BadRequestException("No images to reorder");
        }
        List<Long> existingIds = existing.stream()
                .map(ProductImage::getId)
                .toList();
        Set<Long> existingSet = new LinkedHashSet<>(existingIds);
        Set<Long> requestedSet = new LinkedHashSet<>(imageIds);
        if (!existingSet.equals(requestedSet)) {
            throw new BadRequestException("Image order does not match existing images");
        }
        int order = 1;
        for (Long imageId : imageIds) {
            for (ProductImage image : existing) {
                if (imageId.equals(image.getId())) {
                    image.setDisplayOrder(order);
                    image.setPrimary(order == 1);
                    order += 1;
                    break;
                }
            }
        }
        return productImageRepository.saveAll(existing);
    }

    public List<Product> listProductsForStore(Long storeId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        List<Product> products = productRepository.findByTenantId(store.getTenantId());
        if (products.isEmpty()) {
            return List.of();
        }
        List<ProductStoreInventory> inventories = inventoryRepository.findByStoreId(storeId);
        Set<Long> activeProductIds = inventories.stream()
                .filter(ProductStoreInventory::isActiveInStore)
                .map(ProductStoreInventory::getProductId)
                .collect(Collectors.toSet());
        return products.stream()
                .filter(product -> !product.isTrackStock() || activeProductIds.contains(product.getId()))
                .toList();
    }

    public int bulkUpdatePricing(
            List<Long> productIds,
            java.math.BigDecimal initialPrice,
            java.math.BigDecimal finalPrice,
            java.math.BigDecimal shippingPrice,
            java.math.BigDecimal shippingCostPrice
    ) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BadRequestException("Product ids are required");
        }
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        List<Product> toUpdate = new ArrayList<>();
        List<ProductPriceHistory> histories = new ArrayList<>();
        for (Long id : productIds) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
                throw new ForbiddenException("Tenant access required");
            }
            BigDecimal previousInitial = product.getInitialPrice();
            BigDecimal previousFinal = product.getFinalPrice();
            BigDecimal nextInitial = initialPrice != null ? initialPrice : product.getInitialPrice();
            BigDecimal nextFinal = finalPrice != null ? finalPrice : product.getFinalPrice();
            validatePricing(nextInitial, nextFinal);
            if (initialPrice != null) {
                product.setInitialPrice(initialPrice);
            }
            if (finalPrice != null) {
                product.setFinalPrice(finalPrice);
            }
            if (shippingPrice != null) {
                product.setShippingPrice(shippingPrice);
            }
            if (shippingCostPrice != null) {
                product.setShippingCostPrice(shippingCostPrice);
            }
            product.setUpdatedBy(currentUser.userId());
            toUpdate.add(product);
            if (priceChanged(previousInitial, nextInitial)
                    || priceChanged(previousFinal, nextFinal)) {
                histories.add(buildPriceHistory(product, currentUser.userId()));
            }
        }
        if (!toUpdate.isEmpty()) {
            productRepository.saveAll(toUpdate);
        }
        if (!histories.isEmpty()) {
            priceHistoryRepository.saveAll(histories);
        }
        return toUpdate.size();
    }

    public int bulkDeleteProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BadRequestException("Product ids are required");
        }
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        List<Product> toDelete = new ArrayList<>();
        for (Long id : productIds) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
                throw new ForbiddenException("Tenant access required");
            }
            toDelete.add(product);
        }
        for (Product product : toDelete) {
            productCategoryRepository.deleteByProductId(product.getId());
            productImageRepository.deleteByProductId(product.getId());
            inventoryRepository.deleteByProductId(product.getId());
            productRepository.delete(product);
        }
        return toDelete.size();
    }

    public int bulkUpdateStatus(List<Long> productIds, ProductStatus status) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BadRequestException("Product ids are required");
        }
        if (status == null) {
            throw new BadRequestException("Status is required");
        }
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        List<Product> toUpdate = new ArrayList<>();
        for (Long id : productIds) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            if (!isSuperAdmin && !product.getTenantId().equals(requesterTenantId)) {
                throw new ForbiddenException("Tenant access required");
            }
            if (status == ProductStatus.ACTIVE) {
                validatePreorder(status, product.getAvailability(), product.getAvailabilityText());
            }
            product.setStatus(status);
            product.setUpdatedBy(currentUser.userId());
            toUpdate.add(product);
        }
        if (!toUpdate.isEmpty()) {
            productRepository.saveAll(toUpdate);
        }
        return toUpdate.size();
    }

    public BigDecimal getLowestPrice(Long productId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        BigDecimal lowest = priceHistoryRepository.findLowestPriceSince(productId, since);
        if (lowest == null) {
            lowest = priceHistoryRepository.findLowestPriceAllTime(productId);
        }
        return lowest;
    }

    private void saveProductCategories(
            Long productId,
            Long tenantId,
            List<Long> categoryIds,
            Long primaryCategoryId,
            Long createdBy
    ) {
        productCategoryRepository.deleteByProductId(productId);
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        if (primaryCategoryId != null) {
            uniqueIds.add(primaryCategoryId);
        }
        uniqueIds.addAll(categoryIds);

        List<ProductCategory> links = new ArrayList<>();
        int order = 0;
        for (Long categoryId : uniqueIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            if (!tenantId.equals(category.getTenantId())) {
                throw new ForbiddenException("Category belongs to another tenant");
            }
            ProductCategory link = new ProductCategory();
            link.setTenantId(tenantId);
            link.setProductId(productId);
            link.setCategoryId(categoryId);
            link.setPrimary(primaryCategoryId != null && primaryCategoryId.equals(categoryId));
            link.setDisplayOrder(order++);
            link.setCreatedBy(createdBy);
            links.add(link);
        }
        productCategoryRepository.saveAll(links);
    }

    private void replaceInventory(Long productId, Long tenantId, List<ProductStoreInventory> inventories) {
        inventoryRepository.deleteByProductId(productId);
        if (inventories == null || inventories.isEmpty()) {
            return;
        }
        List<ProductStoreInventory> toSave = new ArrayList<>();
        for (ProductStoreInventory inventory : inventories) {
            if (inventory == null || inventory.getStoreId() == null) {
                continue;
            }
            Store store = storeRepository.findById(inventory.getStoreId())
                    .orElseThrow(() -> new NotFoundException("Store not found"));
            if (!tenantId.equals(store.getTenantId())) {
                throw new ForbiddenException("Store belongs to another tenant");
            }
            ProductStoreInventory entry = new ProductStoreInventory();
            entry.setTenantId(tenantId);
            entry.setProductId(productId);
            entry.setStoreId(inventory.getStoreId());
            entry.setQuantity(inventory.getQuantity() == null ? 0 : inventory.getQuantity());
            entry.setLowStockThreshold(inventory.getLowStockThreshold());
            entry.setActiveInStore(inventory.isActiveInStore());
            toSave.add(entry);
        }
        if (!toSave.isEmpty()) {
            inventoryRepository.saveAll(toSave);
        }
    }

    private void replaceVariantInventory(
            Long variantId,
            Long tenantId,
            List<VariantStoreInventory> inventories
    ) {
        variantStoreInventoryRepository.deleteByVariantId(variantId);
        if (inventories == null || inventories.isEmpty()) {
            return;
        }
        List<VariantStoreInventory> toSave = new ArrayList<>();
        for (VariantStoreInventory inventory : inventories) {
            if (inventory == null || inventory.getStoreId() == null) {
                continue;
            }
            Store store = storeRepository.findById(inventory.getStoreId())
                    .orElseThrow(() -> new NotFoundException("Store not found"));
            if (!tenantId.equals(store.getTenantId())) {
                throw new ForbiddenException("Store belongs to another tenant");
            }
            VariantStoreInventory entry = new VariantStoreInventory();
            entry.setTenantId(tenantId);
            entry.setVariantId(variantId);
            entry.setStoreId(inventory.getStoreId());
            entry.setQuantity(inventory.getQuantity() == null ? 0 : inventory.getQuantity());
            entry.setLowStockThreshold(inventory.getLowStockThreshold());
            entry.setActiveInStore(inventory.isActiveInStore());
            toSave.add(entry);
        }
        if (!toSave.isEmpty()) {
            variantStoreInventoryRepository.saveAll(toSave);
        }
    }

    private void recordPriceHistory(Product product, Long changedBy) {
        if (product == null || product.getId() == null) {
            return;
        }
        priceHistoryRepository.save(buildPriceHistory(product, changedBy));
    }

    private ProductPriceHistory buildPriceHistory(Product product, Long changedBy) {
        ProductPriceHistory history = new ProductPriceHistory();
        history.setTenantId(product.getTenantId());
        history.setProductId(product.getId());
        history.setInitialPrice(product.getInitialPrice());
        history.setFinalPrice(product.getFinalPrice());
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        return history;
    }

    private void validatePricing(BigDecimal initialPrice, BigDecimal finalPrice) {
        if (initialPrice != null && finalPrice != null && finalPrice.compareTo(initialPrice) > 0) {
            throw new BadRequestException("Le prix final doit être inférieur ou égal au prix initial");
        }
    }

    private void validatePreorder(ProductStatus status, ProductAvailability availability, String availabilityText) {
        if (status == ProductStatus.ACTIVE
                && availability == ProductAvailability.PRE_ORDER
                && (availabilityText == null || availabilityText.isBlank())) {
            throw new BadRequestException("La date de disponibilité est obligatoire pour une précommande");
        }
    }

    private boolean priceChanged(BigDecimal previous, BigDecimal next) {
        if (previous == null && next == null) {
            return false;
        }
        if (previous == null || next == null) {
            return true;
        }
        return previous.compareTo(next) != 0;
    }

    private String resolveUniqueSlug(Long tenantId, String baseSlug, Long currentId) {
        String candidate = baseSlug;
        int suffix = 2;
        while (true) {
            boolean exists = productRepository.existsByTenantIdAndSlug(tenantId, candidate);
            if (!exists) {
                return candidate;
            }
            if (currentId != null) {
                Product current = productRepository.findById(currentId).orElse(null);
                if (current != null && candidate.equals(current.getSlug())) {
                    return candidate;
                }
            }
            candidate = baseSlug + "-" + suffix;
            suffix += 1;
        }
    }

    private String slugify(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (normalized.startsWith("-")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("-")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String buildProductDescriptionPrompt(
            String name,
            String description,
            String categoryContext,
            String language,
            int maxSentences,
            String tone
    ) {
        String current = description == null || description.isBlank() ? "(vide)" : description.trim();
        String categoryLine = categoryContext == null || categoryContext.isBlank()
                ? ""
                : " Categorie principale: " + categoryContext + ".";
        return String.format(
                "Ameliore cette description de produit en %s, %d phrase(s), ton %s. " +
                        "Ne renvoie que le texte final, sans liste ni titre. " +
                        "%s Produit: %s. Description actuelle: %s.",
                language,
                maxSentences,
                tone,
                categoryLine,
                name,
                current
        );
    }

    private String resolvePrimaryCategoryContext(Long productId) {
        if (productId == null) {
            return null;
        }
        List<ProductCategory> links = productCategoryRepository.findByProductId(productId);
        Long primaryId = links.stream()
                .filter(ProductCategory::isPrimary)
                .map(ProductCategory::getCategoryId)
                .findFirst()
                .orElse(null);
        if (primaryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(primaryId).orElse(null);
        if (category == null || category.getName() == null) {
            return null;
        }
        return buildCategoryPath(category);
    }

    private String buildCategoryPath(Category category) {
        List<String> parts = new ArrayList<>();
        Category current = category;
        int guard = 0;
        while (current != null && guard < 10) {
            if (current.getName() != null && !current.getName().isBlank()) {
                parts.add(0, current.getName().trim());
            }
            Long parentId = current.getParentCategoryId();
            if (parentId == null) {
                break;
            }
            current = categoryRepository.findById(parentId).orElse(null);
            guard += 1;
        }
        return parts.isEmpty() ? null : String.join(" > ", parts);
    }

    private void deleteVariantsForProduct(Long productId) {
        List<ProductVariant> existing = productVariantRepository.findByProductId(productId);
        if (existing.isEmpty()) {
            return;
        }
        List<Long> variantIds = existing.stream()
                .map(ProductVariant::getId)
                .toList();
        variantStoreInventoryRepository.deleteByVariantIds(variantIds);
        variantOptionValueRepository.deleteByVariantIds(variantIds);
        productVariantRepository.deleteByProductId(productId);
    }

    private void deleteOptionsForProduct(Long productId) {
        List<ProductOption> options = productOptionRepository.findByProductId(productId);
        if (options.isEmpty()) {
            return;
        }
        List<Long> optionIds = options.stream()
                .map(ProductOption::getId)
                .toList();
        productOptionValueRepository.deleteByOptionIds(optionIds);
        productOptionRepository.deleteByProductId(productId);
    }

    private void validateOptionGroups(List<ProductOptionGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        Set<String> optionNames = new LinkedHashSet<>();
        for (ProductOptionGroup group : groups) {
            if (group == null || group.option() == null) {
                throw new BadRequestException("Option is required");
            }
            ProductOption option = group.option();
            String name = option.getName() == null ? "" : option.getName().trim();
            if (name.isEmpty()) {
                throw new BadRequestException("Option name is required");
            }
            String key = name.toLowerCase();
            if (!optionNames.add(key)) {
                throw new BadRequestException("Option name already exists: " + name);
            }
            List<ProductOptionValue> values = group.values() == null ? List.of() : group.values();
            if (Boolean.TRUE.equals(option.isUsedForVariants()) && values.isEmpty()) {
                throw new BadRequestException("Option values are required for: " + name);
            }
            Set<String> valueNames = new LinkedHashSet<>();
            for (ProductOptionValue value : values) {
                if (value == null) {
                    throw new BadRequestException("Option value is required");
                }
                String val = value.getValue() == null ? "" : value.getValue().trim();
                if (val.isEmpty()) {
                    throw new BadRequestException("Option value is required for: " + name);
                }
                String valKey = val.toLowerCase();
                if (!valueNames.add(valKey)) {
                    throw new BadRequestException("Duplicate value '" + val + "' in option: " + name);
                }
                normalizeHex(value.getHexColor());
            }
        }
    }

    private void validateVariantGroups(Long productId, Long tenantId, List<ProductVariantGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        List<ProductOption> options = productOptionRepository.findByProductId(productId);
        List<Long> optionIds = options.stream().map(ProductOption::getId).toList();
        List<ProductOptionValue> values = optionIds.isEmpty()
                ? List.of()
                : productOptionValueRepository.findByOptionIds(optionIds);
        var valueById = values.stream()
                .collect(Collectors.toMap(ProductOptionValue::getId, value -> value));
        var optionById = options.stream()
                .collect(Collectors.toMap(ProductOption::getId, option -> option));
        Set<Long> requiredOptionIds = options.stream()
                .filter(ProductOption::isUsedForVariants)
                .map(ProductOption::getId)
                .collect(Collectors.toSet());

        Set<String> skus = new LinkedHashSet<>();
        int defaultCount = 0;
        for (ProductVariantGroup group : groups) {
            if (group == null || group.variant() == null) {
                throw new BadRequestException("Variant is required");
            }
            ProductVariant variant = group.variant();
            String sku = normalizeSku(variant.getSku());
            if (sku != null) {
                String key = sku.toLowerCase();
                if (!skus.add(key)) {
                    throw new BadRequestException("Duplicate variant SKU: " + sku);
                }
                if (productVariantRepository.existsByTenantIdAndSkuAndProductIdNot(tenantId, sku, productId)) {
                    throw new ConflictException("Variant SKU already exists");
                }
            }
            if (variant.isDefaultVariant()) {
                defaultCount += 1;
                if (defaultCount > 1) {
                    throw new BadRequestException("Only one default variant is allowed");
                }
            }
            if (variant.getProductImageId() != null) {
                ProductImage image = productImageRepository.findById(variant.getProductImageId()).orElse(null);
                if (image == null || !productId.equals(image.getProductId())) {
                    throw new BadRequestException("Variant image must belong to the product");
                }
            }

            List<Long> optionValueIds = group.optionValueIds() == null ? List.of() : group.optionValueIds();
            Set<Long> optionIdsUsed = new LinkedHashSet<>();
            for (Long optionValueId : optionValueIds) {
                ProductOptionValue value = valueById.get(optionValueId);
                if (value == null) {
                    throw new BadRequestException("Invalid option value for variant");
                }
                ProductOption option = optionById.get(value.getOptionId());
                if (option != null) {
                    optionIdsUsed.add(option.getId());
                }
            }
            if (!requiredOptionIds.isEmpty() && !optionIdsUsed.containsAll(requiredOptionIds)) {
                throw new BadRequestException("Variant must include all variant options");
            }
        }
    }

    private int resolveDisplayOrder(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeHex(String hex) {
        if (hex == null || hex.isBlank()) {
            return null;
        }
        String trimmed = hex.trim();
        String normalized = trimmed.startsWith("#") ? trimmed : "#" + trimmed;
        if (!normalized.matches("^#[0-9a-fA-F]{6}$")) {
            throw new BadRequestException("Invalid hex color: " + hex);
        }
        return normalized.toUpperCase();
    }

    private String normalizeSku(String sku) {
        if (sku == null) {
            return null;
        }
        String trimmed = sku.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
