package com.ofmesh.backend.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminHealthService {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public AdminHealthService(JdbcTemplate jdbcTemplate, StringRedisTemplate stringRedisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> db = checkDb();
        Map<String, Object> redis = checkRedis();

        boolean dbOk = Boolean.TRUE.equals(db.get("ok"));
        boolean redisOk = Boolean.TRUE.equals(redis.get("ok"));

        result.put("status", (dbOk && redisOk) ? "UP" : "DOWN");
        result.put("db", db);
        result.put("redis", redis);

        // 你 AuthService 里用过 email:circuit_breaker 作为熔断 key
        Boolean breaker = stringRedisTemplate.hasKey("email:circuit_breaker");
        result.put("mailCircuitBreaker", Map.of("enabled", Boolean.TRUE.equals(breaker)));

        return result;
    }

    private Map<String, Object> checkDb() {
        long t0 = System.currentTimeMillis();
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long ms = System.currentTimeMillis() - t0;
            return Map.of("ok", one != null && one == 1, "latencyMs", ms);
        } catch (DataAccessException ex) {
            long ms = System.currentTimeMillis() - t0;
            return Map.of("ok", false, "latencyMs", ms, "error", ex.getClass().getSimpleName());
        }
    }

    private Map<String, Object> checkRedis() {
        long t0 = System.currentTimeMillis();
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            long ms = System.currentTimeMillis() - t0;
            return Map.of("ok", "PONG".equalsIgnoreCase(pong), "latencyMs", ms);
        } catch (Exception ex) {
            long ms = System.currentTimeMillis() - t0;
            return Map.of("ok", false, "latencyMs", ms, "error", ex.getClass().getSimpleName());
        }
    }
}
