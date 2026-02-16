package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.ProductOption;
import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionJpaEntity;

public final class ProductOptionMapper {

    private ProductOptionMapper() {
    }

    public static ProductOption toDomain(ProductOptionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductOption domain = new ProductOption();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setProductId(entity.getProductId());
        domain.setName(entity.getName());
        domain.setType(entity.getType());
        domain.setRequired(entity.isRequired());
        domain.setUsedForVariants(entity.isUsedForVariants());
        domain.setDisplayOrder(entity.getDisplayOrder());
        domain.setCreatedBy(entity.getCreatedBy());
        return domain;
    }

    public static ProductOptionJpaEntity toJpa(ProductOption domain) {
        if (domain == null) {
            return null;
        }
        ProductOptionJpaEntity entity = new ProductOptionJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setProductId(domain.getProductId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setRequired(domain.isRequired());
        entity.setUsedForVariants(domain.isUsedForVariants());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }
}
