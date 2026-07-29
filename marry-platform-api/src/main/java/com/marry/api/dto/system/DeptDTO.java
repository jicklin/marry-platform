package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Department create/update request")
public class DeptDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;

    @NotBlank
    private String name;

    private String code;
    private String leader;
    private String phone;
    private String email;

    private Integer orderNum;
    private Integer status;
}