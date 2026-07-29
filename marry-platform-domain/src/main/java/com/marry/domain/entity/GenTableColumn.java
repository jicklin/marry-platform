package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Code-gen metadata for a database column.
 */
@Data
@TableName("gen_table_column")
public class GenTableColumn implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tableId;

    private String columnName;

    private String columnComment;

    private String columnType;

    private String javaType;

    private String javaField;

    private Integer isPk;

    private Integer isIncrement;

    private Integer isRequired;

    private Integer isInsert;

    private Integer isEdit;

    private Integer isList;

    private Integer isQuery;

    /** EQ / LIKE / BETWEEN */
    private String queryType;

    /** input / textarea / select / datetime */
    private String htmlType;

    private String dictType;

    private Integer sort;
}