# 登录封禁返回链路与前端适配清单

## 后端链路核查
- **触发点**：`UserDetailsServiceImpl` 在加载用户时检测 `accountStatus == BANNED` 且未解禁（`banUntil` 为空或未来），抛出 `DisabledException` 阻断认证。【F:src/main/java/com/ofmesh/backend/security/UserDetailsServiceImpl.java†L19-L37】
- **异常转换**：`AuthService.login` 捕获 `AccountStatusException`，按登录键再次查询用户，将数据库中的 `banUntil/banReason` 封装到 `AccountBannedException` 抛出。【F:src/main/java/com/ofmesh/backend/service/AuthService.java†L149-L180】
- **HTTP 输出**：`AuthController.login` 捕获 `AccountBannedException`，返回 403 且 body 固定为 `code/banUntil/banReason`（`code=ACCOUNT_BANNED`）。【F:src/main/java/com/ofmesh/backend/controller/AuthController.java†L65-L86】【F:src/main/java/com/ofmesh/backend/dto/AuthBanResponse.java†L3-L16】
- **时间序列化**：`JacksonConfig` 注册 `JavaTimeModule` 并禁止时间戳，使用 UTC 时区，前端可直接按 ISO-8601 解析 `OffsetDateTime` 字符串。【F:src/main/java/com/ofmesh/backend/config/JacksonConfig.java†L15-L31】

## 前端适配清单
1. **登录失败分支**：若响应 `status=403` 且 `body.code === "ACCOUNT_BANNED"`，读取 `banUntil`（可能为 `null` 表示永久）与 `banReason` 展示封禁信息；否则按通用错误处理。
2. **时间显示**：`banUntil` 为 ISO-8601 UTC，需按前端时区/格式化显示；`null` 表示永久封禁。
3. **交互提示**：
   - 主文案：建议使用后端返回的 `banReason`，缺省可 fallback 为“账号已被封禁”。
   - 追加提示：若 `banUntil` 非空，显示“解封时间：{formatted banUntil} (UTC)”；为空则标记“永久封禁”。
4. **重试/出口**：封禁态下禁用进一步登录尝试，提供联系客服或提交工单入口。

## 验证方式
- 使用被封禁用户账号调用 `POST /api/auth/login`，应收到 `403` 与 `{"code":"ACCOUNT_BANNED","banUntil":...,"banReason":...}`；
- 将 `banUntil` 置空（永久封禁）或设置为过去时间可分别验证前端的永久/解禁提示逻辑。
