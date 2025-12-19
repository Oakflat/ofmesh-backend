package com.ofmesh.backend.admin.user.controller;

import com.ofmesh.backend.admin.user.dto.AdminUserDTO;
import com.ofmesh.backend.admin.user.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    // ✅ 只读检索：不给任何封禁/工单入口
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public Page<AdminUserDTO> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.search(q, page, size);
    }
}
