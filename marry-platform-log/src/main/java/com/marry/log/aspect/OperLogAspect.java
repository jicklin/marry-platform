package com.marry.log.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.marry.common.util.IpUtils;
import com.marry.domain.entity.SysOperLog;
import com.marry.log.annotation.Log;
import com.marry.log.service.OperLogService;
import com.marry.security.model.LoginUser;
import com.marry.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AOP aspect that captures operation logs for methods annotated with {@link Log}.
 * Storage is asynchronous to avoid blocking the request thread.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogService operLogService;

    @Around("@annotation(logAnn)")
    public Object around(ProceedingJoinPoint pjp, Log logAnn) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable err = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            err = t;
            throw t;
        } finally {
            try {
                long cost = System.currentTimeMillis() - start;
                save(logAnn, pjp, result, err, cost);
            } catch (Exception ex) {
                log.warn("Failed to record operation log: {}", ex.getMessage());
            }
        }
    }

    @Async
    public void save(Log logAnn, ProceedingJoinPoint pjp, Object result, Throwable err, long cost) {
        SysOperLog row = new SysOperLog();
        row.setTitle(logAnn.title());
        row.setBusinessType(logAnn.businessType().name());

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        row.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs == null ? null : attrs.getRequest();
        if (request != null) {
            row.setRequestMethod(request.getMethod());
            row.setOperUrl(request.getRequestURI());
            row.setOperIp(IpUtils.getClientIp(request));
            row.setUserAgent(request.getHeader("User-Agent"));
        }

        // params
        if (logAnn.saveParam()) {
            try {
                Object[] args = pjp.getArgs();
                List<String> exclude = Arrays.asList(logAnn.excludeParamNames());
                String param = formatArgs(args, exclude);
                if (param.length() > 2000) {
                    param = param.substring(0, 2000) + "...(truncated)";
                }
                row.setOperParam(param);
            } catch (Exception ignored) {}
        }
        // result
        if (logAnn.saveResult() && result != null) {
            try {
                String json = JSONUtil.toJsonStr(result);
                if (json != null && json.length() > 2000) {
                    json = json.substring(0, 2000) + "...(truncated)";
                }
                row.setJsonResult(json);
            } catch (Exception ignored) {}
        }

        // user
        LoginUser u = SecurityUtil.currentUser();
        if (u != null) {
            row.setOperId(u.getUserId());
            row.setOperName(u.getUsername());
            row.setDeptId(u.getDeptId());
            row.setDeptName(u.getDeptName());
        }

        row.setStatus(err == null ? 1 : 0);
        if (err != null) {
            String msg = err.getMessage();
            if (msg != null && msg.length() > 2000) {
                msg = msg.substring(0, 2000);
            }
            row.setErrorMsg(msg);
        }
        row.setCostTime(cost);

        operLogService.save(row);
    }

    private String formatArgs(Object[] args, List<String> excludeParamNames) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .filter(a -> !(a instanceof MultipartFile) && !(a instanceof jakarta.servlet.ServletRequest))
                .map(a -> {
                    String s;
                    try {
                        s = a == null ? "null" : JSONUtil.toJsonStr(a);
                    } catch (Exception e) {
                        s = String.valueOf(a);
                    }
                    // crude mask: if key contains password, replace value
                    for (String ex : excludeParamNames) {
                        if (StrUtil.isNotBlank(ex) && s.toLowerCase().contains("\"" + ex.toLowerCase() + "\"")) {
                            s = s.replaceAll("(?i)(\"" + ex + "\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
                        }
                    }
                    return s;
                })
                .collect(Collectors.joining(", "));
    }
}