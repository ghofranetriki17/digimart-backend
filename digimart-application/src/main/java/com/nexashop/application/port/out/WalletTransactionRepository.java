package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.WalletTransaction;
import java.util.List;

public interface WalletTransactionRepository extends CrudRepositoryPort<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByTransactionDateDesc(Long walletId);

    List<WalletTransaction> findByTenantIdOrderByTransactionDateDesc(Long tenantId);
}
