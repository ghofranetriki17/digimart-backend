package com.nexashop.api.dto.request.billing;

import com.nexashop.domain.billing.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePlanRequest {

    private String name;
    private String description;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    private String currency;

    private BillingCycle billingCycle;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal discountPercentage;

    private Boolean active;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Size(max = 50)
    private List<Long> featureIds;
}
