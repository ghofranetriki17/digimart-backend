package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.domain.user.entity.User;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter
        extends JpaRepositoryAdapter<User, UserJpaEntity, Long>
        implements UserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryAdapter(UserJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected UserJpaEntity toJpa(User domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected User toDomain(UserJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        return repository.countByTenantId(tenantId);
    }

    @Override
    public List<User> findByTenantId(Long tenantId) {
        return toDomainList(repository.findByTenantId(tenantId));
    }

    @Override
    public PageResult<User> findByTenantId(PageRequest request, Long tenantId) {
        Page<UserJpaEntity> page = repository.findByTenantId(
                tenantId,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public Optional<User> findFirstByTenantIdOrderByIdAsc(Long tenantId) {
        return repository.findFirstByTenantIdOrderByIdAsc(tenantId).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findFirstByOrderByIdAsc() {
        return repository.findFirstByOrderByIdAsc().map(UserMapper::toDomain);
    }
}
