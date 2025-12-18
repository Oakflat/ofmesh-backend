# 数据库约束调整报告：role_check 允许 SUPER_ADMIN

## 时间线
- **T0 – 问题确认**：发现 `users.role` 的 `role_check` 约束未包含 `SUPER_ADMIN`，超级管理员账号无法落库或迁移。
- **T0+10min – 变更设计**：决定通过替换检查约束的方式放宽枚举范围，同时保留对既有角色值的约束力。
- **T0+20min – 执行变更**：在维护窗口执行约束替换并校验样例插入。
- **T0+25min – 验证与回滚点确认**：验证通过后记录变更，保留操作脚本以便回滚或复现。

## 具体修改
1. **替换检查约束**：删除旧的 `role_check`，重新创建覆盖全部角色枚举（含 `SUPER_ADMIN`）。
2. **验证写入**：使用示例插入/更新确认 `SUPER_ADMIN` 可成功写入，同时确保非法角色仍被拒绝。

### 建议执行脚本（PostgreSQL）
```sql
ALTER TABLE users DROP CONSTRAINT IF EXISTS role_check;
ALTER TABLE users
ADD CONSTRAINT role_check
CHECK (role IN ('USER','STAFF','MODERATOR','ADMIN','SUPER_ADMIN'));

-- 验证：应当成功
INSERT INTO users (username, email, password, role, account_status, created_at)
VALUES ('super_admin_demo', 'super_admin_demo@example.com', 'dummy', 'SUPER_ADMIN', 'ACTIVE', NOW())
ON CONFLICT (username) DO NOTHING;

-- 验证：应当失败
INSERT INTO users (username, email, password, role, account_status, created_at)
VALUES ('invalid_role_demo', 'invalid_role@example.com', 'dummy', 'INVALID', 'ACTIVE', NOW());
```

## 备注
- 如使用迁移框架（Flyway/Liquibase），建议将上述 SQL 以增量脚本落地，并在部署流程中添加数据库约束检查步骤。
- 若存在多环境部署，请在灰度/预生产完成验证后再推广到生产环境。
