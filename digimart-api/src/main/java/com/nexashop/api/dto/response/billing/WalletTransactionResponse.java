package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.WalletTxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletTransactionResponse {
    private Long id;
    private Long tenantId;
    private Long walletId;
    private WalletTxnType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reason;
    private String reference;
    private Long processedBy;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
