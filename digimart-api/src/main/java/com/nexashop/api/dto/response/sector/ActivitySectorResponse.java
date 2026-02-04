package com.nexashop.api.dto.response.sector;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivitySectorResponse {

    private Long id;
    private String label;
    private String description;
    private boolean active;
    private long tenantCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
