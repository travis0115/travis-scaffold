# 快速开始

本文给出从空环境启动 Travis Scaffold 的最短路径。Compose 与 dev Profile 已使用一致的本地默认值，首次启动不需要额外拼接环境变量。

## 环境要求

| 工具 | 要求 |
| --- | --- |
| JDK | 25 |
| Maven | 与 JDK 25 兼容的 Maven 3.9+ |
| Node.js | `>=22.18.0` |
| pnpm | `>=11.0.0`，项目锁定 `pnpm@11.2.2` |
| Docker / Compose | 用于本地 MySQL、Redis，以及按需启用的 RocketMQ |

前端只允许 pnpm，不要用 npm/yarn 重写 lockfile。

## 1. 启动基础设施

```bash
cd backend/travis-monolith
docker compose up -d
docker compose ps
```

本地端口为 MySQL 3306、Redis 6379；Compose 还预留 RocketMQ NameServer 9876、Broker 10909/10911/10912、Proxy gRPC 8081。当前单体未引入 RocketMQ starter，不依赖其启动。后端使用 8080，不与 Compose 冲突。

## 2. 准备数据库和环境变量

Compose 创建数据库 `travis_monolith`，本地账号为 `travis/travis-local-password`。dev Profile 已提供相同默认值和仅限开发使用的 JWT 密钥，通常无需设置环境变量。

需要覆盖时，在当前终端设置：

```bash
export MYSQL_URL='jdbc:mysql://127.0.0.1:3306/travis_monolith?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true'
export MYSQL_USERNAME='travis'
export MYSQL_PASSWORD='travis-local-password'
export JWT_SECRET_KEY='<替换为足够长的本地开发密钥>'
export SPRING_FLYWAY_ENABLED='true'
```

把尖括号占位内容替换为真实值。开发环境默认关闭 Flyway，脚手架维护者可以先整理、合并尚未发布的 SQL，再对全新数据库显式启用。已有数据库启用前应先完成基线核对。

这些示例凭证只能用于本机开发。prod Profile 不提供 MySQL、Redis、JWT 的弱默认值，缺失时会启动失败；RocketMQ 变量仅在业务接入对应 starter 后需要。

## 3. 构建并启动后端

基础设施 BOM/starter 首次使用或发生变更时先安装：

```bash
cd backend/travis-dependencies
mvn clean install -DskipTests

cd ../travis-infrastructure
mvn clean install -DskipTests
```

然后启动单体应用：

```bash
cd ../travis-monolith
mvn spring-boot:run -pl travis-server
```

启动成功后，管理端 API 前缀是 `http://localhost:8080/api/admin`，app API 前缀是 `http://localhost:8080/api/app`，WebSocket 端点分别是 `/ws/admin` 和 `/ws/app`。

若启动失败，先按顺序检查 MySQL 实际端口/库名、Redis 6379、JWT 密钥和数据库表结构；仅在业务接入 RocketMQ 后检查 9876/8081，不要只看 Java 编译是否通过。

## 4. 配置并启动管理端

管理端仓库默认 API 地址没有端口，适合经过网关访问。直接连接本地 8080 时，新建不提交的 `frontend/admin-vben/apps/travis-admin/.env.development.local`：

```dotenv
VITE_GLOB_API_URL=http://localhost:8080/api/admin
VITE_GLOB_WS_URL=ws://localhost:8080/ws/admin
```

安装并启动：

```bash
cd frontend/admin-vben
pnpm install
pnpm dev:travis-admin
```

开发服务器默认端口是 5999。执行当前初始化 SQL 后，首次登录账号和密码均为 `admin718`。该凭据仅用于脚手架开发和演示，基于脚手架创建业务项目时必须立即替换。

## 5. 基础验证

```bash
# 后端编译与测试
cd backend/travis-monolith
mvn clean compile -DskipTests
mvn test

# 前端类型与构建
cd ../../frontend/admin-vben
pnpm check:type
pnpm build:travis-admin
```

按需运行单模块测试能更快定位问题。涉及 MySQL、Redis、Quartz、RocketMQ、WebSocket 或集群行为时，仍需保留真实运行环境验证记录。

## 下一步

- 先看 [项目能力总览](project-overview.md)，确认已有能力；
- 后端配置看 [后端配置项参考](backend/configuration.md)，前端变量看 [前端配置](frontend/configuration.md)；
- 新增后端域看 [后端模块开发](backend/module-development.md)；
- 日常公共封装从 [文档总目录](README.md) 按“我想做什么”进入。
