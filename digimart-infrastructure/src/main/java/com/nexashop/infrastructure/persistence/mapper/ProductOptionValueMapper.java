package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductOptionValue;
import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionValueJpaEntity;

public final class ProductOptionValueMapper {

    private ProductOptionValueMapper() {
    }

    public static ProductOptionValue toDomain(ProductOptionValueJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductOptionValue domain = new ProductOptionValue();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setOptionId(entity.getOptionId());
        domain.setValue(entity.getValue());
        domain.setHexColor(entity.getHexColor());
        domain.setDisplayOrder(entity.getDisplayOrder());
        domain.setCreatedBy(entity.getCreatedBy());
        return domain;
    }

    public static ProductOptionValueJpaEntity toJpa(ProductOptionValue domain) {
        if (domain == null) {
            return null;
        }
        ProductOptionValueJpaEntity entity = new ProductOptionValueJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setOptionId(domain.getOptionId());
        entity.setValue(domain.getValue());
        entity.setHexColor(domain.getHexColor());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }
}
