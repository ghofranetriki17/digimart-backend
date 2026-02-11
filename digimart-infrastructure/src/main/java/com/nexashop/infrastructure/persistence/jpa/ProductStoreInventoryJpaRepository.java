package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductStoreInventoryJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStoreInventoryJpaRepository extends JpaRepository<ProductStoreInventoryJpaEntity, Long> {

    List<ProductStoreInventoryJpaEntity> findByProductId(Long productId);

    List<ProductStoreInventoryJpaEntity> findByStoreId(Long storeId);

    void deleteByProductId(Long productId);
}
