package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.TenantWallet;
import java.util.Optional;

public interface TenantWalletRepository extends CrudRepositoryPort<TenantWallet, Long> {

    Optional<TenantWallet> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}
