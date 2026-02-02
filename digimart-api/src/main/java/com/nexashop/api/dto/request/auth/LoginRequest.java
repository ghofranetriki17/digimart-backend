package com.nexashop.api.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotNull
    private Long tenantId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
