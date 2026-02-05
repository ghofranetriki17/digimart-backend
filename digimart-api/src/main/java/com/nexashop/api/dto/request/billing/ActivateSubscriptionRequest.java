package com.nexashop.api.dto.request.billing;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateSubscriptionRequest {

    @NotNull
    private Long planId;

    private BigDecimal pricePaid;

    private String paymentReference;
}
