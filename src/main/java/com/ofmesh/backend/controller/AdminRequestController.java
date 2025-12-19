package com.ofmesh.backend.controller;

import com.ofmesh.backend.dto.AdminRequestDTO;
import com.ofmesh.backend.dto.CreateAdminRequestRequest;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.service.AdminRequestService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminRequestController {

    private final AdminRequestService service;

    public AdminRequestController(AdminRequestService service) {
        this.service = service;
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User u) return u.getId();
        throw new RuntimeException("无法获取当前用户");
    }

    // ✅ 只能 ADMIN / SUPER_ADMIN 发起工单（按你们的新需求收紧）
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public AdminRequestDTO create(@RequestBody CreateAdminRequestRequest req) {
        return service.create(req, currentUserId());
    }

    // ✅ 只能 ADMIN / SUPER_ADMIN 查看工单
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public Page<AdminRequestDTO> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(Optional.ofNullable(status), Optional.ofNullable(type), page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}")
    public AdminRequestDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    // ✅ 只能 SUPER_ADMIN 批复同意（并执行）
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/approve")
    public AdminRequestDTO approveAndExecute(@PathVariable Long id) {
        return service.approveAndExecute(id, currentUserId());
    }

    // ✅ 只能 SUPER_ADMIN 驳回
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/reject")
    public AdminRequestDTO reject(@PathVariable Long id, @RequestBody RejectRequest body) {
        String reason = body == null ? null : body.reason();
        return service.reject(id, currentUserId(), reason);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/ban")
    public AdminRequestDTO createBan(@RequestBody CreateAdminRequestRequest req) {
        req.setType("USER_BAN"); // 或者构造一个新的 DTO
        return service.create(req, currentUserId());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/unban")
    public AdminRequestDTO createUnban(@RequestBody CreateAdminRequestRequest req) {
        req.setType("USER_UNBAN");
        return service.create(req, currentUserId());
    }

    // 简单请求体：{ "reason": "xxx" }
    public record RejectRequest(String reason) {}
}
