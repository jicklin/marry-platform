package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marry.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * System menu (directory/menu/button).
 * <ul>
 *   <li>menuType = M: directory (parent only, no component)</li>
 *   <li>menuType = C: menu page (path + component)</li>
 *   <li>menuType = F: button (perm key only)</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    /** M directory, C menu, F button */
    private String menuType;

    private String path;

    private String component;

    /** Button permission key, e.g. system:user:add */
    private String perm;

    private String icon;

    private Integer orderNum;

    /** 0 hidden, 1 shown */
    private Integer visible;

    /** 0 disabled, 1 enabled */
    private Integer status;

    private Integer isCache;

    private Integer isFrame;
}