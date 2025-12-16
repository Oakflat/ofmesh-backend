# 封禁逻辑与工单系统评估报告

## 封禁逻辑现状
- **状态字段与判定**：`User` 实体记录 `accountStatus`、`banUntil` 与 `banReason`，并在 `UserDetailsServiceImpl` 中对登录用户做封禁校验，永久封禁或未过期的临时封禁都会抛出 `DisabledException` 阻断认证流程。【F:src/main/java/com/ofmesh/backend/entity/User.java†L31-L76】【F:src/main/java/com/ofmesh/backend/security/UserDetailsServiceImpl.java†L18-L37】
- **工单执行入口**：`UserBanExecutor` 作为管理员工单执行器，将封禁信息写入用户表，支持永久封禁或指定未来时间点的封禁，并在保存时校验 `until` 字段格式及是否晚于当前时间。【F:src/main/java/com/ofmesh/backend/service/executor/UserBanExecutor.java†L13-L73】

## 封禁逻辑风险与缺口
- **未区分封禁状态的认证回写**：`User` 始终向 Spring Security 声明 `isAccountNonLocked/isEnabled=true`，只有业务层的 `DisabledException` 阻断登录；其他安全组件或第三方集成若依赖 `UserDetails` 状态字段，将无法感知封禁状态。
- **缺少解禁与审计链路**：目前仅实现封禁写入，未提供解封接口、到期自动解封、封禁历史追踪或操作审计，可能导致手动数据库操作或长期误封。
- **Token 吊销缺失**：即便封禁成功，已签发的 JWT 仍可使用直至过期，没有黑名单或版本号机制确保封禁即时生效。

## 封禁逻辑改进建议
1. **收敛安全状态来源**：在 `User` 中实现 `isAccountNonLocked/isEnabled` 等方法时同步读取 `accountStatus/banUntil`，并确保登录、权限校验、会话刷新都统一依赖该状态。
2. **补充分级封禁策略**：区分临时封禁、设备封禁、功能级封禁等策略；为临时封禁增加定时解封任务，并提供解封 API 与审计日志（操作者、原因、持续时间）。
3. **即刻失效机制**：引入 Token 黑名单或版本号字段（如 `tokenVersion`），在封禁时递增以让旧 JWT 立即失效，并在网关/过滤器层校验。
4. **防错与告警**：封禁执行器增加对不可封禁对象（如超级管理员）的保护，记录封禁操作日志并触发告警，防止误操作或内部滥用。

## 工单系统设计现状
- **工单实体**：`AdminRequest` 记录类型、状态、目标用户、payload、创建/审批/执行人员与时间戳，并带乐观锁版本号；`@PrePersist` 默认状态为 `PENDING`。【F:src/main/java/com/ofmesh/backend/entity/AdminRequest.java†L1-L76】
- **API 与权限**：`AdminRequestController` 仅向 `ADMIN/MODERATOR` 暴露创建、分页查询、按 ID 查询接口；创建时把当前用户 ID 记为 `createdBy`。【F:src/main/java/com/ofmesh/backend/controller/AdminRequestController.java†L15-L49】
- **工单生命周期**：`AdminRequestService` 只负责创建与查询；未实现审批、执行、状态流转。创建时校验目标用户存在并将 payload 序列化为 JSON；未校验请求人权限与类型有效载荷约束。【F:src/main/java/com/ofmesh/backend/service/AdminRequestService.java†L23-L77】
- **执行器模式**：定义 `AdminRequestExecutor` 接口及三种实现（封禁、密码重置、徽章发放）。执行器内部直接执行业务，但尚未与审批/执行流对接，也未做权限分级或输入幂等校验。【F:src/main/java/com/ofmesh/backend/service/executor/AdminRequestExecutor.java†L1-L10】【F:src/main/java/com/ofmesh/backend/service/executor/PasswordResetExecutor.java†L13-L52】【F:src/main/java/com/ofmesh/backend/service/executor/BadgeGrantExecutor.java†L13-L37】【F:src/main/java/com/ofmesh/backend/service/executor/UserBanExecutor.java†L13-L73】

## 工单系统风险与缺口
- **缺少审批与执行轨迹**：目前没有审批接口与执行入口，`AdminRequestStatus` 只在创建时被设为 `PENDING`；无法记录审批人、执行人和结果，导致工单体系无法真正闭环。
- **权限与范围未收紧**：创建接口只限制角色为 `ADMIN/MODERATOR`，但未按工单类型区分最小权限（如封禁/密码重置应仅限管理员）；也未校验 payload 与目标用户的安全约束（如禁止对更高权限用户执行）。
- **缺少幂等与防重**：没有防重策略或并发保护（虽有 `@Version` 但未使用），同一目标用户可能被重复创建相同工单，或在执行时出现竞态。
- **审计与通知缺失**：没有持久化的操作日志、变更记录或通知机制，难以事后追溯和告警。

## 工单系统改进建议
1. **补全状态机**：为 `PENDING -> APPROVED -> EXECUTED/FAILED` 等状态设计服务层流转，记录审批/执行人和时间，并结合乐观锁防止重复执行。
2. **权限与校验细化**：按工单类型配置所需角色/权限，限制对高权限用户的敏感操作；对 payload 做模式/字段校验（如封禁必须指定截止时间或永久标记）。
3. **执行入口统一化**：实现调度/执行服务，按工单类型路由到对应执行器，处理异常并写入 `resultMessage`，确保幂等（如通过状态与版本号控制）。
4. **审计与告警**：落地操作审计日志，结合事件通知（邮件/IM）推送审批、执行结果；为高风险操作（封禁、密码重置）提供双人审批或二次确认。
