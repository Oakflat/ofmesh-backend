package com.ofmesh.backend.entity;

public enum Role {
    USER,
    STAFF,        // 未来发起工单，但不能直接改权限
    MODERATOR,
    ADMIN,
    SUPER_ADMIN   // 最高权限（比如 uid=0001）
}
