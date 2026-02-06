package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import java.util.List;
import java.util.Optional;

public interface TenantSubscriptionRepository extends CrudRepositoryPort<TenantSubscription, Long> {

    List<TenantSubscription> findByTenantId(Long tenantId);

    Optional<TenantSubscription> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status);

    <S extends TenantSubscription> S saveAndFlush(S entity);
}
