package com.marry.security.filter;

import cn.hutool.core.util.StrUtil;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.domain.R;
import com.marry.common.core.exception.BizException;
import com.marry.common.security.CurrentUserContext;
import com.marry.common.security.LoginUser;
import com.marry.security.properties.JwtProperties;
import com.marry.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Extracts and validates JWT from the request, then populates the SecurityContext.
 *
 * <p>Perm-key claim entries are translated into {@link SimpleGrantedAuthority} so that
 * Spring's {@code @PreAuthorize("hasAuthority('system:user:add')")} matches one-to-one
 * with {@code sys_menu.perm}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Endpoints that must always bypass JWT validation, even when the request
     * carries an expired or invalid {@code Authorization} header. Login and
     * refresh especially need this — a client refreshing an expired access
     * token should always reach {@code /auth/refresh} with whatever tokens it
     * has, otherwise the filter would 401 it before the controller can issue a
     * new pair.
     */
    private static final List<String> AUTH_BYPASS_PATTERNS = List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/captcha",
            "/auth/logout"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Auth-flow endpoints are responsible for their own token semantics.
        // Skip our JWT validation entirely so an expired access token carried
        // on the request can't 401 the request before the controller decides.
        String path = stripContextPath(request);
        if (AUTH_BYPASS_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, path))) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (StrUtil.isBlank(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parseOrThrow(token);

            // Check blacklist (fail-open when Redis is unreachable so a temporary
            // outage doesn't lock everyone out)
            try {
                String blacklistKey = jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId();
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    writeUnauthorized(response, "令牌已注销");
                    return;
                }
            } catch (Exception redisErr) {
                log.warn("Redis blacklist check failed (continuing): {}", redisErr.getMessage());
            }

            String username = claims.getSubject();
            Long userId = claims.get("uid", Long.class);
            List<String> perms = jwtUtil.getPerms(claims);

            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setUsername(username);
            loginUser.setPermissions(perms);
            // roles are populated by controller /auth/getInfo

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    loginUser,
                    null,
                    perms.stream().map(SimpleGrantedAuthority::new).toList());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            // Mirror the user id into a common-classpath ThreadLocal so that
            // modules without a security dependency (e.g. MybatisPlusConfig in
            // the persistence module) can still read it without forming a
            // module cycle.
            CurrentUserContext.set(loginUser.getUserId());

        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "令牌已过期");
            return;
        } catch (BizException e) {
            writeUnauthorized(response, e.getMessage());
            return;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            writeUnauthorized(response, "令牌无效");
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }

    /** Strip the configured context-path (e.g. {@code /api}) so path patterns match. */
    private static String stripContextPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (StrUtil.isNotBlank(ctx) && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        return path;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(jwtProperties.getHeader());
        if (StrUtil.isNotBlank(bearer) && bearer.startsWith(jwtProperties.getTokenPrefix())) {
            return bearer.substring(jwtProperties.getTokenPrefix().length()).trim();
        }
        // Allow token in URL query for download endpoints
        String q = request.getParameter(jwtProperties.getHeader());
        return StrUtil.isBlank(q) ? null : q;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(BizCode.UNAUTHORIZED, message)));
    }
}