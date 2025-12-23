package com.ofmesh.backend.user.media.gc;

import com.ofmesh.backend.user.media.service.AvatarService;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class AvatarGcJob {

    private final AvatarGcQueueRepository repo;
    private final UserRepository userRepository;
    private final AvatarService avatarService;

    public AvatarGcJob(AvatarGcQueueRepository repo, UserRepository userRepository, AvatarService avatarService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.avatarService = avatarService;
    }

    @Scheduled(fixedDelayString = "PT5M") // 每 5 分钟跑一次
    @Transactional
    public void run() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<AvatarGcQueueItem> due = repo.findDue(now, PageRequest.of(0, 200));

        for (AvatarGcQueueItem item : due) {
            String key = item.getObjectKey();

            //  安全网：仍被引用就别删
            if (userRepository.countAvatarKeyReferences(key) > 0) {
                item.setStatus("SKIPPED");
                item.setLastError("still referenced by users");
                continue;
            }

            try {
                avatarService.deleteObjectIgnoreNotFound(key);
                item.setStatus("DELETED");
                item.setLastError(null);
            } catch (Exception e) {
                item.setLastError(e.getMessage());
                item.setDeleteAfter(now.plusMinutes(10)); // 简单退避重试
            }
        }
    }
}
