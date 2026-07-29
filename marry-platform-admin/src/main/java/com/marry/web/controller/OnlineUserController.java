package com.marry.web.controller;

import com.marry.common.core.domain.R;
import com.marry.security.properties.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Online user registry backed by Redis keys created on /auth/login.
 */
@Tag(name = "在线用户")
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    @Operation(summary = "在线用户列表")
    @PreAuthorize("hasAuthority('monitor:online:list')")
    @GetMapping("/list")
    public R<List<OnlineVO>> list() {
        Set<String> keys = redisTemplate.keys(jwtProperties.getOnlineKey() + ":*");
        List<OnlineVO> list = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                String[] parts = k.split(":");
                try {
                    OnlineVO v = new OnlineVO();
                    v.setUserId(Long.valueOf(parts[parts.length - 1]));
                    v.setUsername(redisTemplate.opsForValue().get(k));
                    Long ttl = redisTemplate.getExpire(k);
                    v.setTtlSeconds(ttl);
                    list.add(v);
                } catch (Exception ignored) {}
            }
        }
        return R.ok(list);
    }

    @Operation(summary = "强制下线")
    @PreAuthorize("hasAuthority('monitor:online:forceLogout')")
    @DeleteMapping("/{userId}")
    public R<Void> forceLogout(@PathVariable Long userId) {
        redisTemplate.delete(jwtProperties.getOnlineKey() + ":" + userId);
        return R.ok();
    }

    @Data
    public static class OnlineVO {
        private Long userId;
        private String username;
        private Long ttlSeconds;
    }
}