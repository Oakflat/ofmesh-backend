package com.ofmesh.backend.dto;

import java.time.OffsetDateTime;

public class AuthErrorResponse {
    public String code;
    public String message;
    public OffsetDateTime banUntil;
    public String banReason;

    public static AuthErrorResponse banned(OffsetDateTime banUntil, String banReason) {
        AuthErrorResponse r = new AuthErrorResponse();
        r.code = "ACCOUNT_BANNED";
        r.message = "账号已被封禁";
        r.banUntil = banUntil;
        r.banReason = banReason;
        return r;
    }

    public static AuthErrorResponse badCredentials() {
        AuthErrorResponse r = new AuthErrorResponse();
        r.code = "BAD_CREDENTIALS";
        r.message = "账号或密码错误";
        return r;
    }
}
