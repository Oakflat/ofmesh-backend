package com.ofmesh.backend.service.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.entity.AdminRequest;
import com.ofmesh.backend.entity.AdminRequestType;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.service.BadgeService;
import org.springframework.stereotype.Component;

@Component
public class BadgeGrantExecutor implements AdminRequestExecutor {

    private final BadgeService badgeService;
    private final ObjectMapper objectMapper;

    public BadgeGrantExecutor(BadgeService badgeService, ObjectMapper objectMapper) {
        this.badgeService = badgeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminRequestType supports() {
        return AdminRequestType.BADGE_GRANT;
    }

    @Override
    public String execute(AdminRequest request, User executor) {
        try {
            JsonNode node = objectMapper.readTree(request.getPayload() == null ? "{}" : request.getPayload());
            String badgeKey = node.path("badgeKey").asText(null);
            if (badgeKey == null || badgeKey.isBlank()) throw new RuntimeException("payload 缺少 badgeKey");

            badgeService.grantBadge(request.getTargetUserId(), badgeKey, executor.getId());
            return "徽章已发放: " + badgeKey;
        } catch (Exception e) {
            throw new RuntimeException("执行 BADGE_GRANT 失败: " + e.getMessage());
        }
    }
}
