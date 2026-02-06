package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.PermissionRepository;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.infrastructure.persistence.jpa.PermissionJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.PermissionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRepositoryAdapter
        extends JpaRepositoryAdapter<Permission, PermissionJpaEntity, Long>
        implements PermissionRepository {

    private final PermissionJpaRepository repository;

    public PermissionRepositoryAdapter(PermissionJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected PermissionJpaEntity toJpa(Permission domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected Permission toDomain(PermissionJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return repository.findByCode(code).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public List<Permission> findByCodeIn(Set<String> codes) {
        return toDomainList(repository.findByCodeIn(codes));
    }
}
