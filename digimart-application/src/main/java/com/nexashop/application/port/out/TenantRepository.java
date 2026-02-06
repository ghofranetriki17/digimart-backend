package com.nexashop.application.port.out;

import com.nexashop.domain.tenant.entity.Tenant;
import java.util.List;

public interface TenantRepository extends CrudRepositoryPort<Tenant, Long> {

    boolean existsBySubdomain(String subdomain);

    List<Tenant> findBySectorId(Long sectorId);

    List<Tenant> findBySectorIdIn(List<Long> sectorIds);
}
