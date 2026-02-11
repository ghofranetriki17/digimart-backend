package com.nexashop.api.dto.response.product;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImageResponse {

    private Long id;
    private Long productId;
    private String imageUrl;
    private String altText;
    private Integer displayOrder;
    private boolean primary;
    private LocalDateTime createdAt;
}
