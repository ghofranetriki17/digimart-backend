package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductPriceHistory extends TenantEntity {

    private Long productId;

    private BigDecimal initialPrice;

    private BigDecimal finalPrice;

    private LocalDateTime changedAt;

    private Long changedBy;
}
