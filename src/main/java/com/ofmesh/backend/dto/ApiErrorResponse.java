package com.ofmesh.backend.dto;

import java.time.OffsetDateTime;

public class ApiErrorResponse {
    public String code;        // e.g. ACCOUNT_BANNED / BAD_CREDENTIALS / FORBIDDEN
    public String message;     // 给用户看的提示

    // 可选：封禁场景用
    public OffsetDateTime banUntil; // null = 永久 或 非封禁
    public String banReason;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiErrorResponse(String code, String message, OffsetDateTime banUntil, String banReason) {
        this.code = code;
        this.message = message;
        this.banUntil = banUntil;
        this.banReason = banReason;
    }
}
