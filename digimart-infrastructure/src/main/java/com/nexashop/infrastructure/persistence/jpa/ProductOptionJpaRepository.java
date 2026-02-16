package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOptionJpaEntity, Long> {

    List<ProductOptionJpaEntity> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
