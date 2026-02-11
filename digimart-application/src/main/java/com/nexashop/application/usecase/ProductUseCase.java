package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.BadRequestException;
import com.nexashop.application.exception.ConflictException;
import com.nexashop.application.exception.ForbiddenException;
import com.nexashop.application.exception.NotFoundException;
import com.nexashop.application.port.out.CategoryRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.ProductCategoryRepository;
import com.nexashop.application.port.out.ProductImageRepository;
import com.nexashop.application.port.out.ProductPriceHistoryRepository;
import com.nexashop.application.port.out.ProductRepository;
import com.nexashop.application.port.out.ProductStoreInventoryRepository;
import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.catalog.entity.Category;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductCategory;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import com.nexashop.domain.catalog.entity.ProductStatus;
import com.nexashop.domain.catalog.entity.ProductStoreInventory;
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
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final ProductStoreInventoryRepository inventoryRepository;
    private final TenantRepository tenantRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public ProductUseCase(
            CurrentUserProvider currentUserProvider,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductImageRepository productImageRepository,
            ProductPriceHistoryRepository priceHistoryRepository,
            ProductStoreInventoryRepository inventoryRepository,
            TenantRepository tenantRepository,
            CategoryRepository categoryRepository,
            StoreRepository storeRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productImageRepository = productImageRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.inventoryRepository = inventoryRepository;
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
}
