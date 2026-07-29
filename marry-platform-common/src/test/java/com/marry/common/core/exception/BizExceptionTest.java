package com.marry.common.core.exception;

import com.marry.common.core.domain.BizCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BizExceptionTest {

    @Test
    void bizCodeException_hasCorrectCode() {
        BizException ex = new BizException(BizCode.USER_NOT_FOUND);
        assertEquals(BizCode.USER_NOT_FOUND.getCode(), ex.getCode());
        assertEquals(BizCode.USER_NOT_FOUND.getMsg(), ex.getMessage());
    }

    @Test
    void bizCodeAndCustomMessageException() {
        BizException ex = new BizException(BizCode.BAD_REQUEST, "missing field");
        assertEquals(BizCode.BAD_REQUEST.getCode(), ex.getCode());
        assertEquals("missing field", ex.getMessage());
    }

    @Test
    void messageOnlyConstructorUsesInternalError() {
        BizException ex = new BizException("oops");
        assertEquals(BizCode.INTERNAL_ERROR.getCode(), ex.getCode());
    }
}