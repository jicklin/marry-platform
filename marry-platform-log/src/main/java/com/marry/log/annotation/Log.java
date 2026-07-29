package com.marry.log.annotation;

import com.marry.log.enums.BusinessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method to be captured by {@link com.marry.log.aspect.OperLogAspect}.
 * Persists a {@code sys_oper_log} row after the method completes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /** Module / functional area name (e.g. "用户管理"). */
    String title();

    /** Operation type. */
    BusinessType businessType() default BusinessType.OTHER;

    /** Whether to save request parameters. */
    boolean saveParam() default true;

    /** Whether to save response result. */
    boolean saveResult() default true;

    /** Parameter names to exclude from logging (e.g. passwords). */
    String[] excludeParamNames() default {"password", "oldPassword", "newPassword"};
}