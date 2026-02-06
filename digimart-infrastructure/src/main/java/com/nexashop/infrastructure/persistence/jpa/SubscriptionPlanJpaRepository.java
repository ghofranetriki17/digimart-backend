package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionPlanJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlanJpaEntity, Long> {
    Optional<SubscriptionPlanJpaEntity> findByCode(String code);

    List<SubscriptionPlanJpaEntity> findByActiveTrueOrderByNameAsc();

    List<SubscriptionPlanJpaEntity> findByStandardTrue();
}

