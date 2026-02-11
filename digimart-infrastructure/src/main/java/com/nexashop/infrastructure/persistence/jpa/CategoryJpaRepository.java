package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    Optional<CategoryJpaEntity> findByTenantIdAndSlug(Long tenantId, String slug);

    Optional<CategoryJpaEntity> findByIdAndTenantId(Long id, Long tenantId);

    List<CategoryJpaEntity> findByTenantId(Long tenantId);

    Page<CategoryJpaEntity> findByTenantId(Long tenantId, Pageable pageable);

    List<CategoryJpaEntity> findByTenantIdAndParentCategoryId(Long tenantId, Long parentCategoryId);

    Page<CategoryJpaEntity> findByTenantIdAndParentCategoryId(Long tenantId, Long parentCategoryId, Pageable pageable);

    List<CategoryJpaEntity> findByTenantIdAndParentCategoryIdIsNull(Long tenantId);

    Page<CategoryJpaEntity> findByTenantIdAndParentCategoryIdIsNull(Long tenantId, Pageable pageable);
}
