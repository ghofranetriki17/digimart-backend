package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantJpaRepository extends JpaRepository<Tenant, Long> {
    boolean existsBySubdomain(String subdomain);
}
