package com.ofmesh.backend.dto;

import java.util.Map;

public class CreateAdminRequestRequest {
    private String type;
    private Long targetUserId;
    private Map<String, Object> payload;
    private String reason;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
