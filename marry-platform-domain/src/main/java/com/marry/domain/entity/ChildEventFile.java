package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Child event attachment link (many-to-many with sys_file).
 */
@Data
@TableName("child_event_file")
public class ChildEventFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    /** refs sys_file.id */
    private Long fileId;

    /** image / file */
    private String mediaType;

    private Integer sortNo;

    private Long createBy;

    private LocalDateTime createTime;

    /** joined from sys_file, not table columns */
    @TableField(exist = false)
    private String url;

    @TableField(exist = false)
    private String originalName;

    @TableField(exist = false)
    private String contentType;

    @TableField(exist = false)
    private Long size;
}
