package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.common.AuditableEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPlan extends AuditableEntity {

    private String code;

    private String name;

    private String description;

    private BigDecimal price = BigDecimal.ZERO;

    private String currency = "TND";

    private BillingCycle billingCycle;

    private BigDecimal discountPercentage = BigDecimal.ZERO;

    private boolean standard = false;

    private boolean active = true;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long createdBy;
}
