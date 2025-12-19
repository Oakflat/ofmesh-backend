package com.ofmesh.backend.admin.health.controller;

import com.ofmesh.backend.admin.health.service.AdminHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminHealthController {

    private final AdminHealthService adminHealthService;

    public AdminHealthController(AdminHealthService adminHealthService) {
        this.adminHealthService = adminHealthService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return adminHealthService.health();
    }
}
