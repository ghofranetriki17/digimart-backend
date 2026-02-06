package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.audit.entity.AuditEvent;
import com.nexashop.infrastructure.persistence.model.audit.AuditEventJpaEntity;

public final class AuditMapper {

    private AuditMapper() {
    }

    public static AuditEvent toDomain(AuditEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        AuditEvent domain = new AuditEvent();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setEntityType(entity.getEntityType());
        domain.setEntityId(entity.getEntityId());
        domain.setAction(entity.getAction());
        domain.setBeforeJson(entity.getBeforeJson());
        domain.setAfterJson(entity.getAfterJson());
        domain.setActorUserId(entity.getActorUserId());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        domain.setCorrelationId(entity.getCorrelationId());
        domain.setOccurredAt(entity.getOccurredAt());
        return domain;
    }

    public static AuditEventJpaEntity toJpa(AuditEvent domain) {
        if (domain == null) {
            return null;
        }
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(domain.getEntityId());
        entity.setAction(domain.getAction());
        entity.setBeforeJson(domain.getBeforeJson());
        entity.setAfterJson(domain.getAfterJson());
        entity.setActorUserId(domain.getActorUserId());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setCorrelationId(domain.getCorrelationId());
        entity.setOccurredAt(domain.getOccurredAt());
        return entity;
    }
}
