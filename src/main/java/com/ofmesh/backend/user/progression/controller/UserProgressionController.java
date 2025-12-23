package com.ofmesh.backend.user.progression.controller;

import com.ofmesh.backend.user.progression.dto.ProgressionDTO;
import com.ofmesh.backend.user.progression.service.ProgressionService;
import com.ofmesh.backend.user.profile.entity.User;
import com.ofmesh.backend.user.profile.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
public class UserProgressionController {

    private final ProgressionService progressionService;
    private final UserRepository userRepository;

    public UserProgressionController(ProgressionService progressionService, UserRepository userRepository) {
        this.progressionService = progressionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/progression")
    public ResponseEntity<ProgressionDTO> me(Authentication auth) {
        String loginKey = auth.getName();
        User me = userRepository.findByLoginKey(loginKey)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return ResponseEntity.ok(progressionService.getOrInit(me.getId()));
    }
}
