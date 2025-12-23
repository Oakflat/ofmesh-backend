package com.ofmesh.backend.internal.forum.controller;

import com.ofmesh.backend.user.progression.config.ProgressionProperties;
import com.ofmesh.backend.user.progression.dto.ProgressionDTO;
import com.ofmesh.backend.user.progression.dto.ProgressionEventRequest;
import com.ofmesh.backend.user.progression.service.ProgressionService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/forum/progression")
public class InternalProgressionController {

    private final ProgressionService progressionService;
    private final ProgressionProperties props;

    public InternalProgressionController(ProgressionService progressionService, ProgressionProperties props) {
        this.progressionService = progressionService;
        this.props = props;
    }

    @PostMapping("/events")
    public ResponseEntity<ProgressionDTO> ingest(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody ProgressionEventRequest req
    ) {
        if (token == null || !token.equals(props.getInternalToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(progressionService.applyEvent(req));
    }
}
