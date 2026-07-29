package com.marry.security.util;

import com.marry.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties props;

    @BeforeEach
    void setup() {
        props = new JwtProperties();
        props.setSecret("please-replace-me-with-32-or-more-characters-please");
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void issuedTokenCanBeParsed() {
        String token = jwtUtil.issueAccessToken(42L, "admin", List.of("system:user:add"));
        Claims claims = jwtUtil.parse(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(42L, claims.get("uid", Long.class));
        assertEquals("access", claims.get("type", String.class));
        assertTrue(jwtUtil.getPerms(claims).contains("system:user:add"));
    }

    @Test
    void refreshTokenCarriesExpectedClaim() {
        String token = jwtUtil.issueRefreshToken(7L, "demo");
        Claims claims = jwtUtil.parse(token);
        assertEquals("refresh", claims.get("type", String.class));
        assertEquals(7L, claims.get("uid", Long.class));
    }

    @Test
    void parsingBogusTokenThrows() {
        assertThrows(Exception.class, () -> jwtUtil.parse("not-a-valid-jwt-token"));
    }
}