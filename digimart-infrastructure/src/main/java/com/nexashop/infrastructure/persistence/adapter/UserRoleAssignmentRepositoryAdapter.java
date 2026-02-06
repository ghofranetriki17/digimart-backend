package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.UserRoleAssignmentJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleAssignmentRepositoryAdapter
        extends JpaRepositoryAdapter<UserRoleAssignment, UserRoleAssignmentJpaEntity, Long>
        implements UserRoleAssignmentRepository {

    private final UserRoleAssignmentJpaRepository repository;

    public UserRoleAssignmentRepositoryAdapter(UserRoleAssignmentJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected UserRoleAssignmentJpaEntity toJpa(UserRoleAssignment domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected UserRoleAssignment toDomain(UserRoleAssignmentJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public List<UserRoleAssignment> findByTenantIdAndUserIdAndActiveTrue(Long tenantId, Long userId) {
        return toDomainList(repository.findByTenantIdAndUserIdAndActiveTrue(tenantId, userId));
    }

    @Override
    public Optional<UserRoleAssignment> findByTenantIdAndUserIdAndRoleId(
            Long tenantId,
            Long userId,
            Long roleId
    ) {
        return repository.findByTenantIdAndUserIdAndRoleId(tenantId, userId, roleId)
                .map(UserMapper::toDomain);
    }

    @Override
    public void deleteByTenantIdAndRoleId(Long tenantId, Long roleId) {
        repository.deleteByTenantIdAndRoleId(tenantId, roleId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        repository.deleteByRoleId(roleId);
    }
}
