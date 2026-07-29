package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Operation log captured by AOP.
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String businessType;

    private String method;

    private String requestMethod;

    private String operUrl;

    private String operParam;

    private String jsonResult;

    private Long operId;

    private String operName;

    private Long deptId;

    private String deptName;

    private String operIp;

    private String userAgent;

    /** 0 fail, 1 success */
    private Integer status;

    private String errorMsg;

    private Long costTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operTime;
}