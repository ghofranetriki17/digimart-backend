package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.catalog.ProductPriceHistoryJpaEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPriceHistoryJpaRepository extends JpaRepository<ProductPriceHistoryJpaEntity, Long> {

    @Query("""
            select min(coalesce(h.finalPrice, h.initialPrice))
            from ProductPriceHistoryJpaEntity h
            where h.productId = :productId
            """)
    BigDecimal findLowestPriceAllTime(@Param("productId") Long productId);

    @Query("""
            select min(coalesce(h.finalPrice, h.initialPrice))
            from ProductPriceHistoryJpaEntity h
            where h.productId = :productId
              and h.changedAt >= :since
            """)
    BigDecimal findLowestPriceSince(@Param("productId") Long productId, @Param("since") LocalDateTime since);

    Page<ProductPriceHistoryJpaEntity> findByProductId(Long productId, Pageable pageable);
}
