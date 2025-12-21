package com.ofmesh.backend.user.media.gc;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "avatar_gc_queue")
public class AvatarGcQueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "delete_after", nullable = false)
    private OffsetDateTime deleteAfter;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_error")
    private String lastError;

    @Column(name="object_type", nullable=false)
    private String objectType; // "AVATAR" / "BANNER"

    // getters/setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public OffsetDateTime getDeleteAfter() { return deleteAfter; }
    public void setDeleteAfter(OffsetDateTime deleteAfter) { this.deleteAfter = deleteAfter; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
