package com.ofmesh.backend.admin.stats.controller;

import com.ofmesh.backend.admin.stats.service.AdminStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return adminStatsService.overview();
    }
}
