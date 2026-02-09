package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends CrudRepositoryPort<Category, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    Optional<Category> findByTenantIdAndSlug(Long tenantId, String slug);

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    List<Category> findByTenantId(Long tenantId);

    List<Category> findByTenantIdAndParentCategoryId(Long tenantId, Long parentCategoryId);

    List<Category> findByTenantIdAndParentCategoryIdIsNull(Long tenantId);
}
