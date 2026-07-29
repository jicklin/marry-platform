package com.marry.api.vo.system;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Department tree node.
 */
@Data
public class DeptTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String leader;
    private String phone;
    private String email;
    private Integer orderNum;
    private Integer status;

    private List<DeptTreeVO> children = new ArrayList<>();
}