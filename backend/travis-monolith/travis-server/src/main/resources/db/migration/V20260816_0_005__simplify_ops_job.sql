ALTER TABLE `ops_job`
  DROP INDEX `idx_ops_job_owner_user_id`,
  DROP COLUMN `param_schema`,
  DROP COLUMN `priority`,
  DROP COLUMN `calendar_config`,
  DROP COLUMN `owner_user_id`,
  DROP COLUMN `log_retention_days`;
