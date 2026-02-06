package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.RoleJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryAdapter
        extends JpaRepositoryAdapter<Role, RoleJpaEntity, Long>
        implements RoleRepository {

    private final RoleJpaRepository repository;

    public RoleRepositoryAdapter(RoleJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected RoleJpaEntity toJpa(Role domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected Role toDomain(RoleJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<Role> findByTenantIdAndCode(Long tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, code).map(UserMapper::toDomain);
    }

    @Override
    public List<Role> findByTenantIdAndIdIn(Long tenantId, Collection<Long> ids) {
        return toDomainList(repository.findByTenantIdAndIdIn(tenantId, ids));
    }

    @Override
    public List<Role> findByTenantIdIn(Set<Long> tenantIds) {
        return toDomainList(repository.findByTenantIdIn(tenantIds));
    }

    @Override
    public List<Role> findByTenantIdAndCodeIn(Long tenantId, Set<String> codes) {
        return toDomainList(repository.findByTenantIdAndCodeIn(tenantId, codes));
    }
}
