package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.infrastructure.persistence.model.catalog.ProductJpaEntity;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Product domain = new Product();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setName(entity.getName());
        domain.setSlug(entity.getSlug());
        domain.setSku(entity.getSku());
        domain.setDescription(entity.getDescription());
        domain.setInitialPrice(entity.getInitialPrice());
        domain.setFinalPrice(entity.getFinalPrice());
        domain.setCostPrice(entity.getCostPrice());
        domain.setShippingPrice(entity.getShippingPrice());
        domain.setShippingCostPrice(entity.getShippingCostPrice());
        domain.setTrackStock(entity.isTrackStock());
        domain.setStockQuantity(entity.getStockQuantity());
        domain.setLowStockThreshold(entity.getLowStockThreshold());
        domain.setStatus(entity.getStatus());
        domain.setAvailability(entity.getAvailability());
        domain.setAvailabilityText(entity.getAvailabilityText());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setUpdatedBy(entity.getUpdatedBy());
        return domain;
    }

    public static ProductJpaEntity toJpa(Product domain) {
        if (domain == null) {
            return null;
        }
        ProductJpaEntity entity = new ProductJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setSku(domain.getSku());
        entity.setDescription(domain.getDescription());
        entity.setInitialPrice(domain.getInitialPrice());
        entity.setFinalPrice(domain.getFinalPrice());
        entity.setCostPrice(domain.getCostPrice());
        entity.setShippingPrice(domain.getShippingPrice());
        entity.setShippingCostPrice(domain.getShippingCostPrice());
        entity.setTrackStock(domain.isTrackStock());
        entity.setStockQuantity(domain.getStockQuantity());
        entity.setLowStockThreshold(domain.getLowStockThreshold());
        entity.setStatus(domain.getStatus());
        entity.setAvailability(domain.getAvailability());
        entity.setAvailabilityText(domain.getAvailabilityText());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());
        return entity;
    }
}
