package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.WalletTxnType;
import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletTransaction extends TenantEntity {

    private Long walletId;

    private WalletTxnType type;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String reason;

    private String reference;

    private Long processedBy;

    private LocalDateTime transactionDate = LocalDateTime.now();
}
