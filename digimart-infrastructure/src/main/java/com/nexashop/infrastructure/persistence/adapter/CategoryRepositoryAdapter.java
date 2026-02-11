package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.CategoryRepository;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.catalog.entity.Category;
import com.nexashop.infrastructure.persistence.jpa.CategoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.CategoryMapper;
import com.nexashop.infrastructure.persistence.model.catalog.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepositoryAdapter
        extends JpaRepositoryAdapter<Category, CategoryJpaEntity, Long>
        implements CategoryRepository {

    private final CategoryJpaRepository repository;

    public CategoryRepositoryAdapter(CategoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected CategoryJpaEntity toJpa(Category domain) {
        return CategoryMapper.toJpa(domain);
    }

    @Override
    protected Category toDomain(CategoryJpaEntity entity) {
        return CategoryMapper.toDomain(entity);
    }

    @Override
    public boolean existsByTenantIdAndSlug(Long tenantId, String slug) {
        return repository.existsByTenantIdAndSlug(tenantId, slug);
    }

    @Override
    public Optional<Category> findByTenantIdAndSlug(Long tenantId, String slug) {
        return repository.findByTenantIdAndSlug(tenantId, slug).map(CategoryMapper::toDomain);
    }

    @Override
    public Optional<Category> findByIdAndTenantId(Long id, Long tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(CategoryMapper::toDomain);
    }

    @Override
    public List<Category> findByTenantId(Long tenantId) {
        return toDomainList(repository.findByTenantId(tenantId));
    }

    @Override
    public PageResult<Category> findByTenantId(PageRequest request, Long tenantId) {
        Page<CategoryJpaEntity> page = repository.findByTenantId(
                tenantId,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public List<Category> findByTenantIdAndParentCategoryId(Long tenantId, Long parentCategoryId) {
        return toDomainList(repository.findByTenantIdAndParentCategoryId(tenantId, parentCategoryId));
    }

    @Override
    public PageResult<Category> findByTenantIdAndParentCategoryId(
            PageRequest request,
            Long tenantId,
            Long parentCategoryId
    ) {
        Page<CategoryJpaEntity> page = repository.findByTenantIdAndParentCategoryId(
                tenantId,
                parentCategoryId,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public List<Category> findByTenantIdAndParentCategoryIdIsNull(Long tenantId) {
        return toDomainList(repository.findByTenantIdAndParentCategoryIdIsNull(tenantId));
    }

    @Override
    public PageResult<Category> findByTenantIdAndParentCategoryIdIsNull(PageRequest request, Long tenantId) {
        Page<CategoryJpaEntity> page = repository.findByTenantIdAndParentCategoryIdIsNull(
                tenantId,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }
}
