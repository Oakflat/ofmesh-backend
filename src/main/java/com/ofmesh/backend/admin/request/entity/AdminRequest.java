package com.ofmesh.backend.admin.request.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_requests")
public class AdminRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminRequestStatus status;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(columnDefinition = "text")
    private String payload; // JSON string

    @Column(columnDefinition = "text")
    private String reason;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "executed_at")
    private OffsetDateTime executedAt;

    @Column(name = "result_message", columnDefinition = "text")
    private String resultMessage;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = AdminRequestStatus.PENDING;
    }

    // --- getters/setters ---
    public Long getId() { return id; }
    public AdminRequestType getType() { return type; }
    public void setType(AdminRequestType type) { this.type = type; }
    public AdminRequestStatus getStatus() { return status; }
    public void setStatus(AdminRequestStatus status) { this.status = status; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public Long getExecutedBy() { return executedBy; }
    public void setExecutedBy(Long executedBy) { this.executedBy = executedBy; }
    public OffsetDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
}
