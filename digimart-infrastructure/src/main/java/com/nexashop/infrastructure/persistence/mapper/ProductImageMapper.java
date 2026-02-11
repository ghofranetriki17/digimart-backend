package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.infrastructure.persistence.model.catalog.ProductImageJpaEntity;

public final class ProductImageMapper {

    private ProductImageMapper() {
    }

    public static ProductImage toDomain(ProductImageJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductImage domain = new ProductImage();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setImageUrl(entity.getImageUrl());
        domain.setAltText(entity.getAltText());
        domain.setDisplayOrder(entity.getDisplayOrder());
        domain.setPrimary(entity.isPrimary());
        return domain;
    }

    public static ProductImageJpaEntity toJpa(ProductImage domain) {
        if (domain == null) {
            return null;
        }
        ProductImageJpaEntity entity = new ProductImageJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setImageUrl(domain.getImageUrl());
        entity.setAltText(domain.getAltText());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setPrimary(domain.isPrimary());
        return entity;
    }
}
