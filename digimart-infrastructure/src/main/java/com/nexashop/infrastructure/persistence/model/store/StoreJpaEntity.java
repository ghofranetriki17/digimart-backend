package com.nexashop.infrastructure.persistence.model.store;

import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "stores",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "code"})
        }
)
@Getter
@Setter
public class StoreJpaEntity extends TenantScopedJpaEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    private String phone;

    private String email;

    private String imageUrl;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean active = true;
}
