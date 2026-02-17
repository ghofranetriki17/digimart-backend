package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.infrastructure.persistence.model.billing.TenantSubscriptionJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantSubscriptionJpaRepository extends JpaRepository<TenantSubscriptionJpaEntity, Long> {
    List<TenantSubscriptionJpaEntity> findByTenantId(Long tenantId);

    boolean existsByPlanId(Long planId);

    long countByPlanId(Long planId);

    Optional<TenantSubscriptionJpaEntity> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status);
}

