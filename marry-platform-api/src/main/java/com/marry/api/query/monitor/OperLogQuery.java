package com.marry.api.query.monitor;

import com.marry.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Operation log list query")
public class OperLogQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String operName;
    private String businessType;
    private Integer status;
    private LocalDate beginTime;
    private LocalDate endTime;
}