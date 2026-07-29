package com.marry.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a Mapper method to be intercepted by
 * {@link com.marry.persistence.config.DataScopeInnerInterceptor} so that
 * role-based data-scope WHERE clauses are spliced automatically.
 *
 * <p>Tables annotated without this annotation are unaffected.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /** Table alias in the SQL to which the WHERE clause is appended. */
    String alias() default "";

    /** Dept column name (default "dept_id"). */
    String deptColumn() default "dept_id";

    /** Create-by column name (default "create_by"). */
    String userColumn() default "create_by";
}