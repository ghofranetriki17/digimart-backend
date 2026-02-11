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
        name = "product_store_inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "product_id", "store_id"})
        }
)
@Getter
@Setter
public class ProductStoreInventoryJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "is_active_in_store", nullable = false)
    private boolean activeInStore = true;
}
