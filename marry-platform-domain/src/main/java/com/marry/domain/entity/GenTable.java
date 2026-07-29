package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Code-gen metadata for a database table.
 */
@Data
@TableName("gen_table")
public class GenTable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tableName;

    private String tableComment;

    private String className;

    /** crud / tree */
    private String tplCategory;

    private String packageName;

    private String moduleName;

    private String businessName;

    private String functionName;

    /** zip / project-path */
    private String genType;

    private String options;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}