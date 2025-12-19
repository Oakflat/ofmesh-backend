package com.ofmesh.backend.admin.badge.dto;

public class GrantBadgeRequest {
    private Long userId;
    private String badgeKey;

    // 手动 Getter / Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBadgeKey() { return badgeKey; }
    public void setBadgeKey(String badgeKey) { this.badgeKey = badgeKey; }
}