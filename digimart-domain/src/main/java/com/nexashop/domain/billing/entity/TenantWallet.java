package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.WalletStatus;
import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantWallet extends TenantEntity {

    private BigDecimal balance = BigDecimal.ZERO;

    private String currency = "TND";

    private WalletStatus status = WalletStatus.ACTIVE;

    private LocalDateTime lastTransactionAt;
}
