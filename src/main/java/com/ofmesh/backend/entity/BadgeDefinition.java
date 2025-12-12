package com.ofmesh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badge_definitions")
public class BadgeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key; // 唯一标识，如 "founder"

    @Column(nullable = false)
    private String category; // identity, contribution, honor

    @Column(name = "display_name_zh", nullable = false)
    private String displayNameZh;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String icon;

    private Integer priority;

    @Column(name = "is_exclusive")
    private Boolean isExclusive;

    @Column(name = "style_config", columnDefinition = "TEXT")
    private String styleConfig; // 暂时存 JSON 字符串

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- 构造方法 ---
    public BadgeDefinition() {
    }

    // --- Getter / Setter (手动生成) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDisplayNameZh() { return displayNameZh; }
    public void setDisplayNameZh(String displayNameZh) { this.displayNameZh = displayNameZh; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIsExclusive() { return isExclusive; }
    public void setIsExclusive(Boolean exclusive) { isExclusive = exclusive; }

    public String getStyleConfig() { return styleConfig; }
    public void setStyleConfig(String styleConfig) { this.styleConfig = styleConfig; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    // 这是一个生命周期回调，插入前自动设置时间
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.priority == null) this.priority = 0;
        if (this.isExclusive == null) this.isExclusive = false;
        if (this.isActive == null) this.isActive = true;
    }
}