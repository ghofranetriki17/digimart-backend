package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreJpaRepository extends JpaRepository<Store, Long> {
    boolean existsByTenantIdAndCode(Long tenantId, String code);

    Optional<Store> findByIdAndTenantId(Long id, Long tenantId);

    List<Store> findByTenantId(Long tenantId);
}
