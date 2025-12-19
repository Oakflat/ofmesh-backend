package com.ofmesh.backend.common.status;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        // 最小探活：只表示服务进程在跑
        return Map.of("status", "UP");
    }
}
