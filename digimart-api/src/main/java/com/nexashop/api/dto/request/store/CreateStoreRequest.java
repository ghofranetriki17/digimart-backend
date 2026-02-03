package com.nexashop.api.dto.request.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStoreRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;

    private String phone;

    private String email;

    private String imageUrl;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
