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
 * Uploaded file metadata.
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String originalName;

    private String bucket;

    private String path;

    private String url;

    private String contentType;

    private Long size;

    private String md5;

    /** local / minio / oss */
    private String storageType;

    private String uploadBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}