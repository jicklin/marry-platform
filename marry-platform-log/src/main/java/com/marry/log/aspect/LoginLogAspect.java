package com.marry.log.aspect;

import cn.hutool.core.util.StrUtil;
import com.marry.common.util.IpUtils;
import com.marry.common.util.UserAgentUtils;
import com.marry.domain.entity.SysLoginLog;
import com.marry.persistence.mapper.SysLoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Captures login success and failure for any controller method whose name starts with
 * "login" or accepts a parameter of type LoginDTO. Persists to {@code sys_login_log}.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoginLogAspect {

    private final SysLoginLogMapper loginLogMapper;

    @Pointcut("execution(* com.marry..controller..*.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @AfterReturning(value = "execution(* com.marry..controller.AuthController.login(..))", returning = "ret")
    public void onSuccess(JoinPoint jp, Object ret) {
        recordLogin(jp, "SUCCESS", null);
    }

    @AfterThrowing(value = "execution(* com.marry..controller.AuthController.login(..))", throwing = "ex")
    public void onFail(JoinPoint jp, Throwable ex) {
        recordLogin(jp, "FAIL", ex.getMessage());
    }

    @Async
    public void recordLogin(JoinPoint jp, String status, String message) {
        try {
            String username = extractUsername(jp);
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs == null ? null : attrs.getRequest();

            SysLoginLog row = new SysLoginLog();
            row.setUserName(username);
            row.setIp(req == null ? null : IpUtils.getClientIp(req));
            row.setUserAgent(req == null ? null : req.getHeader("User-Agent"));
            row.setBrowser(UserAgentUtils.getBrowser(req));
            row.setOs(UserAgentUtils.getOs(req));
            row.setStatus(status);
            row.setMessage(StrUtil.maxLength(message == null ? (status.equals("SUCCESS") ? "登录成功" : "登录失败") : message, 500));
            loginLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("Failed to write login log: {}", e.getMessage());
        }
    }

    private String extractUsername(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args == null) return null;
        for (Object a : args) {
            if (a == null) continue;
            // Best-effort reflection to fetch "username"
            try {
                Method m = a.getClass().getMethod("getUsername");
                Object u = m.invoke(a);
                return u == null ? null : u.toString();
            } catch (Exception ignored) {}
        }
        return null;
    }
}