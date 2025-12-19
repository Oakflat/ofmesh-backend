package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.UserBadge;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.ofmesh.backend.entity.AccountStatus;
import com.ofmesh.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    // 查找某个用户的所有徽章
    List<UserBadge> findByUserId(Long userId);

    // 检查用户是否已经有了某个徽章 (防止重复发放)
    boolean existsByUserIdAndBadgeKey(Long userId, String badgeKey);

    // 删除某个徽章 (例如撤销)
    void deleteByUserIdAndBadgeKey(Long userId, String badgeKey);

    @Query("""
        select u from User u
        where u.accountStatus = :status
          and u.banUntil is not null
          and u.banUntil <= :now
        """)
    List<User> findExpiredBans(
            @Param("status") AccountStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}