package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.RolePermission;
import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends CrudRepositoryPort<RolePermission, Long> {

    List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId);

    List<RolePermission> findByTenantIdAndRoleIdIn(Long tenantId, Collection<Long> roleIds);

    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);

    void deleteByRoleId(Long roleId);

    void flush();
}
