package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends CrudRepositoryPort<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByCodeIn(Set<String> codes);

    List<Permission> findByIdIn(Collection<Long> ids);
}
