package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "System config request")
public class ConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String configKey;

    private String configValue;
    private Integer configType;
    private Integer isBuiltin;
    private String remark;
}