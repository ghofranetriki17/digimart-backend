package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepositoryPort<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByTenantId(Long tenantId);

    List<User> findByTenantId(Long tenantId);

    Optional<User> findFirstByTenantIdOrderByIdAsc(Long tenantId);

    Optional<User> findFirstByOrderByIdAsc();
}
