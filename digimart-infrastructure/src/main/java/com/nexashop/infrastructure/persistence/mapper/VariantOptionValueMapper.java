package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.catalog.entity.VariantOptionValue;
import com.nexashop.infrastructure.persistence.model.catalog.VariantOptionValueJpaEntity;

public final class VariantOptionValueMapper {

    private VariantOptionValueMapper() {
    }

    public static VariantOptionValue toDomain(VariantOptionValueJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        VariantOptionValue domain = new VariantOptionValue();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setVariantId(entity.getVariantId());
        domain.setOptionValueId(entity.getOptionValueId());
        return domain;
    }

    public static VariantOptionValueJpaEntity toJpa(VariantOptionValue domain) {
        if (domain == null) {
            return null;
        }
        VariantOptionValueJpaEntity entity = new VariantOptionValueJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setVariantId(domain.getVariantId());
        entity.setOptionValueId(domain.getOptionValueId());
        return entity;
    }
}
