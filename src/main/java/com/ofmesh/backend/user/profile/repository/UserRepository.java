package com.ofmesh.backend.user.profile.repository;

import com.ofmesh.backend.user.profile.entity.AccountStatus;
import com.ofmesh.backend.user.profile.entity.Role;
import com.ofmesh.backend.user.profile.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying
    @Query("""
        update User u
        set u.accountStatus = :active,
            u.banUntil = null,
            u.banReason = null
        where u.id = :userId
          and u.accountStatus = :banned
          and u.banUntil is not null
          and u.banUntil <= :now
    """)
    int normalizeExpiredBan(
            @Param("userId") Long userId,
            @Param("now") OffsetDateTime now,
            @Param("banned") AccountStatus banned,
            @Param("active") AccountStatus active
    );
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

    List<User> findByAccountStatusAndBanUntilIsNotNullAndBanUntilLessThanEqual(AccountStatus accountStatus, OffsetDateTime now, PageRequest of);
    @Query("""
        select u from User u
        where u.username = :key or u.email = :key
    """)
    Optional<User> findByLoginKey(@Param("key") String key);
}
