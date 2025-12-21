package com.ofmesh.backend.user.media.dto;

import lombok.Getter;

@Getter
public class AvatarUploadUrlResponse {
    private String objectKey;
    private String publicUrl;
    private String uploadUrl;
    private int expiresInSeconds;

    public AvatarUploadUrlResponse(String objectKey, String publicUrl, String uploadUrl, int expiresInSeconds) {
        this.objectKey = objectKey;
        this.publicUrl = publicUrl;
        this.uploadUrl = uploadUrl;
        this.expiresInSeconds = expiresInSeconds;
    }

}
