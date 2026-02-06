package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.store.StoreJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StoreJpaRepository extends JpaRepository<StoreJpaEntity, Long> {
    boolean existsByTenantIdAndCode(Long tenantId, String code);

    Optional<StoreJpaEntity> findByIdAndTenantId(Long id, Long tenantId);

    List<StoreJpaEntity> findByTenantId(Long tenantId);

    List<StoreJpaEntity> findByTenantIdIn(List<Long> tenantIds);
}

