# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 项目概述

travis-scaffold 是一个单体多模块架构的 Java 脚手架项目，提供开箱即用的快速开发方案。后端基于 Spring Boot 4 + JDK 25，前端基于 Vue 3 + Vite 的 Vben Admin 模板。

## 构建与运行命令

### 后端（Maven 多模块）

```bash
# 进入后端根目录
cd backend/travis-monolith

# 编译（跳过测试）
mvn clean compile -DskipTests

# 运行（需要先启动 MySQL 和 Redis，见 compose.yaml）
mvn spring-boot:run -pl travis-server

# 运行所有测试
mvn test

# 运行单个模块测试
mvn test -pl travis-module-platform/travis-module-system

# 代码格式化（Spotless，AOSP 风格）
mvn spotless:apply

# 检查格式
mvn spotless:check

# 本地依赖（infrastructure 模块变更后需先安装）
cd backend/travis-infrastructure && mvn clean install -DskipTests
cd backend/travis-dependencies && mvn clean install -DskipTests
```

### 前端（pnpm + Turbo monorepo）

```bash
# 进入前端目录
cd frontend/admin-vben

# 安装依赖（仅允许 pnpm）
pnpm install

# 启动开发服务器
pnpm dev

# 启动 travis-admin 应用
pnpm dev:travis-admin

# 构建
pnpm build

# 仅构建 travis-admin
pnpm build:travis-admin

# Lint
pnpm lint

# 格式化
pnpm format

# 类型检查
pnpm check:type

# 单元测试
pnpm test:unit

# E2E 测试
pnpm test:e2e
```

### 本地基础设施

```bash
# 启动 MySQL + Redis（后端目录下）
cd backend/travis-monolith
docker compose up -d
```

## 后端架构

### 模块层级

```
backend/
├── travis-dependencies/          # 全局 BOM，管理所有第三方和基础设施模块版本
│   └── pom.xml                   # Spring Boot 4.0.6, MyBatis Plus 3.5.16, Sa-Token 1.45.0 等
│
├── travis-infrastructure/        # 跨项目共享的基础设施
│   ├── travis-common/            # 通用模型：ApiResponse、IErrorCode、验证注解、MapStruct 基础配置
│   └── travis-framework/         # 自定义 Spring Boot Starter（通过 AutoConfiguration.imports 注册）
│       ├── travis-spring-boot-starter-webmvc/      # Web 配置、全局异常处理、API 前缀自动映射
│       ├── travis-spring-boot-starter-sa-token/    # Sa-Token 认证、多 loginType 拦截
│       ├── travis-spring-boot-starter-mybatis/     # MyBatis-Plus 配置、BaseEntity、自动填充
│       ├── travis-spring-boot-starter-redis/       # Redis + Redisson 配置
│       ├── travis-spring-boot-starter-jackson/     # JSON 序列化配置
│       ├── travis-spring-boot-starter-logging/     # 日志配置
│       └── travis-spring-boot-starter-desensitize/ # 数据脱敏（邮箱、手机号等）
│
└── travis-monolith/              # 单体应用（具体业务项目）
    ├── travis-module-dependencies/  # 业务模块 BOM
    ├── travis-module-demo/         # 示例模块
    ├── travis-module-platform/     # 平台模块
    │   ├── travis-module-system/   # 系统管理（用户、角色、菜单、字典、部门等）
    │   └── travis-module-ops/      # 运维模块
    └── travis-server/              # 启动入口（MonolithServerApplication）
```

### 关键约定

- **API 前缀自动映射**：Controller 按包名自动添加路径前缀。`controller.admin` → `/api/admin`，`controller.app` → `/api/app`（在 `application.yml` 的 `travis.web.apis` 中配置）
- **统一响应**：所有 Controller 返回 `ApiResponse<T>`，静态方法 `success()` / `error()`
- **错误码**：实现 `IErrorCode` 接口的枚举。全局错误码在 `CommonErrorCode`，各模块在各自的 `exception/` 包下定义（如 `SystemErrorCode`，前缀 `SYS_`）
- **业务异常**：抛出 `BizException(errorCode)`，由全局 `BizExceptionHandler` 统一处理
- **实体基类**：所有数据库实体继承 `BaseEntity`，提供 id（雪花算法）、createTime/updateTime、createBy/updateBy、逻辑删除字段，由 MyBatis-Plus 自动填充
- **认证框架**：Sa-Token + JWT，支持多 loginType（admin/app），通过 `travis.web.security.auth-rules` 配置路径拦截规则
- **MapStruct**：全局处理器路径已配置 Lombok-MapStruct 绑定，使用 `BaseMapperConfig` 作为共享配置
- **版本管理**：使用 `flatten-maven-plugin` + `${revision}` 统一版本号，版本号集中管理在各 BOM 的 `<properties>` 中

### 添加新业务模块

1. 在 `travis-module-platform/` 下创建新模块，参考 `travis-module-system` 的结构
2. 在 `travis-module-dependencies/pom.xml` 中添加版本管理
3. 在 `travis-server/pom.xml` 中添加依赖
4. Controller 放在 `internal/controller/admin/` 或 `internal/controller/app/` 下，路径前缀自动生效
5. 错误码在模块的 `internal/exception/` 下定义枚举

### 注解处理器

`maven-compiler-plugin` 配置了以下处理器路径（顺序重要）：
1. `spring-boot-configuration-processor`
2. `lombok`
3. `mapstruct-processor`
4. `lombok-mapstruct-binding`

### Maven 仓库

项目使用华为云和阿里云镜像加速依赖下载，同时配置了 Spring Milestones/Snapshots 仓库（因为依赖 Spring Boot 4.x 预发布版本）。

## 前端架构

基于 Vben Admin 的 pnpm workspace monorepo：

- `apps/travis-admin/` — 主管理后台应用
- `playground/` — 开发调试用的 playground
- `packages/` — 共享包（@core、effects、icons、styles、types、utils）
- `scripts/` — 部署脚本（Docker + Nginx）

Node.js >= 22.18.0，pnpm >= 11.0.0。

## 环境配置

后端通过环境变量注入敏感配置：
- `MYSQL_URL` — 数据库连接（默认 `jdbc:mysql://127.0.0.1:3306/travis_monolith`）
- `MYSQL_USERNAME` / `MYSQL_PASSWORD` — 数据库账号
- `JWT_SECRET_KEY` — JWT 签名密钥
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_DATABASE` — Redis 配置

Profile：`dev`（默认，debug 日志）和 `prod`（JSON 格式日志）

## 代码格式

- **Java**：Spotless + Google Java Format（AOSP 风格，4空格缩进）
- **前端**：ESLint + oxlint + oxfmt
- **Git Hooks**：Lefthook（前后端共用配置在根目录 `lefthook.yml`）
