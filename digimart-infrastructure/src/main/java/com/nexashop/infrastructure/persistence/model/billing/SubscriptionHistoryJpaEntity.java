package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_history")
@Getter
@Setter
public class SubscriptionHistoryJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "old_plan_id")
    private Long oldPlanId;

    @Column(name = "new_plan_id", nullable = false)
    private Long newPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionAction action;

    private String notes;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt = LocalDateTime.now();
}
