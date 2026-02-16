package com.nexashop.infrastructure.persistence.model.catalog;

import com.nexashop.domain.catalog.entity.VariantStatus;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "sku"})
        }
)
@Getter
@Setter
public class ProductVariantJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column
    private String sku;

    @Column(name = "price_override")
    private BigDecimal priceOverride;

    @Column(name = "initial_price_override")
    private BigDecimal initialPriceOverride;

    @Column(name = "final_price_override")
    private BigDecimal finalPriceOverride;

    @Column(name = "cost_price_override")
    private BigDecimal costPriceOverride;

    @Column(name = "shipping_price_override")
    private BigDecimal shippingPriceOverride;

    @Column(name = "shipping_cost_price_override")
    private BigDecimal shippingCostPriceOverride;

    @Column(name = "track_stock", nullable = false)
    private boolean trackStock = false;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariantStatus status = VariantStatus.ACTIVE;

    @Column(name = "is_default", nullable = false)
    private boolean defaultVariant = false;

    @Column(name = "continue_selling_override")
    private Boolean continueSellingOverride;

    @Column(name = "product_image_id")
    private Long productImageId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
