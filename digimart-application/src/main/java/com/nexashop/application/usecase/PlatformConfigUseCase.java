package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.PlatformConfig;
import java.util.List;


public class PlatformConfigUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final PlatformConfigRepository configRepository;

    public PlatformConfigUseCase(CurrentUserProvider currentUserProvider, PlatformConfigRepository configRepository) {
        this.currentUserProvider = currentUserProvider;
        this.configRepository = configRepository;
    }

    public List<PlatformConfig> listConfigs() {
        currentUserProvider.requireSuperAdmin();
        return configRepository.findAll();
    }

    public PlatformConfig updateConfig(String configKey, String configValue, String description) {
        currentUserProvider.requireSuperAdmin();
        CurrentUser currentUser = currentUserProvider.requireUser();
        PlatformConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new NotFoundException("Config not found"));
        config.setConfigValue(configValue);
        config.setDescription(description);
        config.setUpdatedBy(currentUser.userId());
        return configRepository.save(config);
    }
}


