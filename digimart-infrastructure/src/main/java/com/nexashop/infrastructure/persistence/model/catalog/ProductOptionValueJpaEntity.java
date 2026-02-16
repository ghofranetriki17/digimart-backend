package com.nexashop.infrastructure.persistence.model.catalog;

import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_option_values",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "option_id", "value"})
        }
)
@Getter
@Setter
public class ProductOptionValueJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(nullable = false)
    private String value;

    @Column(name = "hex_color")
    private String hexColor;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;
}
