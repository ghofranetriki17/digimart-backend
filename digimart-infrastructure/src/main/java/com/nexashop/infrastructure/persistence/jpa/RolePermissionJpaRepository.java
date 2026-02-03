package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId);

    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);
}
