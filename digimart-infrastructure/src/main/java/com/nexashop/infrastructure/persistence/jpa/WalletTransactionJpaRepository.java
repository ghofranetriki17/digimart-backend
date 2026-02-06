package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.WalletTransactionJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionJpaEntity, Long> {
    List<WalletTransactionJpaEntity> findByWalletIdOrderByTransactionDateDesc(Long walletId);

    List<WalletTransactionJpaEntity> findByTenantIdOrderByTransactionDateDesc(Long tenantId);
}

