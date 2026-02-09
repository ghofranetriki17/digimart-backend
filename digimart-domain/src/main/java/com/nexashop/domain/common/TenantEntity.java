package com.nexashop.domain.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TenantEntity extends AuditableEntity {

    private Long tenantId;
}
