package com.ofmesh.backend.user.auth.dto;

import java.time.OffsetDateTime;

public class AuthBanResponse {
    public String code;
    public OffsetDateTime banUntil;
    public String banReason;

    public static AuthBanResponse banned(OffsetDateTime banUntil, String banReason) {
        AuthBanResponse response = new AuthBanResponse();
        response.code = "ACCOUNT_BANNED";
        response.banUntil = banUntil;
        response.banReason = banReason;
        return response;
    }
}
