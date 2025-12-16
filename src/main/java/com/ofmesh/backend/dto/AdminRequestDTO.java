package com.ofmesh.backend.dto;

import java.time.OffsetDateTime;

public class AdminRequestDTO {
    public Long id;
    public String type;
    public String status;
    public Long targetUserId;
    public String payload; // JSON string
    public String reason;

    public Long createdBy;
    public OffsetDateTime createdAt;

    public Long approvedBy;
    public OffsetDateTime approvedAt;

    public Long executedBy;
    public OffsetDateTime executedAt;

    public String resultMessage;
}
