# Travis Scaffold

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F.svg" alt="Spring Boot 4.1.0">
  <img src="https://img.shields.io/badge/JDK-25-007396.svg" alt="JDK 25">
  <img src="https://img.shields.io/badge/Vue-3-42B883.svg" alt="Vue 3">
  <img src="https://img.shields.io/github/license/travis0115/travis-scaffold" alt="License">
</p>

Travis Scaffold 是一个面向业务系统快速开发的前后端脚手架。后端采用 Spring Boot + Spring Modulith 的模块化单体架构，前端基于 Vue 3、Vite 和 Vben Admin，提供系统管理、运维治理、认证、缓存、消息、调度、文件和实时通信等可复用能力。

项目强调明确的模块边界和公共入口：开发新功能前，先复用已有 starter、基础类、业务 API、事件和前端公共组件，避免在业务模块中重复封装。

## 核心能力

### 后端基础设施

- 统一响应、分页、业务异常、国际化和参数校验；
- MyBatis-Plus 基类、审计字段、逻辑删除、分页和安全拦截；
- Jackson/JSON、Redis、Spring Cache、Redisson 分布式锁；
- Sa-Token + JWT 多登录体系，支持 admin/app；
- WebMVC API 前缀、CORS、防重复提交和富文本清洗；
- WebSocket ticket、点对点推送、命名空间和 Redis 集群广播；
- Spring Modulith 事件、事务提交后处理和可选 RocketMQ 集成；
- Quartz JDBC 集群调度、后台动态任务和业务一次性任务；
- 访问日志、操作日志、结构化日志和统一错误上报；
- DTO、JSON、参数和日志数据脱敏。

### 业务模块

- 系统管理：用户、角色、菜单、部门、字典、参数配置；
- 文件管理：文件夹、存储配置、本地上传、引用保护、富文本图片；
- 内容发布：公告、版本发布及客户端查询；
- 消息中心：模板、站内信、定时推送、撤回、收件箱和未读数；
- 运维管理：Quartz 任务、执行日志、错误聚合和处理闭环；
- 管理端：动态路由、权限、表单、VXE Grid、字典和公共操作组件。

完整能力边界见 [项目能力总览](docs/project-overview.md)。

## 项目结构

```text
travis-scaffold/
├── backend/
│   ├── travis-dependencies/        # 全局依赖 BOM
│   ├── travis-infrastructure/      # common 与自定义 Spring Boot starters
│   └── travis-monolith/
│       ├── travis-module-app/      # app 端接口与适配
│       ├── travis-module-platform/ # system、ops 等平台模块
│       └── travis-server/          # 启动、配置和数据库迁移
├── frontend/
│   └── admin-vben/
│       ├── apps/travis-admin/      # 管理端应用
│       └── packages/               # Vben workspace 共享包
└── docs/                            # 项目使用文档
```

后端跨模块调用只能依赖目标模块公开的 `api`、`event` 或 `@NamedInterface`，不要直接引用其他模块的 `internal` 实现。

## 环境要求

| 工具 | 要求 |
| --- | --- |
| JDK | 25 |
| Maven | 3.9+ |
| Node.js | `>=22.18.0` |
| pnpm | `>=11.0.0`，项目指定 `pnpm@11.2.2` |
| Docker / Compose | 用于本地 MySQL、Redis，以及按需启用的 RocketMQ |

## 快速开始

Compose 与 dev Profile 已对齐 MySQL、Redis 和端口默认值，并预留 RocketMQ。按照 [快速开始](docs/getting-started.md) 即可启动；本地示例凭据不得用于生产。

准备完成后，常用启动命令如下：

```bash
# 后端
cd backend/travis-monolith
mvn spring-boot:run -pl travis-server

# 管理端
cd frontend/admin-vben
pnpm install
pnpm dev:travis-admin
```

## 文档

所有文档从 [文档总入口](docs/README.md) 开始。

| 我想了解 | 文档 |
| --- | --- |
| 第一次如何启动 | [快速开始](docs/getting-started.md) |
| 如何部署任意实例数、HTTPS、备份和升级回滚 | [生产部署](docs/production-deployment.md) |
| 如何新增一个客户端/登录端 | [新增客户端指南](docs/backend/new-client.md) |
| 项目能做什么、已有封装有哪些 | [项目能力总览](docs/project-overview.md) |
| 后端模块、starter、yml 和业务接入 | [后端文档](docs/backend/README.md) |
| 前端配置与页面开发约定 | [前端文档](docs/frontend/README.md) |

## 常用命令

### 后端

```bash
cd backend/travis-monolith

mvn clean compile -DskipTests
mvn test
mvn spotless:check
```

基础设施模块变更后先安装：

```bash
cd backend/travis-dependencies
mvn clean install -DskipTests

cd ../travis-infrastructure
mvn clean install -DskipTests
```

### 前端

```bash
cd frontend/admin-vben

pnpm dev:travis-admin
pnpm check:type
pnpm lint
pnpm test:unit
pnpm build:travis-admin
```

前端只使用 pnpm，不要使用 npm 或 yarn 重写 lockfile。

## 主要技术栈

| 领域 | 技术 |
| --- | --- |
| 模块化后端 | Spring Boot 4.1.0、Spring Modulith 2.1.0、JDK 25 |
| 数据访问 | MyBatis-Plus、Druid、MySQL、Flyway |
| 认证与权限 | Sa-Token、JWT |
| 缓存与并发 | Redis、Redisson、Spring Cache |
| 消息与实时通信 | 可选 RocketMQ、WebSocket |
| 调度与治理 | Quartz JDBC、Actuator、结构化日志 |
| 管理端 | Vue 3、Vite、Vben Admin、Ant Design Vue、VXE Grid |
| 工程化 | Maven、pnpm workspace、Turbo、Spotless、ESLint、Lefthook |

## 开发约定

- 新增封装前先搜索已有工具类、starter、公共 API 和相似调用点；
- Controller 使用统一响应、校验、错误码和后端权限校验；
- 首版发布前可合并尚未发布的 SQL；已经发布或执行的迁移只新增、不修改；
- 前后端字段契约一起核对，前端隐藏不是安全边界；
- 文档需要同步说明新增的公共类、配置项、starter 和业务接入方式。

详细约定见 [后端模块开发](docs/backend/module-development.md) 和 [管理端开发约定](docs/frontend/development.md)。

## License

本项目基于 [LICENSE](LICENSE) 中的许可协议发布。
