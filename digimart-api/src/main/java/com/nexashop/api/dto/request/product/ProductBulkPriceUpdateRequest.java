package com.nexashop.api.dto.request.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductBulkPriceUpdateRequest {

    @NotEmpty
    private List<Long> productIds;

    @PositiveOrZero
    private BigDecimal initialPrice;

    @PositiveOrZero
    private BigDecimal finalPrice;

    @PositiveOrZero
    private BigDecimal shippingPrice;

    @PositiveOrZero
    private BigDecimal shippingCostPrice;
}
