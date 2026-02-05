package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSubscriptionJpaRepository extends JpaRepository<TenantSubscription, Long> {
    List<TenantSubscription> findByTenantId(Long tenantId);

    Optional<TenantSubscription> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status);
}
