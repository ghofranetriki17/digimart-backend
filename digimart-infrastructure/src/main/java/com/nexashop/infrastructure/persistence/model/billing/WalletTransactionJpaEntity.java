package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.domain.billing.enums.WalletTxnType;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
public class WalletTransactionJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTxnType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    private String reference;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
}
