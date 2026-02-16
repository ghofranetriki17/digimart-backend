package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariant extends TenantEntity {

    private Long productId;

    private String sku;

    private BigDecimal priceOverride;

    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private VariantStatus status = VariantStatus.ACTIVE;

    private boolean defaultVariant = false;

    private Boolean continueSellingOverride;

    private Long productImageId;

    private Long createdBy;

    private Long updatedBy;
}
