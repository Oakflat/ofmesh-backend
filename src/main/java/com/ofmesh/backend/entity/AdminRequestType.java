package com.ofmesh.backend.entity;

public enum AdminRequestType {
    PASSWORD_RESET,
    USER_BAN,
    USER_UNBAN,        // ✅ 新增：解封
    USER_BAN_UPDATE,   // ✅ 可选：延长/缩短封禁（如果你们要做）
    BADGE_GRANT
}
