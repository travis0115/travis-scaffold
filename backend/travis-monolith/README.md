# Travis Monolith

Travis Monolith 是 Travis Scaffold 的 Spring Boot 4.1.0 + JDK 25 模块化单体后端，使用 Spring Modulith 约束业务边界。

## 模块

```text
travis-monolith/
├── travis-module-dependencies/  # 业务模块 BOM
├── travis-module-app/           # app 端适配模块
├── travis-module-demo/          # 示例模块
├── travis-module-platform/      # system、ops 等平台模块
└── travis-server/               # 启动入口、环境配置和数据库迁移
```

跨模块调用应依赖公开 API、事件或 `@NamedInterface`，不要直接引用其他模块的 `internal` 实现。

## 本地启动

```bash
docker compose up -d
mvn spring-boot:run -pl travis-server
```

开发环境默认不执行 Flyway，便于脚手架首版维护期间合并尚未发布的 SQL。需要初始化全新数据库时，先审阅迁移内容，再显式设置 `SPRING_FLYWAY_ENABLED=true`。初始账号和密码均为 `admin718`，仅用于本地开发和演示。

## 验证

```bash
mvn clean compile -DskipTests
mvn test
mvn spotless:check
```

如果修改了 `backend/travis-dependencies` 或 `backend/travis-infrastructure`，先分别执行 `mvn clean install -DskipTests`，再验证本项目。

完整说明见仓库根目录的 [README](../../README.md) 与 [后端文档](../../docs/backend/README.md)。
