package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.ProductRepository;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.infrastructure.persistence.jpa.ProductJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter
        extends JpaRepositoryAdapter<Product, ProductJpaEntity, Long>
        implements ProductRepository {

    private final ProductJpaRepository repository;

    public ProductRepositoryAdapter(ProductJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductJpaEntity toJpa(Product domain) {
        return ProductMapper.toJpa(domain);
    }

    @Override
    protected Product toDomain(ProductJpaEntity entity) {
        return ProductMapper.toDomain(entity);
    }

    @Override
    public boolean existsByTenantIdAndSlug(Long tenantId, String slug) {
        return repository.existsByTenantIdAndSlug(tenantId, slug);
    }

    @Override
    public boolean existsByTenantIdAndSku(Long tenantId, String sku) {
        return repository.existsByTenantIdAndSku(tenantId, sku);
    }

    @Override
    public Optional<Product> findByIdAndTenantId(Long id, Long tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findByTenantId(Long tenantId) {
        return toDomainList(repository.findByTenantId(tenantId));
    }

    @Override
    public PageResult<Product> findByTenantId(PageRequest request, Long tenantId) {
        Page<ProductJpaEntity> page = repository.findByTenantId(
                tenantId,
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
