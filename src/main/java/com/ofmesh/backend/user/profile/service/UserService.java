package com.ofmesh.backend.user.profile.service;

import com.ofmesh.backend.user.badge.entity.UserBadge;
import com.ofmesh.backend.user.badge.repository.UserBadgeRepository;
import com.ofmesh.backend.user.media.service.AvatarService;
import com.ofmesh.backend.user.profile.dto.PublicUserProfileDTO;
import com.ofmesh.backend.user.profile.dto.UserProfileDTO;
import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserBadgeRepository badgeRepo;
    private final AvatarService avatarService;

    public UserService(UserRepository userRepo,
                       UserBadgeRepository badgeRepo,
                       AvatarService avatarService) {
        this.userRepo = userRepo;
        this.badgeRepo = badgeRepo;
        this.avatarService = avatarService;
    }

    // =========================
    // Helpers
    // =========================

    private List<String> loadBadges(Long userId) {
        return badgeRepo.findByUserId(userId)
                .stream()
                .map(UserBadge::getBadgeKey)
                .collect(Collectors.toList());
    }

    private String bannerUrlOrNull(String bannerKey) {
        if (!hasText(bannerKey)) return null;
        return avatarService.buildPublicUrl(bannerKey);
    }

    // =========================
    // 1) 私有：本人资料 /api/users/me
    // =========================

    public UserProfileDTO getUserProfile(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<String> badges = loadBadges(user.getId());

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt().toString());
        dto.setBadges(badges);

        // 默认值（你们目前还没落库）
        dto.setBio("热爱 Minecraft，热爱开源。");
        dto.setLevel(1);

        // 内部字段（给本人页面/编辑页用）
        dto.setBannerKey(user.getBannerKey());
        dto.setBannerPrevKey(user.getBannerPrevKey());

        // ✅ 建议：给前端直接可用的 banner URL（需要你给 UserProfileDTO 增加 banner 字段）
        // 如果你暂时不想改 DTO，可以先注释掉这一行，公开页仍能靠 Public DTO 显示 banner
        dto.setBannerKey(bannerUrlOrNull(user.getBannerKey()));

        return dto;
    }

    // =========================
    // 2) 公开：公开主页 /api/users/public/id/{id}
    // =========================

    public PublicUserProfileDTO getPublicProfileById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<String> badges = loadBadges(user.getId());

        PublicUserProfileDTO dto = new PublicUserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar()); // avatar 已是 URL
        dto.setBanner(bannerUrlOrNull(user.getBannerKey())); // bannerKey -> URL
        dto.setCreatedAt(user.getCreatedAt().toString());

        // 目前默认值阶段：先与 /me 保持一致
        dto.setBio("热爱 Minecraft，热爱开源。");
        dto.setLevel(1);
        dto.setBadges(badges);

        return dto;
    }
}
