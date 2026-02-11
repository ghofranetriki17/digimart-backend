package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductStoreInventoryJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductStoreInventoryJpaRepository extends JpaRepository<ProductStoreInventoryJpaEntity, Long> {

    List<ProductStoreInventoryJpaEntity> findByProductId(Long productId);

    List<ProductStoreInventoryJpaEntity> findByStoreId(Long storeId);

    @Query("""
            select case when count(inv) > 0 then true else false end
            from ProductStoreInventoryJpaEntity inv
            where inv.productId = :productId
              and inv.lowStockThreshold is not null
              and inv.quantity is not null
              and inv.quantity <= inv.lowStockThreshold
            """)
    boolean existsLowStockByProductId(@Param("productId") Long productId);

    void deleteByProductId(Long productId);
}
