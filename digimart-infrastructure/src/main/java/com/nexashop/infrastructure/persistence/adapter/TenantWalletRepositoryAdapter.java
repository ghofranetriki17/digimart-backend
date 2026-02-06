package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.TenantWalletRepository;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.infrastructure.persistence.jpa.TenantWalletJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.TenantWalletJpaEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TenantWalletRepositoryAdapter
        extends JpaRepositoryAdapter<TenantWallet, TenantWalletJpaEntity, Long>
        implements TenantWalletRepository {

    private final TenantWalletJpaRepository repository;

    public TenantWalletRepositoryAdapter(TenantWalletJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected TenantWalletJpaEntity toJpa(TenantWallet domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected TenantWallet toDomain(TenantWalletJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public Optional<TenantWallet> findByTenantId(Long tenantId) {
        return repository.findByTenantId(tenantId).map(BillingMapper::toDomain);
    }

    @Override
    public boolean existsByTenantId(Long tenantId) {
        return repository.existsByTenantId(tenantId);
    }
}
