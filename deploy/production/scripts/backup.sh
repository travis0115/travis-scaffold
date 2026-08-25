#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

for command in docker gzip tar sha256sum; do
    require_command "${command}"
done
load_env
validate_deploy_root
acquire_deploy_lock

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
backup_dir="${DEPLOY_ROOT}/backups/${timestamp}"
install -d -m 0750 "${backup_dir}"

# 变量在容器内由 sh 展开。
# shellcheck disable=SC2016
infra_compose exec -T mysql sh -ceu \
    'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction --routines --events "$MYSQL_DATABASE"' \
    | gzip -9 >"${backup_dir}/mysql.sql.gz"
# 通过 Redis 复制协议导出独立 RDB，避免在持续写入时打包整个 AOF 目录。
redis_snapshot="/tmp/travis-backup-${timestamp}.rdb"
# 变量在容器内由 sh 展开。
# shellcheck disable=SC2016
infra_compose exec -T redis sh -ceu \
    'rm -f "$1"; REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli --rdb "$1" >/dev/null' \
    sh "${redis_snapshot}"
infra_compose cp "redis:${redis_snapshot}" "${backup_dir}/redis.rdb"
infra_compose exec -T redis rm -f "${redis_snapshot}"
gzip -9 "${backup_dir}/redis.rdb"
sudo tar -C "${DEPLOY_ROOT}/data" -czf "${backup_dir}/uploads.tgz" uploads
sudo chown "$(id -u):$(id -g)" "${backup_dir}/uploads.tgz"
(
    cd "${backup_dir}"
    sha256sum mysql.sql.gz redis.rdb.gz uploads.tgz >SHA256SUMS
)
echo "备份完成: ${backup_dir}"
echo "脚本不会自动删除旧备份；请在完成异机复制与恢复演练后按保留策略清理。"
