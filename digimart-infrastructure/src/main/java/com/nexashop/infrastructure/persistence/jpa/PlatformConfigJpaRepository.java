package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.billing.entity.PlatformConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformConfigJpaRepository extends JpaRepository<PlatformConfig, Long> {
    Optional<PlatformConfig> findByConfigKey(String configKey);
}
