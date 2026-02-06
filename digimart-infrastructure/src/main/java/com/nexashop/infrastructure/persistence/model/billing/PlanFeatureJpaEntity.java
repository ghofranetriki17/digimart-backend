package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.infrastructure.persistence.model.common.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "plan_features",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"plan_id", "feature_id"})
        }
)
@Getter
@Setter
public class PlanFeatureJpaEntity extends AuditableJpaEntity {

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "feature_id", nullable = false)
    private Long featureId;
}
