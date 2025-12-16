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

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PostMapping
    public AdminRequestDTO create(@RequestBody CreateAdminRequestRequest req) {
        return service.create(req, currentUserId());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @GetMapping
    public Page<AdminRequestDTO> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(Optional.ofNullable(status), Optional.ofNullable(type), page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @GetMapping("/{id}")
    public AdminRequestDTO get(@PathVariable Long id) {
        return service.get(id);
    }
}
