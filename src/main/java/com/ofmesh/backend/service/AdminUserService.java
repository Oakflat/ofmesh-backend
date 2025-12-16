package com.ofmesh.backend.service;

import com.ofmesh.backend.dto.AdminUserDTO;
import com.ofmesh.backend.entity.User;
import com.ofmesh.backend.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepo;

    public AdminUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Page<AdminUserDTO> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100), Sort.by(Sort.Direction.DESC, "id"));
        String keyword = q == null ? "" : q.trim();

        Page<User> users;

        if (keyword.isBlank()) {
            users = userRepo.findAll(pageable);
        } else if (keyword.matches("\\d+")) {
            Long id = Long.parseLong(keyword);
            users = userRepo.findById(id)
                    .map(u -> new PageImpl<>(List.of(u), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
        } else {
            users = userRepo.searchAdminUsers(keyword, pageable);
        }

        return users.map(this::toDTO);
    }

    private AdminUserDTO toDTO(User u) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.id = u.getId();
        dto.username = u.getUsername();
        dto.email = u.getEmail();
        dto.role = (u.getRole() == null) ? "USER" : u.getRole().name();

        dto.accountStatus = (u.getAccountStatus() == null) ? "ACTIVE" : u.getAccountStatus().name();
        dto.banUntil = u.getBanUntil();
        dto.banReason = u.getBanReason();
        dto.createdAt = u.getCreatedAt();
        return dto;
    }
}
