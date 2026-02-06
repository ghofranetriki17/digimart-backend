package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionHistoryJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SubscriptionHistoryJpaRepository extends JpaRepository<SubscriptionHistoryJpaEntity, Long> {
    List<SubscriptionHistoryJpaEntity> findBySubscriptionIdOrderByPerformedAtDesc(Long subscriptionId);

    List<SubscriptionHistoryJpaEntity> findByTenantIdOrderByPerformedAtDesc(Long tenantId);
}

