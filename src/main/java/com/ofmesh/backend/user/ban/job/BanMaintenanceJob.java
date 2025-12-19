package com.ofmesh.backend.user.ban.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestStatus;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import com.ofmesh.backend.admin.request.repository.AdminRequestRepository;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import com.ofmesh.backend.user.profile.entity.AccountStatus;
import com.ofmesh.backend.user.profile.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BanMaintenanceJob {

    private final UserRepository userRepo;
    private final AdminRequestRepository adminRequestRepo;
    private final ObjectMapper objectMapper;

    // 系统操作者 ID（你要求的 createdBy/executedBy=0）
    private static final long SYSTEM_UID = 0L;

    public BanMaintenanceJob(UserRepository userRepo,
                             AdminRequestRepository adminRequestRepo,
                             ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.adminRequestRepo = adminRequestRepo;
        this.objectMapper = objectMapper;
    }

    // 1~5 分钟都行：建议先 1 分钟，后续压力大再调
    @Scheduled(fixedDelayString = "${ban.maintenance.delay-ms:60000}")
    @Transactional
    public void autoUnbanExpired() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 分批扫，避免一次性扫太多
        int batchSize = Integer.parseInt(System.getProperty("ban.maintenance.batch", "200"));

        while (true) {
            List<User> expired = userRepo
                    .findByAccountStatusAndBanUntilIsNotNullAndBanUntilLessThanEqual(
                            AccountStatus.BANNED,
                            now,
                            PageRequest.of(0, batchSize)
                    );

            if (expired.isEmpty()) break;

            List<AdminRequest> audit = new ArrayList<>(expired.size());

            for (User u : expired) {
                // 先记录旧值（用于审计 payload）
                OffsetDateTime prevUntil = u.getBanUntil();
                String prevReason = u.getBanReason();

                // 真正收敛状态
                u.setAccountStatus(AccountStatus.ACTIVE);
                u.setBanUntil(null);
                u.setBanReason(null);

                AdminRequest ar = new AdminRequest();
                ar.setType(AdminRequestType.USER_UNBAN);
                ar.setStatus(AdminRequestStatus.EXECUTED);
                ar.setTargetUserId(u.getId());
                ar.setReason("AUTO_UNBAN");
                ar.setCreatedBy(SYSTEM_UID);
                ar.setExecutedBy(SYSTEM_UID);
                ar.setExecutedAt(now);
                ar.setResultMessage("AUTO_UNBAN_OK");

                try {
                    String payload = objectMapper.writeValueAsString(
                            Map.of(
                                    "source", "AUTO",
                                    "previousBanUntil", prevUntil,
                                    "previousBanReason", prevReason
                            )
                    );
                    ar.setPayload(payload);
                } catch (Exception ignore) {
                    // payload 写不了也不影响解封
                    ar.setPayload("{\"source\":\"AUTO\"}");
                }

                audit.add(ar);
            }

            userRepo.saveAll(expired);
            adminRequestRepo.saveAll(audit);
        }
    }
}
