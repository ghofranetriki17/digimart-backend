package com.nexashop.domain.tenant.entity;

import com.nexashop.domain.common.AuditableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivitySector extends AuditableEntity {

    private String label;

    private String description;

    private boolean active = true;
}
