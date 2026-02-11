package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductPriceHistoryRepository {

    ProductPriceHistory save(ProductPriceHistory history);

    List<ProductPriceHistory> saveAll(List<ProductPriceHistory> histories);

    PageResult<ProductPriceHistory> findByProductId(PageRequest request, Long productId);

    BigDecimal findLowestPriceAllTime(Long productId);

    BigDecimal findLowestPriceSince(Long productId, LocalDateTime since);
}
