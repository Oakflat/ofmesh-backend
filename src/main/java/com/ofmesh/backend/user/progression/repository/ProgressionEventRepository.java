package com.ofmesh.backend.user.progression.repository;

import com.ofmesh.backend.user.progression.entity.ProgressionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressionEventRepository extends JpaRepository<ProgressionEvent, Long> {
    boolean existsByEventId(String eventId);
}
