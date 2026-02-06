package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.TenantWalletJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TenantWalletJpaRepository extends JpaRepository<TenantWalletJpaEntity, Long> {
    Optional<TenantWalletJpaEntity> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}

