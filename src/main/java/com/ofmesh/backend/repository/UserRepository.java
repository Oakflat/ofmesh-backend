package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找
    Optional<User> findByUsername(String username);
    // 根据邮箱查找
    Optional<User> findByEmail(String email);

    // 检查是否存在
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}