package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.Role;
import com.ofmesh.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByRole(Role role);

    // ✅ 与 User.createdAt: OffsetDateTime 对齐（DB: timestamptz）
    long countByCreatedAtAfter(OffsetDateTime time);

    // ✅ 管理后台只读检索：用户名/邮箱模糊匹配（分页）
    @Query("""
        select u from User u
        where lower(u.username) like lower(concat('%', :q, '%'))
           or lower(u.email) like lower(concat('%', :q, '%'))
    """)
    Page<User> searchAdminUsers(@Param("q") String q, Pageable pageable);
}
