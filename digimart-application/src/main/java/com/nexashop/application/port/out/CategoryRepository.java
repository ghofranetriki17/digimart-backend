package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.Category;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends CrudRepositoryPort<Category, Long> {

    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    Optional<Category> findByTenantIdAndSlug(Long tenantId, String slug);

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    List<Category> findByTenantId(Long tenantId);

    PageResult<Category> findByTenantId(PageRequest request, Long tenantId);

    List<Category> findByTenantIdAndParentCategoryId(Long tenantId, Long parentCategoryId);

    PageResult<Category> findByTenantIdAndParentCategoryId(PageRequest request, Long tenantId, Long parentCategoryId);

    List<Category> findByTenantIdAndParentCategoryIdIsNull(Long tenantId);

    PageResult<Category> findByTenantIdAndParentCategoryIdIsNull(PageRequest request, Long tenantId);
}
