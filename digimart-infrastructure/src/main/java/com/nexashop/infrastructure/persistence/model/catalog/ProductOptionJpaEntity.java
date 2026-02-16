package com.nexashop.infrastructure.persistence.model.catalog;

import com.nexashop.domain.catalog.entity.OptionType;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_options",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "product_id", "name"})
        }
)
@Getter
@Setter
public class ProductOptionJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionType type = OptionType.TEXT;

    @Column(nullable = false)
    private boolean required = false;

    @Column(name = "used_for_variants", nullable = false)
    private boolean usedForVariants = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;
}
