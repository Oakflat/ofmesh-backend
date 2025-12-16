package com.ofmesh.backend.service.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.entity.*;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Component
public class UserBanExecutor implements AdminRequestExecutor {

    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;

    public UserBanExecutor(UserRepository userRepo, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminRequestType supports() {
        return AdminRequestType.USER_BAN;
    }

    @Override
    public String execute(AdminRequest request, User executor) {
        User target = userRepo.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        // ✅ 最高权限保护：SUPER_ADMIN 不可被封
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("禁止封禁 SUPER_ADMIN");
        }

        try {
            JsonNode node = objectMapper.readTree(request.getPayload() == null ? "{}" : request.getPayload());

            boolean permanent = node.path("permanent").asBoolean(false);
            String untilStr = node.path("until").asText(null);
            String banReason = node.path("banReason").asText(null);

            LocalDateTime until = null;

            if (!permanent) {
                if (untilStr == null || untilStr.isBlank()) {
                    throw new RuntimeException("非永久封禁必须提供 until");
                }
                until = parseToLocalDateTime(untilStr);

                if (!until.isAfter(LocalDateTime.now())) {
                    throw new RuntimeException("until 必须是未来时间");
                }
            }

            target.setAccountStatus(AccountStatus.BANNED);
            target.setBanUntil(until);
            target.setBanReason((banReason == null || banReason.isBlank()) ? "N/A" : banReason.trim());
            userRepo.save(target);

            return permanent ? "已永久封禁" : ("已封禁至 " + until);
        } catch (Exception e) {
            throw new RuntimeException("执行 USER_BAN 失败: " + e.getMessage());
        }
    }

    private LocalDateTime parseToLocalDateTime(String input) {
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException ignore) {}
        try {
            return OffsetDateTime.parse(input).toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("until 时间格式不合法: " + input);
        }
    }
}
