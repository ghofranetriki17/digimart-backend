package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.PremiumFeature;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumFeatureJpaRepository extends JpaRepository<PremiumFeature, Long> {
    Optional<PremiumFeature> findByCode(String code);

    List<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc();
}
