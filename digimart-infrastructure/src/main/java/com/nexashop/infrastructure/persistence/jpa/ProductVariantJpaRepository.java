package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductVariantJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, Long> {

    List<ProductVariantJpaEntity> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    boolean existsByProductId(Long productId);

    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    boolean existsByTenantIdAndSkuAndProductIdNot(Long tenantId, String sku, Long productId);

    @Query("""
            select case when count(v) > 0 then true else false end
            from ProductVariantJpaEntity v
            where v.productId = :productId
              and v.stockQuantity is not null
              and v.lowStockThreshold is not null
              and v.stockQuantity <= v.lowStockThreshold
            """)
    boolean existsLowStockByProductId(@Param("productId") Long productId);

    @Query("""
            select case when count(v) > 0 then true else false end
            from ProductVariantJpaEntity v
            where v.productId = :productId
              and v.defaultVariant = true
            """)
    boolean existsDefaultVariant(@Param("productId") Long productId);

    @Modifying
    @Query("""
            update ProductVariantJpaEntity v
            set v.productImageId = null
            where v.productId = :productId
              and v.productImageId = :productImageId
            """)
    void clearProductImage(@Param("productId") Long productId, @Param("productImageId") Long productImageId);
}
