package com.marry.common.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    void ok_returnsSuccess() {
        R<String> r = R.ok("hello");
        assertEquals(0, r.code());
        assertEquals("hello", r.data());
        assertTrue(r.isSuccess());
    }

    @Test
    void fail_returnsNonZero() {
        R<Void> r = R.fail(BizCode.NOT_FOUND);
        assertEquals(404, r.code());
        assertFalse(r.isSuccess());
    }

    @Test
    void fail_withBizAndMsg() {
        R<Void> r = R.fail(BizCode.BAD_REQUEST, "missing field");
        assertEquals(BizCode.BAD_REQUEST.getCode(), r.code());
        assertEquals("missing field", r.msg());
    }
}