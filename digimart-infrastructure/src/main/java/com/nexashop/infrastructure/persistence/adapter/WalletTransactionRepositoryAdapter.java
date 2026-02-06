package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.WalletTransactionRepository;
import com.nexashop.domain.billing.entity.WalletTransaction;
import com.nexashop.infrastructure.persistence.jpa.WalletTransactionJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.WalletTransactionJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class WalletTransactionRepositoryAdapter
        extends JpaRepositoryAdapter<WalletTransaction, WalletTransactionJpaEntity, Long>
        implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository repository;

    public WalletTransactionRepositoryAdapter(WalletTransactionJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected WalletTransactionJpaEntity toJpa(WalletTransaction domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected WalletTransaction toDomain(WalletTransactionJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public List<WalletTransaction> findByWalletIdOrderByTransactionDateDesc(Long walletId) {
        return toDomainList(repository.findByWalletIdOrderByTransactionDateDesc(walletId));
    }

    @Override
    public List<WalletTransaction> findByTenantIdOrderByTransactionDateDesc(Long tenantId) {
        return toDomainList(repository.findByTenantIdOrderByTransactionDateDesc(tenantId));
    }
}
