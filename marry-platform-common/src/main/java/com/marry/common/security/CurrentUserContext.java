package com.marry.common.security;

/**
 * Thread-local carrier for the currently authenticated user id.
 *
 * <p>Populated by the security module's JWT filter and read by infrastructure
 * code (e.g. MyBatis-Plus {@code MetaObjectHandler}). Living in {@code common}
 * keeps the persistence module free of any direct dependency on the security
 * module, avoiding a Maven module cycle.</p>
 *
 * <p>Mirrors the pattern used by {@link DataScopeContext}; filters should set
 * the value in a try/finally to guarantee {@link #clear()} on completion.</p>
 */
public final class CurrentUserContext {

    /** Sentinel for system-initiated writes (scheduled jobs, startup, seed). */
    public static final long SYSTEM_USER_ID = 0L;

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private CurrentUserContext() {}

    /** Bind the authenticated user id to the current thread. {@code null} clears. */
    public static void set(Long userId) {
        if (userId == null) {
            USER_ID.remove();
        } else {
            USER_ID.set(userId);
        }
    }

    /**
     * Returns the authenticated user id, or {@link #SYSTEM_USER_ID} when no user
     * has been bound (background jobs, async tasks, MQ consumers, etc.).
     */
    public static long currentUserIdOrSystem() {
        Long id = USER_ID.get();
        return id == null ? SYSTEM_USER_ID : id;
    }

    /** Returns the raw bound value, or {@code null} when none has been set. */
    public static Long currentUserIdOrNull() {
        return USER_ID.get();
    }

    /** Drop the binding for the current thread. Always invoke from a finally block. */
    public static void clear() {
        USER_ID.remove();
    }
}
