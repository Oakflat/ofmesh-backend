package com.ofmesh.backend.controller;

import com.ofmesh.backend.dto.GrantBadgeRequest;
import com.ofmesh.backend.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/badges")
public class AdminBadgeController {

    private final BadgeService badgeService;

    public AdminBadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * 手动发放徽章接口
     * POST /api/admin/badges/grant
     * Body: { "userId": 1, "badgeKey": "founder" }
     */
    @PostMapping("/grant")
    public ResponseEntity<String> grantBadge(@RequestBody GrantBadgeRequest request) {
        // 1. 获取当前操作管理员的 ID (从 SecurityContext 拿)
        // 这一步是为了审计：必须知道是谁点击了发放按钮
        // 假设 UserDetails 里的 username 是 ID 或者能查到 ID，这里简化处理：
        // Long adminId = currentUserService.getId();
        Long adminId = 1L; // ⚠️ 初期狂野模式：暂时写死为 1号管理员，后期对接真实 ID

        try {
            badgeService.grantBadge(request.getUserId(), request.getBadgeKey(), adminId);
            return ResponseEntity.ok("徽章发放成功！");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 撤销徽章
     * DELETE /api/admin/badges/revoke?userId=1&badgeKey=founder
     */
    @DeleteMapping("/revoke")
    public ResponseEntity<String> revokeBadge(@RequestParam Long userId, @RequestParam String badgeKey) {
        badgeService.revokeBadge(userId, badgeKey);
        return ResponseEntity.ok("徽章已撤销");
    }
}