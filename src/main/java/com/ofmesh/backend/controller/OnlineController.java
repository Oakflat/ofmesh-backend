package com.ofmesh.backend.controller;

import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.service.OnlineService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/online")
public class OnlineController {

    private final OnlineService onlineService;

    public OnlineController(OnlineService onlineService) {
        this.onlineService = onlineService;
    }

    @PostMapping("/heartbeat")
    public Map<String, Object> heartbeat() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        // 你的 principal 基本就是 User 实体（实现了 UserDetails）
        String member;
        if (principal instanceof User u) {
            member = String.valueOf(u.getId());
        } else {
            member = auth.getName(); // fallback：username
        }

        onlineService.heartbeat(member);
        return Map.of("ok", true);
    }
}
