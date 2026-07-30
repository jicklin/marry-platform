package com.marry.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Lightweight authenticated-user representation. Carried in Spring Security's
 * {@code Authentication#getPrincipal()} and read by downstream code (MyBatis-Plus
 * audit fill, DataScope interceptor, etc.).
 *
 * <p>Lives in {@code common} so that both the security module (which writes it)
 * and the persistence module (which reads it via {@code MetaObjectHandler})
 * can depend on it without forming a module cycle.</p>
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
