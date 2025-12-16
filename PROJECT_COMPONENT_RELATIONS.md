# 项目组件关系复盘

## 顶层结构
- **Spring Boot 启动**：`OfmeshBackendApplication` 提供应用入口，加载全部配置与组件扫描。对应文件：`src/main/java/com/ofmesh/backend/OfmeshBackendApplication.java`。
- **配置层**：`config/` 目录包含安全与 JSON 配置，`SecurityConfig` 定义无状态 JWT 安全链、CORS 策略与认证提供器；`JacksonConfig` 管理序列化配置。

## 安全与用户会话
- **JWT 过滤链**：`JwtAuthenticationFilter` 从 `Authorization` 头解析 Bearer Token，利用 `UserDetailsService` 加载用户，失败时返回 403，成功则把认证对象写入上下文，后续控制器即可读取登录身份。`SecurityConfig` 将该过滤器置于用户名密码过滤器之前，并限定 `/api/admin/**` 必须 ADMIN 角色。
- **认证服务**：`AuthService` 负责验证码发送（IP/全局限流+熔断）、注册、登录与重置密码，并通过 `JwtUtil` 生成 Token。控制器 `AuthController` 提供 `/api/auth` 路由用于注册、登录、验证码与密码重置交互。

## 域模型与数据访问
- **用户与权限模型**：`entity/User` 定义用户名、邮箱、密码与角色、状态等字段，关联 `AccountStatus`、`Role` 枚举。`UserRepository` 提供按用户名/邮箱查询与存在性校验，支撑认证与资料查询。
- **徽章系统**：`BadgeDefinition` 描述徽章元数据，`UserBadge` 记录用户持有的徽章；`BadgeDefinitionRepository` 与 `UserBadgeRepository` 提供持久化访问。
- **管理工单**：`AdminRequest` 表达审批/执行工单，包含类型、状态、目标用户、payload、原因及审批执行人、时间戳等，`AdminRequestRepository` 支持 JPA CRUD 与规格查询。

## 服务层职责
- **用户资料**：`UserService` 从仓储查询并封装当前用户资料为 `UserProfileDTO`，被 `UserController` 的 `/api/users/me` 使用。
- **徽章管理**：`BadgeService` 提供发放与撤销徽章的事务方法，由 `AdminBadgeController` 触发；`BadgeGrantExecutor`、`UserBanExecutor`、`PasswordResetExecutor`、`AdminRequestExecutor` 等位于 `service/executor` 目录，预留对工单执行策略的扩展。
- **管理工单**：`AdminRequestService` 负责工单创建（校验目标用户、序列化 payload）、分页过滤查询与详情获取，供 `AdminRequestController` 的管理员接口使用。
- **监控统计**：`AdminStatsService`、`AdminHealthService`、`OnlineService` 提供后台统计、健康检查与在线状态查询，分别被对应控制器暴露。

## 控制器与 API 关系
- **鉴权接口**：`AuthController` 提供验证码、注册、登录、密码重置；`SecurityConfig` 将 `/api/auth/**` 全部放行、其他接口需认证。
- **用户接口**：`UserController` 提供 `/api/users/me` 返回当前登录用户资料，依赖安全上下文填充的用户名。
- **管理员接口**：
  - `AdminRequestController` 提供创建、分页过滤、详情查询工单，限制 ADMIN/MODERATOR。
  - `AdminBadgeController` 管理徽章发放/撤销，依据认证用户或邮箱解析操作者 ID。
  - `AdminStatsController`、`AdminHealthController` 分别暴露统计与健康检查。
- **状态/在线接口**：`StatusController` 与 `OnlineController` 提供基础可用性与在线计数查询。

## 依赖关系总览
- 控制器层调用对应服务，服务依赖仓储与工具（`JwtUtil`、`IpUtil`、`StringRedisTemplate`、`EmailService`）。
- 安全过滤链在请求进入控制器前完成 Token 解析与用户加载，确保控制器可直接通过 `SecurityContext` 获取认证身份。
- 实体与仓储定义数据模型关系，为服务层提供持久化能力；DTO 用于在控制器与服务之间传递请求/响应数据。
