package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.ProductPriceHistoryRepository;
import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import com.nexashop.infrastructure.persistence.jpa.ProductPriceHistoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductPriceHistoryMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductPriceHistoryJpaEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ProductPriceHistoryRepositoryAdapter
        extends JpaRepositoryAdapter<ProductPriceHistory, ProductPriceHistoryJpaEntity, Long>
        implements ProductPriceHistoryRepository {

    private final ProductPriceHistoryJpaRepository repository;

    public ProductPriceHistoryRepositoryAdapter(ProductPriceHistoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductPriceHistoryJpaEntity toJpa(ProductPriceHistory domain) {
        return ProductPriceHistoryMapper.toJpa(domain);
    }

    @Override
    protected ProductPriceHistory toDomain(ProductPriceHistoryJpaEntity entity) {
        return ProductPriceHistoryMapper.toDomain(entity);
    }

    @Override
    public ProductPriceHistory save(ProductPriceHistory history) {
        return ProductPriceHistoryMapper.toDomain(repository.save(ProductPriceHistoryMapper.toJpa(history)));
    }

    @Override
    public List<ProductPriceHistory> saveAll(List<ProductPriceHistory> histories) {
        return histories == null || histories.isEmpty()
                ? List.of()
                : repository.saveAll(
                        histories.stream().map(ProductPriceHistoryMapper::toJpa).toList()
                ).stream().map(ProductPriceHistoryMapper::toDomain).toList();
    }

    @Override
    public PageResult<ProductPriceHistory> findByProductId(PageRequest request, Long productId) {
        Page<ProductPriceHistoryJpaEntity> page = repository.findByProductId(
                productId,
                org.springframework.data.domain.PageRequest.of(
                        request.page(),
                        request.size(),
                        Sort.by(Sort.Direction.DESC, "changedAt")
                )
        );
        return PageResult.of(
                page.getContent().stream().map(ProductPriceHistoryMapper::toDomain).toList(),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public BigDecimal findLowestPriceAllTime(Long productId) {
        return repository.findLowestPriceAllTime(productId);
    }

    @Override
    public BigDecimal findLowestPriceSince(Long productId, LocalDateTime since) {
        return repository.findLowestPriceSince(productId, since);
    }
}
