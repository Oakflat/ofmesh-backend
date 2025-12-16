package com.ofmesh.backend.service.executor;

import com.ofmesh.backend.entity.AdminRequest;
import com.ofmesh.backend.entity.AdminRequestType;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import com.ofmesh.backend.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordResetExecutor implements AdminRequestExecutor {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetExecutor(UserRepository userRepo, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public AdminRequestType supports() {
        return AdminRequestType.PASSWORD_RESET;
    }

    @Override
    public String execute(AdminRequest request, User executor) {
        User target = userRepo.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        String tempPassword = randomPassword(12);
        target.setPassword(passwordEncoder.encode(tempPassword));
        userRepo.save(target);

        // 你需要在 EmailService 增加这个方法（下面我给补丁）
        emailService.sendTemporaryPassword(target.getEmail(), tempPassword);

        return "临时密码已发送至用户邮箱";
    }

    private String randomPassword(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
