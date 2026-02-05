package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.PlanFeature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanFeatureJpaRepository extends JpaRepository<PlanFeature, Long> {
    List<PlanFeature> findByPlanId(Long planId);
}
