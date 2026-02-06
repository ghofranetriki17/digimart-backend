package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.common.AuditableEntity;
import com.nexashop.domain.common.BaseEntity;
import com.nexashop.domain.common.TenantEntity;
import com.nexashop.infrastructure.persistence.model.common.AuditableJpaEntity;
import com.nexashop.infrastructure.persistence.model.common.BaseJpaEntity;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;

public final class MapperUtils {

    private MapperUtils() {
    }

    public static void mapBaseToJpa(BaseEntity domain, BaseJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        entity.setId(domain.getId());
    }

    public static void mapBaseToDomain(BaseJpaEntity entity, BaseEntity domain) {
        if (domain == null || entity == null) {
            return;
        }
        domain.setId(entity.getId());
    }

    public static void mapAuditableToJpa(AuditableEntity domain, AuditableJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        mapBaseToJpa(domain, entity);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }

    public static void mapAuditableToDomain(AuditableJpaEntity entity, AuditableEntity domain) {
        if (domain == null || entity == null) {
            return;
        }
        mapBaseToDomain(entity, domain);
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
    }

    public static void mapTenantToJpa(TenantEntity domain, TenantScopedJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        mapAuditableToJpa(domain, entity);
        entity.setTenantId(domain.getTenantId());
    }

    public static void mapTenantToDomain(TenantScopedJpaEntity entity, TenantEntity domain) {
        if (domain == null || entity == null) {
            return;
        }
        mapAuditableToDomain(entity, domain);
        domain.setTenantId(entity.getTenantId());
    }
}
