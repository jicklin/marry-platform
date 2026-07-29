package com.marry.common.core.domain;

/**
 * Business error codes used across the platform.
 */
public enum BizCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "认证失败"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_INVALID(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    ROLE_EXISTS(1005, "角色编码已存在"),
    ROLE_HAS_USERS(1006, "角色已分配用户，无法删除"),
    MENU_HAS_ROLES(1007, "菜单已分配角色，无法删除"),
    DEPT_HAS_USERS(1008, "部门存在用户，无法删除"),

    TOKEN_EXPIRED(2001, "令牌已过期"),
    TOKEN_INVALID(2002, "令牌无效"),
    TOKEN_BLACKLIST(2003, "令牌已注销"),

    DATA_SCOPE_ERROR(3001, "数据权限不足");

    private final int code;
    private final String msg;

    BizCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}