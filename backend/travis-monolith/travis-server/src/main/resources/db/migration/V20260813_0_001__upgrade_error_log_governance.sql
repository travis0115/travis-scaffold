-- 扩大持久化事件载荷，支持保存完整异常堆栈。
ALTER TABLE `EVENT_PUBLICATION`
  MODIFY COLUMN `SERIALIZED_EVENT` mediumtext NOT NULL;

-- 将错误流水升级为可聚合、可处理的错误日志。
ALTER TABLE `sys_error_log`
  ADD COLUMN `fingerprint` char(64) DEFAULT NULL COMMENT '异常聚合指纹' AFTER `id`,
  ADD COLUMN `module_name` varchar(100) DEFAULT NULL COMMENT '模块名称' AFTER `fingerprint`,
  ADD COLUMN `platform_type` varchar(16) NOT NULL DEFAULT 'SYSTEM' COMMENT '平台类型' AFTER `module_name`,
  ADD COLUMN `username` varchar(100) DEFAULT NULL COMMENT '登录用户名快照' AFTER `user_id`,
  ADD COLUMN `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0-待处理，1-已解决，2-已忽略' AFTER `ip`,
  ADD COLUMN `occurrence_count` bigint NOT NULL DEFAULT '1' COMMENT '发生次数' AFTER `status`,
  ADD COLUMN `first_occurrence_time` datetime NULL COMMENT '首次发生时间' AFTER `occurrence_count`,
  ADD COLUMN `last_occurrence_time` datetime NULL COMMENT '最后发生时间' AFTER `first_occurrence_time`,
  ADD COLUMN `handled_by` bigint DEFAULT NULL COMMENT '处理人ID' AFTER `last_occurrence_time`,
  ADD COLUMN `handled_time` datetime DEFAULT NULL COMMENT '处理时间' AFTER `handled_by`,
  ADD COLUMN `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注' AFTER `handled_time`,
  ADD COLUMN `application_name` varchar(100) DEFAULT NULL COMMENT '应用名称' AFTER `handle_remark`,
  ADD COLUMN `application_version` varchar(64) DEFAULT NULL COMMENT '应用版本' AFTER `application_name`,
  ADD COLUMN `instance_name` varchar(100) DEFAULT NULL COMMENT '实例名称' AFTER `application_version`;

UPDATE `sys_error_log`
SET `platform_type` = CASE
    WHEN `request_url` LIKE '/api/admin/%' THEN 'ADMIN'
    WHEN `request_url` LIKE '/api/app/%' THEN 'APP'
    ELSE 'SYSTEM'
  END,
  `module_name` = CASE
    WHEN COALESCE(`source_name`, `controller_method`) LIKE 'com.travis.monolith.%'
      THEN SUBSTRING_INDEX(SUBSTRING(COALESCE(`source_name`, `controller_method`), LENGTH('com.travis.monolith.') + 1), '.', 2)
    ELSE LOWER(`source_type`)
  END,
  `first_occurrence_time` = `create_time`,
  `last_occurrence_time` = `create_time`,
  `fingerprint` = SHA2(CONCAT_WS('|', `source_type`, COALESCE(`source_name`, `controller_method`, ''), `exception_class`, `id`), 256);

UPDATE `sys_error_log` error_log
LEFT JOIN `sys_user` admin_user
  ON error_log.`platform_type` = 'ADMIN' AND admin_user.`id` = error_log.`user_id`
LEFT JOIN `app_user` app_user
  ON error_log.`platform_type` = 'APP' AND app_user.`id` = error_log.`user_id`
SET error_log.`username` = CASE
  WHEN error_log.`platform_type` = 'ADMIN' THEN admin_user.`username`
  WHEN error_log.`platform_type` = 'APP' THEN app_user.`username`
  ELSE NULL
END;

ALTER TABLE `sys_error_log`
  MODIFY COLUMN `fingerprint` char(64) NOT NULL COMMENT '异常聚合指纹',
  MODIFY COLUMN `first_occurrence_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次发生时间',
  MODIFY COLUMN `last_occurrence_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后发生时间',
  ADD UNIQUE KEY `uk_error_fingerprint` (`fingerprint`),
  ADD KEY `idx_error_last_occurrence_time` (`last_occurrence_time`),
  ADD KEY `idx_error_status` (`status`);

CREATE TABLE `sys_error_log_occurrence` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `error_log_id` bigint NOT NULL COMMENT '错误日志聚合ID',
  `user_id` bigint DEFAULT NULL COMMENT '登录用户ID',
  `username` varchar(100) DEFAULT NULL COMMENT '登录用户名快照',
  `request_id` varchar(100) DEFAULT NULL COMMENT '请求ID',
  `trace_id` varchar(100) DEFAULT NULL COMMENT '链路追踪ID',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(16) DEFAULT NULL COMMENT 'HTTP方法',
  `controller_method` varchar(1000) DEFAULT NULL COMMENT '控制器方法',
  `request_params` text COMMENT '脱敏后的请求参数',
  `message` text COMMENT '异常消息',
  `stack_trace` mediumtext COMMENT '异常堆栈',
  `ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `application_name` varchar(100) DEFAULT NULL COMMENT '应用名称',
  `application_version` varchar(64) DEFAULT NULL COMMENT '应用版本',
  `instance_name` varchar(100) DEFAULT NULL COMMENT '实例名称',
  `occurred_time` datetime NOT NULL COMMENT '发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_error_occurrence_log_time` (`error_log_id`,`occurred_time`),
  CONSTRAINT `fk_error_occurrence_log` FOREIGN KEY (`error_log_id`) REFERENCES `sys_error_log` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错误日志发生明细表';

INSERT INTO `sys_error_log_occurrence` (
  `id`, `error_log_id`, `user_id`, `username`, `request_id`, `trace_id`, `request_url`,
  `request_method`, `controller_method`, `request_params`, `message`, `stack_trace`, `ip`,
  `application_name`, `application_version`, `instance_name`, `occurred_time`
)
SELECT
  `id`, `id`, `user_id`, `username`, `request_id`, `trace_id`, `request_url`,
  `request_method`, `controller_method`, `request_params`, `message`, `stack_trace`, `ip`,
  `application_name`, `application_version`, `instance_name`, `create_time`
FROM `sys_error_log`;

-- 增加处理与删除按钮权限，并默认授予超级管理员角色。
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`)
VALUES
  (1930000000001021, 1930000000001020, '处理', 2, '', '', 'ops:error-log:handle', '', 1, 1, '2026-08-13 00:00:00', 1, NULL, NULL, '{}', 0),
  (1930000000001022, 1930000000001020, '删除', 2, '', '', 'ops:error-log:delete', '', 2, 1, '2026-08-13 00:00:00', 1, NULL, NULL, '{}', 0);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
VALUES
  (2087450000000001021, 1, 1930000000001021, '2026-08-13 00:00:00', 1),
  (2087450000000001022, 1, 1930000000001022, '2026-08-13 00:00:00', 1);
