package com.marry.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Security configuration properties bound from `marry.security.*`.
 */
@Data
@ConfigurationProperties(prefix = "marry.security")
public class SecurityProperties {

    /**
     * CORS allow-list. Wildcard is intentionally NOT supported here because we
     * also set {@code allowCredentials=true}; browsers reject the
     * combination {@code Access-Control-Allow-Origin: *} with credentials, so
     * deploying with {@code "*"} would silently break the cookie-based flow
     * delivered in Phase 3.
     */
    private List<String> allowedOrigins = List.of("*");

    /**
     * Auth-flow endpoints that {@code JwtAuthenticationFilter} must always let
     * through — even with an expired/invalid {@code Authorization} header —
     * so the controller can make the final call (login, refresh, captcha, logout).
     * Single source of truth, shared by both the filter and
     * {@code SecurityConfig.authorizeHttpRequests}.
     */
    private List<String> authBypass = List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/captcha"
//            "/auth/logout"
    );

    /** URI patterns that bypass security. */
    private List<String> permitAll = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/captcha",
            "/auth/logout",
            "/error",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/favicon.ico",
            "/actuator/health",
            "/actuator/info"
    ));
}
