package com.ofmesh.backend.user.media.gc;

import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class AvatarGcService {

    private static final Duration GRACE = Duration.ofDays(7);

    private final AvatarGcQueueRepository repo;
    private final UserRepository userRepository;

    public AvatarGcService(AvatarGcQueueRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    /** 只入队“上上张”，并确保当前没人引用它 */
    @Transactional
    public void enqueueIfNeeded(long userId, String oldPrevKey) {
        if (oldPrevKey == null || oldPrevKey.isBlank()) return;

        //  仍被引用（current/prev）就别入队
        if (userRepository.countAvatarKeyReferences(oldPrevKey) > 0) return;

        AvatarGcQueueItem item = new AvatarGcQueueItem();
        item.setUserId(userId);
        item.setObjectKey(oldPrevKey);
        item.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        item.setDeleteAfter(OffsetDateTime.now(ZoneOffset.UTC).plus(GRACE));
        item.setStatus("PENDING");
        item.setLastError(null);

        repo.save(item);
    }
}
