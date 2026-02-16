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
        domain.setInitialPriceOverride(entity.getInitialPriceOverride());
        domain.setFinalPriceOverride(entity.getFinalPriceOverride());
        domain.setCostPriceOverride(entity.getCostPriceOverride());
        domain.setShippingPriceOverride(entity.getShippingPriceOverride());
        domain.setShippingCostPriceOverride(entity.getShippingCostPriceOverride());
        domain.setTrackStock(entity.isTrackStock());
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
        entity.setInitialPriceOverride(domain.getInitialPriceOverride());
        entity.setFinalPriceOverride(domain.getFinalPriceOverride());
        entity.setCostPriceOverride(domain.getCostPriceOverride());
        entity.setShippingPriceOverride(domain.getShippingPriceOverride());
        entity.setShippingCostPriceOverride(domain.getShippingCostPriceOverride());
        entity.setTrackStock(domain.isTrackStock());
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
