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

    /** URI patterns that bypass security. */
    private List<String> permitAll = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/captcha",
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