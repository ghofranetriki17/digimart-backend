package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleAssignmentJpaRepository extends JpaRepository<UserRoleAssignment, Long> {
    List<UserRoleAssignment> findByTenantIdAndUserIdAndActiveTrue(Long tenantId, Long userId);

    Optional<UserRoleAssignment> findByTenantIdAndUserIdAndRoleId(
            Long tenantId,
            Long userId,
            Long roleId
    );
}
