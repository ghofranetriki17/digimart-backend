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
    private BigDecimal initialPriceOverride;
    private BigDecimal finalPriceOverride;
    private BigDecimal costPriceOverride;
    private BigDecimal shippingPriceOverride;
    private BigDecimal shippingCostPriceOverride;
    private boolean trackStock;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private VariantStatus status;
    private boolean isDefault;
    private Boolean continueSellingOverride;
    private Long productImageId;
    private String productImageUrl;
    private Integer productImageFocusX;
    private Integer productImageFocusY;
    private List<Long> optionValueIds;
    private List<VariantInventoryResponse> inventories;
    private String displayName;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
