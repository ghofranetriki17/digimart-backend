package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.user.RefreshTokenJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}

