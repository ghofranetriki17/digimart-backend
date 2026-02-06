package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.UpdateConfigRequest;
import com.nexashop.api.dto.response.billing.PlatformConfigResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.application.usecase.PlatformConfigUseCase;
import com.nexashop.domain.billing.entity.PlatformConfig;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-config")
public class PlatformConfigController {

    private final PlatformConfigUseCase configUseCase;

    public PlatformConfigController(PlatformConfigUseCase configUseCase) {
        this.configUseCase = configUseCase;
    }

    @GetMapping
    public List<PlatformConfigResponse> listConfigs() {
        SecurityContextUtil.requireSuperAdmin();
        return configUseCase.listConfigs().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{configKey}")
    public PlatformConfigResponse updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateConfigRequest request
    ) {
        SecurityContextUtil.requireSuperAdmin();
        PlatformConfig config = configUseCase.updateConfig(
                configKey,
                request.getConfigValue(),
                request.getDescription(),
                SecurityContextUtil.requireUser().getUserId()
        );
        return toResponse(config);
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
