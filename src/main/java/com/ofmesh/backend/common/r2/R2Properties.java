package com.ofmesh.backend.common.r2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ofmesh.r2")
public class R2Properties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicBase;
    private long bannerMaxBytes = 5 * 1024 * 1024; // 默认 5MB

    private int presignExpiresSeconds = 120;

    private long avatarMaxBytes = 2 * 1024 * 1024;
    private List<String> allowedContentTypes = List.of("image/webp", "image/png", "image/jpeg");

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getPublicBase() { return publicBase; }
    public void setPublicBase(String publicBase) { this.publicBase = publicBase; }

    public int getPresignExpiresSeconds() { return presignExpiresSeconds; }
    public void setPresignExpiresSeconds(int presignExpiresSeconds) { this.presignExpiresSeconds = presignExpiresSeconds; }

    public long getAvatarMaxBytes() { return avatarMaxBytes; }
    public void setAvatarMaxBytes(long avatarMaxBytes) { this.avatarMaxBytes = avatarMaxBytes; }

    public List<String> getAllowedContentTypes() { return allowedContentTypes; }
    public void setAllowedContentTypes(List<String> allowedContentTypes) { this.allowedContentTypes = allowedContentTypes; }

    public long getBannerMaxBytes() { return bannerMaxBytes; }
    public void setBannerMaxBytes(long bannerMaxBytes) { this.bannerMaxBytes = bannerMaxBytes; }

}
