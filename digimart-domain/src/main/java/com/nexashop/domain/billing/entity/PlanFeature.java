package com.nexashop.domain.billing.entity;

import com.nexashop.domain.common.AuditableEntity;
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
public class PlanFeature extends AuditableEntity {

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "feature_id", nullable = false)
    private Long featureId;
}
