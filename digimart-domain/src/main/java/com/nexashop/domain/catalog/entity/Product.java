package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product extends TenantEntity {

    private String name;

    private String slug;

    private String sku;

    private String description;

    private BigDecimal initialPrice;

    private BigDecimal finalPrice;

    private BigDecimal costPrice;

    private BigDecimal shippingPrice;

    private BigDecimal shippingCostPrice;

    private boolean trackStock = false;

    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private ProductStatus status = ProductStatus.ACTIVE;

    private ProductAvailability availability = ProductAvailability.IN_STOCK;

    private String availabilityText;

    private boolean showLowestPrice = false;

    private Long createdBy;

    private Long updatedBy;
}
