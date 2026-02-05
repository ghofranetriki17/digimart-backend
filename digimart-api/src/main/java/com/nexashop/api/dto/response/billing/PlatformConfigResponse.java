package com.nexashop.api.dto.response.billing;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformConfigResponse {
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
