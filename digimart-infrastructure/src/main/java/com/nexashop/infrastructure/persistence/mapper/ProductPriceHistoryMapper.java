package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import com.nexashop.infrastructure.persistence.model.catalog.ProductPriceHistoryJpaEntity;

public final class ProductPriceHistoryMapper {

    private ProductPriceHistoryMapper() {
    }

    public static ProductPriceHistory toDomain(ProductPriceHistoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductPriceHistory domain = new ProductPriceHistory();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setInitialPrice(entity.getInitialPrice());
        domain.setFinalPrice(entity.getFinalPrice());
        domain.setChangedAt(entity.getChangedAt());
        domain.setChangedBy(entity.getChangedBy());
        return domain;
    }

    public static ProductPriceHistoryJpaEntity toJpa(ProductPriceHistory domain) {
        if (domain == null) {
            return null;
        }
        ProductPriceHistoryJpaEntity entity = new ProductPriceHistoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setInitialPrice(domain.getInitialPrice());
        entity.setFinalPrice(domain.getFinalPrice());
        entity.setChangedAt(domain.getChangedAt());
        entity.setChangedBy(domain.getChangedBy());
        return entity;
    }
}
