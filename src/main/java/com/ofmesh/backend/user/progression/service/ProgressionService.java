package com.ofmesh.backend.user.progression.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.user.progression.config.ProgressionProperties;
import com.ofmesh.backend.user.progression.dto.ProgressionDTO;
import com.ofmesh.backend.user.progression.dto.ProgressionEventRequest;
import com.ofmesh.backend.user.progression.entity.ProgressionEvent;
import com.ofmesh.backend.user.progression.entity.UserProgression;
import com.ofmesh.backend.user.progression.repository.ProgressionEventRepository;
import com.ofmesh.backend.user.progression.repository.UserProgressionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProgressionService {

    private final UserProgressionRepository progressionRepo;
    private final ProgressionEventRepository eventRepo;
    private final ProgressionProperties props;
    private final ObjectMapper objectMapper;
    private final LevelingStrategy leveling;

    public ProgressionService(UserProgressionRepository progressionRepo,
                              ProgressionEventRepository eventRepo,
                              ProgressionProperties props,
                              ObjectMapper objectMapper) {
        this.progressionRepo = progressionRepo;
        this.eventRepo = eventRepo;
        this.props = props;
        this.objectMapper = objectMapper;
        this.leveling = new ThresholdLevelingStrategy(props.getLevelThresholds());
    }

    @Transactional(readOnly = true)
    public ProgressionDTO getOrInit(Long userId) {
        UserProgression p = progressionRepo.findById(userId).orElseGet(() -> new UserProgression(userId));
        int level = leveling.levelForXp(p.getXpTotal());
        long next = leveling.nextLevelXp(level);
        return ProgressionDTO.of(userId, p.getXpTotal(), level, next);
    }

    @Transactional
    public ProgressionDTO applyEvent(ProgressionEventRequest req) {
        validate(req);

        long delta = resolveDelta(req.getEventType(), req.getDeltaOverride());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            payloadJson = null;
        }

        // 1) 先写事件（唯一键保证幂等）
        try {
            eventRepo.save(new ProgressionEvent(
                    req.getEventId(),
                    req.getUserId(),
                    req.getEventType(),
                    req.getSource(),
                    delta,
                    payloadJson,
                    req.getOccurredAt()
            ));
        } catch (DataIntegrityViolationException dup) {
            // 重复事件：直接返回现状
            return getOrInit(req.getUserId());
        }

        // 2) 锁住 progression 行，防并发乱加
        UserProgression p = progressionRepo.findForUpdate(req.getUserId())
                .orElseGet(() -> new UserProgression(req.getUserId()));

        long newXp = Math.max(0, p.getXpTotal() + delta);
        int newLevel = leveling.levelForXp(newXp);
        p.apply(newXp, newLevel);

        progressionRepo.save(p);

        long next = leveling.nextLevelXp(newLevel);
        return ProgressionDTO.of(p.getUserId(), p.getXpTotal(), p.getLevel(), next);
    }

    private long resolveDelta(String eventType, Long override) {
        if (override != null) return override;
        Map<String, Long> rules = props.getRules();
        if (rules == null) return 0;
        return rules.getOrDefault(eventType, 0L);
    }

    private void validate(ProgressionEventRequest req) {
        if (req.getEventId() == null || req.getEventId().isBlank())
            throw new IllegalArgumentException("eventId 不能为空（用于幂等）");
        if (req.getUserId() == null)
            throw new IllegalArgumentException("userId 不能为空");
        if (req.getEventType() == null || req.getEventType().isBlank())
            throw new IllegalArgumentException("eventType 不能为空");
    }
}
