#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

for command in docker curl nginx; do
    require_command "${command}"
done
load_env
validate_deploy_root
[[ -n "${PREVIOUS_RELEASE:-}" ]] || die "没有可回滚的上一发布"
[[ -n "${PREVIOUS_BACKEND_IMAGE:-}" ]] || die "缺少上一发布镜像记录"
validate_release_id "${PREVIOUS_RELEASE}"
validate_image "${PREVIOUS_BACKEND_IMAGE}"
previous_frontend="${DEPLOY_ROOT}/frontend/releases/${PREVIOUS_RELEASE}"
[[ -f "${previous_frontend}/index.html" ]] \
    || die "上一发布前端产物不存在: ${previous_frontend}/index.html"
acquire_deploy_lock

mapfile -t previous_ids < <(release_container_ids "${PREVIOUS_RELEASE}")
[[ "${#previous_ids[@]}" -gt 0 ]] || die "上一发布实例组不存在: ${PREVIOUS_RELEASE}"
for id in "${previous_ids[@]}"; do
    wait_for_container "${id}" || die "上一发布实例未通过 readiness: ${id}"
done
current_ids=()
mapfile -t current_ids < <(release_container_ids "${ACTIVE_RELEASE}")
activate_release "${PREVIOUS_RELEASE}" "${previous_ids[@]}"
if ! sudo ln -sfn "${previous_frontend}" "${FRONTEND_ROOT}"; then
    if [[ "${#current_ids[@]}" -gt 0 ]]; then
        activate_release "${ACTIVE_RELEASE}" "${current_ids[@]}"
    fi
    die "前端回滚失败，已恢复原后端 upstream"
fi

current_release="${ACTIVE_RELEASE}"
current_image="${BACKEND_IMAGE}"
target_release="${PREVIOUS_RELEASE}"
target_image="${PREVIOUS_BACKEND_IMAGE}"
set_env_values \
    ACTIVE_RELEASE "${target_release}" \
    BACKEND_IMAGE "${target_image}" \
    PREVIOUS_RELEASE "${current_release}" \
    PREVIOUS_BACKEND_IMAGE "${current_image}" \
    APP_REPLICAS "${#previous_ids[@]}"
echo "回滚完成: ${current_release} -> ${target_release}"
