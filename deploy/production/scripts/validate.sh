#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

for command in docker curl envsubst nginx; do
    require_command "${command}"
done
docker compose version >/dev/null
load_env
validate_deploy_root
validate_release_id "${ACTIVE_RELEASE}"
validate_replicas "${APP_REPLICAS}"
validate_image "${BACKEND_IMAGE}"

required_variables=(
    DEPLOY_ROOT PUBLIC_DOMAIN TLS_CERTIFICATE TLS_CERTIFICATE_KEY FRONTEND_ROOT
    NGINX_SITE_FILE NGINX_UPSTREAM_FILE NGINX_RATE_LIMIT_FILE MYSQL_DATABASE
    MYSQL_USERNAME MYSQL_PASSWORD MYSQL_ROOT_PASSWORD REDIS_PASSWORD JWT_SECRET_KEY
)
for variable in "${required_variables[@]}"; do
    require_var "${variable}"
done
[[ "${#MYSQL_PASSWORD}" -ge 16 ]] || die "MYSQL_PASSWORD 至少需要 16 个字符"
[[ "${#MYSQL_ROOT_PASSWORD}" -ge 16 ]] || die "MYSQL_ROOT_PASSWORD 至少需要 16 个字符"
[[ "${MYSQL_PASSWORD}" != "${MYSQL_ROOT_PASSWORD}" ]] || die "MySQL 普通账号与 root 密码不能相同"
[[ "${#REDIS_PASSWORD}" -ge 16 ]] || die "REDIS_PASSWORD 至少需要 16 个字符"
[[ "${#JWT_SECRET_KEY}" -ge 32 ]] || die "JWT_SECRET_KEY 至少需要 32 个字符"
[[ "${ADMIN_LOGIN_RATE}" =~ ^[1-9][0-9]*$ && "${ADMIN_LOGIN_RATE}" -le 600 ]] \
    || die "ADMIN_LOGIN_RATE 必须在 1..600"
[[ "${APP_LOGIN_RATE}" =~ ^[1-9][0-9]*$ && "${APP_LOGIN_RATE}" -le 6000 ]] \
    || die "APP_LOGIN_RATE 必须在 1..6000"
for burst in "${ADMIN_LOGIN_BURST}" "${APP_LOGIN_BURST}"; do
    [[ "${burst}" =~ ^[1-9][0-9]*$ && "${burst}" -le 1000 ]] \
        || die "登录 burst 必须在 1..1000"
done

infra_compose config --quiet
app_compose "${ACTIVE_RELEASE}" config --quiet
echo "生产部署配置校验通过"
