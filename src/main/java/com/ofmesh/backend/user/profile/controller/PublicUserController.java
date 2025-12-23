package com.ofmesh.backend.user.profile.controller;

import com.ofmesh.backend.user.profile.dto.PublicUserProfileDTO;
import com.ofmesh.backend.user.profile.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/public")
public class PublicUserController {

    private final UserService userService;

    public PublicUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<PublicUserProfileDTO> getPublicById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfileById(id));
    }
}
