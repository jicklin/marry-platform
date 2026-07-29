package com.marry.common.core.exception;

import com.marry.common.core.domain.BizCode;
import lombok.Getter;

/**
 * Domain business exception that maps to an {@link BizCode}.
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = BizCode.INTERNAL_ERROR.getCode();
    }

    public BizException(BizCode biz) {
        super(biz.getMsg());
        this.code = biz.getCode();
    }

    public BizException(BizCode biz, String message) {
        super(message);
        this.code = biz.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}