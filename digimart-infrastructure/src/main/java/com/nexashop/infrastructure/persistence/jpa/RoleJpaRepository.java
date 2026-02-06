package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.user.RoleJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByTenantIdAndCode(Long tenantId, String code);

    List<RoleJpaEntity> findByTenantIdAndIdIn(Long tenantId, Collection<Long> ids);

    List<RoleJpaEntity> findByTenantIdIn(Set<Long> tenantIds);

    List<RoleJpaEntity> findByTenantIdAndCodeIn(Long tenantId, Set<String> codes);
}

