#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

[[ "$#" -eq 1 ]] || die "用法: $0 <备份目录>"
backup_dir="$1"
[[ -d "${backup_dir}" ]] || die "备份目录不存在: ${backup_dir}"
for command in gzip tar sha256sum; do
    require_command "${command}"
done
for file in SHA256SUMS mysql.sql.gz redis.rdb.gz uploads.tgz; do
    [[ -f "${backup_dir}/${file}" ]] || die "备份文件不完整: ${file}"
done

(
    cd "${backup_dir}"
    sha256sum --check SHA256SUMS
    gzip -t mysql.sql.gz
    gzip -t redis.rdb.gz
    tar -tzf uploads.tgz >/dev/null
)
echo "备份校验通过: ${backup_dir}"
echo "该检查不代替将 MySQL、Redis 和上传文件恢复到隔离环境的完整演练。"
