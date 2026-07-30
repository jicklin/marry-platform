package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "User create/update request")
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "User id (null for create)")
    private Long id;

    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "Password (required on create)")
    @Size(min = 8, max = 64, message = "密码长度需 8-64 字符")
    private String password;

    private String nickName;

    @Email
    @Size(max = 64, message = "邮箱长度最多 64 字符")
    private String email;

    private String phone;

    private Integer sex;

    private Long deptId;

    @Schema(description = "0 disabled, 1 enabled")
    private Integer status;

    private String remark;

    @Schema(description = "Role ids to assign to this user")
    private java.util.List<Long> roleIds;
}