package com.nexashop.api.dto.response.product;

import com.nexashop.domain.catalog.entity.VariantStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductVariantResponse {

    private Long id;
    private Long productId;
    private String sku;
    private BigDecimal priceOverride;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private VariantStatus status;
    private boolean isDefault;
    private Boolean continueSellingOverride;
    private Long productImageId;
    private String productImageUrl;
    private List<Long> optionValueIds;
    private String displayName;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
