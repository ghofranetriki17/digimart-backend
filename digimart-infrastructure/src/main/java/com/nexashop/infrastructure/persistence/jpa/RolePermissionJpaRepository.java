package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.user.RolePermissionJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;


public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {

    List<RolePermissionJpaEntity> findByTenantIdAndRoleId(Long tenantId, Long roleId);

    @Transactional
    @Modifying
    void deleteByTenantIdAndRoleId(Long tenantId, Long roleId);

    @Transactional
    @Modifying
    void deleteByRoleId(Long roleId);
}

