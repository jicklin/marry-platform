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
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "登录")
    @IgnoreLog
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // Verify captcha if provided
        if (StrUtil.isNotBlank(dto.getUuid()) && StrUtil.isNotBlank(dto.getCode())) {
            if (!CaptchaController.verify(redisTemplate, dto.getUuid(), dto.getCode())) {
                throw new BizException(BizCode.BAD_REQUEST, "验证码错误或已过期");
            }
        }
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BizException(BizCode.USER_PASSWORD_INVALID);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(BizCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(BizCode.USER_PASSWORD_INVALID);
        }

        List<String> perms = menuService.permsByUserId(user.getId());
        String access = jwtUtil.issueAccessToken(user.getId(), user.getUsername(), perms);
        String refresh = jwtUtil.issueRefreshToken(user.getId(), user.getUsername());

        // Register online user (TTL = access TTL)
        String onlineKey = jwtProperties.getOnlineKey() + ":" + user.getId();
        redisTemplate.opsForValue().set(onlineKey, user.getUsername(),
                jwtProperties.getAccessTtl(), TimeUnit.MILLISECONDS);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(access);
        vo.setRefreshToken(refresh);
        vo.setExpiresIn(jwtProperties.getAccessTtl() / 1000);
        vo.setUserInfo(userService.getLoginUserInfo(user.getId()));
        return R.ok(vo);
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<LoginVO> refresh(@RequestBody RefreshDTO dto) {
        if (StrUtil.isBlank(dto.getRefreshToken())) {
            throw new BizException(BizCode.TOKEN_INVALID);
        }
        try {
            Claims claims = jwtUtil.parseOrThrow(dto.getRefreshToken());
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new BizException(BizCode.TOKEN_INVALID);
            }
            // check blacklist
            if (Boolean.TRUE.equals(redisTemplate.hasKey(jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId()))) {
                throw new BizException(BizCode.TOKEN_BLACKLIST);
            }
            Long userId = claims.get("uid", Long.class);
            String username = claims.getSubject();
            List<String> perms = menuService.permsByUserId(userId);
            String access = jwtUtil.issueAccessToken(userId, username, perms);
            String newRefresh = jwtUtil.issueRefreshToken(userId, username);
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
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            String token = authHeader.substring(jwtProperties.getTokenPrefix().length()).trim();
            try {
                Claims claims = jwtUtil.parse(token);
                Long uid = claims.get("uid", Long.class);
                // Blacklist current token until expiry
                long ttl = jwtUtil.getRemainingMillis(claims);
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(
                            jwtProperties.getRedisKeyPrefix() + "bl:" + claims.getId(),
                            "1",
                            ttl, TimeUnit.MILLISECONDS);
                }
                // Remove from online users
                if (uid != null) {
                    redisTemplate.delete(jwtProperties.getOnlineKey() + ":" + uid);
                }
            } catch (Exception e) {
                log.debug("logout: invalid token {}", e.getMessage());
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
}