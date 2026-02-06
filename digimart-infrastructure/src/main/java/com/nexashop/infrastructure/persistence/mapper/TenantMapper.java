package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.infrastructure.persistence.model.tenant.ActivitySectorJpaEntity;
import com.nexashop.infrastructure.persistence.model.tenant.TenantJpaEntity;

public final class TenantMapper {

    private TenantMapper() {
    }

    public static ActivitySector toDomain(ActivitySectorJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ActivitySector domain = new ActivitySector();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setLabel(entity.getLabel());
        domain.setDescription(entity.getDescription());
        domain.setActive(entity.isActive());
        return domain;
    }

    public static ActivitySectorJpaEntity toJpa(ActivitySector domain) {
        if (domain == null) {
            return null;
        }
        ActivitySectorJpaEntity entity = new ActivitySectorJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setLabel(domain.getLabel());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        return entity;
    }

    public static Tenant toDomain(TenantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Tenant domain = new Tenant();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setName(entity.getName());
        domain.setSubdomain(entity.getSubdomain());
        domain.setContactEmail(entity.getContactEmail());
        domain.setContactPhone(entity.getContactPhone());
        domain.setLogoUrl(entity.getLogoUrl());
        domain.setStatus(entity.getStatus());
        domain.setDefaultLocale(entity.getDefaultLocale());
        domain.setSectorId(entity.getSectorId());
        return domain;
    }

    public static TenantJpaEntity toJpa(Tenant domain) {
        if (domain == null) {
            return null;
        }
        TenantJpaEntity entity = new TenantJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setName(domain.getName());
        entity.setSubdomain(domain.getSubdomain());
        entity.setContactEmail(domain.getContactEmail());
        entity.setContactPhone(domain.getContactPhone());
        entity.setLogoUrl(domain.getLogoUrl());
        entity.setStatus(domain.getStatus());
        entity.setDefaultLocale(domain.getDefaultLocale());
        entity.setSectorId(domain.getSectorId());
        return entity;
    }
}
