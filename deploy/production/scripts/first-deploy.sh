#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

"${SCRIPT_DIR}/validate.sh"
load_env
acquire_deploy_lock
[[ -f "${TLS_CERTIFICATE}" ]] || die "TLS 证书不存在: ${TLS_CERTIFICATE}"
[[ -f "${TLS_CERTIFICATE_KEY}" ]] || die "TLS 私钥不存在: ${TLS_CERTIFICATE_KEY}"
[[ -f "${FRONTEND_ROOT}/index.html" ]] || die "前端产物不存在: ${FRONTEND_ROOT}/index.html"

sudo install -d -m 0750 -o "$(id -u)" -g "$(id -g)" \
    "${DEPLOY_ROOT}/data/mysql" "${DEPLOY_ROOT}/data/redis" \
    "${DEPLOY_ROOT}/data/uploads" "${DEPLOY_ROOT}/logs" "${DEPLOY_ROOT}/backups"
sudo chown -R 10001:10001 "${DEPLOY_ROOT}/data/uploads" "${DEPLOY_ROOT}/logs"
docker network inspect travis-internal >/dev/null 2>&1 || docker network create travis-internal >/dev/null

infra_compose up -d --wait
app_compose "${ACTIVE_RELEASE}" up -d --scale "backend=${APP_REPLICAS}" --wait
wait_for_release "${ACTIVE_RELEASE}" "${APP_REPLICAS}"

install_rendered_template "${PRODUCTION_DIR}/nginx/login-rate-limit.conf.template" \
    "${NGINX_RATE_LIMIT_FILE}"
build_upstream_file "${ACTIVE_RELEASE}" "${NGINX_UPSTREAM_FILE}"
install_rendered_template "${PRODUCTION_DIR}/nginx/travis.conf.template" "${NGINX_SITE_FILE}"
nginx_test_and_reload
echo "首次部署完成: release=${ACTIVE_RELEASE}, replicas=${APP_REPLICAS}"
