package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.TenantMapper;
import com.nexashop.infrastructure.persistence.model.tenant.TenantJpaEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class TenantRepositoryAdapter
        extends JpaRepositoryAdapter<Tenant, TenantJpaEntity, Long>
        implements TenantRepository {

    private final TenantJpaRepository repository;

    public TenantRepositoryAdapter(TenantJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected TenantJpaEntity toJpa(Tenant domain) {
        return TenantMapper.toJpa(domain);
    }

    @Override
    protected Tenant toDomain(TenantJpaEntity entity) {
        return TenantMapper.toDomain(entity);
    }

    @Override
    public boolean existsBySubdomain(String subdomain) {
        return repository.existsBySubdomain(subdomain);
    }

    @Override
    public List<Tenant> findBySectorId(Long sectorId) {
        return toDomainList(repository.findBySectorId(sectorId));
    }

    @Override
    public List<Tenant> findBySectorIdIn(List<Long> sectorIds) {
        return toDomainList(repository.findBySectorIdIn(sectorIds));
    }

    @Override
    public PageResult<Tenant> findAll(PageRequest request) {
        Page<TenantJpaEntity> page = repository.findAll(
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }
}
