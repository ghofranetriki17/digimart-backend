package com.nexashop.api.dto.response.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
