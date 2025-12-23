package com.ofmesh.backend.user.media.controller;

import com.ofmesh.backend.user.media.dto.AvatarCommitRequest;
import com.ofmesh.backend.user.media.dto.AvatarUploadUrlRequest;
import com.ofmesh.backend.user.media.dto.AvatarUploadUrlResponse;
import com.ofmesh.backend.user.media.gc.AvatarGcService;
import com.ofmesh.backend.user.media.service.AvatarService;
import com.ofmesh.backend.user.media.service.UserAvatarWriteFacade;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/avatar")
public class UserAvatarController {

    private final AvatarService avatarService;
    private final UserAvatarWriteFacade userAvatarWriteFacade;
    private final AvatarGcService avatarGcService;

    public UserAvatarController(
            AvatarService avatarService,
            UserAvatarWriteFacade userAvatarWriteFacade,
            AvatarGcService avatarGcService
    ) {
        this.avatarService = avatarService;
        this.userAvatarWriteFacade = userAvatarWriteFacade;
        this.avatarGcService = avatarGcService;
    }

    @PostMapping("/upload-url")
    public AvatarUploadUrlResponse uploadUrl(@RequestBody AvatarUploadUrlRequest req) {
        Long userId = userAvatarWriteFacade.currentUserId();

        avatarService.validateUploadRequestOrThrow(req.getContentType(), req.getContentLength());

        String objectKey = avatarService.generateAvatarObjectKey(userId, req.getContentType());
        var presigned = avatarService.presignPut(objectKey, req.getContentType());

        return new AvatarUploadUrlResponse(
                objectKey,
                avatarService.buildPublicUrl(objectKey),
                presigned.url().toString(),
                (int) presigned.expiration().getEpochSecond()
        );
    }

    @PostMapping("/commit")
    public Object commit(@RequestBody AvatarCommitRequest req) {
        Long userId = userAvatarWriteFacade.currentUserId();
        String objectKey = req == null ? null : req.getObjectKey();

        avatarService.validateAvatarOwnershipOrThrow(userId, objectKey);
        avatarService.validateUploadedObjectOrThrow(objectKey);

        String publicUrl = avatarService.buildPublicUrl(objectKey);

        //  返回 oldCurrent（给前端） + oldPrev（给 GC）
        var shift = userAvatarWriteFacade.updateAvatarAndReturnOldKeys(userId, objectKey, publicUrl);

        //  只入队上上张（7天后删）
        avatarGcService.enqueueIfNeeded(userId, shift.oldPrevKey());

        return new java.util.HashMap<String, Object>() {{
            put("avatarKey", objectKey);
            put("avatarUrl", publicUrl);
            put("prevAvatarKey", shift.oldCurrentKey());
        }};
    }

    @PostMapping("/rollback")
    public Object rollback() {
        Long userId = userAvatarWriteFacade.currentUserId();

        String prevKey = userAvatarWriteFacade.getPrevAvatarKey(userId);
        if (prevKey == null || prevKey.isBlank()) {
            throw new IllegalArgumentException("No previous avatar");
        }

        String publicUrl = avatarService.buildPublicUrl(prevKey);
        String newCurrentKey = userAvatarWriteFacade.rollbackToPrev(userId, publicUrl);

        return new java.util.HashMap<String, Object>() {{
            put("avatarKey", newCurrentKey);
            put("avatarUrl", avatarService.buildPublicUrl(newCurrentKey));
        }};
    }
}
