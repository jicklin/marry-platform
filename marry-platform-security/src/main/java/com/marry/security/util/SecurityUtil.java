package com.marry.security.util;

import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience accessors for the currently authenticated user. Reads
 * {@link LoginUser} directly out of the Spring Security
 * {@link SecurityContextHolder}.
 *
 * <p>Lives in the security module since it depends on
 * {@code org.springframework.security.*}. Modules that want to stay
 * framework-agnostic should use {@code com.marry.common.security.CurrentUserContext}
 * (a ThreadLocal populated by {@code JwtAuthenticationFilter}) instead.</p>
 */
public final class SecurityUtil {

    /** Sentinel user id for system-initiated writes (jobs, startup, seed). */
    public static final long SYSTEM_USER_ID = 0L;

    /** Sentinel username for system-initiated writes. */
    public static final String SYSTEM_USERNAME = "system";

    private SecurityUtil() {}

    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof LoginUser lu ? lu : null;
    }

    public static LoginUser requireCurrentUser() {
        LoginUser u = currentUser();
        if (u == null) throw new BizException(BizCode.UNAUTHORIZED, "请先登录");
        return u;
    }

    public static String currentUsername() {
        LoginUser u = currentUser();
        return u == null ? null : u.getUsername();
    }

    public static Long currentUserId() {
        LoginUser u = currentUser();
        return u == null ? null : u.getUserId();
    }

    public static Long currentDeptId() {
        LoginUser u = currentUser();
        return u == null ? null : u.getDeptId();
    }

    /**
     * Authenticated user id, or {@link #SYSTEM_USER_ID} when running outside a
     * security context (scheduled jobs, startup listeners, async tasks).
     */
    public static long currentUserIdOrSystem() {
        Long id = currentUserId();
        return id == null ? SYSTEM_USER_ID : id;
    }

    /**
     * Authenticated username, or {@link #SYSTEM_USERNAME} when running outside a
     * security context.
     */
    public static String currentUsernameOrSystem() {
        String name = currentUsername();
        return name == null ? SYSTEM_USERNAME : name;
    }
}
