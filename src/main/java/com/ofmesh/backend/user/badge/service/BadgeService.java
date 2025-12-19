package com.ofmesh.backend.user.badge.service;

import com.ofmesh.backend.user.badge.entity.BadgeDefinition;
import com.ofmesh.backend.user.badge.entity.UserBadge;
import com.ofmesh.backend.user.badge.repository.BadgeDefinitionRepository;
import com.ofmesh.backend.user.badge.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BadgeService {

    private final BadgeDefinitionRepository defRepo;
    private final UserBadgeRepository userBadgeRepo;

    // 构造器注入
    public BadgeService(BadgeDefinitionRepository defRepo, UserBadgeRepository userBadgeRepo) {
        this.defRepo = defRepo;
        this.userBadgeRepo = userBadgeRepo;
    }

    /**
     * 获取所有徽章定义（用于前端展示字典）
     * 实际生产中这里应该加上 @Cacheable("badges")
     */
    public List<BadgeDefinition> getAllDefinitions() {
        return defRepo.findAllByIsActiveTrueOrderByPriorityDesc();
    }

    /**
     * 核心发放逻辑
     * @param userId 接收用户 ID
     * @param badgeKey 徽章 Key
     * @param operatorId 操作人 ID (管理员)，如果是系统自动发放则为 0
     */
    @Transactional
    public void grantBadge(Long userId, String badgeKey, Long operatorId) {
        // 1. 校验徽章是否存在 (防瞎填)
        BadgeDefinition def = defRepo.findByKey(badgeKey)
                .orElseThrow(() -> new RuntimeException("徽章不存在: " + badgeKey));

        // 2. 校验用户是否已拥有 (防重复)
        if (userBadgeRepo.existsByUserIdAndBadgeKey(userId, badgeKey)) {
            throw new RuntimeException("用户已拥有该徽章，无需重复发放");
        }

        // 3. 构建用户徽章对象
        UserBadge newBadge = new UserBadge(userId, badgeKey);
        newBadge.setGrantedBy(operatorId); // 记录是谁发的！(审计关键)

        // 如果是手动操作，可以标记来源，方便以后排查
        // newBadge.setGrantSource("admin_console");

        // 4. 保存
        userBadgeRepo.save(newBadge);

        // TODO: 如果是 'identity' 类型的独占徽章 (isExclusive=true)，
        // 这里需要逻辑去删除该用户旧的 identity 徽章。初期先不加，防止误删。
    }

    /**
     * 撤销徽章 (预留给 Console 使用)
     */
    @Transactional
    public void revokeBadge(Long userId, String badgeKey) {
        userBadgeRepo.deleteByUserIdAndBadgeKey(userId, badgeKey);
    }
}