package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTenantIdAndCode(Long tenantId, String code);

    List<Role> findByTenantIdAndIdIn(Long tenantId, Collection<Long> ids);
}
