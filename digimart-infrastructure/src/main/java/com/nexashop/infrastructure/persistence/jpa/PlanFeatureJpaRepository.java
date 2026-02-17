package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.PlanFeatureJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PlanFeatureJpaRepository extends JpaRepository<PlanFeatureJpaEntity, Long> {
    List<PlanFeatureJpaEntity> findByPlanId(Long planId);

    boolean existsByFeatureId(Long featureId);
}

