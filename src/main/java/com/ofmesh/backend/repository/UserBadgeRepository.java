package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    // 查找某个用户的所有徽章
    List<UserBadge> findByUserId(Long userId);

    // 检查用户是否已经有了某个徽章 (防止重复发放)
    boolean existsByUserIdAndBadgeKey(Long userId, String badgeKey);

    // 删除某个徽章 (例如撤销)
    void deleteByUserIdAndBadgeKey(Long userId, String badgeKey);
}