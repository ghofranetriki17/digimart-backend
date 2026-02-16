package com.nexashop.api.dto.request.product;

import com.nexashop.domain.catalog.entity.VariantStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantRequest {

    private String sku;

    private BigDecimal priceOverride;

    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private VariantStatus status;

    private Boolean isDefault;

    private Boolean continueSellingOverride;

    private Long productImageId;

    private List<Long> optionValueIds;
}
