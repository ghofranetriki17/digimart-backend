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
        name = "variant_store_inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "variant_id", "store_id"})
        }
)
@Getter
@Setter
public class VariantStoreInventoryJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "is_active_in_store", nullable = false)
    private boolean activeInStore = true;
}
