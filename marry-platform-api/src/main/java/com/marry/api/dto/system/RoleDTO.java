package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Role create/update request")
public class RoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "code 仅允许字母、数字、下划线")
    private String code;

    @Schema(description = "1 all, 2 dept, 3 dept+sub, 4 self, 5 custom")
    private Integer dataScope;

    private Integer status;

    private String remark;

    @Schema(description = "Menu ids this role grants")
    private java.util.List<Long> menuIds;
}