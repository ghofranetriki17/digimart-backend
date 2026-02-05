package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.SubscriptionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryJpaRepository extends JpaRepository<SubscriptionHistory, Long> {
    List<SubscriptionHistory> findBySubscriptionIdOrderByPerformedAtDesc(Long subscriptionId);

    List<SubscriptionHistory> findByTenantIdOrderByPerformedAtDesc(Long tenantId);
}
