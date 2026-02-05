package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantSubscriptionResponse {
    private Long id;
    private Long tenantId;
    private Long planId;
    private String planCode;
    private String planName;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextBillingDate;
    private BigDecimal pricePaid;
    private String paymentReference;
    private Long activatedBy;
    private LocalDateTime activatedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
