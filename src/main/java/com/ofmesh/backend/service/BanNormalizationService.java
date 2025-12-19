package com.ofmesh.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.entity.*;
import com.ofmesh.backend.repository.AdminRequestRepository;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
public class BanNormalizationService {

    private final UserRepository userRepo;
    private final AdminRequestRepository adminRequestRepo;
    private final ObjectMapper objectMapper;

    private static final long SYSTEM_UID = 0L;

    public BanNormalizationService(UserRepository userRepo,
                                   AdminRequestRepository adminRequestRepo,
                                   ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.adminRequestRepo = adminRequestRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * 如果用户 ban 已到期但状态未收敛，则执行收敛 + 写审计。
     * @param user 传入已查询到的 user（用于拿 prevUntil/prevReason 写 payload）
     * @param source 触发来源：LOGIN_PRECHECK / USERDETAILS / API_ACCESS...
     * @return 是否发生了收敛（true=本次确实把用户从“过期封禁”修正为 ACTIVE）
     */
    @Transactional
    public boolean normalizeIfExpired(User user, String source) {
        if (user == null) return false;

        // 只处理“临时封禁且已到期”的情况；永久封禁（banUntil=null）不在此处解
        if (user.getAccountStatus() != AccountStatus.BANNED) return false;
        if (user.getBanUntil() == null) return false;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (user.getBanUntil().isAfter(now)) return false; // 未到期，不动

        // 先做条件更新：避免并发下重复更新/重复写审计
        int updated = userRepo.normalizeExpiredBan(
                user.getId(),
                now,
                AccountStatus.BANNED,
                AccountStatus.ACTIVE
        );

        if (updated <= 0) return false; // 说明已被别人收敛/或 ban 被延长/状态已变

        // ✅ 写审计：AdminRequest 记录“到期解封兜底”
        AdminRequest ar = new AdminRequest();
        ar.setType(AdminRequestType.USER_UNBAN);
        ar.setStatus(AdminRequestStatus.EXECUTED);
        ar.setTargetUserId(user.getId());
        ar.setReason("EXPIRED_UNBAN");
        ar.setCreatedBy(SYSTEM_UID);
        ar.setExecutedBy(SYSTEM_UID);
        ar.setExecutedAt(now);
        ar.setResultMessage("EXPIRED_UNBAN_OK");

        try {
            ar.setPayload(objectMapper.writeValueAsString(Map.of(
                    "source", source,
                    "previousBanUntil", user.getBanUntil(),
                    "previousBanReason", user.getBanReason()
            )));
        } catch (Exception e) {
            ar.setPayload("{\"source\":\"" + source + "\"}");
        }

        adminRequestRepo.save(ar);

        // ✅ 同步内存态（避免调用方继续拿着旧对象判断 banned）
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBanUntil(null);
        user.setBanReason(null);

        return true;
    }
}
