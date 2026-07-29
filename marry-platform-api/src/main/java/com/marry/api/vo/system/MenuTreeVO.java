package com.marry.api.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Frontend-friendly menu tree node. Used both for /system/menu/tree and /system/menu/routers.
 */
@Data
@Schema(description = "Menu tree node (routes payload)")
public class MenuTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private String menuType;
    private String path;
    private String component;
    private String perm;
    private String icon;
    private Integer orderNum;
    private Integer visible;
    private Integer status;
    private Integer isCache;

    @Schema(description = "Frontend-only meta")
    private Meta meta;

    private List<MenuTreeVO> children = new ArrayList<>();

    @Data
    public static class Meta implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private String icon;
        private Boolean hidden;
        private Boolean keepAlive;
        private List<String> perms;
    }
}