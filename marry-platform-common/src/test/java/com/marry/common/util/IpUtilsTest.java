package com.marry.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpUtilsTest {

    @Test
    void picksXForwardedFor() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        assertEquals("203.0.113.5", IpUtils.getClientIp(req));
    }

    @Test
    void fallsBackToRemoteAddr() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("198.51.100.7");
        assertEquals("198.51.100.7", IpUtils.getClientIp(req));
    }

    @Test
    void returnsUnknownForNullRequest() {
        HttpServletRequest nullReq = null;
        assertEquals("unknown", IpUtils.getClientIp(nullReq));
    }
}