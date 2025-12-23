package com.ofmesh.backend.user.progression.dto;

import java.time.OffsetDateTime;

public class ProgressionEventRequest {
    private String eventId;       // 幂等关键：建议 "POST_CREATED:12345"
    private Long userId;
    private String eventType;     // POST_CREATED / REPLY_CREATED ...
    private String source;        // forum / resource / admin ...
    private OffsetDateTime occurredAt;
    private Long deltaOverride;   // 可选：需要强制改分时用

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }

    public Long getDeltaOverride() { return deltaOverride; }
    public void setDeltaOverride(Long deltaOverride) { this.deltaOverride = deltaOverride; }
}
