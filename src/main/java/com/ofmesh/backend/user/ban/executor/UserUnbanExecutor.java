package com.ofmesh.backend.user.ban.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import com.ofmesh.backend.admin.request.executor.AdminRequestExecutor;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import com.ofmesh.backend.user.profile.entity.AccountStatus;
import com.ofmesh.backend.user.profile.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserUnbanExecutor implements AdminRequestExecutor {

    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;

    public UserUnbanExecutor(UserRepository userRepo, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminRequestType supports() {
        return AdminRequestType.USER_UNBAN;
    }

    @Override
    public String execute(AdminRequest request, User executor) {
        User target = userRepo.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        //  不限制 SUPER_ADMIN 解封（解封不会造成权限风险）
        // 如果你希望严格，也可以允许，但一般不需要拦。

        // 可选：读取 payload，做审计信息回显（不影响执行）
        String unbanReason = null;
        try {
            JsonNode node = objectMapper.readTree(request.getPayload() == null ? "{}" : request.getPayload());
            unbanReason = node.path("unbanReason").asText(null);
        } catch (Exception ignore) {}

        //  解封：状态收敛（幂等）
        target.setAccountStatus(AccountStatus.ACTIVE);
        target.setBanUntil(null);
        target.setBanReason(null);
        userRepo.save(target);

        if (unbanReason != null && !unbanReason.isBlank()) {
            return "已解封（原因：" + unbanReason.trim() + "）";
        }
        return "已解封";
    }
}
