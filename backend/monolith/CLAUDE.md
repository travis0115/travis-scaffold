# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# 全量构建（跳过测试）
mvn clean package -DskipTests

# 启动应用（需要先启动 MySQL 和 Redis）
mvn -pl travis-server spring-boot:run

# 仅编译不打包
mvn compile

# 运行测试
mvn test

# 运行单个模块的测试
mvn -pl travis-module-demo test
```

基础设施依赖（MySQL、Redis）通过 `compose.yaml` 提供，可用 `docker compose up -d` 启动。

## Architecture

多模块单体架构脚手架，基于 Spring Boot 4 + JDK 25。基础包路径：`com.travis.monolith`。

### 模块依赖层级（从底到上）

```
travis-dependencies (BOM，版本管理)
  └─ travis-common (常量、工具类)
  └─ travis-framework/* (Spring Boot Starters，自动配置)
       ├─ starter-webmvc    — CORS、全局异常处理、RequestId/TraceId 过滤器
       ├─ starter-mybatis   — MyBatis-Plus 自动配置（分页、乐观锁、自动填充）
       ├─ starter-redis     — RedisTemplate + RedisUtils + 缓存管理器
       ├─ starter-sa-token  — Sa-Token + JWT 认证拦截器
       ├─ starter-jackson   — 自定义时间序列化格式
       ├─ starter-desensitize — 数据脱敏（Jackson 集成 + SpEL）
       └─ starter-logging   — 访问日志、MDC 脱敏
travis-module-dependencies (BOM，业务模块版本管理)
  └─ travis-module-demo    — Demo 业务模块
  └─ travis-module-platform
       ├─ travis-module-system — 系统管理
       └─ travis-module-ops    — 运维管理
travis-server — 启动模块，聚合所有业务模块
```

**版本管理**：使用 `travis-dependencies`（跨项目复用）和 `travis-module-dependencies`（项目内）两个 BOM，通过 `${revision}` + flatten-maven-plugin 统一版本。

**启动入口**：`travis-server` 中的 `MonolithServerApplication`，通过 `@SpringBootApplication(scanBasePackages = {"${travis.application.base-package}"})` 扫描 `com.travis.monolith` 包。

### 业务模块代码分层（以 demo 为例）

```
internal/
  controller/   — REST 控制器
  service/      — 业务接口（extends IService）+ 实现（extends ServiceImpl）
  mapper/       — 数据访问（extends BaseMapper）
  model/        — 实体类（extends BaseEntity）
```

所有 starter 自动配置通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册。

## Key Configuration

- **应用配置**：`travis-server/src/main/resources/application.yml`，通过 `spring.profiles.active` 切换 dev/prod
- **数据库**：MySQL + Druid 连接池，MyBatis-Plus 表前缀 `t_`
- **缓存**：Redis (Lettuce) + Redisson
- **认证**：Sa-Token + JWT，Token 通过 Authorization Header 传递（Bearer 前缀）
- **虚拟线程**：已启用（`spring.threads.virtual.enabled: true`）
- **国际化**：i18n 已启用，消息文件在 `i18n/messages`

## Conventions

- Java 25，使用 Lombok 减少样板代码
- Maven 仓库使用华为云/阿里云镜像加速
- 配置属性使用 `@ConfigurationProperties` 而非 `@Value`
- 统一响应格式 `ApiResponse`
- 实体类继承 `BaseEntity`，包含公共字段（createTime、updateTime 等）和自动填充
