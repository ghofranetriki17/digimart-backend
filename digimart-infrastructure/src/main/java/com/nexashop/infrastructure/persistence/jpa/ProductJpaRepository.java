package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    Optional<ProductJpaEntity> findByIdAndTenantId(Long id, Long tenantId);

    List<ProductJpaEntity> findByTenantId(Long tenantId);

    Page<ProductJpaEntity> findByTenantId(Long tenantId, Pageable pageable);
}
