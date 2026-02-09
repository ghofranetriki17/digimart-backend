package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantSubscription extends TenantEntity {

    private Long planId;

    private SubscriptionStatus status = SubscriptionStatus.PENDING_ACTIVATION;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime nextBillingDate;

    private BigDecimal pricePaid;

    private String paymentReference;

    private Long activatedBy;

    private LocalDateTime activatedAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;
}
