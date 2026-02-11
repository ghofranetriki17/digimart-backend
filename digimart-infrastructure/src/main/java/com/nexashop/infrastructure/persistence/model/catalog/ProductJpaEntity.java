package com.nexashop.infrastructure.persistence.model.catalog;

import com.nexashop.domain.catalog.entity.ProductAvailability;
import com.nexashop.domain.catalog.entity.ProductStatus;
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
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "slug"}),
                @UniqueConstraint(columnNames = {"tenant_id", "sku"})
        }
)
@Getter
@Setter
public class ProductJpaEntity extends TenantScopedJpaEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column
    private String sku;

    @Column
    private String description;

    @Column
    private BigDecimal initialPrice;

    @Column
    private BigDecimal finalPrice;

    @Column
    private BigDecimal costPrice;

    @Column
    private BigDecimal shippingPrice;

    @Column
    private BigDecimal shippingCostPrice;

    @Column(nullable = false)
    private boolean trackStock = false;

    @Column
    private Integer stockQuantity;

    @Column
    private Integer lowStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductAvailability availability = ProductAvailability.IN_STOCK;

    @Column
    private String availabilityText;

    @Column(name = "show_lowest_price", nullable = false)
    private boolean showLowestPrice = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
