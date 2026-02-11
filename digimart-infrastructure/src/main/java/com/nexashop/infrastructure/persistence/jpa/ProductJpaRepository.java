package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductStatus;
import com.nexashop.infrastructure.persistence.model.catalog.ProductJpaEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    Optional<ProductJpaEntity> findByIdAndTenantId(Long id, Long tenantId);

    List<ProductJpaEntity> findByTenantId(Long tenantId);

    Page<ProductJpaEntity> findByTenantId(Long tenantId, Pageable pageable);

    @Query("""
            select distinct p
            from ProductJpaEntity p
            left join ProductCategoryJpaEntity pc on pc.productId = p.id
            where p.tenantId = :tenantId
              and (:status is null or p.status = :status)
              and (:availability is null or p.availability = :availability)
              and (
                :search = ''
                or lower(p.name) like concat('%', :search, '%')
                or lower(p.description) like concat('%', :search, '%')
                or lower(p.sku) like concat('%', :search, '%')
              )
              and (:minPrice is null or coalesce(p.finalPrice, p.initialPrice) >= :minPrice)
              and (:maxPrice is null or coalesce(p.finalPrice, p.initialPrice) <= :maxPrice)
              and (:categoryId is null or pc.categoryId = :categoryId)
              and (
                :stockLow is null
                or :stockLow = false
                or (
                  p.trackStock = false
                  and p.lowStockThreshold is not null
                  and p.stockQuantity is not null
                  and p.stockQuantity <= p.lowStockThreshold
                )
                or (
                  p.trackStock = true
                  and exists (
                    select 1 from ProductStoreInventoryJpaEntity inv
                    where inv.productId = p.id
                      and inv.lowStockThreshold is not null
                      and inv.quantity <= inv.lowStockThreshold
                  )
                )
              )
            """)
    Page<ProductJpaEntity> searchProducts(
            @Param("tenantId") Long tenantId,
            @Param("status") ProductStatus status,
            @Param("availability") ProductAvailability availability,
            @Param("stockLow") Boolean stockLow,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
