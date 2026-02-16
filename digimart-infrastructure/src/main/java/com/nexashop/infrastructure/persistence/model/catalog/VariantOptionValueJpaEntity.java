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
        name = "variant_option_values",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "variant_id", "option_value_id"})
        }
)
@Getter
@Setter
public class VariantOptionValueJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "option_value_id", nullable = false)
    private Long optionValueId;
}
