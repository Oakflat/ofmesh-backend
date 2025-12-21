package com.ofmesh.backend.user.media.controller;

import com.ofmesh.backend.user.media.dto.AvatarCommitRequest;
import com.ofmesh.backend.user.media.dto.AvatarUploadUrlRequest;
import com.ofmesh.backend.user.media.dto.AvatarUploadUrlResponse;
import com.ofmesh.backend.user.media.service.AvatarService;
import com.ofmesh.backend.user.media.service.UserAvatarWriteFacade;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users/me/banner")
public class UserBannerController {

    private final AvatarService avatarService;
    private final UserAvatarWriteFacade writeFacade;
    private final UserRepository userRepository;

    public UserBannerController(AvatarService avatarService,
                                UserAvatarWriteFacade writeFacade,
                                UserRepository userRepository) {
        this.avatarService = avatarService;
        this.writeFacade = writeFacade;
        this.userRepository = userRepository;
    }

    /**
     * ✅ 查询当前 banner（给前端渲染用）
     * GET /users/me/banner
     */
    @GetMapping
    public Map<String, Object> getCurrentBanner() {
        Long userId = writeFacade.currentUserId();

        String key = userRepository.getBannerKey(userId);
        String prevKey = userRepository.getBannerPrevKey(userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("bannerKey", key);
        resp.put("bannerUrl", (key == null || key.isBlank()) ? null : avatarService.buildPublicUrl(key));
        resp.put("prevBannerKey", prevKey);
        resp.put("hasPrev", prevKey != null && !prevKey.isBlank());
        return resp;
    }

    /**
     * ✅ 获取 presigned PUT 上传地址
     * POST /users/me/banner/upload-url
     */
    @PostMapping("/upload-url")
    public AvatarUploadUrlResponse uploadUrl(@RequestBody AvatarUploadUrlRequest req) {
        Long userId = writeFacade.currentUserId();

        // ✅ banner 专用校验（5MB）
        avatarService.validateBannerUploadRequestOrThrow(req.getContentType(), req.getContentLength());

        String objectKey = avatarService.generateBannerObjectKey(userId, req.getContentType());
        var presigned = avatarService.presignPut(objectKey, req.getContentType());

        int expiresInSeconds = (int) Math.max(
                1,
                presigned.expiration().getEpochSecond() - Instant.now().getEpochSecond()
        );

        return new AvatarUploadUrlResponse(
                objectKey,
                avatarService.buildPublicUrl(objectKey),
                presigned.url().toString(),
                expiresInSeconds
        );
    }

    /**
     * ✅ commit：写入 users.banner_key，并只保留“上一个”
     * POST /users/me/banner/commit
     */
    @PostMapping("/commit")
    public Map<String, Object> commit(@RequestBody AvatarCommitRequest req) {
        Long userId = writeFacade.currentUserId();
        String objectKey = (req == null) ? null : req.getObjectKey();

        avatarService.validateBannerOwnershipOrThrow(userId, objectKey);

        // ✅ 关键：banner 用 bannerMaxBytes 校验，不能复用 avatar 的 2MB 校验
        avatarService.validateUploadedBannerObjectOrThrow(objectKey);

        // ✅ 写库：bannerPrevKey <- bannerKey, bannerKey <- newKey
        var shift = writeFacade.updateBannerAndReturnOldKeys(userId, objectKey);

        String publicUrl = avatarService.buildPublicUrl(objectKey);

        Map<String, Object> resp = new HashMap<>();
        resp.put("bannerKey", objectKey);
        resp.put("bannerUrl", publicUrl);
        resp.put("prevBannerKey", shift.oldCurrentKey()); // 给前端决定是否显示“回滚”
        resp.put("hasPrev", shift.oldCurrentKey() != null && !shift.oldCurrentKey().isBlank());
        return resp;
    }

    /**
     * ✅ rollback：只能回到上一个 banner
     * POST /users/me/banner/rollback
     */
    @PostMapping("/rollback")
    public Map<String, Object> rollback() {
        Long userId = writeFacade.currentUserId();

        String prevKey = writeFacade.getPrevBannerKey(userId);
        if (prevKey == null || prevKey.isBlank()) {
            throw new IllegalArgumentException("No previous banner");
        }

        String currentKey = writeFacade.rollbackBannerToPrev(userId);

        String newPrevKey = writeFacade.getPrevBannerKey(userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("bannerKey", currentKey);
        resp.put("bannerUrl", (currentKey == null || currentKey.isBlank()) ? null : avatarService.buildPublicUrl(currentKey));
        resp.put("prevBannerKey", newPrevKey);
        resp.put("hasPrev", newPrevKey != null && !newPrevKey.isBlank());
        return resp;
    }
}
