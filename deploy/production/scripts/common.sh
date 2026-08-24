#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
PRODUCTION_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd -P)"
REPO_ROOT="$(cd -- "${PRODUCTION_DIR}/../.." && pwd -P)"
ENV_FILE="${ENV_FILE:-${PRODUCTION_DIR}/.env}"
INFRA_COMPOSE_FILE="${PRODUCTION_DIR}/compose.infra.yml"
APP_COMPOSE_FILE="${PRODUCTION_DIR}/compose.app.yml"

die() {
    echo "错误: $*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || die "缺少命令: $1"
}

load_env() {
    [[ -f "${ENV_FILE}" ]] || die "环境文件不存在: ${ENV_FILE}"
    local mode
    mode="$(stat -c '%a' "${ENV_FILE}" 2>/dev/null || stat -f '%Lp' "${ENV_FILE}")"
    (( 10#${mode} <= 600 )) || die "环境文件权限必须不宽于 600，当前为 ${mode}"
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
}

require_var() {
    local name="$1"
    local value="${!name:-}"
    [[ -n "${value}" ]] || die "缺少必需变量: ${name}"
    [[ "${value}" != *replace-me* && "${value}" != replace-with-* ]] \
        || die "变量仍为占位值: ${name}"
}

validate_deploy_root() {
    [[ "${DEPLOY_ROOT:-}" = /* ]] || die "DEPLOY_ROOT 必须是绝对路径"
    local normalized
    normalized="$(cd -- "$(dirname -- "${DEPLOY_ROOT}")" 2>/dev/null && pwd -P)/$(basename -- "${DEPLOY_ROOT}")" \
        || die "无法解析 DEPLOY_ROOT"
    [[ "${normalized}" != / ]] || die "DEPLOY_ROOT 不能是根目录"
    [[ "${normalized}" != "${HOME}" ]] || die "DEPLOY_ROOT 不能是用户家目录"
    [[ "${normalized}" != "${REPO_ROOT}" ]] || die "DEPLOY_ROOT 不能是仓库根目录"
}

validate_release_id() {
    [[ "$1" =~ ^[a-zA-Z0-9][a-zA-Z0-9._-]{0,63}$ ]] || die "发布标识格式错误: $1"
}

validate_replicas() {
    [[ "$1" =~ ^[1-9][0-9]*$ ]] || die "副本数必须是正整数"
}

validate_image() {
    local image="$1"
    [[ -n "${image}" ]] || die "业务镜像不能为空"
    [[ "${image}" =~ ^[a-zA-Z0-9][a-zA-Z0-9._/@:-]+$ ]] \
        || die "业务镜像包含不安全字符: ${image}"
    [[ "${image}" != *:latest ]] || die "业务镜像禁止使用 latest"
    [[ "${image}" == *@sha256:* || "${image##*/}" == *:* ]] \
        || die "业务镜像必须包含不可变 tag 或 digest: ${image}"
}

acquire_deploy_lock() {
    require_command flock
    mkdir -p "${DEPLOY_ROOT}"
    exec 9>"${DEPLOY_ROOT}/deploy.lock"
    flock -n 9 || die "已有部署操作正在执行"
}

infra_compose() {
    docker compose --env-file "${ENV_FILE}" -f "${INFRA_COMPOSE_FILE}" -p travis-infra "$@"
}

app_project() {
    local release="$1"
    validate_release_id "${release}"
    printf 'travis-app-%s' "${release,,}"
}

app_compose() {
    local release="$1"
    shift
    docker compose --env-file "${ENV_FILE}" -f "${APP_COMPOSE_FILE}" \
        -p "$(app_project "${release}")" "$@"
}

container_port() {
    local container_id="$1"
    docker port "${container_id}" 8080/tcp \
        | awk -F: '$1 == "127.0.0.1" {print $NF; exit}'
}

release_container_ids() {
    local release="$1"
    app_compose "${release}" ps -q backend
}

wait_for_container() {
    local container_id="$1"
    local timeout="${HEALTH_TIMEOUT_SECONDS:-120}"
    local deadline=$((SECONDS + timeout))
    local port
    port="$(container_port "${container_id}")"
    [[ -n "${port}" ]] || return 1
    until curl --fail --silent --show-error \
        "http://127.0.0.1:${port}/actuator/health/readiness" >/dev/null; do
        (( SECONDS < deadline )) || return 1
        sleep 2
    done
}

wait_for_release() {
    local release="$1"
    local expected="$2"
    local ids
    mapfile -t ids < <(release_container_ids "${release}")
    [[ "${#ids[@]}" -eq "${expected}" ]] \
        || die "发布 ${release} 实际副本数 ${#ids[@]}，预期 ${expected}"
    local id
    for id in "${ids[@]}"; do
        wait_for_container "${id}" || die "实例未通过 readiness: ${id}"
    done
}

render_template() {
    local source="$1"
    local destination="$2"
    local temporary
    temporary="$(mktemp "${TMPDIR:-/tmp}/travis-render.XXXXXX")"
    local substitutions
    # 这是传给 envsubst 的变量白名单，必须保留字面量美元符号。
    # shellcheck disable=SC2016
    substitutions='${PUBLIC_DOMAIN} ${TLS_CERTIFICATE} ${TLS_CERTIFICATE_KEY} ${FRONTEND_ROOT} ${NGINX_UPSTREAM_FILE} ${ADMIN_LOGIN_RATE} ${ADMIN_LOGIN_BURST} ${APP_LOGIN_RATE} ${APP_LOGIN_BURST}'
    envsubst "${substitutions}" <"${source}" >"${temporary}"
    chmod 644 "${temporary}"
    printf '%s' "${temporary}"
}

build_upstream_file() {
    local release="$1"
    local destination="$2"
    shift 2
    local ids=("$@")
    if [[ "${#ids[@]}" -eq 0 ]]; then
        mapfile -t ids < <(release_container_ids "${release}")
    fi
    [[ "${#ids[@]}" -gt 0 ]] || die "发布 ${release} 没有实例"
    local temporary
    temporary="$(mktemp "${TMPDIR:-/tmp}/travis-upstream.XXXXXX")"
    local healthy=0
    local id port
    for id in "${ids[@]}"; do
        if wait_for_container "${id}"; then
            port="$(container_port "${id}")"
            printf 'server 127.0.0.1:%s max_fails=2 fail_timeout=10s;\n' "${port}" \
                >>"${temporary}"
            healthy=$((healthy + 1))
        fi
    done
    [[ "${healthy}" -eq "${#ids[@]}" ]] || {
        rm -f "${temporary}"
        die "仅 ${healthy}/${#ids[@]} 个实例通过 readiness，拒绝更新 upstream"
    }
    install_atomically "${temporary}" "${destination}"
    rm -f "${temporary}"
}

install_atomically() {
    local source="$1"
    local destination="$2"
    local staged
    staged="$(sudo mktemp "${destination}.tmp.XXXXXX")"
    if ! sudo install -m 0644 "${source}" "${staged}"; then
        sudo rm -f "${staged}"
        return 1
    fi
    sudo mv -f "${staged}" "${destination}"
}

nginx_test_and_reload() {
    sudo nginx -t
    sudo nginx -s reload
}

install_rendered_template() {
    local source="$1"
    local destination="$2"
    local rendered
    rendered="$(render_template "${source}" "${destination}")"
    install_atomically "${rendered}" "${destination}"
    rm -f "${rendered}"
}

activate_release() {
    local release="$1"
    shift
    local ids=("$@")
    local backup=""
    if sudo test -f "${NGINX_UPSTREAM_FILE}"; then
        backup="$(mktemp "${TMPDIR:-/tmp}/travis-upstream-backup.XXXXXX")"
        sudo cp "${NGINX_UPSTREAM_FILE}" "${backup}"
    fi
    build_upstream_file "${release}" "${NGINX_UPSTREAM_FILE}" "${ids[@]}"
    if ! sudo nginx -t; then
        if [[ -n "${backup}" ]]; then
            install_atomically "${backup}" "${NGINX_UPSTREAM_FILE}"
        else
            sudo rm -f "${NGINX_UPSTREAM_FILE}"
        fi
        rm -f "${backup}"
        die "Nginx 配置校验失败，已恢复原 upstream"
    fi
    if ! sudo nginx -s reload; then
        if [[ -n "${backup}" ]]; then
            install_atomically "${backup}" "${NGINX_UPSTREAM_FILE}"
        else
            sudo rm -f "${NGINX_UPSTREAM_FILE}"
        fi
        sudo nginx -t && sudo nginx -s reload || true
        rm -f "${backup}"
        die "Nginx reload 失败，已恢复原 upstream"
    fi
    rm -f "${backup}"
}

set_env_values() {
    (( $# > 0 && $# % 2 == 0 )) || die "环境变量更新参数必须为 key/value 对"
    local temporary
    temporary="$(mktemp "${ENV_FILE}.tmp.XXXXXX")"
    cp "${ENV_FILE}" "${temporary}"
    while (( $# > 0 )); do
        local key="$1"
        local value="$2"
        shift 2
        [[ "${key}" =~ ^[A-Z][A-Z0-9_]*$ ]] || die "环境变量名格式错误: ${key}"
        local next
        next="$(mktemp "${ENV_FILE}.tmp.XXXXXX")"
        awk -v key="${key}" -v value="${value}" '
            BEGIN { updated = 0 }
            $0 ~ "^" key "=" { print key "=" value; updated = 1; next }
            { print }
            END { if (!updated) print key "=" value }
        ' "${temporary}" >"${next}"
        mv "${next}" "${temporary}"
        export "${key}=${value}"
    done
    chmod 600 "${temporary}"
    local backup
    backup="$(mktemp "${ENV_FILE}.bak.$(date -u +%Y%m%dT%H%M%SZ).XXXXXX")"
    cp -p "${ENV_FILE}" "${backup}"
    mv "${temporary}" "${ENV_FILE}"
}

set_env_value() {
    set_env_values "$1" "$2"
}
