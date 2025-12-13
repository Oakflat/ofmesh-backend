package com.ofmesh.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OnlineService {

    private static final String KEY = "online:zset";

    private final StringRedisTemplate redis;

    public OnlineService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void heartbeat(String member) {
        long now = Instant.now().getEpochSecond();
        redis.opsForZSet().add(KEY, member, now);
        // 清理过期成员（可选但建议做）
        redis.opsForZSet().removeRangeByScore(KEY, 0, now - 3600);
    }

    public long countOnline(int windowSec) {
        long now = Instant.now().getEpochSecond();
        Long cnt = redis.opsForZSet().count(KEY, now - windowSec, now + 1);
        return cnt == null ? 0 : cnt;
    }
}
