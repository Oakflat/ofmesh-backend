package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.Role;
import com.ofmesh.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByRole(Role role);
    long countByCreatedAtAfter(LocalDateTime time);
}
