package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.tenant.TenantJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, Long> {
    boolean existsBySubdomain(String subdomain);

    List<TenantJpaEntity> findBySectorId(Long sectorId);

    List<TenantJpaEntity> findBySectorIdIn(List<Long> sectorIds);
}

