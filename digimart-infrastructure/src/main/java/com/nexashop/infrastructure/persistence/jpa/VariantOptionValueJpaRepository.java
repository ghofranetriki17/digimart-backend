package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.VariantOptionValueJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantOptionValueJpaRepository extends JpaRepository<VariantOptionValueJpaEntity, Long> {

    List<VariantOptionValueJpaEntity> findByVariantId(Long variantId);

    List<VariantOptionValueJpaEntity> findByVariantIdIn(List<Long> variantIds);

    void deleteByVariantId(Long variantId);

    void deleteByVariantIdIn(List<Long> variantIds);
}
