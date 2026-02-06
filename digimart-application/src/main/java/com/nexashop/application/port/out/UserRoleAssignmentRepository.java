package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.List;
import java.util.Optional;

public interface UserRoleAssignmentRepository extends CrudRepositoryPort<UserRoleAssignment, Long> {

    List<UserRoleAssignment> findByTenantIdAndUserIdAndActiveTrue(Long tenantId, Long userId);

    Optional<UserRoleAssignment> findByTenantIdAndUserIdAndRoleId(Long tenantId, Long userId, Long roleId);

    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);

    void deleteByRoleId(Long roleId);
}
