package com.nexashop.application.usecase;

import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.domain.billing.entity.PlatformConfig;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PlatformConfigUseCase {

    private final PlatformConfigRepository configRepository;

    public PlatformConfigUseCase(PlatformConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<PlatformConfig> listConfigs() {
        return configRepository.findAll();
    }

    public PlatformConfig updateConfig(String configKey, String configValue, String description, Long updatedBy) {
        PlatformConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Config not found"));
        config.setConfigValue(configValue);
        config.setDescription(description);
        config.setUpdatedBy(updatedBy);
        return configRepository.save(config);
    }
}
