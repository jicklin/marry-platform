package com.marry.common.core.domain;

/**
 * Unified API response wrapper.
 * <p>
 * Convention: {@code code == 0} for success, non-zero for failure.
 * </p>
 *
 * @param <T> data type
 */
public record R<T>(int code, String msg, T data) {

    public static final int SUCCESS_CODE = 0;

    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, "ok", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, "ok", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS_CODE, msg, data);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(BizCode biz) {
        return new R<>(biz.getCode(), biz.getMsg(), null);
    }

    public static <T> R<T> fail(BizCode biz, String msg) {
        return new R<>(biz.getCode(), msg, null);
    }

    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }
}