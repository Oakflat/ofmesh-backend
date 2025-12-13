package com.ofmesh.backend.controller;

import com.ofmesh.backend.dto.GrantBadgeRequest;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import com.ofmesh.backend.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/badges")
public class AdminBadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    public AdminBadgeController(BadgeService badgeService, UserRepository userRepository) {
        this.badgeService = badgeService;
        this.userRepository = userRepository;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("未登录");

        String key = auth.getName(); // 通常是 username（JwtUtil 里 subject）
        return userRepository.findByUsername(key)
                .or(() -> userRepository.findByEmail(key))
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("无法识别当前操作者"));
    }

    @PostMapping("/grant")
    public ResponseEntity<String> grantBadge(@RequestBody GrantBadgeRequest request) {
        Long adminId = currentUserId();
        badgeService.grantBadge(request.getUserId(), request.getBadgeKey(), adminId);
        return ResponseEntity.ok("徽章发放成功！");
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<String> revokeBadge(@RequestParam Long userId, @RequestParam String badgeKey) {
        badgeService.revokeBadge(userId, badgeKey);
        return ResponseEntity.ok("徽章已撤销");
    }
}
