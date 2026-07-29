package com.marry.api.query.monitor;

import com.marry.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Login log list query")
public class LoginLogQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;
    private String status;
    private String ip;
    private LocalDate beginTime;
    private LocalDate endTime;
}