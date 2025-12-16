package com.ofmesh.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofmesh.backend.dto.AdminRequestDTO;
import com.ofmesh.backend.dto.CreateAdminRequestRequest;
import com.ofmesh.backend.entity.AdminRequest;
import com.ofmesh.backend.entity.AdminRequestStatus;
import com.ofmesh.backend.entity.AdminRequestType;
import com.ofmesh.backend.repository.AdminRequestRepository;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
public class AdminRequestService {

    private final AdminRequestRepository repo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;

    public AdminRequestService(AdminRequestRepository repo, UserRepository userRepo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }

    public AdminRequestDTO create(CreateAdminRequestRequest req, Long actorUserId) {
        AdminRequestType type = AdminRequestType.valueOf(req.getType());

        // target 必须存在（避免脏工单）
        userRepo.findById(req.getTargetUserId()).orElseThrow(() -> new RuntimeException("目标用户不存在"));

        AdminRequest ar = new AdminRequest();
        ar.setType(type);
        ar.setStatus(AdminRequestStatus.PENDING);
        ar.setTargetUserId(req.getTargetUserId());
        ar.setReason(req.getReason());
        ar.setCreatedBy(actorUserId);

        try {
            ar.setPayload(req.getPayload() == null ? null : objectMapper.writeValueAsString(req.getPayload()));
        } catch (Exception e) {
            throw new RuntimeException("payload 序列化失败");
        }

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
        return toDTO(repo.findById(id).orElseThrow(() -> new RuntimeException("工单不存在")));
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
}
