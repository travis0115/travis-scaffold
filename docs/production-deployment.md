# 生产部署

本方案面向 Ubuntu 单机部署，入口为宿主机 Nginx，MySQL/Redis 与任意数量的后端容器位于内部 Docker 网络。它不新增 `/healthz`：容器编排使用 Spring Boot 原生 `/actuator/health/readiness`，公网只精确开放 `/actuator/health`。

## 架构与边界

```text
Internet -> Nginx :443 -> 127.0.0.1:动态端口 -> backend x APP_REPLICAS
                         |                    -> MySQL
                         |                    -> Redis
                         `-> 前端静态目录
```

- `APP_REPLICAS` 是任意正整数，没有两实例上限，也没有固定的 18081/18082。
- 所有后端端口只随机绑定到 `127.0.0.1`，脚本发现健康实例后生成 Nginx upstream。
- 升级启动新的 release 实例组，健康后一次切流；旧组保留供回滚。
- MySQL、Redis、宿主机和 Nginx 仍是单点。跨机器高可用需要外置数据库、Redis 和负载均衡。
- WebSocket 长连接无法迁移；实例退出时客户端需要重连。

## 1. 主机准备

安装 Docker Engine、Compose v2、Nginx、curl、gettext-base、flock 和证书工具，并仅开放 22、80、443。部署脚本依赖 Bash 4+，建议 Ubuntu 24.04。

```bash
sudo apt update
sudo apt install -y nginx curl gettext-base util-linux ca-certificates
docker version
docker compose version
```

创建由部署用户持有的明确根目录（不要把仓库根目录或用户家目录作为 `DEPLOY_ROOT`）：

```bash
sudo install -d -m 0750 -o "$USER" -g "$(id -gn)" /opt/travis
```

部署用户需要使用 Docker，并具有安装/校验 Nginx 配置的 sudo 权限。Docker 组等同高权限，只授予可信运维用户。

## 2. 环境与前端产物

```bash
cd /opt/travis/source/deploy/production
cp .env.example .env
chmod 600 .env
```

逐项替换 `.env` 占位值。值按 shell 环境文件语法书写；包含空格或特殊字符时正确引用。生产必须使用不可变镜像 tag/digest、独立随机的 MySQL/Redis 密码和至少 32 字符的 JWT 密钥。

当前单体应用未引入 RocketMQ starter，`.env` 中相关变量可保持为空。业务模块接入 RocketMQ 后，再填写外部或独立部署集群的地址与凭据，并把连通性加入发布验收。

构建后端镜像和前端发布目录：

```bash
./scripts/build-release.sh v1.0.0 registry.example.com/travis-scaffold:1.0.0
docker push registry.example.com/travis-scaffold:1.0.0
```

也可以在 CI 构建镜像，并把前端产物放到 `frontend/releases/<release>`。首次构建会创建 `FRONTEND_ROOT` 软链接；后续构建只准备产物，由升级/回滚脚本与后端 release 一起切换。Dockerfile 的基础镜像已固定版本；后端以 UID 10001 非 root、只读根文件系统运行。

## 3. 首次部署

先准备 `PUBLIC_DOMAIN` 对应证书，再执行：

```bash
./scripts/validate.sh
./scripts/first-deploy.sh
./scripts/status.sh
```

脚本将：

1. 校验必填项、弱密钥、镜像 tag、Compose 配置和登录限流范围；
2. 创建 `travis-internal` 网络与持久化目录；
3. 启动 MySQL/Redis，然后按 `APP_REPLICAS` 启动后端；
4. 等待每个实例的 readiness；
5. 写临时 upstream，执行 `nginx -t` 后平滑 reload。

任何健康检查或 Nginx 校验失败都会拒绝切流。prod Profile 使用公共配置并在应用启动时执行 Flyway；首次部署和升级前必须完成数据库备份、迁移评审，并确保变更与保留运行的上一版本兼容。开发环境默认关闭 Flyway，不影响生产行为。

## 4. 扩缩容

```bash
# 扩到 5 个实例
./scripts/scale.sh 5

# 缩到 3 个实例
./scripts/scale.sh 3
```

扩容顺序是“创建 -> readiness -> 加入 upstream”；缩容顺序是“从 upstream 摘除 -> 等待 `DRAIN_SECONDS` -> 停止并删除多余容器”。脚本更新 `.env` 中的 `APP_REPLICAS`，并保留带 UTC 时间戳的备份。

容器异常重建、宿主机重启或手工维护后，可以重新发现实例：

```bash
./scripts/sync-upstreams.sh
```

## 5. 升级与回滚

```bash
./scripts/build-release.sh v1.0.1 registry.example.com/travis-scaffold:1.0.1
docker push registry.example.com/travis-scaffold:1.0.1
./scripts/upgrade.sh registry.example.com/travis-scaffold:1.0.1 v1.0.1
./scripts/status.sh
```

升级不逐个修改当前实例，而是创建新的 Compose release 项目。全部新实例通过 readiness 后才切换 upstream；旧 release 和旧镜像记录在 `.env` 中。

需要回滚时：

```bash
./scripts/rollback.sh
```

回滚要求旧实例组仍在运行且健康。确认观察期结束后，可以显式删除不再需要的旧项目：

```bash
docker compose --env-file .env -f compose.app.yml -p travis-app-旧发布标识 down
```

升级与回滚只切应用流量，不回滚数据库。数据库变更必须满足向前/向后兼容，或另行制定停机恢复方案。

## 6. 备份

```bash
./scripts/backup.sh
./scripts/verify-backup.sh /opt/travis/backups/<UTC时间戳>
```

每次生成独立 UTC 时间目录，包含 MySQL 逻辑备份、Redis 独立 RDB 快照、上传文件和 SHA-256 校验清单。`verify-backup.sh` 校验文件完整性和压缩包可读性。脚本不会自动删除旧备份；先完成加密异机复制和恢复演练，再由运维策略清理。

至少演练：创建临时 MySQL/Redis、校验 `SHA256SUMS`、恢复数据、启动同版本应用、检查登录/文件/消息/任务。未演练的备份不能视为可恢复。

## 7. Actuator 与登录保护

生产配置只暴露 `health`，不显示组件和详情。Nginx 仅允许精确路径 `/actuator/health`；`/actuator/**` 的其他路径和 `/druid/**` 返回 404。readiness 仅供回环端口和部署脚本访问。

登录保护分两层：

- Nginx 按真实来源 IP 对 admin/app 登录路径分别限流，洪泛时统一返回 429；
- 应用按账号和 IP 记录 Redis TTL 失败计数，超过阈值临时锁定。Redis 故障时记录告警并 fail-open，由 Nginx 继续兜底。

调整 Nginx 限流：

```bash
./scripts/set-login-rate-limit.sh admin 20 10
./scripts/set-login-rate-limit.sh app 300 100
```

脚本限制合法范围，重新渲染配置，只有 `nginx -t` 成功才 reload，失败自动恢复。

应用层参数通过系统参数管理动态调整，key 如下：

| key 后缀 | admin 默认值 | app 默认值 | 允许范围 |
| --- | ---: | ---: | ---: |
| `window-seconds` | 600 | 600 | 60..3600 |
| `account-max-failures` | 5 | 8 | 3..20 |
| `account-lock-seconds` | 900 | 600 | 60..86400 |
| `ip-max-failures` | 60 | 300 | 10..5000 |
| `ip-lock-seconds` | 900 | 600 | 60..86400 |

完整 key 是 `security.login.admin.<后缀>` 或 `security.login.app.<后缀>`。修改后系统配置缓存会失效；非法值被忽略并回退到安全默认值。观测 429、账号锁日志和正常用户误伤后再逐步调整。

如果前面还有 CDN/负载均衡，当前 Nginx 会把 TCP 对端作为来源并覆盖客户端伪造的 `X-Forwarded-For`。只有在列出可信代理 CIDR并配置 Nginx `real_ip` 后，才能使用上游传递的真实 IP。

## 8. 发布验收

- `validate.sh`、`status.sh` 均成功；
- 公网 `/actuator/health` 返回 UP，其他 Actuator/Druid 路径不可访问；
- 后端实例数与 `.env` 一致，端口仅绑定 `127.0.0.1`；
- admin/app 登录正常，连续失败能触发限制，日志不包含用户名原文；
- 上传与下载、WebSocket 重连、消息、Quartz 在多实例下完成运行时验证；
- 已保存数据库、Redis、上传目录备份并验证校验和；
- 已记录 release、镜像 digest、Git commit、迁移版本与回滚窗口。

HSTS 默认未开启。确认域名全部子域长期 HTTPS、证书续期和回退方案后，再在 Nginx 增加 HSTS，避免错误配置导致无法通过 HTTP 恢复。
