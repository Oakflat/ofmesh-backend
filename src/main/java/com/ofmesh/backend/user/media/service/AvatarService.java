package com.ofmesh.backend.user.media.service;

import com.ofmesh.backend.common.r2.R2Properties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
public class AvatarService {

    private final R2Properties props;
    private final S3Presigner presigner;
    private final S3Client s3;

    public AvatarService(R2Properties props, S3Presigner presigner, S3Client s3) {
        this.props = props;
        this.presigner = presigner;
        this.s3 = s3;
    }

    public void validateUploadRequestOrThrow(String contentType, Long contentLength) {
        String ct = normalizeContentType(contentType);

        if (!props.getAllowedContentTypes().contains(ct)) {
            throw new IllegalArgumentException("Unsupported content-type: " + ct);
        }
        if (contentLength != null && contentLength > props.getAvatarMaxBytes()) {
            throw new IllegalArgumentException("Avatar too large: " + contentLength);
        }
    }

    public String generateAvatarObjectKey(Long userId, String contentType) {
        String ct = normalizeContentType(contentType);
        String ext = contentTypeToExt(ct);

        String hash = UUID.randomUUID().toString().replace("-", "");
        return "avatars/" + userId + "/" + hash + "." + ext;
    }

    public PresignedPutObjectRequest presignPut(String objectKey, String contentType) {
        String ct = normalizeContentType(contentType);

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .contentType(ct)
                .build();

        PutObjectPresignRequest preReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.getPresignExpiresSeconds()))
                .putObjectRequest(putReq)
                .build();

        return presigner.presignPutObject(preReq);
    }

    public String buildPublicUrl(String objectKey) {
        String base = props.getPublicBase();
        if (!base.endsWith("/")) base += "/";
        if (objectKey.startsWith("/")) objectKey = objectKey.substring(1);
        return base + objectKey;
    }

    public void validateAvatarOwnershipOrThrow(Long userId, String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("objectKey required");
        }
        String prefix = "avatars/" + userId + "/";
        if (!objectKey.startsWith(prefix)) {
            throw new IllegalArgumentException("objectKey not owned by user");
        }
    }

    public void validateUploadedObjectOrThrow(String objectKey) {
        try {
            var head = s3.headObject(HeadObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(objectKey)
                    .build());

            Long size = head.contentLength();
            if (size != null && size > props.getAvatarMaxBytes()) {
                throw new IllegalArgumentException("Avatar too large: " + size);
            }

            String ct = normalizeContentType(head.contentType());
            if (!props.getAllowedContentTypes().contains(ct)) {
                throw new IllegalArgumentException("Unsupported content-type: " + ct);
            }

        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("Object not found");
        }
    }

    public void deleteObjectIgnoreNotFound(String objectKey) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(objectKey)
                    .build());
        } catch (NoSuchKeyException e) {
            // ✅ 不存在也当成功
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return;
            throw e;
        }
    }
    public String generateBannerObjectKey(Long userId, String contentType) {
        String ct = normalizeContentType(contentType);
        String ext = contentTypeToExt(ct);
        String hash = UUID.randomUUID().toString().replace("-", "");
        return "banners/" + userId + "/" + hash + "." + ext;
    }

    public void validateBannerOwnershipOrThrow(Long userId, String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("objectKey required");
        }
        String prefix = "banners/" + userId + "/";
        if (!objectKey.startsWith(prefix)) {
            throw new IllegalArgumentException("objectKey not owned by user");
        }
    }

    private String normalizeContentType(String contentType) {
        String ct = (contentType == null || contentType.isBlank())
                ? "image/webp"
                : contentType.trim().toLowerCase(Locale.ROOT);

        if ("image/jpg".equals(ct)) ct = "image/jpeg";
        return ct;
    }

    private String contentTypeToExt(String ct) {
        return switch (ct) {
            case "image/webp" -> "webp";
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            default -> "bin";
        };
    }
    public void validateBannerUploadRequestOrThrow(String contentType, Long contentLength) {
        String ct = normalizeContentType(contentType);

        if (!props.getAllowedContentTypes().contains(ct)) {
            throw new IllegalArgumentException("Unsupported content-type: " + ct);
        }
        if (contentLength != null && contentLength > props.getBannerMaxBytes()) {
            throw new IllegalArgumentException("Banner too large: " + contentLength);
        }
    }
    public void validateUploadedBannerObjectOrThrow(String objectKey) {
        try {
            var head = s3.headObject(HeadObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(objectKey)
                    .build());

            Long size = head.contentLength();
            if (size != null && size > props.getBannerMaxBytes()) {
                throw new IllegalArgumentException("Banner too large: " + size);
            }

            String ct = normalizeContentType(head.contentType());
            if (!props.getAllowedContentTypes().contains(ct)) {
                throw new IllegalArgumentException("Unsupported content-type: " + ct);
            }

        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("Object not found");
        }
    }

}
