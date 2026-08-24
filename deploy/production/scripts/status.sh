#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command docker
require_command curl
load_env
echo "基础设施:"
infra_compose ps
echo
echo "业务实例: release=${ACTIVE_RELEASE}"
app_compose "${ACTIVE_RELEASE}" ps backend

failed=0
count=0
while IFS= read -r id; do
    [[ -n "${id}" ]] || continue
    count=$((count + 1))
    port="$(container_port "${id}")"
    state="$(docker inspect --format '{{.State.Status}}/{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' "${id}")"
    if curl --fail --silent "http://127.0.0.1:${port}/actuator/health/readiness" >/dev/null; then
        readiness=UP
    else
        readiness=DOWN
        failed=1
    fi
    printf '%s port=%s state=%s readiness=%s\n' "${id:0:12}" "${port}" "${state}" "${readiness}"
done < <(release_container_ids "${ACTIVE_RELEASE}")
if (( count == 0 )); then
    echo "当前发布没有运行中的业务实例" >&2
    exit 1
fi
exit "${failed}"
