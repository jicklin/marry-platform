package com.marry.security.util;

import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience accessors for the current authenticated user.
 */
public final class SecurityUtil {

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
}