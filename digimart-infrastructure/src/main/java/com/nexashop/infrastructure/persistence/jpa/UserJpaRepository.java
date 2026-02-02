package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    boolean existsByTenantIdAndEmail(Long tenantId, String email);

    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);
}
