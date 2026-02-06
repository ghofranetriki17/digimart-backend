package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.infrastructure.persistence.model.common.AuditableJpaEntity;
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
        name = "subscription_plans",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"})
        }
)
@Getter
@Setter
public class SubscriptionPlanJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "TND";

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "is_standard", nullable = false)
    private boolean standard = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_by")
    private Long createdBy;
}
