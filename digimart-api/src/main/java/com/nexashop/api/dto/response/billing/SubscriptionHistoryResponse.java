package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.SubscriptionAction;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionHistoryResponse {
    private Long id;
    private Long tenantId;
    private Long subscriptionId;
    private Long oldPlanId;
    private Long newPlanId;
    private SubscriptionAction action;
    private String notes;
    private Long performedBy;
    private LocalDateTime performedAt;
}
