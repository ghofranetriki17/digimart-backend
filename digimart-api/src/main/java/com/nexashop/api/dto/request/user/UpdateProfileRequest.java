package com.nexashop.api.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    private String imageUrl;
}
