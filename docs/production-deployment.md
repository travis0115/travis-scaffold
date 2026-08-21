# 单机双实例生产部署教程

本文从一台空白服务器开始，部署 Travis Scaffold 管理端、两个后端实例、MySQL 和 Redis，并实现数据持久化、HTTPS、备份、滚动升级和回滚。

## 1. 架构与边界

```text
互联网 -> Nginx :80/:443
             |-- /                 -> 静态前端 current 软链接
             `-- /api、/files、/ws -> app1 :18081
                                    -> app2 :18082
                                           |-- MySQL
                                           `-- Redis
```

这套方案可以做到：

- 一个后端实例升级或异常时，另一个实例继续处理 HTTP 请求；
- Nginx reload、前端软链接切换不停止对外服务；
- MySQL、Redis、上传文件和日志不写在容器临时层；
- 当前项目的 JDBC Quartz 集群和 Redis WebSocket 广播可在两个实例间协作；
- 升级时逐个摘流、替换、健康检查、重新加入。

它仍不是整机高可用。服务器、宿主机 Nginx、单节点 MySQL 和单节点 Redis 都是单点；服务器宕机时两个应用实例会一起不可用。真正高可用至少需要两台应用服务器和独立的数据库、Redis、负载均衡服务。

“不中断”主要指普通 HTTP 请求持续可用。升级承载现有 WebSocket 的 JVM 时，该实例上的长连接会断开；当前管理端会自动重连，但无法把已经建立的 TCP 连接无损迁移到另一个进程。

## 2. 假设和资源

本文假设：

- Ubuntu 24.04 LTS，`amd64` 或 `arm64`；
- 域名如 `admin.example.com` 已解析到服务器；
- 普通部署用户为 `deploy`；
- 项目位于 `/opt/travis/source`，数据位于 `/opt/travis`；
- 示例使用 MySQL 9.7、Redis 7.2、JRE 25。上线前需在测试环境验证，并固定准确版本或镜像 digest，不使用 `latest`。

低流量测试环境至少建议 4 核 8 GB、100 GB SSD；较稳妥的生产起点为 8 核 16 GB。资源不足时，两个 JVM、MySQL 和 Redis 争抢内存反而会降低稳定性。

## 3. 初始化服务器

云安全组只开放 22、80、443。22 最好只允许你的固定 IP；不要开放 3306、6379、18081、18082。

首次以云厂商提供的管理员账号登录，创建部署用户：

```bash
adduser deploy
usermod -aG sudo deploy
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
cp /root/.ssh/authorized_keys /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys
```

新开终端确认 `ssh deploy@SERVER_IP` 可用后，再关闭 root 和密码登录。创建 `/etc/ssh/sshd_config.d/99-hardening.conf`：

```text
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
```

```bash
sudo sshd -t
sudo systemctl reload ssh
sudo apt update
sudo apt full-upgrade -y
sudo apt install -y ca-certificates curl git nginx ufw certbot python3-certbot-nginx
sudo timedatectl set-timezone Asia/Shanghai
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
```

Docker 发布端口可能绕过 UFW，因此数据库端口仍必须只绑定回环地址或仅进入内部网络。参见 [Docker 防火墙说明](https://docs.docker.com/engine/install/ubuntu/#firewall-limitations)。

## 4. 安装 Docker Engine 和 Compose

使用 Docker 官方 apt 仓库：

```bash
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
```

```bash
sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io \
  docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker deploy
sudo systemctl enable --now docker
```

重新登录后验证：

```bash
docker version
docker compose version
docker run --rm hello-world
```

安装细节以 [Docker 官方 Ubuntu 指南](https://docs.docker.com/engine/install/ubuntu/) 为准。`docker` 组拥有很高的宿主机权限，只加入可信用户。

## 5. 目录、源码和密钥

```bash
sudo mkdir -p /opt/travis/{source,deploy,data/{mysql,redis,uploads},logs,backups/{mysql,redis,uploads},frontend/releases}
sudo chown -R deploy:deploy /opt/travis
chmod 750 /opt/travis/deploy /opt/travis/backups
git clone YOUR_REPOSITORY_URL /opt/travis/source
cd /opt/travis/source
git checkout YOUR_RELEASE_TAG_OR_COMMIT
```

生产发布必须记录明确的 Git commit 或 tag。私有仓库使用只读 Deploy Key，不把个人令牌写入命令。

运行四次 `openssl rand -hex 24` 生成密码，再创建 `/opt/travis/deploy/.env`：

```dotenv
COMPOSE_PROJECT_NAME=travis
PUBLIC_ORIGIN=https://admin.example.com
MYSQL_IMAGE=mysql:9.7
REDIS_IMAGE=redis:7.2-alpine
MYSQL_DATABASE=travis_monolith
MYSQL_USER=travis
MYSQL_PASSWORD=CHANGE_ME
MYSQL_ROOT_PASSWORD=CHANGE_ME
REDIS_PASSWORD=CHANGE_ME
JWT_SECRET_KEY=CHANGE_ME_LONG_RANDOM_VALUE
DRUID_USERNAME=travis-monitor
DRUID_PASSWORD=CHANGE_ME
APP1_IMAGE=travis-backend:initial
APP2_IMAGE=travis-backend:initial
```

```bash
chmod 600 /opt/travis/deploy/.env
```

`.env` 不能提交。它只是限制普通读取，不是专业密钥保险库；还需保护服务器、备份和发布日志。

## 6. 构建后端镜像

后端包含依赖 BOM、基础设施和业务单体三个 Maven 根项目。下面在容器内构建，服务器无需安装 JDK/Maven。

创建 `/opt/travis/source/deploy/backend.Dockerfile`：

```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /workspace
COPY backend /workspace/backend
RUN mvn -f backend/travis-dependencies/pom.xml -DskipTests clean install
RUN mvn -f backend/travis-infrastructure/pom.xml -DskipTests clean install
RUN mvn -f backend/travis-monolith/pom.xml \
    -pl travis-server -am -DskipTests clean install
RUN mvn -f backend/travis-monolith/travis-server/pom.xml \
    -DskipTests package spring-boot:repackage
RUN jar tf backend/travis-monolith/travis-server/target/travis-server-*.jar \
    | grep -q '^BOOT-INF/'

FROM eclipse-temurin:25-jre-jammy
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --create-home app
WORKDIR /app
COPY --from=builder \
  /workspace/backend/travis-monolith/travis-server/target/travis-server-*.jar \
  /app/application.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
```

这里显式执行 `spring-boot:repackage`，因为当前 `travis-server/pom.xml` 只绑定了 `build-info`；`BOOT-INF` 检查防止把普通 jar 当成可执行包。参见 [Spring Boot 可执行归档](https://docs.spring.io/spring-boot/4.0/maven-plugin/packaging.html)。

```bash
cd /opt/travis/source
VERSION="$(git rev-parse --short=12 HEAD)"
docker build -f deploy/backend.Dockerfile -t "travis-backend:${VERSION}" .
docker image inspect "travis-backend:${VERSION}" --format '{{.Id}}'
```

不要反复覆盖 `latest`；不可变 tag 或 digest 是可靠回滚的前提。

## 7. MySQL、Redis 和双后端 Compose

创建 `/opt/travis/deploy/compose.yml`：

```yaml
name: travis

x-app-common: &app-common
  restart: unless-stopped
  init: true
  depends_on:
    mysql:
      condition: service_healthy
    redis:
      condition: service_healthy
  environment:
    SPRING_PROFILES_ACTIVE: prod
    SERVER_PORT: 8080
    SERVER_SHUTDOWN: graceful
    SPRING_LIFECYCLE_TIMEOUT_PER_SHUTDOWN_PHASE: 30s
    MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED: "true"
    SPRING_FLYWAY_ENABLED: "true"
    MYSQL_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
    MYSQL_USERNAME: ${MYSQL_USER}
    MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    REDIS_HOST: redis
    REDIS_PORT: 6379
    REDIS_DATABASE: 0
    SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD}
    JWT_SECRET_KEY: ${JWT_SECRET_KEY}
    DRUID_USERNAME: ${DRUID_USERNAME}
    DRUID_PASSWORD: ${DRUID_PASSWORD}
    TRAVIS_WEB_CORS_ALLOWED_ORIGIN_PATTERNS_0: ${PUBLIC_ORIGIN}
    JAVA_TOOL_OPTIONS: -Xms512m -Xmx1536m -XX:+ExitOnOutOfMemoryError
    OPS_JOB_LOG_RETENTION_DAYS: 30
    ROCKETMQ_CONSUMER_ACCESS_KEY: ""
    ROCKETMQ_CONSUMER_SECRET_KEY: ""
    ROCKETMQ_PRODUCER_ACCESS_KEY: ""
    ROCKETMQ_PRODUCER_SECRET_KEY: ""
  volumes:
    - /opt/travis/data/uploads:/home/app/data/uploads
    - /opt/travis/logs:/data/logs
  networks: [internal]
  stop_grace_period: 45s

services:
  mysql:
    image: ${MYSQL_IMAGE}
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      TZ: Asia/Shanghai
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
    volumes:
      - /opt/travis/data/mysql:/var/lib/mysql
    ports:
      - 127.0.0.1:3306:3306
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD --silent"]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 30s
    networks: [internal]

  redis:
    image: ${REDIS_IMAGE}
    restart: unless-stopped
    command:
      - redis-server
      - --appendonly
      - "yes"
      - --appendfsync
      - everysec
      - --save
      - "900"
      - "1"
      - --save
      - "300"
      - "10"
      - --requirepass
      - ${REDIS_PASSWORD}
    environment:
      REDIS_PASSWORD: ${REDIS_PASSWORD}
    volumes:
      - /opt/travis/data/redis:/data
    ports:
      - 127.0.0.1:6379:6379
    healthcheck:
      test: ["CMD-SHELL", "REDISCLI_AUTH=$$REDIS_PASSWORD redis-cli ping | grep -q PONG"]
      interval: 10s
      timeout: 5s
      retries: 20
    networks: [internal]

  app1:
    <<: *app-common
    image: ${APP1_IMAGE}
    hostname: app1
    ports:
      - 127.0.0.1:18081:8080

  app2:
    <<: *app-common
    image: ${APP2_IMAGE}
    hostname: app2
    ports:
      - 127.0.0.1:18082:8080

networks:
  internal:
    driver: bridge
```

两个镜像变量必须分开，才能逐实例升级。共享上传目录对应数据库默认的 `${user.home}/data/uploads`；否则负载均衡后会出现文件只在某个实例可见。

```bash
sudo chown -R 10001:10001 /opt/travis/data/uploads /opt/travis/logs
cd /opt/travis/deploy
sed -i "s/^APP1_IMAGE=.*/APP1_IMAGE=travis-backend:${VERSION}/" .env
sed -i "s/^APP2_IMAGE=.*/APP2_IMAGE=travis-backend:${VERSION}/" .env
docker compose --env-file .env -f compose.yml config --quiet
docker compose --env-file .env -f compose.yml up -d mysql redis
docker compose --env-file .env -f compose.yml ps
```

Redis 同时启用 AOF `everysec` 和 RDB 快照。AOF 通常把故障数据窗口压到约一秒，RDB 方便备份；参见 [Redis 持久化说明](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/)。持久化不等于备份。

先启动 app1，确认迁移和健康后再启动 app2：

```bash
docker compose --env-file .env -f compose.yml up -d app1
docker compose --env-file .env -f compose.yml logs -f --tail=200 app1
curl --fail --silent http://127.0.0.1:18081/actuator/health/readiness
docker compose --env-file .env -f compose.yml up -d app2
curl --fail --silent http://127.0.0.1:18082/actuator/health/readiness
```

部署环境显式打开了项目默认关闭的 Flyway。全新数据库会执行 `db/migration`；已有但未接入 Flyway 的数据库不能直接启动，应先按 [数据库迁移说明](../backend/travis-monolith/travis-server/src/main/resources/db/README.md) 完成基线接管。

两个实例连接同一 MySQL、Redis 和上传目录。当前 Quartz 使用 JDBC clustered JobStore 和 `instanceId=AUTO`，不要给两个实例配置相同的固定 instance ID。

## 8. 构建和发布前端

生产前端必须构建 `@travis/travis-admin`。仓库现有 `scripts/deploy/Dockerfile` 复制的是 `playground/dist`，不用于本教程。

创建 `/opt/travis/source/deploy/frontend-build.Dockerfile`：

```dockerfile
FROM node:22-bookworm-slim AS builder
ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH
ENV CI=true
ENV NODE_OPTIONS=--max-old-space-size=4096
ARG VITE_GLOB_API_URL=/api/admin
ARG VITE_GLOB_WS_URL=/ws/admin
WORKDIR /workspace
COPY frontend/admin-vben /workspace
RUN corepack enable \
    && corepack prepare pnpm@11.2.2 --activate \
    && pnpm install --frozen-lockfile
RUN pnpm --filter @travis/travis-admin build

FROM scratch AS export
COPY --from=builder /workspace/apps/travis-admin/dist /
```

使用相对 API/WS 地址，浏览器会采用当前域名并把 HTTPS 下的 WebSocket 转成 WSS：

```bash
cd /opt/travis/source
VERSION="$(git rev-parse --short=12 HEAD)"
FRONTEND_RELEASE="/opt/travis/frontend/releases/${VERSION}"
mkdir -p "$FRONTEND_RELEASE"
docker build -f deploy/frontend-build.Dockerfile \
  --output "type=local,dest=${FRONTEND_RELEASE}" .
test -f "${FRONTEND_RELEASE}/index.html"
ln -sfn "$FRONTEND_RELEASE" /opt/travis/frontend/current.new
mv -Tf /opt/travis/frontend/current.new /opt/travis/frontend/current
```

保留旧 release，便于原子切回；带 hash 的静态资源也不会相互覆盖。

## 9. Nginx、WebSocket 和 HTTPS

创建 `/etc/nginx/travis-upstreams.conf`：

```nginx
server 127.0.0.1:18081 max_fails=2 fail_timeout=10s;
server 127.0.0.1:18082 max_fails=2 fail_timeout=10s;
```

创建 `/etc/nginx/sites-available/travis.conf`，替换域名：

```nginx
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

upstream travis_backend {
    least_conn;
    include /etc/nginx/travis-upstreams.conf;
    keepalive 64;
}

server {
    listen 80;
    listen [::]:80;
    server_name admin.example.com;
    root /opt/travis/frontend/current;
    index index.html;
    client_max_body_size 100m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~* \.(?:css|js|mjs|png|jpg|jpeg|gif|svg|ico|woff2?)$ {
        try_files $uri =404;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    location ~ ^/(?:api|files)/ {
        proxy_pass http://travis_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 3s;
        proxy_read_timeout 60s;
        proxy_next_upstream error timeout http_502 http_503 http_504;
    }

    location /ws/ {
        proxy_pass http://travis_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    location /actuator/ {
        return 404;
    }
}
```

WebSocket 反向代理必须显式传递 `Upgrade` 和 `Connection`，参见 [Nginx WebSocket proxying](https://nginx.org/en/docs/http/websocket.html)。这里不需要粘性会话：登录状态和缓存位于 Redis；连接建立后 WebSocket 本身固定在该实例，跨实例推送由 Redis Pub/Sub 广播。

```bash
sudo ln -sfn /etc/nginx/sites-available/travis.conf /etc/nginx/sites-enabled/travis.conf
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
curl -I http://admin.example.com
sudo certbot --nginx -d admin.example.com
sudo certbot renew --dry-run
```

首次登录后，在“文件管理 → 存储配置”中把默认本地存储的“访问域名”从初始化值 `http://127.0.0.1` 改成 `https://admin.example.com`。文件接口会把该值与 `/files/**` 路径拼接；不修改会导致其他用户的浏览器访问他们自己的 `127.0.0.1`。同一个域名也应填写到 `.env` 的 `PUBLIC_ORIGIN`，将生产 CORS 来源限制为真实站点。

然后在浏览器中验证登录、权限、写操作、文件上传/访问和 WebSocket 通知。Actuator 只通过 `127.0.0.1:18081/18082` 检查，不暴露公网。

## 10. 备份和恢复

至少备份 MySQL、Redis、`uploads`、部署配置、密钥副本，并记录 Git commit、镜像 digest、前端 release。每天把加密副本传到另一个可用区或对象存储；本机备份无法应对整机或磁盘丢失。

MySQL 在线逻辑备份：

```bash
cd /opt/travis/deploy
set -a; . ./.env; set +a
BACKUP_TIME="$(date +%Y%m%d-%H%M%S)"
docker compose --env-file .env -f compose.yml exec -T \
  -e MYSQL_PWD="$MYSQL_PASSWORD" mysql \
  mysqldump -u"$MYSQL_USER" --single-transaction \
    --routines --events --triggers --set-gtid-purged=OFF "$MYSQL_DATABASE" \
  | gzip > "/opt/travis/backups/mysql/${MYSQL_DATABASE}-${BACKUP_TIME}.sql.gz"
gzip -t "/opt/travis/backups/mysql/${MYSQL_DATABASE}-${BACKUP_TIME}.sql.gz"
```

参见 [MySQL Docker backup and restore](https://dev.mysql.com/doc/refman/8.0/en/docker-mysql-more-topics.html)。大数据库还应使用物理备份、binlog 和时间点恢复。

Redis 和上传文件：

```bash
cd /opt/travis/deploy
set -a; . ./.env; set +a
BACKUP_TIME="$(date +%Y%m%d-%H%M%S)"
docker compose --env-file .env -f compose.yml exec -T \
  -e REDISCLI_AUTH="$REDIS_PASSWORD" redis redis-cli BGSAVE
until docker compose --env-file .env -f compose.yml exec -T \
  -e REDISCLI_AUTH="$REDIS_PASSWORD" redis redis-cli --raw INFO persistence \
  | tr -d '\r' | grep -q 'rdb_bgsave_in_progress:0'; do sleep 1; done
REDIS_CONTAINER="$(docker compose --env-file .env -f compose.yml ps -q redis)"
docker cp "${REDIS_CONTAINER}:/data/dump.rdb" \
  "/opt/travis/backups/redis/dump-${BACKUP_TIME}.rdb"
tar -C /opt/travis/data -czf \
  "/opt/travis/backups/uploads/uploads-${BACKUP_TIME}.tar.gz" uploads
```

恢复必须先在隔离环境演练。MySQL 可将解压 SQL 输入 `mysql` 客户端。Redis 恢复时需停服、放回选定 RDB 并校正权限；若同时存在 AOF，Redis会优先用更完整的 AOF，因此必须同时处理 `appendonlydir`，不能只覆盖 RDB 就宣称已回滚。恢复上传文件后还要抽查数据库路径与 `/files/**`。

## 11. 后端滚动升级

滚动升级要求新旧代码短暂共存。数据库迁移必须采用 expand/contract：先增加兼容结构，所有旧实例下线后的后续版本再删列或收紧约束。已执行的 Flyway 脚本不能修改、重命名或删除。

先备份并构建不可变新镜像：

```bash
cd /opt/travis/source
git fetch --all --tags
git checkout NEW_RELEASE_TAG_OR_COMMIT
NEW_VERSION="$(git rev-parse --short=12 HEAD)"
docker build -f deploy/backend.Dockerfile -t "travis-backend:${NEW_VERSION}" .
```

### 11.1 升级 app1

先从 Nginx 摘除 app1：

```bash
sudo tee /etc/nginx/travis-upstreams.conf >/dev/null <<'EOF'
server 127.0.0.1:18082 max_fails=2 fail_timeout=10s;
EOF
sudo nginx -t && sudo systemctl reload nginx
```

Nginx reload 会启动采用新配置的 worker 并平滑关闭旧 worker，参见 [Nginx reload 说明](https://nginx.org/en/docs/switches.html)。等待普通请求完成后替换实例：

```bash
cd /opt/travis/deploy
sed -i "s/^APP1_IMAGE=.*/APP1_IMAGE=travis-backend:${NEW_VERSION}/" .env
docker compose --env-file .env -f compose.yml up -d --no-deps app1
docker compose --env-file .env -f compose.yml logs --tail=200 app1
for attempt in $(seq 1 60); do
  curl --fail --silent http://127.0.0.1:18081/actuator/health/readiness \
    >/dev/null && break
  sleep 2
done
curl --fail --silent http://127.0.0.1:18081/actuator/health/readiness
```

app1 会执行尚未执行的 Flyway 迁移，此时旧 app2 仍在线，所以迁移必须向后兼容。健康和业务冒烟测试通过后，加回两个实例：

```bash
sudo tee /etc/nginx/travis-upstreams.conf >/dev/null <<'EOF'
server 127.0.0.1:18081 max_fails=2 fail_timeout=10s;
server 127.0.0.1:18082 max_fails=2 fail_timeout=10s;
EOF
sudo nginx -t && sudo systemctl reload nginx
```

### 11.2 升级 app2

```bash
sudo tee /etc/nginx/travis-upstreams.conf >/dev/null <<'EOF'
server 127.0.0.1:18081 max_fails=2 fail_timeout=10s;
EOF
sudo nginx -t && sudo systemctl reload nginx
cd /opt/travis/deploy
sed -i "s/^APP2_IMAGE=.*/APP2_IMAGE=travis-backend:${NEW_VERSION}/" .env
docker compose --env-file .env -f compose.yml up -d --no-deps app2
for attempt in $(seq 1 60); do
  curl --fail --silent http://127.0.0.1:18082/actuator/health/readiness \
    >/dev/null && break
  sleep 2
done
curl --fail --silent http://127.0.0.1:18082/actuator/health/readiness
```

再恢复包含两个 server 的 upstream，执行 `nginx -t` 和 reload，最后检查：

```bash
docker compose --env-file .env -f compose.yml ps
docker compose --env-file .env -f compose.yml logs --since=10m app1 app2
curl --fail --silent https://admin.example.com/ >/dev/null
```

不要执行 `docker compose down`，否则会同时停止两个实例和基础设施。

### 11.3 回滚

若数据库变更保持向后兼容，逐个摘流，把 `APP1_IMAGE`、`APP2_IMAGE` 改回旧 tag，再按同样顺序重建。数据库不会随镜像自动回滚；若迁移删除列或改变数据语义，旧镜像可能已经不能运行，此时应发布 forward fix，或按已演练的数据库恢复方案处理。

## 12. 前端升级和回滚

把新版本构建到新的 release 后原子切换：

```bash
NEW_FRONTEND=/opt/travis/frontend/releases/NEW_VERSION
test -f "$NEW_FRONTEND/index.html"
ln -sfn "$NEW_FRONTEND" /opt/travis/frontend/current.new
mv -Tf /opt/travis/frontend/current.new /opt/travis/frontend/current
```

回滚时把 `current` 指回旧目录。前后端契约不兼容时，先发布兼容后端，再发布前端；删除旧接口放到后续版本。

## 13. 运维与 RocketMQ

日常检查：

```bash
df -h
free -h
docker system df
cd /opt/travis/deploy
docker compose --env-file .env -f compose.yml ps
docker compose --env-file .env -f compose.yml logs --since=30m app1 app2
curl --fail --silent http://127.0.0.1:18081/actuator/health/readiness
curl --fail --silent http://127.0.0.1:18082/actuator/health/readiness
sudo nginx -t
```

至少监控 HTTPS、两个 readiness、关键业务接口、CPU、内存、磁盘、MySQL 连接和慢查询、Redis 内存和 AOF/RDB、JVM/HTTP 5xx、Quartz 失败任务、备份和证书续期。

不要把 `docker system prune -a --volumes` 或 `docker compose down -v` 当成日常清理，它们可能删除回滚镜像或持久卷。

当前仓库存在 RocketMQ starter 和生产配置，但 `travis-server` 当前引入的 system、ops、app 模块没有依赖该 starter，也没有业务消费者，因此基础部署不启动 RocketMQ。业务真正引入 starter 后，需另行部署并持久化 NameServer/Broker/Proxy，配置 Broker 可达地址、ACL、磁盘水位、Topic/消费者组，并验证重复消费、重试和顺序消息。

## 14. 上线验收清单

- [ ] 公网只开放 22、80、443，数据库、Redis、实例端口无法公网访问。
- [ ] `.env` 权限为 600，代码和日志没有真实密钥。
- [ ] MySQL、Redis、上传和日志均映射到宿主机持久目录。
- [ ] Redis AOF/RDB 正常，MySQL 和上传备份已复制到异机。
- [ ] app1、app2 readiness 均为 `UP`，Quartz 使用不同的 AUTO instance ID。
- [ ] API、文件、WebSocket 代理正常，Actuator 未暴露公网。
- [ ] HTTPS 有效，Certbot dry-run 成功。
- [ ] 浏览器验证了登录、权限、写操作、文件和 WebSocket。
- [ ] 实际演练过双实例滚动升级、应用/前端回滚和隔离数据恢复。
- [ ] 团队清楚单机双实例不覆盖整机、MySQL 或 Redis 故障。
