package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.WalletStatus;
import com.nexashop.domain.common.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "tenant_wallets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id"})
        }
)
@Getter
@Setter
public class TenantWallet extends TenantEntity {

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "TND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;
}
