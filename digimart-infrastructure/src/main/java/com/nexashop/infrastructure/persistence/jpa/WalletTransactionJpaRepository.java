package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.WalletTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletIdOrderByTransactionDateDesc(Long walletId);

    List<WalletTransaction> findByTenantIdOrderByTransactionDateDesc(Long tenantId);
}
