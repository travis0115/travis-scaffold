#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

[[ "$#" -eq 2 ]] || die "用法: $0 <新镜像> <新发布标识>"
new_image="$1"
new_release="$2"
validate_image "${new_image}"
validate_release_id "${new_release}"
for command in docker curl nginx; do
    require_command "${command}"
done
load_env
validate_deploy_root
[[ "${new_release}" != "${ACTIVE_RELEASE}" ]] || die "新发布标识不能与当前发布相同"
frontend_release="${DEPLOY_ROOT}/frontend/releases/${new_release}"
[[ -f "${frontend_release}/index.html" ]] || die "新前端产物不存在: ${frontend_release}/index.html"
acquire_deploy_lock

docker pull "${new_image}"
BACKEND_IMAGE="${new_image}" app_compose "${new_release}" up -d \
    --scale "backend=${APP_REPLICAS}" --wait
wait_for_release "${new_release}" "${APP_REPLICAS}"
old_release="${ACTIVE_RELEASE}"
old_image="${BACKEND_IMAGE}"
activate_release "${new_release}"
if ! sudo ln -sfn "${frontend_release}" "${FRONTEND_ROOT}"; then
    activate_release "${old_release}"
    die "前端切换失败，已恢复旧后端 upstream"
fi

set_env_values \
    PREVIOUS_RELEASE "${old_release}" \
    PREVIOUS_BACKEND_IMAGE "${old_image}" \
    ACTIVE_RELEASE "${new_release}" \
    BACKEND_IMAGE "${new_image}"
echo "升级完成: ${old_release} -> ${new_release}；旧实例组已保留，可执行 rollback.sh"
