package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.Category;
import com.nexashop.infrastructure.persistence.model.catalog.CategoryJpaEntity;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Category domain = new Category();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setName(entity.getName());
        domain.setSlug(entity.getSlug());
        domain.setDescription(entity.getDescription());
        domain.setParentCategoryId(entity.getParentCategoryId());
        domain.setDisplayOrder(entity.getDisplayOrder());
        domain.setActive(entity.isActive());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setUpdatedBy(entity.getUpdatedBy());
        return domain;
    }

    public static CategoryJpaEntity toJpa(Category domain) {
        if (domain == null) {
            return null;
        }
        CategoryJpaEntity entity = new CategoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setDescription(domain.getDescription());
        entity.setParentCategoryId(domain.getParentCategoryId());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setActive(domain.isActive());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());
        return entity;
    }
}
