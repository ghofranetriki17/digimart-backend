package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.UpdateConfigRequest;
import com.nexashop.api.dto.response.billing.PlatformConfigResponse;
import com.nexashop.api.util.UploadUtil;
import com.nexashop.application.usecase.PlatformConfigUseCase;
import com.nexashop.domain.billing.entity.PlatformConfig;
import java.io.IOException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/platform-config")
public class PlatformConfigController {

    private final PlatformConfigUseCase configUseCase;
    private final String uploadBaseDir;

    public PlatformConfigController(
            PlatformConfigUseCase configUseCase,
            @Value("${app.upload.dir:}") String uploadBaseDir
    ) {
        this.configUseCase = configUseCase;
        this.uploadBaseDir = uploadBaseDir;
    }

    @GetMapping
    public List<PlatformConfigResponse> listConfigs() {
        return configUseCase.listConfigs().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{configKey}")
    public PlatformConfigResponse updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateConfigRequest request
    ) {
        PlatformConfig config = configUseCase.updateConfig(
                configKey,
                request.getConfigValue(),
                request.getDescription()
        );
        return toResponse(config);
    }

    @PostMapping(value = "/watermark-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PlatformConfigResponse uploadPlatformWatermarkLogo(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadUtil.StoredFile stored = UploadUtil.storeImage(file, uploadBaseDir, "platform");
        configUseCase.updateConfig(
                "PLATFORM_WATERMARK_KIND",
                "IMAGE",
                "platform watermark kind"
        );
        PlatformConfig config = configUseCase.updateConfig(
                "PLATFORM_WATERMARK_LOGO_URL",
                stored.relativeUrl(),
                "platform watermark logo url"
        );
        return toResponse(config);
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PlatformConfigResponse uploadPlatformLogo(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadUtil.StoredFile stored = UploadUtil.storeImage(file, uploadBaseDir, "platform");
        PlatformConfig config = configUseCase.updateConfig(
                "PLATFORM_LOGO_URL",
                stored.relativeUrl(),
                "platform logo url"
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
