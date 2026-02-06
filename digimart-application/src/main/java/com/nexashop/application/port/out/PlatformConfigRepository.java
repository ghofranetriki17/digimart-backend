package com.nexashop.application.port.out;

import com.nexashop.domain.billing.entity.PlatformConfig;
import java.util.Optional;

public interface PlatformConfigRepository extends CrudRepositoryPort<PlatformConfig, Long> {

    Optional<PlatformConfig> findByConfigKey(String configKey);
}
