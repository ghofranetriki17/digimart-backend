package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.SubscriptionHistory;
import java.util.List;

public interface SubscriptionHistoryRepository extends CrudRepositoryPort<SubscriptionHistory, Long> {

    List<SubscriptionHistory> findBySubscriptionIdOrderByPerformedAtDesc(Long subscriptionId);

    List<SubscriptionHistory> findByTenantIdOrderByPerformedAtDesc(Long tenantId);
}
