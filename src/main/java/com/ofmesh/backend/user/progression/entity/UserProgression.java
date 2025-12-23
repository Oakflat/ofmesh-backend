package com.ofmesh.backend.user.progression.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_progression")
public class UserProgression {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "xp_total", nullable = false)
    private long xpTotal = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0;

    public UserProgression() {}

    public UserProgression(Long userId) {
        this.userId = userId;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getUserId() { return userId; }
    public long getXpTotal() { return xpTotal; }
    public int getLevel() { return level; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void apply(long newXpTotal, int newLevel) {
        this.xpTotal = Math.max(0, newXpTotal);
        this.level = Math.max(1, newLevel);
        this.updatedAt = OffsetDateTime.now();
    }
}
