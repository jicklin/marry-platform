package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Menu create/update request")
public class MenuDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    @NotBlank
    private String name;

    @Schema(description = "M directory, C menu, F button")
    private String menuType;

    private String path;

    private String component;

    private String perm;

    private String icon;

    private Integer orderNum;

    private Integer visible;

    private Integer status;

    private Integer isCache;

    private Integer isFrame;
}