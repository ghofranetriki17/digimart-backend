package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.user.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);

    long countByTenantId(Long tenantId);

    java.util.List<UserJpaEntity> findByTenantId(Long tenantId);

    Page<UserJpaEntity> findByTenantId(Long tenantId, Pageable pageable);

    java.util.Optional<UserJpaEntity> findFirstByTenantIdOrderByIdAsc(Long tenantId);

    java.util.Optional<UserJpaEntity> findFirstByOrderByIdAsc();
}

