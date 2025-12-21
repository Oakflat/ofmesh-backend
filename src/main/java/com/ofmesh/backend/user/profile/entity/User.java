package com.ofmesh.backend.user.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter

public class User implements UserDetails {
    // 头像：对外展示 URL（保留你现在字段名 avatar，不动前端/DTO）
    private String avatar;

    @Column(name = "banner_key")
    private String bannerKey;

    @Column(name = "banner_prev_key")
    private String bannerPrevKey;

    @Column(name = "banner_updated_at")
    private OffsetDateTime bannerUpdatedAt;

    // ✅ 新增：内部 objectKey（用于校验/回滚/GC）
    @Column(name = "avatar_key")
    private String avatarKey;

    @Column(name = "avatar_prev_key")
    private String avatarPrevKey;

    @Column(name = "avatar_updated_at")
    private OffsetDateTime avatarUpdatedAt;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "mc_uuid")
    private String mcUuid;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ====== Account status / ban fields ======

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    /**
     * 统一 UTC 时间轴：
     * - null => 永久封禁
     * - 非 null => 封禁截止时间（timestamptz）
     */
    @Column(name = "ban_until")
    private OffsetDateTime banUntil;

    @Column(name = "ban_reason", columnDefinition = "text")
    private String banReason;

    /**
     * 注册时间：timestamptz（UTC 时间轴）
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    // === 手动 Getter / Setter（不依赖 Lombok） ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getMcUuid() { return mcUuid; }
    public void setMcUuid(String mcUuid) { this.mcUuid = mcUuid; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; } // 可选：一般不建议外部修改

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public OffsetDateTime getBanUntil() { return banUntil; }
    public void setBanUntil(OffsetDateTime banUntil) { this.banUntil = banUntil; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    // ===== helper =====

    /**
     * 当前是否处于封禁状态（统一按 UTC 判断，避免服务器时区导致误判）
     */
    private boolean isCurrentlyBanned() {
        if (accountStatus != AccountStatus.BANNED) return false;

        // banUntil == null => 永久封禁
        if (banUntil == null) return true;

        // banUntil 在未来 => 仍在封禁期
        return banUntil.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    // === Security UserDetails 接口实现 ===

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    // ✅ 封禁 => locked
    @Override
    public boolean isAccountNonLocked() {
        return !isCurrentlyBanned();
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // ✅ 封禁 => disabled
    @Override
    public boolean isEnabled() {
        return !isCurrentlyBanned();
    }

}
