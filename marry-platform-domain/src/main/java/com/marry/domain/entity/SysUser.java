package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marry.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

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
}