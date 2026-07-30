package com.marry.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates a {@code X-Request-Id} across the request lifecycle, exposes it
 * via SLF4J {@link MDC} so log patterns like {@code [%X{requestId}]} can
 * stitch a single user's trace, and echoes the value back on the response.
 *
 * <p>Highest-priority filter: register via a {@code FilterRegistrationBean}
 * with {@code setOrder(Ordered.HIGHEST_PRECEDENCE)} (or {@code @Order}
 * annotation on a wrapper bean).</p>
 */
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String id = request.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_KEY, id);
        response.setHeader(HEADER, id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
