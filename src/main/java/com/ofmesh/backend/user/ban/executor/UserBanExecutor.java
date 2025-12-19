package com.ofmesh.backend.user.ban.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import com.ofmesh.backend.admin.request.executor.AdminRequestExecutor;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import com.ofmesh.backend.user.profile.entity.AccountStatus;
import com.ofmesh.backend.user.profile.entity.Role;
import com.ofmesh.backend.user.profile.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("禁止封禁 SUPER_ADMIN");
        }

        try {
            JsonNode node = objectMapper.readTree(request.getPayload() == null ? "{}" : request.getPayload());

            boolean permanent = node.path("permanent").asBoolean(false);

            // ✅ 统一字段名：banUntil（兼容旧 until）
            String untilStr = textOrNull(node, "banUntil");
            if (untilStr == null) untilStr = textOrNull(node, "until"); // backward compatible

            // ✅ banReason 兜底：优先 payload，其次工单 reason
            String banReason = textOrNull(node, "banReason");
            if (banReason == null || banReason.isBlank()) {
                banReason = (request.getReason() == null || request.getReason().isBlank())
                        ? "N/A"
                        : request.getReason().trim();
            } else {
                banReason = banReason.trim();
            }

            OffsetDateTime until = null;

            if (!permanent) {
                if (untilStr == null || untilStr.isBlank()) {
                    throw new RuntimeException("非永久封禁必须提供 banUntil");
                }
                until = parseUntil(untilStr);

                if (!until.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
                    throw new RuntimeException("banUntil 必须是未来时间");
                }
            }

            target.setAccountStatus(AccountStatus.BANNED);
            target.setBanUntil(until);
            target.setBanReason(banReason);
            userRepo.save(target);

            return permanent ? "已永久封禁" : ("已封禁至 " + until);
        } catch (Exception e) {
            throw new RuntimeException("执行 USER_BAN 失败: " + e.getMessage());
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText(null);
        return (s == null || s.isBlank()) ? null : s;
    }


    private OffsetDateTime parseUntil(String input) {
        try {
            return OffsetDateTime.parse(input).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignore) {}
        try {
            return OffsetDateTime.of(LocalDateTime.parse(input), ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("until 时间格式不合法: " + input);
        }
    }
}
