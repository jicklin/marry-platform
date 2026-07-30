package com.marry.web.controller;

import cn.hutool.core.util.StrUtil;
import com.marry.api.dto.auth.LoginDTO;
import com.marry.api.vo.auth.LoginVO;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.domain.R;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.SysUser;
import com.marry.log.annotation.IgnoreLog;
import com.marry.security.properties.JwtProperties;
import com.marry.security.util.JwtUtil;
import com.marry.system.service.IUserService;
import com.marry.system.service.IMenuService;
import com.marry.web.service.LoginAttemptService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Authentication endpoints: login, refresh, logout, getInfo.
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final IMenuService menuService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final LoginAttemptService loginAttempt;

    @Operation(summary = "登录")
    @IgnoreLog
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        // 1) Captcha is mandatory — prevent scripted brute force.
        if (StrUtil.isBlank(dto.getUuid()) || StrUtil.isBlank(dto.getCode())) {
            throw new BizException(BizCode.BAD_REQUEST, "请输入验证码");
        }
        if (!CaptchaController.verify(redisTemplate, dto.getUuid(), dto.getCode())) {
            throw new BizException(BizCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 2) Per-username lockout (Redis-backed).
        loginAttempt.assertNotLocked(dto.getUsername());

        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttempt.recordFailure(dto.getUsername());
            // Identical message for unknown-user vs wrong-password so the API
            // doesn't leak which usernames exist.
            throw new BizException(BizCode.USER_PASSWORD_INVALID);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(BizCode.USER_DISABLED);
        }
        loginAttempt.clear(dto.getUsername());

        List<String> perms = menuService.permsByUserId(user.getId());
        String access = jwtUtil.issueAccessToken(user.getId(), user.getUsername(), perms);
        String refresh = jwtUtil.issueRefreshToken(user.getId(), user.getUsername());

        // Register online user (TTL = access TTL)
        String onlineKey = jwtProperties.getOnlineKey() + ":" + user.getId();
        redisTemplate.opsForValue().set(onlineKey, user.getUsername(),
                jwtProperties.getAccessTtl(), TimeUnit.MILLISECONDS);

        writeRefreshCookie(response, refresh);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(access);
        // refreshToken is delivered via HttpOnly cookie now; body field kept
        // for one release as a transitional fallback.
        vo.setRefreshToken(refresh);
        vo.setExpiresIn(jwtProperties.getAccessTtl() / 1000);
        vo.setUserInfo(userService.getLoginUserInfo(user.getId()));
        return R.ok(vo);
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<LoginVO> refresh(@org.springframework.web.bind.annotation.CookieValue(
                                    name = "refresh_token", required = false) String cookieToken,
                              @RequestBody(required = false) RefreshDTO body,
                              HttpServletResponse response) {
        // Phase 3: prefer cookie; body field is transitional fallback.
        String refreshToken = cookieToken;
        if (StrUtil.isBlank(refreshToken) && body != null) {
            refreshToken = body.getRefreshToken();
        }
        if (StrUtil.isBlank(refreshToken)) {
            throw new BizException(BizCode.TOKEN_INVALID);
        }
        try {
            Claims claims = jwtUtil.parseOrThrow(refreshToken);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new BizException(BizCode.TOKEN_INVALID);
            }
            // check blacklist (covers both manual logout and rotation-replay)
            if (Boolean.TRUE.equals(redisTemplate.hasKey(jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId()))) {
                throw new BizException(BizCode.TOKEN_BLACKLIST);
            }
            Long userId = claims.get("uid", Long.class);
            String username = claims.getSubject();
            List<String> perms = menuService.permsByUserId(userId);
            String access = jwtUtil.issueAccessToken(userId, username, perms);
            String newRefresh = jwtUtil.issueRefreshToken(userId, username);

            // Rotate: blacklist the OLD refresh token so any further replays die.
            long ttl = jwtUtil.getRemainingMillis(claims);
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId(),
                        "1",
                        ttl, TimeUnit.MILLISECONDS);
            }

            writeRefreshCookie(response, newRefresh);

            LoginVO vo = new LoginVO();
            vo.setAccessToken(access);
            vo.setRefreshToken(newRefresh);
            vo.setExpiresIn(jwtProperties.getAccessTtl() / 1000);
            vo.setUserInfo(userService.getLoginUserInfo(userId));
            return R.ok(vo);
        } catch (Exception e) {
            throw new BizException(BizCode.TOKEN_INVALID, e.getMessage());
        }
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                           @RequestBody(required = false) RefreshDTO refreshBody,
                           HttpServletResponse response) {
        clearRefreshCookie(response);
        // Blacklist access token
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            String token = authHeader.substring(jwtProperties.getTokenPrefix().length()).trim();
            try {
                Claims claims = jwtUtil.parse(token);
                Long uid = claims.get("uid", Long.class);
                long ttl = jwtUtil.getRemainingMillis(claims);
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(
                            jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId(),
                            "1",
                            ttl, TimeUnit.MILLISECONDS);
                }
                if (uid != null) {
                    redisTemplate.delete(jwtProperties.getOnlineKey() + ":" + uid);
                }
            } catch (Exception e) {
                log.debug("logout: invalid access token {}", e.getMessage());
            }
        }
        // Blacklist refresh token if the client sent it
        if (refreshBody != null && StrUtil.isNotBlank(refreshBody.getRefreshToken())) {
            try {
                Claims claims = jwtUtil.parse(refreshBody.getRefreshToken());
                long ttl = jwtUtil.getRemainingMillis(claims);
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(
                            jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId(),
                            "1",
                            ttl, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.debug("logout: invalid refresh token {}", e.getMessage());
            }
        }
        return R.ok();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/getInfo")
    public R<LoginVO.UserInfoVO> getInfo() {
        Long uid = com.marry.security.util.SecurityUtil.currentUserId();
        if (uid == null) throw new BizException(BizCode.UNAUTHORIZED);
        return R.ok(userService.getLoginUserInfo(uid));
    }

    public static class RefreshDTO {
        private String refreshToken;
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    /**
     * Issue (or refresh) the {@code refresh_token} HttpOnly cookie. Secure flag
     * follows {@code marry.jwt.cookie-secure} so local http:// dev still works.
     */
    private void writeRefreshCookie(HttpServletResponse response, String token) {
        boolean secure = Boolean.parseBoolean(
                System.getProperty("marry.jwt.cookie-secure",
                        System.getenv().getOrDefault("MARRY_JWT_COOKIE_SECURE", "true")));
        long maxAge = jwtProperties.getRefreshTtl() / 1000;
        String value = "refresh_token=" + token
                + "; Path=/api/auth"
                + "; Max-Age=" + maxAge
                + "; HttpOnly"
                + "; SameSite=Strict"
                + (secure ? "; Secure" : "");
        response.addHeader("Set-Cookie", value);
    }

    /** Clear the {@code refresh_token} cookie (logout). */
    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "refresh_token=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Strict");
    }
}