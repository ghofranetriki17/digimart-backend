package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.domain.common.TenantEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionHistory extends TenantEntity {

    private Long subscriptionId;

    private Long oldPlanId;

    private Long newPlanId;

    private SubscriptionAction action;

    private String notes;

    private Long performedBy;

    private LocalDateTime performedAt = LocalDateTime.now();
}
