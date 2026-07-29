package com.marry.common.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Minimal browser/OS detection from User-Agent header. Suitable for login logs.
 */
public final class UserAgentUtils {

    private UserAgentUtils() {}

    public static String getBrowser(HttpServletRequest request) {
        String ua = getUA(request);
        if (StrUtil.isBlank(ua)) {
            return "Unknown";
        }
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/")) return "Safari";
        if (ua.contains("MSIE") || ua.contains("Trident/")) return "IE";
        return "Other";
    }

    public static String getOs(HttpServletRequest request) {
        String ua = getUA(request);
        if (StrUtil.isBlank(ua)) {
            return "Unknown";
        }
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Other";
    }

    private static String getUA(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }
}