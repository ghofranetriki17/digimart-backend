package com.nexashop.api.dto.request.billing;

import com.nexashop.domain.billing.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlanRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    private String currency = "TND";

    @NotNull
    private BillingCycle billingCycle;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    private boolean standard = false;

    private boolean active = true;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Size(max = 50)
    private List<Long> featureIds;
}
