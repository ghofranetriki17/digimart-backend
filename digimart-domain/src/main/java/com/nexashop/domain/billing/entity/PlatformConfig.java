package com.nexashop.domain.billing.entity;

import com.nexashop.domain.common.AuditableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformConfig extends AuditableEntity {

    private String configKey;

    private String configValue;

    private String description;

    private Long updatedBy;
}
