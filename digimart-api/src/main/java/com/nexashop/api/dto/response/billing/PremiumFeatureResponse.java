package com.nexashop.api.dto.response.billing;

import com.nexashop.domain.billing.enums.FeatureCategory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PremiumFeatureResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private FeatureCategory category;
    private boolean active;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
