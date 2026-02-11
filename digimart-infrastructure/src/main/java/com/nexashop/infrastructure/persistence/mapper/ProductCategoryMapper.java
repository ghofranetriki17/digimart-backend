package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductCategory;
import com.nexashop.infrastructure.persistence.model.catalog.ProductCategoryJpaEntity;

public final class ProductCategoryMapper {

    private ProductCategoryMapper() {
    }

    public static ProductCategory toDomain(ProductCategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductCategory domain = new ProductCategory();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setCategoryId(entity.getCategoryId());
        domain.setPrimary(entity.isPrimary());
        domain.setDisplayOrder(entity.getDisplayOrder());
        domain.setCreatedBy(entity.getCreatedBy());
        return domain;
    }

    public static ProductCategoryJpaEntity toJpa(ProductCategory domain) {
        if (domain == null) {
            return null;
        }
        ProductCategoryJpaEntity entity = new ProductCategoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setPrimary(domain.isPrimary());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }
}
