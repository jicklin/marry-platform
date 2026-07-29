package com.marry.api.query.system;

import com.marry.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "User list query")
public class UserQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String nickName;
    private String phone;
    private Long deptId;
    private Integer status;
}