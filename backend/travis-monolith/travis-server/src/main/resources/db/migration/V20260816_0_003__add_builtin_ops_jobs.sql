ALTER TABLE `ops_job`
  ADD COLUMN `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是' AFTER `status`;

INSERT INTO `ops_job` (
  `id`, `job_name`, `handler_name`, `schedule_type`, `cron_expression`,
  `interval_millis`, `execute_at`, `params`, `param_schema`, `priority`,
  `concurrent`, `misfire_policy`, `calendar_config`, `alert_user_ids`,
  `owner_user_id`, `log_retention_days`, `status`, `is_builtin`, `remark`,
  `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`, `lock_version`
)
SELECT
  2099000000000000001, 'Quartz任务配置对账', 'opsJobQuartzReconcile', 'INTERVAL', NULL,
  60000, NULL, '{}', NULL, 5,
  0, 3, NULL, NULL,
  NULL, 30, 1, 1, '系统内置：每分钟修复 ops_job 与 Quartz 状态差异，并收敛中断日志',
  CURRENT_TIMESTAMP, NULL, NULL, NULL, 0, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ops_job` WHERE `handler_name` = 'opsJobQuartzReconcile' AND `is_deleted` = 0
);

INSERT INTO `ops_job` (
  `id`, `job_name`, `handler_name`, `schedule_type`, `cron_expression`,
  `interval_millis`, `execute_at`, `params`, `param_schema`, `priority`,
  `concurrent`, `misfire_policy`, `calendar_config`, `alert_user_ids`,
  `owner_user_id`, `log_retention_days`, `status`, `is_builtin`, `remark`,
  `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`, `lock_version`
)
SELECT
  2099000000000000002, '消息定时推送对账', 'sysMessageScheduledPushReconcile', 'INTERVAL', NULL,
  300000, NULL, '{}', NULL, 5,
  0, 3, NULL, NULL,
  NULL, 30, 1, 1, '系统内置：每五分钟修复待推送消息与 Quartz 一次性任务的状态差异',
  CURRENT_TIMESTAMP, NULL, NULL, NULL, 0, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ops_job` WHERE `handler_name` = 'sysMessageScheduledPushReconcile' AND `is_deleted` = 0
);

INSERT INTO `ops_job` (
  `id`, `job_name`, `handler_name`, `schedule_type`, `cron_expression`,
  `interval_millis`, `execute_at`, `params`, `param_schema`, `priority`,
  `concurrent`, `misfire_policy`, `calendar_config`, `alert_user_ids`,
  `owner_user_id`, `log_retention_days`, `status`, `is_builtin`, `remark`,
  `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`, `lock_version`
)
SELECT
  2099000000000000003, '任务执行日志清理', 'opsJobLogCleanup', 'CRON', '0 0 19 * * ?',
  NULL, NULL, '{}', NULL, 5,
  0, 3, NULL, NULL,
  NULL, 30, 1, 1, '系统内置：北京时间每日 03:00（UTC 前一日 19:00）清理过期任务执行日志',
  CURRENT_TIMESTAMP, NULL, NULL, NULL, 0, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ops_job` WHERE `handler_name` = 'opsJobLogCleanup' AND `is_deleted` = 0
);
