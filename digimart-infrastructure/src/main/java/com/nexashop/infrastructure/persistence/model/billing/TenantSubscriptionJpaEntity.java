package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "tenant_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "status"})
        }
)
@Getter
@Setter
public class TenantSubscriptionJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING_ACTIVATION;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "price_paid")
    private BigDecimal pricePaid;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "activated_by")
    private Long activatedBy;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}
