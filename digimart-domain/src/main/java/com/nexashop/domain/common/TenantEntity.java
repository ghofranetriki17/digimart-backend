package com.nexashop.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends AuditableEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
}
