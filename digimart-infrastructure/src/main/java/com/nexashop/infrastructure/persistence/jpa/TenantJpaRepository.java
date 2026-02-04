package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.tenant.entity.Tenant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantJpaRepository extends JpaRepository<Tenant, Long> {
    boolean existsBySubdomain(String subdomain);

    List<Tenant> findBySectorId(Long sectorId);

    List<Tenant> findBySectorIdIn(List<Long> sectorIds);
}
