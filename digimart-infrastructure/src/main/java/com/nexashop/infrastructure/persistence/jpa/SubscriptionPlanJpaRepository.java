package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByCode(String code);

    List<SubscriptionPlan> findByActiveTrueOrderByNameAsc();

    List<SubscriptionPlan> findByStandardTrue();
}
