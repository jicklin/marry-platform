package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Dictionary data request")
public class DictDataDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank
    private String dictType;

    @NotBlank
    private String label;

    @NotBlank
    private String value;

    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer orderNum;
    private Integer status;
    private String remark;
}