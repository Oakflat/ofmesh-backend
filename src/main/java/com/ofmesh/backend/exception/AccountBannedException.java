package com.ofmesh.backend.exception;

import org.springframework.security.authentication.DisabledException;

import java.time.OffsetDateTime;

public class AccountBannedException extends DisabledException {

    private final OffsetDateTime banUntil; // null = 永久
    private final String banReason;

    public AccountBannedException(OffsetDateTime banUntil, String banReason) {
        super("账号已被封禁");
        this.banUntil = banUntil;
        this.banReason = banReason;
    }

    public OffsetDateTime getBanUntil() {
        return banUntil;
    }

    public String getBanReason() {
        return banReason;
    }
}
