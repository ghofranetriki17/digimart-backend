package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductVariant;
import java.util.List;

public interface ProductVariantRepository extends CrudRepositoryPort<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    boolean existsByProductId(Long productId);

    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    boolean existsByTenantIdAndSkuAndProductIdNot(Long tenantId, String sku, Long productId);

    boolean existsLowStockByProductId(Long productId);

    boolean existsDefaultVariant(Long productId);

    void clearProductImage(Long productId, Long productImageId);
}
