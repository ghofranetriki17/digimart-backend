package com.nexashop.api.dto.request.product;

import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

    private Long tenantId;

    @NotBlank
    private String name;

    private String slug;

    private String sku;

    private String description;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialPrice;

    @PositiveOrZero
    private BigDecimal finalPrice;

    @PositiveOrZero
    private BigDecimal costPrice;

    @NotNull
    @PositiveOrZero
    private BigDecimal shippingPrice;

    @PositiveOrZero
    private BigDecimal shippingCostPrice;

    private Boolean trackStock;

    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private ProductStatus status;

    private ProductAvailability availability;

    private String availabilityText;

    private Boolean showLowestPrice;

    private List<Long> categoryIds;

    private Long primaryCategoryId;

    @Valid
    private List<ProductInventoryRequest> inventories;
}
