package com.ofmesh.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatar;

    @Column(name = "mc_uuid")
    private String mcUuid;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ====== Account status / ban fields ======

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "ban_until")
    private LocalDateTime banUntil;

    @Column(name = "ban_reason", columnDefinition = "text")
    private String banReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // === 手动生成的 Getter 和 Setter (专治 Lombok 不生效) ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getMcUuid() { return mcUuid; }
    public void setMcUuid(String mcUuid) { this.mcUuid = mcUuid; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getBanUntil() { return banUntil; }
    public void setBanUntil(LocalDateTime banUntil) { this.banUntil = banUntil; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    // ===== helper =====
    private boolean isCurrentlyBanned() {
        if (accountStatus != AccountStatus.BANNED) return false;
        // banUntil == null => 永久封禁
        if (banUntil == null) return true;
        // banUntil 未来 => 仍在封禁期
        return banUntil.isAfter(LocalDateTime.now());
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

    // ✅ 绑定封禁态
    @Override
    public boolean isAccountNonLocked() {
        return !isCurrentlyBanned();
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // ✅ 绑定封禁态
    @Override
    public boolean isEnabled() {
        return !isCurrentlyBanned();
    }
}
