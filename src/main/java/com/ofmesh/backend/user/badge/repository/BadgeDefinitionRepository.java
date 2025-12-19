package com.ofmesh.backend.user.badge.repository;

import com.ofmesh.backend.user.badge.entity.BadgeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BadgeDefinitionRepository extends JpaRepository<BadgeDefinition, Long> {

    // 根据 key 查找徽章定义
    Optional<BadgeDefinition> findByKey(String key);

    // 查找所有激活的徽章，并按优先级排序 (前端列表展示用)
    List<BadgeDefinition> findAllByIsActiveTrueOrderByPriorityDesc();
}