package com.nexashop.domain.billing.entity;

import com.nexashop.domain.common.AuditableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanFeature extends AuditableEntity {

    private Long planId;

    private Long featureId;
}
