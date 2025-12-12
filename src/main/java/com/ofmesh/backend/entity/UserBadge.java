package com.ofmesh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_key", nullable = false)
    private String badgeKey;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "granted_by")
    private Long grantedBy; // 0=系统

    // --- 构造方法 ---
    public UserBadge() {
    }

    // 方便新建对象的构造器
    public UserBadge(Long userId, String badgeKey) {
        this.userId = userId;
        this.badgeKey = badgeKey;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = 0L; // 默认为系统发放
    }

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBadgeKey() { return badgeKey; }
    public void setBadgeKey(String badgeKey) { this.badgeKey = badgeKey; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public Long getGrantedBy() { return grantedBy; }
    public void setGrantedBy(Long grantedBy) { this.grantedBy = grantedBy; }
}