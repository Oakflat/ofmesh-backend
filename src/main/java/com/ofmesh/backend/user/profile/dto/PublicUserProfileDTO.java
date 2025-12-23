package com.ofmesh.backend.user.profile.dto;

import java.util.List;

public class PublicUserProfileDTO {
    private Long id;
    private String username;
    private String avatar;   // public URL（你实体里已存）
    private String banner;   // public URL（由 bannerKey 转换）

    private String bio;
    private Integer level;
    private String createdAt;
    private List<String> badges;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }


    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
}
