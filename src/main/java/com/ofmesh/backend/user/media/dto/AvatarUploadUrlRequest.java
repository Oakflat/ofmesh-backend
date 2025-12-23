package com.ofmesh.backend.user.media.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AvatarUploadUrlRequest {
    // 前端可不传：默认 image/webp
    private String contentType;
    // 前端可传：用于提前拦截，commit 仍会再校验
    private Long contentLength;

}
