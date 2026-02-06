package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.SubscriptionPlan;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends CrudRepositoryPort<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    List<SubscriptionPlan> findByActiveTrueOrderByNameAsc();

    List<SubscriptionPlan> findByStandardTrue();
}
