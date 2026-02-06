package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.billing.PlatformConfigJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PlatformConfigJpaRepository extends JpaRepository<PlatformConfigJpaEntity, Long> {
    Optional<PlatformConfigJpaEntity> findByConfigKey(String configKey);
}

