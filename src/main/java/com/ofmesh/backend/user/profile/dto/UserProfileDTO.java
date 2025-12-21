package com.ofmesh.backend.user.profile.dto;

import java.util.List;

public class UserProfileDTO {
    private Long id;
    private String username;
    private String avatar;
    private String role;
    private String bio;    // 目前数据库没这字段，先保留占位
    private Integer level; // 同上
    private String createdAt;
    private List<String> badges;
    private String bannerKey;
    private String bannerPrevKey;

    public String getBannerKey() { return bannerKey; }
    public void setBannerKey(String bannerKey) { this.bannerKey = bannerKey; }

    public String getBannerPrevKey() { return bannerPrevKey; }
    public void setBannerPrevKey(String bannerPrevKey) { this.bannerPrevKey = bannerPrevKey; }

    // === 手动 Getter / Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer level() { return level; } // 注意这里 getter 命名风格统一即可
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
}