# 快速开始

本文给出从空环境启动 Travis Scaffold 的最短路径。当前 Compose 与 dev 默认值存在库名和端口差异，下面使用环境变量显式对齐，不要求修改仓库配置。

## 环境要求

| 工具 | 要求 |
| --- | --- |
| JDK | 25 |
| Maven | 与 JDK 25 兼容的 Maven 3.9+ |
| Node.js | `>=22.18.0` |
| pnpm | `>=11.0.0`，项目锁定 `pnpm@11.2.2` |
| Docker / Compose | 用于本地 MySQL、Redis、RocketMQ |

前端只允许 pnpm，不要用 npm/yarn 重写 lockfile。

## 1. 启动基础设施

```bash
cd backend/travis-monolith
docker compose up -d
docker compose ps
docker compose port mysql 3306
```

记录最后一条命令返回的 MySQL 宿主机端口，例如 `0.0.0.0:55001` 中的 `55001`。Redis 固定为 6379，RocketMQ NameServer 为 9876，Proxy gRPC 为 8081。

当前 Compose 还会占用宿主机 8080，而后端默认端口也是 8080，所以下文把后端改为 8082。所有差异详见 [后端配置](backend/configuration.md#本地-compose-注意事项)。

## 2. 准备数据库和环境变量

Compose 创建的数据库为 `mydatabase`，账号为 `myuser/secret`。在当前终端设置：

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:<MySQL宿主机端口>/mydatabase?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true'
export MYSQL_USERNAME='myuser'
export MYSQL_PASSWORD='secret'
export JWT_SECRET_KEY='<替换为足够长的本地开发密钥>'
export SPRING_FLYWAY_ENABLED='true'
export SERVER_PORT='8082'
```

把尖括号占位内容替换为真实值。Flyway 默认关闭；全新本地库可以临时启用，让 `db/migration` 中的初始化和增量脚本建表。已有数据库不要直接开启，先按 [数据库迁移说明](../backend/travis-monolith/travis-server/src/main/resources/db/README.md) 完成基线核对。

这些示例凭证只能用于本机开发。生产环境还必须设置 RocketMQ ACL、真实 Redis/MySQL 地址并更换 Druid 监控账号。

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

启动成功后，管理端 API 前缀是 `http://localhost:8082/api/admin`，app API 前缀是 `http://localhost:8082/api/app`，WebSocket 端点分别是 `/ws/admin` 和 `/ws/app`。

若启动失败，先按顺序检查 MySQL 实际端口/库名、Redis 6379、RocketMQ 9876/8081、JWT 密钥和 Flyway 表结构，不要只看 Java 编译是否通过。

## 4. 配置并启动管理端

管理端仓库默认 API 地址没有端口，适合经过网关访问。直接连接本地 8082 时，新建不提交的 `frontend/admin-vben/apps/travis-admin/.env.development.local`：

```dotenv
VITE_GLOB_API_URL=http://localhost:8082/api/admin
VITE_GLOB_WS_URL=ws://localhost:8082/ws/admin
```

安装并启动：

```bash
cd frontend/admin-vben
pnpm install
pnpm dev:travis-admin
```

开发服务器默认端口是 5999。首次登录账号应以当前初始化脚本或项目维护者提供的信息为准；不要在文档中长期维护共享明文密码。

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
