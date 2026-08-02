package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marry.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Personal note (markdown).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_note")
public class SysNote extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** markdown body */
    private String content;

    /** comma-separated tags */
    private String tags;

    /** 0 normal, 1 pinned to top */
    private Integer isPinned;

    /** 0 disabled, 1 enabled */
    private Integer status;

    private String remark;
}
