package com.marry.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Login request")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(min = 3, max = 64)
    @Schema(description = "Username")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Password")
    private String password;

    @Schema(description = "Captcha code (returned by /auth/captcha)")
    private String code;

    @Schema(description = "Captcha UUID (returned in X-Captcha-Id header)")
    private String uuid;
}