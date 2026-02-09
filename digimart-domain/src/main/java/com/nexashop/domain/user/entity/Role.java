package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role extends TenantEntity {

    private String code;

    private String label;

    private boolean systemRole;
}
