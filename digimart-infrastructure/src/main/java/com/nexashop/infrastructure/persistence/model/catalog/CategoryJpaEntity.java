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
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "slug"})
        }
)
@Getter
@Setter
public class CategoryJpaEntity extends TenantScopedJpaEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column
    private String description;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
