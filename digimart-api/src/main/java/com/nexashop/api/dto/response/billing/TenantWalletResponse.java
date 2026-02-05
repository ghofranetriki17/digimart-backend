package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.WalletStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantWalletResponse {
    private Long id;
    private Long tenantId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private LocalDateTime lastTransactionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
