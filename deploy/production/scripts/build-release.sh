#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

[[ "$#" -eq 2 ]] || die "用法: $0 <版本号> <后端镜像>"
version="$1"
backend_image="$2"
validate_release_id "${version}"
validate_image "${backend_image}"
require_command docker
load_env
validate_deploy_root
acquire_deploy_lock

frontend_image="travis-frontend-artifact:${version}"
docker build -f "${REPO_ROOT}/deploy/backend.Dockerfile" -t "${backend_image}" "${REPO_ROOT}"
docker build -f "${REPO_ROOT}/deploy/frontend.Dockerfile" -t "${frontend_image}" "${REPO_ROOT}"

temporary="$(mktemp -d "${TMPDIR:-/tmp}/travis-frontend.XXXXXX")"
container_id="$(docker create "${frontend_image}")"
cleanup() {
    docker rm -f "${container_id}" >/dev/null 2>&1 || true
    rm -rf "${temporary}"
}
trap cleanup EXIT
docker cp "${container_id}:/dist/." "${temporary}/"
release_dir="${DEPLOY_ROOT}/frontend/releases/${version}"
[[ ! -e "${release_dir}" ]] || die "前端发布目录已存在: ${release_dir}"
sudo install -d -m 0755 "${release_dir}"
sudo cp -R "${temporary}/." "${release_dir}/"
if [[ ! -e "${FRONTEND_ROOT}" && ! -L "${FRONTEND_ROOT}" ]]; then
    sudo install -d -m 0755 "$(dirname -- "${FRONTEND_ROOT}")"
    sudo ln -s "${release_dir}" "${FRONTEND_ROOT}"
fi
echo "构建完成: backend=${backend_image}, frontend=${release_dir}"
