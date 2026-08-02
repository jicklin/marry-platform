package com.marry.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marry.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

/**
 * Child growth event (markdown body with inline images).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("child_event")
public class ChildEvent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** markdown body, may embed image urls inline */
    private String content;

    /** event date (timeline grouping key) */
    private LocalDate eventDate;

    /** 学习 / 运动 / 日常 / 纪念 / 成长 */
    private String category;

    /** comma-separated tags */
    private String tags;

    /** 0 normal, 1 important, 2 milestone */
    private Integer importance;

    private String mood;

    /** disk directory name, e.g. 2026-09-01_开学第一天 */
    private String dirName;

    /** attachments (joined from child_event_file + sys_file), not a table column */
    @TableField(exist = false)
    private List<ChildEventFile> attachFiles;
}
