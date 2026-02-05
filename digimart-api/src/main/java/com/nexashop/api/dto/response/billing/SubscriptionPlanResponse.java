package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.BillingCycle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPlanResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private BillingCycle billingCycle;
    private BigDecimal discountPercentage;
    private boolean standard;
    private boolean active;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PremiumFeatureResponse> features;
}
