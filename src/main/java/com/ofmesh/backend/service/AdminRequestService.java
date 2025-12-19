package com.ofmesh.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.dto.AdminRequestDTO;
import com.ofmesh.backend.dto.CreateAdminRequestRequest;
import com.ofmesh.backend.entity.*;
import com.ofmesh.backend.repository.AdminRequestRepository;
import com.ofmesh.backend.repository.UserRepository;
import com.ofmesh.backend.service.executor.AdminRequestExecutor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AdminRequestService {

    private final AdminRequestRepository repo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;

    // ✅ executor 路由表：type -> executor
    private final Map<AdminRequestType, AdminRequestExecutor> executorMap;

    public AdminRequestService(
            AdminRequestRepository repo,
            UserRepository userRepo,
            ObjectMapper objectMapper,
            List<AdminRequestExecutor> executors
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;

        Map<AdminRequestType, AdminRequestExecutor> map = new EnumMap<>(AdminRequestType.class);
        for (AdminRequestExecutor ex : executors) {
            map.put(ex.supports(), ex);
        }
        this.executorMap = map;
    }

    private User mustUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("操作者不存在"));
    }

    private AdminRequest mustRequest(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    private void requireSuperAdmin(User u) {
        if (u.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("仅 SUPER_ADMIN 可批复/执行");
        }
    }

    private void requireAdminOrSuperAdmin(User u) {
        if (u.getRole() != Role.ADMIN && u.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("仅 ADMIN / SUPER_ADMIN 可发起该类型工单");
        }
    }

    public AdminRequestDTO create(CreateAdminRequestRequest req, Long actorUserId) {
        User actor = mustUser(actorUserId);

        AdminRequestType type = AdminRequestType.valueOf(req.getType());

        // ✅ 类型级权限收口：封禁/重置密码只能 ADMIN/SUPER_ADMIN 发起
        // ✅ 类型级权限收口：封禁/解封/重置密码只能 ADMIN/SUPER_ADMIN 发起
        if (type == AdminRequestType.USER_BAN
                || type == AdminRequestType.USER_UNBAN
                || type == AdminRequestType.PASSWORD_RESET) {
            requireAdminOrSuperAdmin(actor);
        }

        // target 必须存在（避免脏工单）
        userRepo.findById(req.getTargetUserId()).orElseThrow(() -> new RuntimeException("目标用户不存在"));

        // ✅ 防重复封禁工单（避免连点/刷接口）
        // ✅ 防重复工单（避免连点/刷接口）
        if (type == AdminRequestType.USER_BAN || type == AdminRequestType.USER_UNBAN) {
            boolean exists = repo.existsByTypeAndTargetUserIdAndStatus(
                    type,
                    req.getTargetUserId(),
                    AdminRequestStatus.PENDING
            );
            if (exists) {
                throw new RuntimeException("该用户已有待审批的 " + type + " 工单");
            }
        }

        AdminRequest ar = new AdminRequest();
        ar.setType(type);
        ar.setStatus(AdminRequestStatus.PENDING);
        ar.setTargetUserId(req.getTargetUserId());
        ar.setReason(req.getReason());
        ar.setCreatedBy(actorUserId);

        String payloadJson;
        try {
            payloadJson = (req.getPayload() == null) ? null : objectMapper.writeValueAsString(req.getPayload());
        } catch (Exception e) {
            throw new RuntimeException("payload 序列化失败");
        }

// ✅ 在 service 层统一 payload 字段（保证审计数据一致）
        payloadJson = normalizePayload(type, payloadJson);

        ar.setPayload(payloadJson);

        AdminRequest saved = repo.save(ar);
        return toDTO(saved);
    }

    public Page<AdminRequestDTO> list(Optional<String> status, Optional<String> type, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100), Sort.by(Sort.Direction.DESC, "id"));

        Specification<AdminRequest> spec = (root, query, cb) -> cb.conjunction();

        if (status.isPresent()) {
            AdminRequestStatus s = AdminRequestStatus.valueOf(status.get());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), s));
        }
        if (type.isPresent()) {
            AdminRequestType t = AdminRequestType.valueOf(type.get());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), t));
        }

        return repo.findAll(spec, pageable).map(this::toDTO);
    }

    public AdminRequestDTO get(Long id) {
        return toDTO(mustRequest(id));
    }

    // ✅ SUPER_ADMIN 批复同意：批复即执行
    @Transactional
    public AdminRequestDTO approveAndExecute(Long requestId, Long approverId) {
        User approver = mustUser(approverId);
        requireSuperAdmin(approver);

        AdminRequest r = mustRequest(requestId);
        if (r.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("仅 PENDING 工单可审批，当前状态=" + r.getStatus());
        }

        // 先标记 APPROVED（有审计意义）
        r.setStatus(AdminRequestStatus.APPROVED);
        r.setApprovedBy(approverId);
        r.setApprovedAt(OffsetDateTime.now());
        repo.save(r);

        AdminRequestExecutor executor = executorMap.get(r.getType());
        if (executor == null) {
            r.setStatus(AdminRequestStatus.FAILED);
            r.setExecutedBy(approverId);
            r.setExecutedAt(OffsetDateTime.now());
            r.setResultMessage("未找到执行器: " + r.getType());
            repo.save(r);
            return toDTO(r);
        }

        // ✅ 双保险：禁止封禁 SUPER_ADMIN（即使 executor 忘写也挡住）
        if (r.getType() == AdminRequestType.USER_BAN) {
            User target = userRepo.findById(r.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException("目标用户不存在"));
            if (target.getRole() == Role.SUPER_ADMIN) {
                throw new RuntimeException("禁止封禁 SUPER_ADMIN");
            }
        }

        try {
            String msg = executor.execute(r, approver);

            r.setStatus(AdminRequestStatus.EXECUTED);
            r.setExecutedBy(approverId);
            r.setExecutedAt(OffsetDateTime.now());
            r.setResultMessage(msg);
            repo.save(r);

            return toDTO(r);
        } catch (Exception e) {
            r.setStatus(AdminRequestStatus.FAILED);
            r.setExecutedBy(approverId);
            r.setExecutedAt(OffsetDateTime.now());
            r.setResultMessage(e.getMessage());
            repo.save(r);

            return toDTO(r);
        }
    }

    // ✅ SUPER_ADMIN 驳回
    @Transactional
    public AdminRequestDTO reject(Long requestId, Long approverId, String reason) {
        User approver = mustUser(approverId);
        requireSuperAdmin(approver);

        AdminRequest r = mustRequest(requestId);
        if (r.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("仅 PENDING 工单可驳回，当前状态=" + r.getStatus());
        }

        r.setStatus(AdminRequestStatus.REJECTED);
        r.setApprovedBy(approverId);
        r.setApprovedAt(OffsetDateTime.now());
        r.setResultMessage((reason == null || reason.isBlank()) ? "REJECTED" : ("REJECTED: " + reason.trim()));
        repo.save(r);

        return toDTO(r);
    }

    private AdminRequestDTO toDTO(AdminRequest ar) {
        AdminRequestDTO dto = new AdminRequestDTO();
        dto.id = ar.getId();
        dto.type = ar.getType().name();
        dto.status = ar.getStatus().name();
        dto.targetUserId = ar.getTargetUserId();
        dto.payload = ar.getPayload();
        dto.reason = ar.getReason();
        dto.createdBy = ar.getCreatedBy();
        dto.createdAt = ar.getCreatedAt();
        dto.approvedBy = ar.getApprovedBy();
        dto.approvedAt = ar.getApprovedAt();
        dto.executedBy = ar.getExecutedBy();
        dto.executedAt = ar.getExecutedAt();
        dto.resultMessage = ar.getResultMessage();
        return dto;
    }
    private String normalizePayload(AdminRequestType type, String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return payloadJson;

        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            if (!node.isObject()) return payloadJson;

            // 只对需要规范化的类型做
            if (type == AdminRequestType.USER_BAN) {
                // ✅ 统一 until -> banUntil
                if (node.hasNonNull("until") && !node.hasNonNull("banUntil")) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node)
                            .set("banUntil", node.get("until"));
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove("until");
                }
            }

            // USER_UNBAN 暂时不强制字段转换（后续你们定死 schema 再加也行）
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            // 解析失败就不改，避免影响创建工单
            return payloadJson;
        }
    }

}
