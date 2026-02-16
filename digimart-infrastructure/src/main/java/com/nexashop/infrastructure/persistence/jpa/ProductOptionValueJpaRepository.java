package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionValueJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionValueJpaRepository extends JpaRepository<ProductOptionValueJpaEntity, Long> {

    List<ProductOptionValueJpaEntity> findByOptionId(Long optionId);

    List<ProductOptionValueJpaEntity> findByOptionIdIn(List<Long> optionIds);

    void deleteByOptionId(Long optionId);

    void deleteByOptionIdIn(List<Long> optionIds);
}
