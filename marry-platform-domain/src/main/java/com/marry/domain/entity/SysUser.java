package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marry.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * System user account.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** Never serialised — kept out of all API responses to avoid leaking BCrypt hashes. */
    @JsonIgnore
    private String password;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    /** 0 unknown, 1 male, 2 female */
    private Integer sex;

    private Long deptId;

    /** 0 disabled, 1 enabled */
    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    private String remark;

    /**
     * Role ids assigned to this user. Not a database column — populated by
     * service layer for the user-detail endpoint so the frontend can
     * pre-select roles in the edit dialog.
     */
    @TableField(exist = false)
    private List<Long> roleIds;
}