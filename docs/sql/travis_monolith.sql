/*
 Navicat Premium Dump SQL

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 90600 (9.6.0)
 Source Host           : localhost:3306
 Source Schema         : travis_monolith

 Target Server Type    : MySQL
 Target Server Version : 90600 (9.6.0)
 File Encoding         : 65001

 Date: 04/07/2026 02:26:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar_file_id` bigint DEFAULT NULL COMMENT '头像文件ID',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `last_online_time` datetime DEFAULT NULL COMMENT '最近上线时间',
  `last_online_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近上线IP',
  `last_offline_time` datetime DEFAULT NULL COMMENT '最近下线时间',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`),
  UNIQUE KEY `uk_app_user_mobile` (`mobile`),
  KEY `idx_app_user_status` (`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端用户';

-- ----------------------------
-- Records of app_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for EVENT_PUBLICATION
-- ----------------------------
DROP TABLE IF EXISTS `EVENT_PUBLICATION`;
CREATE TABLE `EVENT_PUBLICATION` (
  `ID` varchar(36) NOT NULL,
  `LISTENER_ID` varchar(512) NOT NULL,
  `EVENT_TYPE` varchar(512) NOT NULL,
  `SERIALIZED_EVENT` varchar(4000) NOT NULL,
  `PUBLICATION_DATE` timestamp(6) NOT NULL,
  `COMPLETION_DATE` timestamp(6) NULL DEFAULT NULL,
  `STATUS` varchar(20) DEFAULT NULL,
  `COMPLETION_ATTEMPTS` int DEFAULT NULL,
  `LAST_RESUBMISSION_DATE` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `EVENT_PUBLICATION_BY_COMPLETION_DATE_IDX` (`COMPLETION_DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring Modulith 事件发布表';

-- ----------------------------
-- Records of EVENT_PUBLICATION
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ops_job
-- ----------------------------
DROP TABLE IF EXISTS `ops_job`;
CREATE TABLE `ops_job` (
  `id` bigint NOT NULL,
  `job_name` varchar(120) NOT NULL,
  `handler_name` varchar(120) NOT NULL,
  `schedule_type` varchar(20) NOT NULL,
  `cron_expression` varchar(120) DEFAULT NULL,
  `interval_millis` bigint DEFAULT NULL,
  `execute_at` datetime DEFAULT NULL,
  `params` longtext,
  `param_schema` longtext,
  `priority` int NOT NULL DEFAULT '5',
  `concurrent` tinyint NOT NULL DEFAULT '0',
  `misfire_policy` tinyint NOT NULL DEFAULT '0',
  `calendar_config` longtext,
  `alert_user_ids` varchar(1000) DEFAULT NULL,
  `owner_user_id` bigint DEFAULT NULL,
  `log_retention_days` int NOT NULL DEFAULT '30',
  `status` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ops_job_handler_name` (`handler_name`),
  KEY `idx_ops_job_status` (`status`),
  KEY `idx_ops_job_owner_user_id` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调度任务表';

-- ----------------------------
-- Records of ops_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ops_job_log
-- ----------------------------
DROP TABLE IF EXISTS `ops_job_log`;
CREATE TABLE `ops_job_log` (
  `id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `job_name` varchar(120) NOT NULL,
  `handler_name` varchar(120) NOT NULL,
  `fire_instance_id` varchar(190) DEFAULT NULL,
  `scheduler_instance_id` varchar(190) DEFAULT NULL,
  `params_snapshot` longtext,
  `scheduled_fire_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `duration_millis` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `result_message` varchar(1000) DEFAULT NULL,
  `exception_class` varchar(500) DEFAULT NULL,
  `exception_message` varchar(2000) DEFAULT NULL,
  `stack_trace` longtext,
  `alert_status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ops_job_log_job_id` (`job_id`),
  KEY `idx_ops_job_log_status` (`status`),
  KEY `idx_ops_job_log_create_time` (`create_time`),
  KEY `idx_ops_job_log_job_create` (`job_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调度任务执行日志表';

-- ----------------------------
-- Records of ops_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `blob_data` blob,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `fk_qrtz_blob_triggers` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz二进制触发器表';

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) NOT NULL,
  `calendar_name` varchar(190) NOT NULL,
  `calendar` blob NOT NULL,
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz日历配置表';

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `cron_expression` varchar(120) NOT NULL,
  `time_zone_id` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `fk_qrtz_cron_triggers` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz Cron触发器表';

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------
BEGIN;
INSERT INTO `qrtz_cron_triggers` (`sched_name`, `trigger_name`, `trigger_group`, `cron_expression`, `time_zone_id`) VALUES ('travisScheduler', 'log-cleanup-trigger', 'ops-internal', '0 0 3 * * ?', 'UTC');
INSERT INTO `qrtz_cron_triggers` (`sched_name`, `trigger_name`, `trigger_group`, `cron_expression`, `time_zone_id`) VALUES ('travisScheduler', 'scheduled-message-push-trigger', 'system-message', '0 * * * * ?', 'UTC');
COMMIT;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `entry_id` varchar(95) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `instance_name` varchar(190) NOT NULL,
  `fired_time` bigint NOT NULL,
  `sched_time` bigint NOT NULL,
  `priority` int NOT NULL,
  `state` varchar(16) NOT NULL,
  `job_name` varchar(190) DEFAULT NULL,
  `job_group` varchar(190) DEFAULT NULL,
  `is_nonconcurrent` varchar(1) DEFAULT NULL,
  `requests_recovery` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`entry_id`),
  KEY `idx_qrtz_ft_trig_inst_name` (`sched_name`,`instance_name`),
  KEY `idx_qrtz_ft_inst_job_req_rcvry` (`sched_name`,`instance_name`,`requests_recovery`),
  KEY `idx_qrtz_ft_j_g` (`sched_name`,`job_name`,`job_group`),
  KEY `idx_qrtz_ft_jg` (`sched_name`,`job_group`),
  KEY `idx_qrtz_ft_t_g` (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `idx_qrtz_ft_tg` (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz已触发任务实例表';

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) NOT NULL,
  `job_name` varchar(190) NOT NULL,
  `job_group` varchar(190) NOT NULL,
  `description` varchar(250) DEFAULT NULL,
  `job_class_name` varchar(250) NOT NULL,
  `is_durable` varchar(1) NOT NULL,
  `is_nonconcurrent` varchar(1) NOT NULL,
  `is_update_data` varchar(1) NOT NULL,
  `requests_recovery` varchar(1) NOT NULL,
  `job_data` blob,
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`),
  KEY `idx_qrtz_j_req_recovery` (`sched_name`,`requests_recovery`),
  KEY `idx_qrtz_j_grp` (`sched_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz任务详情表';

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------
BEGIN;
INSERT INTO `qrtz_job_details` (`sched_name`, `job_name`, `job_group`, `description`, `job_class_name`, `is_durable`, `is_nonconcurrent`, `is_update_data`, `requests_recovery`, `job_data`) VALUES ('travisScheduler', 'log-cleanup', 'ops-internal', '清理过期任务执行日志', 'com.travis.monolith.ops.job.internal.quartz.OpsJobLogCleanupJob', '0', '0', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787000737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F40000000000010770800000010000000007800);
INSERT INTO `qrtz_job_details` (`sched_name`, `job_name`, `job_group`, `description`, `job_class_name`, `is_durable`, `is_nonconcurrent`, `is_update_data`, `requests_recovery`, `job_data`) VALUES ('travisScheduler', 'scheduled-message-push', 'system-message', '推送到期的定时消息', 'com.travis.monolith.system.message.internal.quartz.SysMessageScheduledPushJob', '0', '0', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787000737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F40000000000010770800000010000000007800);
COMMIT;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) NOT NULL,
  `lock_name` varchar(40) NOT NULL,
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz集群锁表';

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
BEGIN;
INSERT INTO `qrtz_locks` (`sched_name`, `lock_name`) VALUES ('travisScheduler', 'STATE_ACCESS');
INSERT INTO `qrtz_locks` (`sched_name`, `lock_name`) VALUES ('travisScheduler', 'TRIGGER_ACCESS');
COMMIT;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz暂停触发器组表';

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) NOT NULL,
  `instance_name` varchar(190) NOT NULL,
  `last_checkin_time` bigint NOT NULL,
  `checkin_interval` bigint NOT NULL,
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz调度器集群状态表';

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------
BEGIN;
INSERT INTO `qrtz_scheduler_state` (`sched_name`, `instance_name`, `last_checkin_time`, `checkin_interval`) VALUES ('travisScheduler', 'Mac1783100527041', 1783103179717, 10000);
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `repeat_count` bigint NOT NULL,
  `repeat_interval` bigint NOT NULL,
  `times_triggered` bigint NOT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `fk_qrtz_simple_triggers` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz简单触发器表';

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `str_prop_1` varchar(512) DEFAULT NULL,
  `str_prop_2` varchar(512) DEFAULT NULL,
  `str_prop_3` varchar(512) DEFAULT NULL,
  `int_prop_1` int DEFAULT NULL,
  `int_prop_2` int DEFAULT NULL,
  `long_prop_1` bigint DEFAULT NULL,
  `long_prop_2` bigint DEFAULT NULL,
  `dec_prop_1` decimal(13,4) DEFAULT NULL,
  `dec_prop_2` decimal(13,4) DEFAULT NULL,
  `bool_prop_1` varchar(1) DEFAULT NULL,
  `bool_prop_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `fk_qrtz_simprop_triggers` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz属性触发器表';

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) NOT NULL,
  `trigger_name` varchar(190) NOT NULL,
  `trigger_group` varchar(190) NOT NULL,
  `job_name` varchar(190) NOT NULL,
  `job_group` varchar(190) NOT NULL,
  `description` varchar(250) DEFAULT NULL,
  `next_fire_time` bigint DEFAULT NULL,
  `prev_fire_time` bigint DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `trigger_state` varchar(16) NOT NULL,
  `trigger_type` varchar(8) NOT NULL,
  `start_time` bigint NOT NULL,
  `end_time` bigint DEFAULT NULL,
  `calendar_name` varchar(190) DEFAULT NULL,
  `misfire_instr` smallint DEFAULT NULL,
  `job_data` blob,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `idx_qrtz_t_j` (`sched_name`,`job_name`,`job_group`),
  KEY `idx_qrtz_t_jg` (`sched_name`,`job_group`),
  KEY `idx_qrtz_t_c` (`sched_name`,`calendar_name`),
  KEY `idx_qrtz_t_g` (`sched_name`,`trigger_group`),
  KEY `idx_qrtz_t_state` (`sched_name`,`trigger_state`),
  KEY `idx_qrtz_t_n_state` (`sched_name`,`trigger_name`,`trigger_group`,`trigger_state`),
  KEY `idx_qrtz_t_n_g_state` (`sched_name`,`trigger_group`,`trigger_state`),
  KEY `idx_qrtz_t_next_fire_time` (`sched_name`,`next_fire_time`),
  KEY `idx_qrtz_t_nft_st` (`sched_name`,`trigger_state`,`next_fire_time`),
  KEY `idx_qrtz_t_nft_misfire` (`sched_name`,`misfire_instr`,`next_fire_time`),
  KEY `idx_qrtz_t_nft_st_misfire` (`sched_name`,`trigger_state`,`misfire_instr`,`next_fire_time`),
  KEY `idx_qrtz_t_nft_st_misfire_grp` (`sched_name`,`trigger_group`,`trigger_state`,`misfire_instr`,`next_fire_time`),
  CONSTRAINT `fk_qrtz_triggers_job` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Quartz触发器基础表';

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------
BEGIN;
INSERT INTO `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`, `job_name`, `job_group`, `description`, `next_fire_time`, `prev_fire_time`, `priority`, `trigger_state`, `trigger_type`, `start_time`, `end_time`, `calendar_name`, `misfire_instr`, `job_data`) VALUES ('travisScheduler', 'log-cleanup-trigger', 'ops-internal', 'log-cleanup', 'ops-internal', NULL, 1783134000000, 1783047600000, 5, 'WAITING', 'CRON', 1781254743000, 0, NULL, 0, '');
INSERT INTO `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`, `job_name`, `job_group`, `description`, `next_fire_time`, `prev_fire_time`, `priority`, `trigger_state`, `trigger_type`, `start_time`, `end_time`, `calendar_name`, `misfire_instr`, `job_data`) VALUES ('travisScheduler', 'scheduled-message-push-trigger', 'system-message', 'scheduled-message-push', 'system-message', NULL, 1783103220000, 1783103160000, 5, 'WAITING', 'CRON', 1782439443000, 0, NULL, 0, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法）',
  `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键（唯一标识）',
  `config_value` text COMMENT '配置值',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0=未删除 1=已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父部门ID，顶级部门为0',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '负责人',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `is_deleted` tinyint unsigned DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `sort`, `leader`, `mobile`, `status`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070438293224722433, 0, '测试', 0, NULL, NULL, 1, 1, '2026-06-26 09:25:31', 1, '2026-06-26 14:55:42', 1);
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `sort`, `leader`, `mobile`, `status`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070444504703705089, 2070438293224722433, '测试1', 0, NULL, NULL, 1, 1, '2026-06-26 09:50:12', 1, '2026-06-26 14:55:42', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `status` tinyint DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint unsigned DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_type` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (1, '角色类型', 'sys_role_type', 1, '0-用户创建\n1-系统内置', 0, '2026-06-16 03:46:25', 1, '2026-07-03 09:30:27', 1, 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (2, '菜单类型', 'sys_menu_type', 1, '0-目录\n1-菜单\n2-按钮', 0, '2026-05-24 01:54:11', 1, '2026-07-03 09:30:31', 1, 2);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (3, '参数类型', 'sys_config_type', 1, '0-用户创建\n1-系统内置', 0, '2026-06-16 04:01:14', 1, '2026-07-03 09:30:30', 1, 3);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (4, '消息推送状态', 'sys_message_status', 1, '0-待推送 1-定时推送 2-已推送 3-已撤回', 0, '2026-06-26 09:59:51', 1, '2026-07-03 09:31:31', NULL, 4);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (5, '消息推送方式', 'sys_message_push_type', 1, '0-手动推送 1-定时推送', 0, '2026-06-26 09:59:51', 1, '2026-07-03 09:31:33', NULL, 5);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (6, '消息来源类型', 'sys_message_source_type', 1, '后台人工推送和业务来源', 0, '2026-06-26 09:59:51', 1, '2026-07-03 09:31:34', 1, 6);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (7, '消息推送通道', 'sys_message_channel', 1, '站内信、短信、微信公众号、微信小程序', 0, '2026-06-26 09:59:51', 1, '2026-07-03 09:31:36', NULL, 7);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (8, '是否置顶', 'is_pinned', 1, '0-否 1-是', 0, '2026-06-25 15:42:59', 1, '2026-07-03 10:02:58', 1, 8);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (9, '启用状态', 'enable_status', 1, '0-禁用\n1-启用', 0, '2026-05-24 01:54:11', 1, '2026-07-03 10:02:52', 1, 9);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (10, '在线状态', 'online_status', 1, '0-离线 1-在线', 0, '2026-06-28 15:59:35', 1, '2026-07-03 09:31:58', NULL, 10);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (11, '操作结果', 'operation_status', 1, '0-失败\n1-成功', 0, '2026-07-03 02:20:27', 1, '2026-07-03 10:21:00', NULL, 11);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (2070500000000000001, '操作业务类型', 'operation_business_type', 1, '', 0, '2026-07-04 01:00:42', 1, '2026-07-03 17:01:16', 1, 13);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `sort`) VALUES (2073032019189133314, '请求方式', 'http_method', 1, '', 0, '2026-07-03 13:12:03', 1, '2026-07-03 13:12:21', 1, 12);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `dict_id` bigint NOT NULL COMMENT '字典类型ID',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  `tag_style` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'default' COMMENT '展示样式',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint unsigned DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_dict_id` (`dict_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (1, 1, '用户创建', '0', 'success', 1, 1, NULL, 0, '2026-06-16 03:48:12', 1, '2026-07-03 10:03:32', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2, 1, '系统内置', '1', 'primary', 2, 1, NULL, 0, '2026-06-16 03:48:23', 1, '2026-07-03 10:03:33', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (3, 2, '目录', '0', 'default', 1, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-03 10:03:44', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (4, 2, '菜单', '1', 'primary', 2, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-03 10:03:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (5, 2, '按钮', '2', 'danger', 3, 1, NULL, 0, '2026-06-16 03:35:00', 1, '2026-07-03 10:03:46', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (6, 3, '用户创建', '0', 'success', 1, 1, NULL, 0, '2026-06-16 04:01:27', 1, '2026-07-03 10:03:47', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (7, 3, '系统内置', '1', 'primary', 2, 1, NULL, 0, '2026-06-16 04:01:38', 1, '2026-07-03 10:03:47', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (8, 4, '待推送', '0', 'default', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:48', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (9, 4, '定时推送', '1', 'warning', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:48', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (10, 4, '已推送', '2', 'success', 3, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:50', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (11, 4, '已撤回', '3', 'danger', 4, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:51', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (12, 5, '手动推送', '0', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:52', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (13, 5, '定时推送', '1', 'warning', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:53', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (14, 6, '后台人工推送', 'MANUAL', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:54', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (15, 7, '短信', 'SMS', 'info', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:55', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (16, 7, '微信小程序', 'WECHAT_MP', 'success', 4, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:56', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (17, 7, '站内信', 'IN_APP', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:57', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (18, 7, '微信公众号', 'WECHAT_OA', 'success', 3, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:58', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (19, 8, '否', '0', 'default', 0, 1, '', 0, '2026-06-25 15:44:15', 1, '2026-07-03 10:04:00', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (20, 8, '置顶', '1', 'primary', 1, 1, NULL, 0, '2026-06-25 15:44:28', 1, '2026-07-03 10:04:01', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (21, 9, '启用', '1', 'success', 1, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-03 10:04:02', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (22, 9, '禁用', '0', 'danger', 2, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-03 10:04:03', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (23, 10, '离线', '0', 'default', 2, 1, NULL, 0, '2026-06-28 16:22:20', 1, '2026-07-03 10:04:04', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (24, 10, '在线', '1', 'success', 1, 1, NULL, 0, '2026-06-28 16:22:31', 1, '2026-07-03 10:04:06', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000101, 2070500000000000001, '新增', 'CREATE', 'success', 1, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000102, 2070500000000000001, '修改', 'UPDATE', 'primary', 2, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000103, 2070500000000000001, '删除', 'DELETE', 'danger', 3, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000104, 2070500000000000001, '授权', 'GRANT', 'warning', 4, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000105, 2070500000000000001, '上传', 'UPLOAD', 'primary', 5, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000106, 2070500000000000001, '导入', 'IMPORT', 'info', 6, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000107, 2070500000000000001, '导出', 'EXPORT', 'info', 7, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000108, 2070500000000000001, '其他', 'OTHER', 'default', 99, 1, NULL, 0, '2026-07-04 01:00:42', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2072873955806928897, 11, '成功', '1', 'success', 1, 1, NULL, 0, '2026-07-03 02:43:58', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2072873991148134402, 11, '失败', '0', 'danger', 2, 1, NULL, 0, '2026-07-03 02:44:06', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032177951928321, 2073032019189133314, 'PUT', 'PUT', 'success', 2, 1, NULL, 0, '2026-07-03 13:12:41', 1, '2026-07-03 13:13:38', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032251595517954, 2073032019189133314, 'GET', 'GET', 'default', 1, 1, NULL, 0, '2026-07-03 13:12:59', 1, '2026-07-03 13:13:31', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032315797729282, 2073032019189133314, 'POST', 'POST', 'primary', 3, 1, NULL, 0, '2026-07-03 13:13:14', 1, '2026-07-03 13:13:46', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032357614940162, 2073032019189133314, 'DELETE', 'DELETE', 'danger', 4, 1, NULL, 0, '2026-07-03 13:13:24', 1, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_error_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_error_log`;
CREATE TABLE `sys_error_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '登录用户ID',
  `request_id` varchar(64) DEFAULT NULL COMMENT '请求ID',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(10) DEFAULT NULL COMMENT 'HTTP方法',
  `controller_method` varchar(1000) DEFAULT NULL COMMENT '控制器方法',
  `exception_class` varchar(500) NOT NULL COMMENT '异常类型',
  `message` text COMMENT '异常消息',
  `stack_trace` mediumtext COMMENT '异常堆栈',
  `ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_error_create_time` (`create_time`),
  KEY `idx_error_exception_class` (`exception_class`(191)),
  KEY `idx_error_request_id` (`request_id`),
  KEY `idx_error_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后端错误日志表';

-- ----------------------------
-- Records of sys_error_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `folder_id` bigint DEFAULT NULL COMMENT '文件夹ID',
  `storage_config_id` bigint NOT NULL COMMENT '存储配置ID',
  `uploader_type` varchar(32) NOT NULL DEFAULT 'admin' COMMENT '上传主体类型',
  `uploader_name` varchar(100) DEFAULT NULL COMMENT '上传主体名称',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `original_name` varchar(500) DEFAULT NULL COMMENT '原始文件名',
  `path` varchar(1000) NOT NULL COMMENT '相对路径',
  `extension` varchar(50) DEFAULT NULL COMMENT '扩展名',
  `mime_type` varchar(200) DEFAULT NULL COMMENT 'MIME类型',
  `size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_file_folder` (`folder_id`),
  KEY `idx_file_storage` (`storage_config_id`),
  KEY `idx_file_uploader` (`uploader_type`,`create_by`),
  KEY `idx_file_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件元数据表';

-- ----------------------------
-- Records of sys_file
-- ----------------------------
BEGIN;
INSERT INTO `sys_file` (`id`, `folder_id`, `storage_config_id`, `uploader_type`, `uploader_name`, `file_name`, `original_name`, `path`, `extension`, `mime_type`, `size`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2071112511029383169, 3, 1930000000001000, 'admin', 'travis0115', '85b046462d474fa1b4ea10711b889a18.jpg', 'avatar.jpg', '/files/2026/06/28/85b046462d474fa1b4ea10711b889a18.jpg', 'jpg', 'image/jpeg', 130883, 0, '2026-06-28 06:04:37', 1, NULL, NULL);
INSERT INTO `sys_file` (`id`, `folder_id`, `storage_config_id`, `uploader_type`, `uploader_name`, `file_name`, `original_name`, `path`, `extension`, `mime_type`, `size`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2071457537206947842, 5, 1930000000001000, 'admin', 'travis0115', '0750449f7b404bfb86ecd8f02149106c.pdf', 'MOTA产品单-202511.pdf', '/files/2026/06/29/0750449f7b404bfb86ecd8f02149106c.pdf', 'pdf', 'application/pdf', 17183010, 1, '2026-06-29 04:55:37', 1, '2026-06-29 04:56:16', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_file_folder
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_folder`;
CREATE TABLE `sys_file_folder` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父文件夹ID',
  `folder_name` varchar(100) NOT NULL COMMENT '文件夹名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_file_folder_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件分类文件夹表';

-- ----------------------------
-- Records of sys_file_folder
-- ----------------------------
BEGIN;
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (1, 0, '后台管理', 1, 1, 0, '2026-06-26 15:28:55', 1, '2026-06-28 14:05:57', 1);
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2, 1, '手动上传', 1, 1, 0, '2026-06-26 15:21:26', 1, '2026-06-27 00:53:33', 1);
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (3, 1, '头像', 2, 1, 0, '2026-06-26 04:35:38', 1, '2026-06-27 00:53:32', 1);
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (4, 1, '系统公告', 3, 1, 0, '2026-06-26 15:21:26', 1, '2026-06-27 00:53:31', 1);
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (5, 1, '版本管理', 4, 1, 0, '2026-06-26 15:21:26', 1, '2026-06-27 00:53:29', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_file_storage_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_storage_config`;
CREATE TABLE `sys_file_storage_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `storage_type` varchar(30) NOT NULL COMMENT '存储类型：LOCAL/S3/ALIYUN_OSS/TENCENT_COS等',
  `storage_path` varchar(500) DEFAULT NULL COMMENT '存储路径：本地目录或对象存储Key前缀',
  `domain` varchar(500) DEFAULT NULL COMMENT '访问域名或CDN域名',
  `endpoint` varchar(500) DEFAULT NULL COMMENT '服务端点',
  `region` varchar(100) DEFAULT NULL COMMENT '地域',
  `bucket_id` varchar(200) DEFAULT NULL COMMENT 'Bucket ID或平台内部标识',
  `bucket_name` varchar(200) DEFAULT NULL COMMENT 'Bucket名称',
  `access_key` varchar(500) DEFAULT NULL COMMENT '访问Key',
  `secret_key` varchar(1000) DEFAULT NULL COMMENT '访问密钥，建议加密存储',
  `meta` json DEFAULT NULL COMMENT '扩展配置，兼容不同平台差异参数',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认：0否 1是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_storage_default` (((case when ((`is_default` = 1) and (`is_deleted` = 0)) then 1 else NULL end))),
  KEY `idx_file_storage_type` (`storage_type`),
  KEY `idx_file_storage_status` (`status`),
  KEY `idx_file_storage_default_status` (`is_default`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件存储配置表';

-- ----------------------------
-- Records of sys_file_storage_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_file_storage_config` (`id`, `config_name`, `storage_type`, `storage_path`, `domain`, `endpoint`, `region`, `bucket_id`, `bucket_name`, `access_key`, `secret_key`, `meta`, `is_default`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (1930000000001000, '本地存储', 'LOCAL', '${user.home}/data/uploads', 'http://127.0.0.1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, '', 0, '2026-06-11 16:38:41', 1, '2026-06-27 23:43:39', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录用户名',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录IP',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作系统',
  `status` tinyint unsigned DEFAULT '1' COMMENT '登录状态 0-失败  1-成功',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071291031210848258, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-06-28 17:53:59');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071291031210848259, 'travis0115', '127.0.0.1', '内网', 'Chrome 149', 'macOS', 1, NULL, '2026-06-28 17:53:59');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071291031210848260, 'travis0115', '127.0.0.1', '内网', 'Chrome 149', 'macOS', 1, NULL, '2026-06-28 17:53:59');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071295000213606402, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-06-28 18:09:46');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071295000213606403, 'travis0115', '127.0.0.1', '内网', 'Chrome 149', 'macOS', 1, NULL, '2026-06-28 18:09:46');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2071452057197776898, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-06-29 04:33:51');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072701258787368961, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:17:44');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072701258787368962, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:17:44');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072702800642838530, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:23:51');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072702800642838531, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:23:51');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072702800642838532, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:23:51');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072711019809464322, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:56:31');
INSERT INTO `sys_login_log` (`id`, `username`, `ip`, `location`, `browser`, `os`, `status`, `message`, `login_time`) VALUES (2072711019809464323, 'travis0115', '127.0.0.1', '内网', 'Chrome 150', 'macOS', 1, NULL, '2026-07-02 15:56:31');
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID，一级菜单为0',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint unsigned DEFAULT '0' COMMENT '类型 0：目录 1：菜单 2：按钮',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '前端组件路径，如 system/user/list',
  `perms` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '授权标识，如：system:user:list',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `meta` json DEFAULT NULL COMMENT '路由元信息JSON（Vben RouteMeta）',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='后台菜单表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1, 0, '主页', 1, '/dashboard', 'dashboard/analytics/index', NULL, 'carbon:home', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 16:11:10', 0, '{\"affixTab\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (3, 0, '系统设置', 0, '/system', '', NULL, 'lucide:settings', 4, 1, '2026-05-27 09:40:25', 1, '2026-06-02 20:17:21', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (4, 3, '菜单管理', 1, '/system/menu', 'system/menu/list', 'system:menu:query', 'lucide:menu', 4, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:42', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (5, 3, '角色管理', 1, '/system/role', 'system/role/list', 'system:role:query', 'lucide:shield', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:56', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (6, 3, '用户管理', 1, '/system/user', 'system/user/list', 'system:user:query', 'lucide:user', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:55', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (7, 3, '部门管理', 1, '/system/dept', 'system/dept/list', 'system:dept:query', 'lucide:building', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:24:02', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (8, 3, '字典管理', 1, '/system/dict', 'system/dict/list', 'system:dict:query', 'lucide:book-open', 5, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:24:06', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (9, 3, '日志管理', 0, '/system/log', '', NULL, 'mdi:file-document-outline', 11, 1, '2026-05-27 09:40:25', 1, '2026-06-15 07:57:40', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (91, 9, '登录日志', 1, '/system/log/login', 'system/log/login-log/list', 'system:log:login:query', 'ion:log-in-outline', 1, 1, '2026-05-27 09:40:25', 1, '2026-07-03 08:05:25', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (92, 9, '操作日志', 1, '/system/log/operation', 'system/log/operation-log/list', 'system:log:operation:query', 'carbon:operations-record', 2, 1, '2026-05-27 09:40:25', 1, '2026-07-03 08:05:29', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (100, 4, '新增', 2, NULL, NULL, 'system:menu:create', '', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:17:56', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (101, 4, '修改', 2, NULL, NULL, 'system:menu:update', '', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:00', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (102, 4, '删除', 2, NULL, NULL, 'system:menu:delete', '', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:04', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (103, 5, '新增', 2, NULL, NULL, 'system:role:create', '', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:08', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (104, 5, '修改', 2, NULL, NULL, 'system:role:update', '', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:14', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (105, 5, '删除', 2, NULL, NULL, 'system:role:delete', '', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:18', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (106, 6, '新增', 2, NULL, NULL, 'system:user:create', '', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:33', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (107, 6, '修改', 2, NULL, NULL, 'system:user:update', '', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:28', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (108, 6, '删除', 2, NULL, NULL, 'system:user:delete', '', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:21', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (109, 7, '新增', 2, '', '', 'system:dept:create', '', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 00:07:47', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (110, 7, '修改', 2, '', '', 'system:dept:update', '', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 00:07:50', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (111, 7, '删除', 2, '', '', 'system:dept:delete', '', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 00:07:52', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (112, 8, '新增', 2, NULL, NULL, 'system:dict:create', '', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:37', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (113, 8, '修改', 2, NULL, NULL, 'system:dict:update', '', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:41', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (114, 8, '删除', 2, NULL, NULL, 'system:dict:delete', '', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:18:44', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000001, 3, '参数配置', 1, '/system/config', 'system/config/list', 'system:config:query', 'ion:options-outline', 6, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:24:16', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000002, 1930000000000001, '新增', 2, NULL, NULL, 'system:config:create', NULL, 1, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:18', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000003, 1930000000000001, '修改', 2, NULL, NULL, 'system:config:update', NULL, 2, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:14', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000004, 1930000000000001, '删除', 2, NULL, NULL, 'system:config:delete', NULL, 3, 1, '2026-06-02 10:50:59', 1, '2026-06-15 00:08:21', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000012, 3, '版本管理', 1, '/system/version', 'system/version/list', 'system:version:query', 'carbon:version', 7, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:24:18', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000013, 1930000000000012, '新增', 2, NULL, NULL, 'system:version:create', NULL, 1, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:34', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000014, 1930000000000012, '修改', 2, NULL, NULL, 'system:version:update', NULL, 2, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:28', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000015, 1930000000000012, '删除', 2, NULL, NULL, 'system:version:delete', NULL, 3, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:20', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001010, 3, '系统公告', 1, '/system/notice', 'system/notice/list', 'system:notice:query', 'ion:megaphone-outline', 8, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001011, 1930000000001010, '新增', 2, NULL, NULL, 'system:notice:create', NULL, 1, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001012, 1930000000001010, '修改', 2, NULL, NULL, 'system:notice:update', NULL, 2, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001013, 1930000000001010, '删除', 2, NULL, NULL, 'system:notice:delete', NULL, 3, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001014, 1930000000002000, '消息推送', 1, '/message-center/message', 'system/message/list', 'system:message:query', 'ion:paper-plane-outline', 1, 1, '2026-06-25 20:00:07', 1, '2026-06-26 09:59:51', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001015, 1930000000001014, '新增', 2, NULL, NULL, 'system:message:create', NULL, 1, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001016, 1930000000001014, '修改', 2, NULL, NULL, 'system:message:update', NULL, 2, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001017, 1930000000001014, '删除', 2, NULL, NULL, 'system:message:delete', NULL, 3, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001020, 2066431140185759745, '错误日志', 1, '/ops/error-log', 'ops/error-log/list', 'ops:error-log:query', 'ion:bug-outline', 2, 1, '2026-06-11 16:38:41', 1, '2026-06-15 18:24:23', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001030, 3, '文件管理', 1, '/system/file-management', 'system/file-management/list', 'system:file:query', 'ion:folder-open-outline', 10, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001031, 1930000000001030, '上传', 2, NULL, NULL, 'system:file:upload', NULL, 1, 1, '2026-06-11 16:38:41', 1, '2026-06-15 00:08:45', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001032, 1930000000001030, '删除', 2, NULL, NULL, 'system:file:delete', NULL, 2, 1, '2026-06-11 16:38:41', 1, '2026-06-15 00:08:49', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001040, 2066431140185759745, '任务调度', 0, '/ops/job', '', NULL, 'ion:timer-outline', 1, 1, '2026-06-11 16:38:41', 1, '2026-06-12 17:32:13', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001041, 1930000000001051, '编辑', 2, '', '', 'ops:job:update', '', 2, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:30:55', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001042, 1930000000001051, '启停', 2, '', '', 'ops:job:operation', '', 3, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:31:03', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001044, 1930000000001051, '执行', 2, '', '', 'ops:job:operation', '', 1, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:31:07', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001051, 1930000000001040, '任务管理', 1, '/ops/job/list', 'ops/job/list', 'ops:job:query', 'carbon:document-tasks', 1, 1, '2026-06-12 17:32:13', 1, '2026-06-15 19:30:29', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001052, 1930000000001040, '执行日志', 1, '/ops/job/log', 'ops/job/log', 'ops:job:log:query', 'mdi:sticker-text-outline', 2, 1, '2026-06-12 17:32:13', 1, '2026-06-15 19:30:41', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002000, 0, '消息中心', 0, '/message-center', '', NULL, 'ion:chatbubbles-outline', 2, 1, '2026-06-26 09:59:51', 1, '2026-06-26 02:58:46', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002001, 1930000000002000, '消息模板', 1, '/message-center/template', 'system/message-template/list', 'system:message:template:query', 'ion:document-text-outline', 2, 1, '2026-06-26 09:59:51', 1, NULL, NULL, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002002, 1930000000002001, '新增', 2, NULL, NULL, 'system:message:template:create', NULL, 1, 1, '2026-06-26 09:59:51', 1, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002003, 1930000000002001, '修改', 2, NULL, NULL, 'system:message:template:update', NULL, 2, 1, '2026-06-26 09:59:51', 1, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002004, 1930000000002001, '删除', 2, NULL, NULL, 'system:message:template:delete', NULL, 3, 1, '2026-06-26 09:59:51', 1, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (2066431140185759745, 0, '系统运维', 0, '/ops', '', NULL, 'carbon:cloud-monitoring', 3, 1, '2026-06-15 08:02:31', 1, '2026-06-15 08:03:12', 1, '{}', 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '站内信内容快照，用于收件箱展示',
  `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1-系统通知 2-业务消息',
  `push_type` tinyint NOT NULL DEFAULT '0' COMMENT '推送方式：0-手动推送 1-定时推送',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT '来源类型：MANUAL-后台人工推送',
  `source_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源ID',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'IN_APP' COMMENT '主推送通道：IN_APP/SMS/WECHAT_OA/WECHAT_MP',
  `enable_inbox_copy` tinyint NOT NULL DEFAULT '0' COMMENT '是否同步生成站内信副本：0-否 1-是',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待推送 1-定时推送 2-已推送 3-已撤回',
  `receiver_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'admin' COMMENT '接收端登录体系，与 LoginType 常量取值保持一致：admin/app',
  `receiver_scope` tinyint NOT NULL COMMENT '接收范围：0-全部用户 1-指定用户 2-指定角色 3-指定部门',
  `receiver_values` json DEFAULT NULL COMMENT '接收范围值JSON数组；全部用户为空',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_message_status_time` (`status`,`is_deleted`,`publish_time`,`create_time`),
  KEY `idx_sys_message_push_type` (`push_type`,`status`,`is_deleted`),
  KEY `idx_sys_message_receiver` (`receiver_type`,`receiver_scope`,`status`,`is_deleted`),
  KEY `idx_sys_message_channel` (`channel`,`status`,`is_deleted`),
  KEY `idx_sys_message_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息推送主体';

-- ----------------------------
-- Records of sys_message
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_message_channel_content
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_channel_content`;
CREATE TABLE `sys_message_channel_content` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推送通道：IN_APP/SMS/WECHAT_OA/WECHAT_MP',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道标题',
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道副标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '渠道内容',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '大图URL',
  `jump_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接',
  `template_id` bigint DEFAULT NULL COMMENT '消息模板ID',
  `template_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '模板参数JSON',
  `word_count` int DEFAULT NULL COMMENT '字数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_message_channel` (`message_id`,`channel`),
  KEY `idx_sys_message_channel_message` (`message_id`,`is_deleted`),
  KEY `idx_sys_message_channel_template` (`template_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息渠道内容';

-- ----------------------------
-- Records of sys_message_channel_content
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_message_receiver
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_receiver`;
CREATE TABLE `sys_message_receiver` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `receiver_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收人登录体系，与 LoginType 常量取值保持一致',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '阅读状态：0-未读 1-已读 2-已删除',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_message_receiver` (`message_id`,`receiver_type`,`receiver_id`),
  KEY `idx_sys_message_receiver_user_status` (`receiver_type`,`receiver_id`,`read_status`,`is_deleted`,`message_id`),
  KEY `idx_sys_message_receiver_message` (`message_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息用户状态';

-- ----------------------------
-- Records of sys_message_receiver
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_message_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template`;
CREATE TABLE `sys_message_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推送通道',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板标题',
  `platform_template_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台模板ID',
  `content_schema` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '模板变量结构JSON',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '模板内容',
  `redirect_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_message_template_code_channel` (`template_code`,`channel`),
  KEY `idx_sys_message_template_channel` (`channel`,`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板';

-- ----------------------------
-- Records of sys_message_template
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `is_pinned` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶 0-否 1-是',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_notice_status` (`status`),
  KEY `idx_notice_publish_time` (`publish_time`),
  KEY `idx_notice_order` (`is_pinned`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` (`id`, `title`, `content`, `status`, `publish_time`, `is_pinned`, `sort`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070374879949410305, '这是第一条公告', '<p>234</p>', 1, '2026-06-26 08:08:03', 1, 0, NULL, 0, '2026-06-26 05:13:32', 1, '2026-06-26 06:09:59', 1);
INSERT INTO `sys_notice` (`id`, `title`, `content`, `status`, `publish_time`, `is_pinned`, `sort`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070418841322033153, '恭喜你，获得618大额券🎉', '<p>234阿萨德发生的</p>', 1, '2026-06-26 08:08:07', 0, 0, NULL, 0, '2026-06-26 08:08:13', 1, '2026-06-26 08:10:51', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人用户名',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作描述',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属模块',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'HTTP请求方式',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '返回结果',
  `request_id` varchar(100) DEFAULT NULL COMMENT '请求ID',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作IP',
  `location` varchar(100) DEFAULT NULL COMMENT '操作地点',
  `user_agent` varchar(512) DEFAULT NULL COMMENT 'User-Agent',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `duration` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
  `status` tinyint unsigned DEFAULT '1' COMMENT '操作状态 0-失败  1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073071746365665281, 1, 'travis0115', '重置用户密码', 'User', 'UPDATE', 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#resetPassword', '/api/admin/system/user/2067566320808062977/reset-password', 'PUT', NULL, NULL, '4cc08bed227febd222ca69c283d04936', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 148, 1, NULL, '2026-07-03 15:49:55');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073071794189119489, 1, 'travis0115', '重置用户密码', 'User', 'UPDATE', 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#resetPassword', '/api/admin/system/user/2067566320808062977/reset-password', 'PUT', NULL, NULL, 'd2ddca0ce41533790b3dd76e19e73032', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 90, 1, NULL, '2026-07-03 15:50:06');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073080262660857858, 1, 'travis0115', '重置用户密码', 'User', NULL, 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#resetPassword', '/api/admin/system/user/2067566320808062977/reset-password', 'PUT', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, NULL, 95, 1, NULL, '2026-07-03 16:23:45');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073086563180818433, 1, 'travis0115', '重置用户密码', 'User', 'UPDATE', 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#resetPassword', '/api/admin/system/user/2067566320808062977/reset-password', 'PUT', NULL, NULL, 'c8fd2275b6ef6d3673e57a0ec4689d36', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 84, 1, NULL, '2026-07-03 16:48:47');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073086681044955138, 1, 'travis0115', '更新用户', 'User', 'UPDATE', 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#update', '/api/admin/system/user/2067566320808062977', 'PUT', '[\"2067566320808062977\",{\"deptId\":\"0\",\"nickname\":\"test123456\",\"username\":\"test123456\"}]', '{\"code\":\"200\",\"msg\":\"操作成功\"}', '89c2364cc378775b39726b28de8b4d27', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 8, 1, NULL, '2026-07-03 16:49:16');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073086681145618433, 1, 'travis0115', '分配用户角色', 'User', 'GRANT', 'com.travis.monolith.system.user.internal.controller.admin.SysUserController#assignRoles', '/api/admin/system/user/roles', 'POST', '[{\"roleIds\":[\"3\"],\"userId\":\"2067566320808062977\"}]', '{\"code\":\"200\",\"msg\":\"操作成功\"}', 'bed0d100ed2a72efceb5113f5cd206fe', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 7, 1, NULL, '2026-07-03 16:49:16');
INSERT INTO `sys_operation_log` (`id`, `user_id`, `username`, `description`, `module`, `business_type`, `method`, `request_url`, `request_method`, `request_params`, `response_result`, `request_id`, `ip`, `location`, `user_agent`, `browser`, `os`, `duration`, `status`, `error_msg`, `create_time`) VALUES (2073089701132578818, 1, 'travis0115', '更新字典', 'Dict', 'UPDATE', 'com.travis.monolith.system.dict.internal.controller.admin.SysDictController#update', '/api/admin/system/dict/2070500000000000001', 'PUT', '[\"2070500000000000001\",{\"dictCode\":\"operation_business_type\",\"dictName\":\"操作业务类型\",\"remark\":\"\",\"sort\":13}]', '{\"code\":\"200\",\"msg\":\"操作成功\"}', '57c1b0c137561fa671a20e33b85bed51', '127.0.0.1', '内网', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'Chrome 150', 'macOS', 10, 1, NULL, '2026-07-03 17:01:16');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码（sa-token权限标识用）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `modifiable` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否可修改 0-否  1-是',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`, `modifiable`, `status`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (1, '管理员', 'admin', '内置角色', 0, 1, 1, 0, '2020-02-18 17:51:26', 1, '2026-06-26 12:07:46', 1);
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`, `modifiable`, `status`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2, '代理商', 'agent', '内置角色', 1, 1, 0, 1, '2020-07-16 04:01:08', 1, '2026-06-15 08:41:52', 1);
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`, `modifiable`, `status`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (3, '用户', 'user', '', 1, 1, 0, 0, '2020-07-30 09:16:59', 1, '2026-05-24 20:57:45', 1);
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`, `modifiable`, `status`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (4, '子账户', 'sub_user', '内置角色', 1, 1, 0, 1, '2020-07-30 09:16:59', 1, '2026-06-15 08:43:57', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台角色与菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243598888961, 1, 1, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243607277569, 1, 2066431140185759745, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243615666177, 1, 1930000000001040, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243624054786, 1, 1930000000001051, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243628249090, 1, 1930000000001044, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243636637697, 1, 1930000000001041, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243640832001, 1, 1930000000001042, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243645026306, 1, 1930000000001052, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243649220610, 1, 3, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243657609217, 1, 6, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243661803521, 1, 106, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243670192129, 1, 107, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243674386434, 1, 108, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243674386435, 1, 5, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243686969345, 1, 103, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243691163649, 1, 104, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243699552258, 1, 105, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243703746561, 1, 7, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243712135169, 1, 109, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243716329474, 1, 110, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243720523777, 1, 111, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243724718081, 1, 4, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243728912385, 1, 100, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243737300994, 1, 101, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243741495298, 1, 102, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243749883906, 1, 8, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243758272513, 1, 112, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243775049729, 1, 113, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243779244033, 1, 114, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243783438337, 1, 1930000000000001, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243787632642, 1, 1930000000000002, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243791826946, 1, 1930000000000003, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243796021250, 1, 1930000000000004, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243800215553, 1, 1930000000000012, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243804409857, 1, 1930000000000013, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243808604162, 1, 1930000000000014, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243816992769, 1, 1930000000000015, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243821187074, 1, 1930000000001010, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243829575682, 1, 1930000000001011, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243833769985, 1, 1930000000001012, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243842158594, 1, 1930000000001013, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243846352897, 1, 1930000000001030, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243854741506, 1, 1930000000001031, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243858935810, 1, 1930000000001032, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243867324417, 1, 9, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243871518721, 1, 91, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243875713026, 1, 92, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2066518243879907329, 1, 1930000000001020, '2026-06-15 13:48:38', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000001014114, 1, 1930000000001014, '2026-06-25 20:00:07', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000001015115, 1, 1930000000001015, '2026-06-25 20:00:07', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000001016116, 1, 1930000000001016, '2026-06-25 20:00:07', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000001017117, 1, 1930000000001017, '2026-06-25 20:00:07', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000002000001, 1, 1930000000002000, '2026-06-26 10:08:42', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000002000002, 1, 1930000000002001, '2026-06-26 10:08:42', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000002000003, 1, 1930000000002002, '2026-06-26 10:08:42', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000002000004, 1, 1930000000002003, '2026-06-26 10:08:42', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2070100000002000005, 1, 1930000000002004, '2026-06-26 10:08:42', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar_file_id` bigint DEFAULT NULL COMMENT '头像文件ID',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `dept_id` bigint DEFAULT '0' COMMENT '部门ID',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `last_online_time` datetime DEFAULT NULL COMMENT '最近上线时间',
  `last_online_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '最近上线IP',
  `last_online_location` varchar(100) DEFAULT NULL COMMENT '最近上线地点',
  `last_offline_time` datetime DEFAULT NULL COMMENT '最近下线时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_file_id`, `email`, `mobile`, `dept_id`, `status`, `last_online_time`, `last_online_ip`, `last_online_location`, `last_offline_time`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `version`) VALUES (1, 'travis0115', '$2a$10$0iPJdkb53DEVol1Un/cOYOV8vKU1Hfwpfhq913/yqvRD.fjSGlxxi', '鸭腿儿', 2071112511029383169, 'travis0115@163.com', '15700070718', 0, 1, '2026-07-03 17:51:01', '127.0.0.1', '内网', '2026-07-02 16:08:52', 0, '2026-05-01 00:00:00', 1, '2026-07-04 01:51:01', 1, 0);
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_file_id`, `email`, `mobile`, `dept_id`, `status`, `last_online_time`, `last_online_ip`, `last_online_location`, `last_offline_time`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `version`) VALUES (2067566320808062977, 'test123456', '$2a$10$US8a8euq7Y7BY3P7oSPlWOXQtton1rQLB./qrwbOWv15cSPfj41mq', 'test123456', NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, 0, '2026-06-18 11:13:19', 1, '2026-06-26 17:28:16', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户与角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `create_by`) VALUES (2060945171012808705, 1, 1, '2026-05-31 12:43:14', 1);
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `create_by`) VALUES (2073086681112064001, 2067566320808062977, 3, '2026-07-03 16:49:16', 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_version
-- ----------------------------
DROP TABLE IF EXISTS `sys_version`;
CREATE TABLE `sys_version` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新日志标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '更新日志内容（支持多行文本）',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态（0=草稿 1=已发布）',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统版本日志表';

-- ----------------------------
-- Records of sys_version
-- ----------------------------
BEGIN;
INSERT INTO `sys_version` (`id`, `title`, `content`, `publish_time`, `status`, `version`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2069841152907317249, '正式发布', '<p>这是第一个正式发布版本。</p><img class=\"vben-tiptap__image\" src=\"https://ai8.rcouyi.com/api/draw/proxy/dd/aHR0cHM6Ly9haTgucmNvdXlpLmNvbS9maWxlL2RyYXcvZ29vZ2xlLWRyYXcvb3JpZ2luLzIwNjkyMTQ1NjI5NjM0MjczMjgucG5n.png\" width=\"313\" style=\"width: 313px; height: auto;\"><p></p>', '2026-06-25 01:52:39', 1, '1.0', '2026-06-24 17:52:41', 1, '2026-06-24 18:11:58', 1);
INSERT INTO `sys_version` (`id`, `title`, `content`, `publish_time`, `status`, `version`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070407295539777537, '测试', '<p>测试</p><img width=\"673\" data-file-id=\"2070353943804170241\"><img width=\"673\" data-file-id=\"2065023466524274690\"><p></p>', '2026-06-26 07:39:18', 1, '2.0', '2026-06-26 07:22:20', 1, '2026-06-26 07:22:23', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
