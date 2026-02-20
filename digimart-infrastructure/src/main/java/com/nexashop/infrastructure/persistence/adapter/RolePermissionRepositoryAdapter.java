package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.RolePermissionRepository;
import com.nexashop.domain.user.entity.RolePermission;
import com.nexashop.infrastructure.persistence.jpa.RolePermissionJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.RolePermissionJpaEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RolePermissionRepositoryAdapter
        extends JpaRepositoryAdapter<RolePermission, RolePermissionJpaEntity, Long>
        implements RolePermissionRepository {

    private final RolePermissionJpaRepository repository;

    public RolePermissionRepositoryAdapter(RolePermissionJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected RolePermissionJpaEntity toJpa(RolePermission domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected RolePermission toDomain(RolePermissionJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId) {
        return toDomainList(repository.findByTenantIdAndRoleId(tenantId, roleId));
    }

    @Override
    public List<RolePermission> findByTenantIdAndRoleIdIn(Long tenantId, Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return toDomainList(repository.findByTenantIdAndRoleIdIn(tenantId, roleIds));
    }

    @Override
    public void deleteByTenantIdAndRoleId(Long tenantId, Long roleId) {
        repository.deleteByTenantIdAndRoleId(tenantId, roleId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        repository.deleteByRoleId(roleId);
    }

    @Override
    public void flush() {
        repository.flush();
    }
}
