package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleAssignment extends TenantEntity {

    private Long userId;

    private Long roleId;

    private boolean active = true;
}
