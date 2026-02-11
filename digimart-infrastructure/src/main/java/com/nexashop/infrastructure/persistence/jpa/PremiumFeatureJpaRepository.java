package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.PremiumFeatureJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PremiumFeatureJpaRepository extends JpaRepository<PremiumFeatureJpaEntity, Long> {
    Optional<PremiumFeatureJpaEntity> findByCode(String code);

    List<PremiumFeatureJpaEntity> findByActiveTrueOrderByDisplayOrderAsc();

    Page<PremiumFeatureJpaEntity> findByActiveTrueOrderByDisplayOrderAsc(Pageable pageable);
}

