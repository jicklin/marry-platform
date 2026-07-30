package com.marry.api.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank
    @Pattern(regexp = "^[CMF]$", message = "menuType 仅允许 C / M / F")
    private String menuType;

    private String path;

    private String component;

    /**
     * Permission key in {@code module:resource:action} form, e.g. {@code system:user:add}.
     * Optional for M / directory entries.
     */
    @Pattern(regexp = "^[a-z][a-zA-Z0-9]*(:[a-zA-Z0-9]+){1,2}$",
            message = "perm 格式：module 或 module:resource 或 module:resource:action")
    private String perm;

    private String icon;

    private Integer orderNum;

    private Integer visible;

    private Integer status;

    private Integer isCache;

    private Integer isFrame;
}