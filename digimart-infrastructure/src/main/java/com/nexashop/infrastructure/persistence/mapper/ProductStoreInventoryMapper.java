package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductStoreInventory;
import com.nexashop.infrastructure.persistence.model.catalog.ProductStoreInventoryJpaEntity;

public final class ProductStoreInventoryMapper {

    private ProductStoreInventoryMapper() {
    }

    public static ProductStoreInventory toDomain(ProductStoreInventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductStoreInventory domain = new ProductStoreInventory();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setStoreId(entity.getStoreId());
        domain.setQuantity(entity.getQuantity());
        domain.setLowStockThreshold(entity.getLowStockThreshold());
        domain.setActiveInStore(entity.isActiveInStore());
        return domain;
    }

    public static ProductStoreInventoryJpaEntity toJpa(ProductStoreInventory domain) {
        if (domain == null) {
            return null;
        }
        ProductStoreInventoryJpaEntity entity = new ProductStoreInventoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setStoreId(domain.getStoreId());
        entity.setQuantity(domain.getQuantity());
        entity.setLowStockThreshold(domain.getLowStockThreshold());
        entity.setActiveInStore(domain.isActiveInStore());
        return entity;
    }
}
