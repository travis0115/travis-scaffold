#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command docker
require_command curl
require_command nginx
load_env
validate_deploy_root
release="${1:-${ACTIVE_RELEASE}}"
validate_release_id "${release}"
acquire_deploy_lock
activate_release "${release}"
echo "已将健康实例同步到 Nginx upstream: ${release}"
