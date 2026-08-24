# 贡献指南

## 开发原则

- 先搜索项目已有 starter、工具、基础类、公开 API、事件和相似页面；
- 模块之间只依赖目标模块公开的 `api`、`event` 或 `@NamedInterface`，不访问 `internal`；
- 使用统一响应、业务异常、校验、审计实体、MapStruct 和前端公共组件；
- 前后端 DTO/API 类型与消费链路同步修改；前端隐藏不是安全边界；
- 数据库变更新增迁移，不修改已发布历史迁移；
- 不提交 `.env`、Token、密码、私钥、真实生产日志和用户数据。

## 提交前验证

```bash
cd backend/travis-monolith
mvn spotless:check
mvn test

cd ../../frontend/admin-vben
pnpm install --frozen-lockfile
pnpm check:type
pnpm lint
pnpm test:unit
pnpm build:travis-admin
```

部署资产变更还要执行 Compose 渲染、`bash -n`、ShellCheck、Nginx 配置校验和文档链接检查。涉及数据库、Redis、RocketMQ、Quartz、WebSocket、代理或多实例时，在变更说明中列出真实运行时验证及未验证边界。

## 变更说明

说明问题、最小方案、风险、验证证据和回滚方式；新增公共能力时同步更新对应文档入口与 `CHANGELOG.md`。未经要求不要提交、推送或部署。
