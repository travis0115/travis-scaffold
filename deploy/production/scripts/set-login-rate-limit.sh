#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

[[ "$#" -eq 3 ]] || die "用法: $0 <admin|app> <每分钟请求数> <burst>"
client="$1"
rate="$2"
burst="$3"
[[ "${rate}" =~ ^[1-9][0-9]*$ ]] || die "请求数必须是正整数"
[[ "${burst}" =~ ^[1-9][0-9]*$ && "${burst}" -le 1000 ]] || die "burst 必须在 1..1000"
case "${client}" in
    admin)
        (( rate <= 600 )) || die "admin 每分钟请求数必须在 1..600"
        rate_key=ADMIN_LOGIN_RATE
        burst_key=ADMIN_LOGIN_BURST
        ;;
    app)
        (( rate <= 6000 )) || die "app 每分钟请求数必须在 1..6000"
        rate_key=APP_LOGIN_RATE
        burst_key=APP_LOGIN_BURST
        ;;
    *) die "客户端类型只能是 admin 或 app" ;;
esac
require_command envsubst
require_command nginx
load_env
validate_deploy_root
acquire_deploy_lock

env_backup="$(mktemp "${TMPDIR:-/tmp}/travis-env-backup.XXXXXX")"
rate_backup="$(mktemp "${TMPDIR:-/tmp}/travis-rate-backup.XXXXXX")"
cp "${ENV_FILE}" "${env_backup}"
sudo cp "${NGINX_RATE_LIMIT_FILE}" "${rate_backup}"
set_env_values "${rate_key}" "${rate}" "${burst_key}" "${burst}"
install_rendered_template "${PRODUCTION_DIR}/nginx/login-rate-limit.conf.template" \
    "${NGINX_RATE_LIMIT_FILE}"
if ! sudo nginx -t; then
    install -m 0600 "${env_backup}" "${ENV_FILE}"
    install_atomically "${rate_backup}" "${NGINX_RATE_LIMIT_FILE}"
    rm -f "${env_backup}" "${rate_backup}"
    die "限流配置校验失败，已恢复原配置"
fi
if ! sudo nginx -s reload; then
    install -m 0600 "${env_backup}" "${ENV_FILE}"
    install_atomically "${rate_backup}" "${NGINX_RATE_LIMIT_FILE}"
    sudo nginx -t && sudo nginx -s reload || true
    rm -f "${env_backup}" "${rate_backup}"
    die "Nginx reload 失败，已恢复原限流配置"
fi
rm -f "${env_backup}" "${rate_backup}"
echo "登录 IP 限流已更新: client=${client}, rate=${rate}r/m, burst=${burst}"
