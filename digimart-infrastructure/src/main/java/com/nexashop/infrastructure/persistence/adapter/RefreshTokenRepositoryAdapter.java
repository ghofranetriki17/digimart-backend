package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.RefreshTokenRepository;
import com.nexashop.domain.user.entity.RefreshToken;
import com.nexashop.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.UserMapper;
import com.nexashop.infrastructure.persistence.model.user.RefreshTokenJpaEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryAdapter
        extends JpaRepositoryAdapter<RefreshToken, RefreshTokenJpaEntity, Long>
        implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository repository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected RefreshTokenJpaEntity toJpa(RefreshToken domain) {
        return UserMapper.toJpa(domain);
    }

    @Override
    protected RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash) {
        return repository.findByTokenHashAndRevokedAtIsNull(tokenHash).map(UserMapper::toDomain);
    }
}
