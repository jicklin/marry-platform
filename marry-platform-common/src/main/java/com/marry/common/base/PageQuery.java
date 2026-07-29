package com.marry.common.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Generic pagination query parameters.
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Page number, 1-based", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "Page size", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "Sort field")
    private String orderByColumn;

    @Schema(description = "Sort direction: asc / desc", defaultValue = "desc")
    private String isAsc = "desc";

    public <T> Page<T> toPage() {
        return Page.of(pageNum, pageSize);
    }
}