package com.marry.web.service;

import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Per-username login attempt counter. Backed by Redis so it survives restarts
 * and works across instances behind a load balancer.
 *
 * <p>On {@code MAX_ATTEMPTS} consecutive failures the user is locked for
 * {@code LOCK_TTL_SECONDS}. A successful login resets the counter.</p>
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TTL_SECONDS = 10 * 60;

    private static final String KEY_PREFIX = "marry:login:fail:";

    private final StringRedisTemplate redis;

    public void assertNotLocked(String username) {
        String key = failKey(username);
        String v = redis.opsForValue().get(key);
        if (v != null && Integer.parseInt(v) >= MAX_ATTEMPTS) {
            throw new BizException(BizCode.USER_PASSWORD_INVALID,
                    "尝试次数过多，请稍后再试");
        }
    }

    public void recordFailure(String username) {
        String key = failKey(username);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void clear(String username) {
        redis.delete(failKey(username));
    }

    private static String failKey(String username) {
        return KEY_PREFIX + (username == null ? "_" : username);
    }

    /** Helper for callers that already have a {@link HttpServletRequest}. */
    public String clientIp(HttpServletRequest request) {
        return IpUtils.getClientIp(request);
    }
}
