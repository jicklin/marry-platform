package com.marry.common.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Thread-local carrier for the current user's data-scope values.
 * Populated by {@code DataScopeFilter} (or any other auth/permission layer)
 * and read by {@code DataScopeInnerInterceptor}.
 *
 * <p>This decouples the persistence module from the security module.</p>
 */
public final class DataScopeContext {

    private static final ThreadLocal<List<Integer>> SCOPES = ThreadLocal.withInitial(Collections::emptyList);
    private static final ThreadLocal<Set<Long>> CUSTOM_DEPTS = ThreadLocal.withInitial(Collections::emptySet);
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Long> DEPT_ID = new ThreadLocal<>();

    private DataScopeContext() {}

    public static void set(List<Integer> scopes, Set<Long> customDepts, String username, Long deptId) {
        SCOPES.set(scopes == null ? Collections.emptyList() : scopes);
        CUSTOM_DEPTS.set(customDepts == null ? Collections.emptySet() : customDepts);
        USERNAME.set(username);
        DEPT_ID.set(deptId);
    }

    public static List<Integer> getScopes() {
        return SCOPES.get();
    }

    public static Set<Long> getCustomDepts() {
        return CUSTOM_DEPTS.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static Long getDeptId() {
        return DEPT_ID.get();
    }

    public static void clear() {
        SCOPES.remove();
        CUSTOM_DEPTS.remove();
        USERNAME.remove();
        DEPT_ID.remove();
    }
}