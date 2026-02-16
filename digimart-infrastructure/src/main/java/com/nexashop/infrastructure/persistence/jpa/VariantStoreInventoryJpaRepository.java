package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.VariantStoreInventoryJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantStoreInventoryJpaRepository extends JpaRepository<VariantStoreInventoryJpaEntity, Long> {

    List<VariantStoreInventoryJpaEntity> findByVariantId(Long variantId);

    List<VariantStoreInventoryJpaEntity> findByVariantIdIn(List<Long> variantIds);

    List<VariantStoreInventoryJpaEntity> findByStoreId(Long storeId);

    void deleteByVariantId(Long variantId);

    void deleteByVariantIdIn(List<Long> variantIds);
}
