package com.ofmesh.backend.user.profile.controller;

import com.ofmesh.backend.user.profile.dto.UserProfileDTO;
import com.ofmesh.backend.user.profile.service.UserService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Data
@Getter
@Setter
@RestController
@RequestMapping("/api/users")
public class UserController {
    private String bannerKey;
    private String bannerPrevKey; // 可选，但建议一起加

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户的资料
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser() {
        // 1. 从 Spring Security 上下文中获取当前登录的用户名
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 2. 查询并返回
        return ResponseEntity.ok(userService.getUserProfile(username));
    }
}