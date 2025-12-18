package com.ofmesh.backend.controller;

import com.ofmesh.backend.dto.AuthBanResponse;
import com.ofmesh.backend.dto.AuthErrorResponse;
import com.ofmesh.backend.dto.AuthTokenResponse;
import com.ofmesh.backend.dto.LoginRequest;
import com.ofmesh.backend.dto.PasswordResetRequest;
import com.ofmesh.backend.dto.RegisterRequest;
import com.ofmesh.backend.exception.AccountBannedException;
import com.ofmesh.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // ✅ 手动构造函数注入
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/ping")
    public String ping() { return "ok"; }

    // ==========================================
    // 1. 发送验证码 (通用接口，或区分注册/重置)
    // ==========================================

    /**
     * 发送注册验证码
     * POST /api/auth/send-code?email=xxx@qq.com&type=register
     */
    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestParam String email, @RequestParam String type) {
        try {
            // 在 Service 里区分 type:
            // "register": 检查邮箱是否已存在 (已存在则报错)
            // "reset": 检查邮箱是否不存在 (不存在则报错)
            authService.sendVerificationCode(email, type);
            return ResponseEntity.ok("验证码已发送，请检查邮箱 (5分钟有效)");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // 2. 注册 (带验证码校验)
    // ==========================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // AuthService.register 内部需要增加：
            // 1. redisTemplate.opsForValue().get("verify:register:" + email)
            // 2. 比对 code 是否一致
            // 3. 删除 redis key
            String token = authService.register(request);
            return ResponseEntity.ok(new AuthTokenResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // 3. 登录 (保持不变)
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(new AuthTokenResponse(token));

        } catch (AccountBannedException e) {
            return ResponseEntity.status(403)
                    .body(AuthBanResponse.banned(e.getBanUntil(), e.getBanReason()));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(AuthErrorResponse.badCredentials());
        }
    }

    // ==========================================
    // 4. 忘记密码流程 (Day 2 讨论部分)
    // ==========================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("密码重置成功，请使用新密码登录");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
