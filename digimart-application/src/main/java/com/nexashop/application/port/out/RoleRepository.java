package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends CrudRepositoryPort<Role, Long> {

    Optional<Role> findByTenantIdAndCode(Long tenantId, String code);

    List<Role> findByTenantIdAndIdIn(Long tenantId, Collection<Long> ids);

    List<Role> findByTenantIdIn(Set<Long> tenantIds);

    List<Role> findByTenantIdAndCodeIn(Long tenantId, Set<String> codes);
}
