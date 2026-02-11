package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends CrudRepositoryPort<Store, Long> {

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    Optional<Store> findByIdAndTenantId(Long id, Long tenantId);

    List<Store> findByTenantId(Long tenantId);

    PageResult<Store> findByTenantId(PageRequest request, Long tenantId);

    List<Store> findByTenantIdIn(List<Long> tenantIds);
}
