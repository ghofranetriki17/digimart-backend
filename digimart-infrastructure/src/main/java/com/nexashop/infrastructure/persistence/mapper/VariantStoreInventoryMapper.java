package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.VariantStoreInventory;
import com.nexashop.infrastructure.persistence.model.catalog.VariantStoreInventoryJpaEntity;

public final class VariantStoreInventoryMapper {

    private VariantStoreInventoryMapper() {
    }

    public static VariantStoreInventory toDomain(VariantStoreInventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        VariantStoreInventory domain = new VariantStoreInventory();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setVariantId(entity.getVariantId());
        domain.setStoreId(entity.getStoreId());
        domain.setQuantity(entity.getQuantity());
        domain.setLowStockThreshold(entity.getLowStockThreshold());
        domain.setActiveInStore(entity.isActiveInStore());
        return domain;
    }

    public static VariantStoreInventoryJpaEntity toJpa(VariantStoreInventory domain) {
        if (domain == null) {
            return null;
        }
        VariantStoreInventoryJpaEntity entity = new VariantStoreInventoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setVariantId(domain.getVariantId());
        entity.setStoreId(domain.getStoreId());
        entity.setQuantity(domain.getQuantity());
        entity.setLowStockThreshold(domain.getLowStockThreshold());
        entity.setActiveInStore(domain.isActiveInStore());
        return entity;
    }
}
