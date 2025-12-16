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

        try {
            JsonNode node = objectMapper.readTree(request.getPayload() == null ? "{}" : request.getPayload());

            boolean permanent = node.path("permanent").asBoolean(false);
            String untilStr = node.path("until").asText(null);
            String banReason = node.path("banReason").asText(null);

            // === 1) 解析封禁到期时间（与 User.banUntil: LocalDateTime 对齐） ===
            LocalDateTime until = null;

            if (!permanent) {
                if (untilStr == null || untilStr.isBlank()) {
                    throw new RuntimeException("非永久封禁必须提供 until");
                }
                until = parseToLocalDateTime(untilStr);

                // 防呆：until 必须在未来
                if (!until.isAfter(LocalDateTime.now())) {
                    throw new RuntimeException("until 必须是未来时间");
                }
            }

            // === 2) 落库 ===
            target.setAccountStatus(AccountStatus.BANNED);
            target.setBanUntil(until); // ✅ LocalDateTime
            target.setBanReason((banReason == null || banReason.isBlank()) ? "N/A" : banReason.trim());
            userRepo.save(target);

            return permanent ? "已永久封禁" : ("已封禁至 " + until);
        } catch (Exception e) {
            throw new RuntimeException("执行 USER_BAN 失败: " + e.getMessage());
        }
    }

    /**
     * 兼容两种输入：
     * - 2025-12-16T10:20:30        (LocalDateTime)
     * - 2025-12-16T10:20:30+08:00  (OffsetDateTime)
     * - 2025-12-16T02:20:30Z       (OffsetDateTime)
     */
    private LocalDateTime parseToLocalDateTime(String input) {
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException ignore) {
            // 尝试 OffsetDateTime
        }
        try {
            return OffsetDateTime.parse(input).toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("until 时间格式不合法: " + input);
        }
    }
}
