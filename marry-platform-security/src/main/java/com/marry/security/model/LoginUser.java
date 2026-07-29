package com.marry.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Lightweight authenticated user representation carried in {@link org.springframework.security.core.Authentication#getPrincipal()}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickName;
    private String avatar;
    private Long deptId;
    private String deptName;
    private List<String> permissions;
    private List<String> roles;
}