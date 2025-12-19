package com.ofmesh.backend.user.progression.repository;

import com.ofmesh.backend.user.progression.entity.UserProgression;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserProgressionRepository extends JpaRepository<UserProgression, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from UserProgression p where p.userId = :userId")
    Optional<UserProgression> findForUpdate(@Param("userId") Long userId);
}
