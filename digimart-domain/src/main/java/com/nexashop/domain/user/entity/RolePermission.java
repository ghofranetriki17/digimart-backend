package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermission extends TenantEntity {

    private Long roleId;

    private Long permissionId;
}
