#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

[[ "$#" -eq 1 ]] || die "用法: $0 <副本数>"
target="$1"
validate_replicas "${target}"
for command in docker curl nginx; do
    require_command "${command}"
done
load_env
validate_deploy_root
acquire_deploy_lock

mapfile -t current_ids < <(release_container_ids "${ACTIVE_RELEASE}")
current="${#current_ids[@]}"
if (( target > current )); then
    app_compose "${ACTIVE_RELEASE}" up -d --no-deps --scale "backend=${target}" backend
    wait_for_release "${ACTIVE_RELEASE}" "${target}"
    activate_release "${ACTIVE_RELEASE}"
elif (( target < current )); then
    mapfile -t ordered_rows < <(
        for id in "${current_ids[@]}"; do
            printf '%s %s\n' "$(docker inspect --format '{{.Name}}' "${id}")" "${id}"
        done | sort -V
    )
    keep_ids=()
    remove_ids=()
    for index in "${!ordered_rows[@]}"; do
        id="${ordered_rows[${index}]##* }"
        if (( index < target )); then
            keep_ids+=("${id}")
        else
            remove_ids+=("${id}")
        fi
    done
    activate_release "${ACTIVE_RELEASE}" "${keep_ids[@]}"
    sleep "${DRAIN_SECONDS:-15}"
    docker stop --time 30 "${remove_ids[@]}" >/dev/null
    docker rm "${remove_ids[@]}" >/dev/null
    app_compose "${ACTIVE_RELEASE}" up -d --no-deps --no-recreate \
        --scale "backend=${target}" backend
else
    activate_release "${ACTIVE_RELEASE}"
fi
set_env_value APP_REPLICAS "${target}"
echo "扩缩容完成: release=${ACTIVE_RELEASE}, replicas=${target}"
