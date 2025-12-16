package com.ofmesh.backend.service;

import com.ofmesh.backend.entity.Role;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final OnlineService onlineService;

    public AdminStatsService(UserRepository userRepository, OnlineService onlineService) {
        this.userRepository = userRepository;
        this.onlineService = onlineService;
    }

    public Map<String, Object> overview() {
        long total = userRepository.count();
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        long new24h = userRepository.countByCreatedAtAfter(nowUtc.minusHours(24));
        long new7d = userRepository.countByCreatedAtAfter(nowUtc.minusDays(7));

        // ✅ key 用 String（Role.name()），避免 EnumMap 泛型问题，同时 JSON 友好
        Map<String, Long> roleCounts = new HashMap<>();
        for (Role r : Role.values()) {
            roleCounts.put(r.name(), userRepository.countByRole(r));
        }

        long adminCount = roleCounts.getOrDefault("ADMIN", 0L);
        long modCount = roleCounts.getOrDefault("MODERATOR", 0L);
        long privilegedTotal = adminCount + modCount;

        int windowSec = 120;
        long onlineNow = onlineService.countOnline(windowSec);

        Map<String, Object> users = new HashMap<>();
        users.put("total", total);
        users.put("newLast24h", new24h);
        users.put("newLast7d", new7d);
        users.put("roles", roleCounts);

        Map<String, Object> privileged = new HashMap<>();
        privileged.put("total", privilegedTotal);
        privileged.put("byRole", Map.of("MODERATOR", modCount, "ADMIN", adminCount));

        Map<String, Object> online = new HashMap<>();
        online.put("now", onlineNow);
        online.put("windowSec", windowSec);

        return Map.of(
                "users", users,
                "online", online,
                "privileged", privileged
        );
    }
}
