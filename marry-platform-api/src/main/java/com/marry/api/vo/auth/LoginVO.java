package com.marry.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Login response containing tokens and user info")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Access token (JWT)")
    private String accessToken;

    @Schema(description = "Refresh token (JWT)")
    private String refreshToken;

    @Schema(description = "Access token expiry in seconds")
    private Long expiresIn;

    @Schema(description = "Current logged-in user info")
    private UserInfoVO userInfo;

    @Data
    public static class UserInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String username;
        private String nickName;
        private String avatar;
        private String email;
        private String phone;
        private Integer sex;
        private Long deptId;
        private String deptName;

        @Schema(description = "Permission keys granted to current user")
        private java.util.List<String> permissions;

        @Schema(description = "Role codes granted to current user")
        private java.util.List<String> roles;
    }
}