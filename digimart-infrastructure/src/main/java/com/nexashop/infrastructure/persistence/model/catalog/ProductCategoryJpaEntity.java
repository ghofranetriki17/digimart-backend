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
        name = "product_categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "product_id", "category_id"})
        }
)
@Getter
@Setter
public class ProductCategoryJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;
}
