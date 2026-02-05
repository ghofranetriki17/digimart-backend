package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.TenantWallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantWalletJpaRepository extends JpaRepository<TenantWallet, Long> {
    Optional<TenantWallet> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}
