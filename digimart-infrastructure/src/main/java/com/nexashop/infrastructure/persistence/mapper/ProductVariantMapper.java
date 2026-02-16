package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductVariant;
import com.nexashop.infrastructure.persistence.model.catalog.ProductVariantJpaEntity;

public final class ProductVariantMapper {

    private ProductVariantMapper() {
    }

    public static ProductVariant toDomain(ProductVariantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductVariant domain = new ProductVariant();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setSku(entity.getSku());
        domain.setPriceOverride(entity.getPriceOverride());
        domain.setStockQuantity(entity.getStockQuantity());
        domain.setLowStockThreshold(entity.getLowStockThreshold());
        domain.setStatus(entity.getStatus());
        domain.setDefaultVariant(entity.isDefaultVariant());
        domain.setContinueSellingOverride(entity.getContinueSellingOverride());
        domain.setProductImageId(entity.getProductImageId());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setUpdatedBy(entity.getUpdatedBy());
        return domain;
    }

    public static ProductVariantJpaEntity toJpa(ProductVariant domain) {
        if (domain == null) {
            return null;
        }
        ProductVariantJpaEntity entity = new ProductVariantJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setSku(domain.getSku());
        entity.setPriceOverride(domain.getPriceOverride());
        entity.setStockQuantity(domain.getStockQuantity());
        entity.setLowStockThreshold(domain.getLowStockThreshold());
        entity.setStatus(domain.getStatus());
        entity.setDefaultVariant(domain.isDefaultVariant());
        entity.setContinueSellingOverride(domain.getContinueSellingOverride());
        entity.setProductImageId(domain.getProductImageId());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());
        return entity;
    }
}
