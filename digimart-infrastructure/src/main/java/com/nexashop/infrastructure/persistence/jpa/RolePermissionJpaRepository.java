package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId);

    @Transactional
    @Modifying
    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);

    @Transactional
    @Modifying
    void deleteByRoleId(Long roleId);
}
