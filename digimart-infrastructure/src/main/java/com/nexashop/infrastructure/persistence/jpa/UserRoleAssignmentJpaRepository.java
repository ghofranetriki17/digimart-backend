package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleAssignmentJpaRepository extends JpaRepository<UserRoleAssignment, Long> {
    List<UserRoleAssignment> findByTenantIdAndUserIdAndActiveTrue(Long tenantId, Long userId);

    Optional<UserRoleAssignment> findByTenantIdAndUserIdAndRoleId(
            Long tenantId,
            Long userId,
            Long roleId
    );

    @Transactional
    @Modifying
    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);

    @Transactional
    @Modifying
    void deleteByRoleId(Long roleId);
}
