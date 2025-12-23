package com.ofmesh.backend.user.media.service;

import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class UserAvatarWriteFacade {

    private final UserRepository userRepository;

    public UserAvatarWriteFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** commit 用：oldCurrent 给前端回滚提示，oldPrev 给 GC */
    public record AvatarShiftResult(String oldCurrentKey, String oldPrevKey) {}

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object p = auth.getPrincipal();
        if (p instanceof User u) {
            return u.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + p.getClass());
    }

    /**
     *  只保留 current + prev
     * - oldCurrent：上一张（会被写入 prev）
     * - oldPrev：上上张（应该进 GC，7 天后删）
     */
    @Transactional
    public AvatarShiftResult updateAvatarAndReturnOldKeys(Long userId, String newKey, String newUrl) {
        //  行锁，防止并发连点导致指针乱序
        User u = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldCurrent = u.getAvatarKey();
        String oldPrev = u.getAvatarPrevKey();

        int updated = userRepository.updateAvatarAndShiftPrev(
                userId,
                newKey,
                newUrl,
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        if (updated == 0) {
            throw new IllegalStateException("Update avatar failed");
        }

        return new AvatarShiftResult(oldCurrent, oldPrev);
    }

    /** rollback：交换 current / prev（仍保留两张，不入 GC） */
    @Transactional
    public String rollbackToPrev(Long userId, String newUrl) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int updated = userRepository.rollbackAvatarToPrev(
                userId,
                newUrl,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (updated == 0) {
            throw new IllegalStateException("No previous avatar to rollback");
        }
        return userRepository.getAvatarKey(userId);
    }

    public String getCurrentAvatarKey(Long userId) {
        return userRepository.getAvatarKey(userId);
    }

    public String getPrevAvatarKey(Long userId) {
        return userRepository.getAvatarPrevKey(userId);
    }
    @Transactional
    public AvatarShiftResult updateBannerAndReturnOldKeys(Long userId, String newKey) {
        // 锁行
        User u = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldCurrent = u.getBannerKey();
        String oldPrev = u.getBannerPrevKey();

        int updated = userRepository.updateBannerAndShiftPrev(
                userId,
                newKey,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (updated == 0) throw new IllegalStateException("Update banner failed");

        return new AvatarShiftResult(oldCurrent, oldPrev);
    }

    @Transactional
    public String rollbackBannerToPrev(Long userId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int updated = userRepository.rollbackBannerToPrev(
                userId,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (updated == 0) throw new IllegalStateException("No previous banner");

        return userRepository.getBannerKey(userId);
    }

    public String getPrevBannerKey(Long userId) {
        return userRepository.getBannerPrevKey(userId);
    }

}
