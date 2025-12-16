package com.ofmesh.backend.dto;

import java.time.OffsetDateTime;

public class AdminUserDTO {
    public Long id;
    public String username;
    public String email;
    public String role;

    public String accountStatus;
    public OffsetDateTime banUntil;
    public String banReason;

    public OffsetDateTime createdAt; // ✅ 注册时间（UTC 时间轴）
}
