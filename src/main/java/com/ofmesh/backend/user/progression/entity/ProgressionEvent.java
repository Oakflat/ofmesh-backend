package com.ofmesh.backend.user.progression.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "progression_event",
        uniqueConstraints = @UniqueConstraint(name = "uk_progression_event_event_id", columnNames = "event_id")
)
public class ProgressionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_id", nullable=false, length=128)
    private String eventId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="event_type", nullable=false, length=64)
    private String eventType;

    @Column(name="source", length=32)
    private String source;

    @Column(name="delta_xp", nullable=false)
    private long deltaXp;

    @Column(name="payload_json", columnDefinition="text")
    private String payloadJson;

    @Column(name="occurred_at")
    private OffsetDateTime occurredAt;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ProgressionEvent() {}

    public ProgressionEvent(String eventId, Long userId, String eventType, String source,
                            long deltaXp, String payloadJson, OffsetDateTime occurredAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.source = source;
        this.deltaXp = deltaXp;
        this.payloadJson = payloadJson;
        this.occurredAt = occurredAt;
        this.createdAt = OffsetDateTime.now();
    }
}
