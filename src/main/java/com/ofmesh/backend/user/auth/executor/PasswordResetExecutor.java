package com.ofmesh.backend.user.auth.executor;

import com.ofmesh.backend.admin.request.executor.AdminRequestExecutor;
import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import com.ofmesh.backend.common.mail.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordResetExecutor implements AdminRequestExecutor {

    private static final int TEMP_PASSWORD_LEN = 12;

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

        String tempPassword = randomPassword(TEMP_PASSWORD_LEN);
        target.setPassword(passwordEncoder.encode(tempPassword));
        userRepo.save(target);

        //  传 3 个参数：邮箱、用户名、临时密码
        emailService.sendTemporaryPassword(target.getEmail(), target.getUsername(), tempPassword);

        return "临时密码已发送至用户邮箱";
    }

    private String randomPassword(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
