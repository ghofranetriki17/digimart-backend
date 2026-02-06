package com.nexashop.domain.tenant.entity;

import com.nexashop.domain.common.AuditableEntity;
import com.nexashop.domain.common.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tenant extends AuditableEntity {

    private String name;

    private String subdomain;

    private String contactEmail;

    private String contactPhone;

    private String logoUrl;

    private TenantStatus status;

    private Locale defaultLocale;

    private Long sectorId;
}
