package com.nexashop.domain.tenant.entity;

import com.nexashop.domain.common.AuditableEntity;
import com.nexashop.domain.common.Locale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subdomain"})
        }
)
@Getter
@Setter
public class Tenant extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String subdomain;

    private String contactEmail;

    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Locale defaultLocale;
}
