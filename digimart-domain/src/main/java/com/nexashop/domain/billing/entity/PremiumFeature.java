package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.FeatureCategory;
import com.nexashop.domain.common.AuditableEntity;
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
        name = "premium_features",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"})
        }
)
@Getter
@Setter
public class PremiumFeature extends AuditableEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeatureCategory category;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}
