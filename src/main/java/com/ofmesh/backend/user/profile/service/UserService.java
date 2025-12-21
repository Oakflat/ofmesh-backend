package com.ofmesh.backend.user.profile.service;

import com.ofmesh.backend.user.profile.dto.UserProfileDTO;
import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.badge.entity.UserBadge;
import com.ofmesh.backend.user.badge.repository.UserBadgeRepository;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Data
@Getter
@Setter
@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserBadgeRepository badgeRepo;

    public UserService(UserRepository userRepo, UserBadgeRepository badgeRepo) {
        this.userRepo = userRepo;
        this.badgeRepo = badgeRepo;
    }

    // ... 保留你之前的 deleteUser 代码 ...

    /**
     * 获取用户个人资料（组装 User + Badges）
     */
    public UserProfileDTO getUserProfile(String username) {
        // 1. 查用户
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 查徽章 Key 列表
        List<String> badges = badgeRepo.findByUserId(user.getId())
                .stream()
                .map(UserBadge::getBadgeKey)
                .collect(Collectors.toList());

        // 3. 组装 DTO
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole().name()); // Enum 转 String
        dto.setCreatedAt(user.getCreatedAt().toString());
        dto.setBadges(badges);

        // 4. 填充默认值 (因为数据库还没这些字段)
        dto.setBio("热爱 Minecraft，热爱开源。");
        dto.setLevel(1);
        dto.setBannerKey(user.getBannerKey());
        dto.setBannerPrevKey(user.getBannerPrevKey());

        return dto;
    }
}