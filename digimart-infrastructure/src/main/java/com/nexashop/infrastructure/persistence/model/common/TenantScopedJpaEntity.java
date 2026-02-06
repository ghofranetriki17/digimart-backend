package com.nexashop.infrastructure.persistence.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantScopedJpaEntity extends AuditableJpaEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
}
