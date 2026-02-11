package com.nexashop.api.dto.response.product;

import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal initialPrice;
    private BigDecimal finalPrice;
    private BigDecimal costPrice;
    private BigDecimal shippingPrice;
    private BigDecimal shippingCostPrice;
    private boolean trackStock;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private ProductStatus status;
    private ProductAvailability availability;
    private String availabilityText;
    private String imageUrl;
    private List<ProductStoreRefResponse> stores;
    private List<String> storeNames;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
