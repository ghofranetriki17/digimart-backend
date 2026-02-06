package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.PlanFeature;
import java.util.List;

public interface PlanFeatureRepository extends CrudRepositoryPort<PlanFeature, Long> {

    List<PlanFeature> findByPlanId(Long planId);
}
