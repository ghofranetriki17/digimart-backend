package com.nexashop.api.dto.response.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPriceHistoryResponse {

    private BigDecimal initialPrice;
    private BigDecimal finalPrice;
    private LocalDateTime changedAt;
    private Long changedBy;
}
