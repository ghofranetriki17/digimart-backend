package com.nexashop.domain.store.entity;

import com.nexashop.domain.common.TenantEntity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Store extends TenantEntity {

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

    private boolean active = true;
}
