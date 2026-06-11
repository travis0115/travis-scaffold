# XXL-JOB 独立部署与 SSO 接入

## 部署边界

- `xxl-job-admin` 独立部署并使用独立数据库。
- `travis-monolith` 仅作为执行器，通过 `travis-spring-boot-starter-xxl-job` 注册任务。
- 管理后台的“任务调度”菜单调用 `/api/admin/ops/job/entry`，在新页面打开 Admin。

## 主系统配置

```bash
XXL_JOB_ADMIN_ADDRESSES=http://xxl-job-admin:8080/xxl-job-admin
XXL_JOB_ADMIN_WEB_URL=https://job.example.com/xxl-job-admin
XXL_JOB_EXECUTOR_ENABLED=true
XXL_JOB_EXECUTOR_APP_NAME=travis-monolith
XXL_JOB_ACCESS_TOKEN=replace-with-a-random-secret
XXL_JOB_SSO_SECRET=replace-with-another-random-secret
XXL_JOB_SSO_ENABLED=true
```

不要使用 XXL-JOB 示例中的 `default_token`，生产环境应通过密钥管理系统注入随机密钥。

## Admin 侧 SSO 契约

Admin 的 `/auth/sso` 入口接收：

- `userId`
- `expiresAt`：Unix 秒时间戳
- `signature`：`HMAC-SHA256(userId + ":" + expiresAt, XXL_JOB_SSO_SECRET)` 的十六进制值

Admin 校验签名和过期时间后，将 `userId` 映射为 XXL-JOB 用户并建立登录会话。反向代理应只暴露 HTTPS，并限制 Admin 的 OpenAPI 与执行器端口访问范围。
