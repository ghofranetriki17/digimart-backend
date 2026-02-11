package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductStatus;
import com.nexashop.domain.catalog.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CrudRepositoryPort<Product, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    List<Product> findByTenantId(Long tenantId);

    PageResult<Product> findByTenantId(PageRequest request, Long tenantId);

    PageResult<Product> searchProducts(
            PageRequest request,
            Long tenantId,
            ProductStatus status,
            ProductAvailability availability,
            Boolean stockLow,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Long categoryId
    );
}
