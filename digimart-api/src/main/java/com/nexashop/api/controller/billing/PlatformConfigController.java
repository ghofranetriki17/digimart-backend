package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.UpdateConfigRequest;
import com.nexashop.api.dto.response.billing.PlatformConfigResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.application.port.out.PlatformConfigRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/platform-config")
public class PlatformConfigController {

    private final PlatformConfigRepository configRepository;

    public PlatformConfigController(PlatformConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping
    public List<PlatformConfigResponse> listConfigs() {
        SecurityContextUtil.requireSuperAdmin();
        return configRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{configKey}")
    public PlatformConfigResponse updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateConfigRequest request
    ) {
        SecurityContextUtil.requireSuperAdmin();
        PlatformConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Config not found"));
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(SecurityContextUtil.requireUser().getUserId());
        return toResponse(configRepository.save(config));
    }

    private PlatformConfigResponse toResponse(PlatformConfig config) {
        return PlatformConfigResponse.builder()
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy())
                .build();
    }
}


