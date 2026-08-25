/*
 Navicat Premium Dump SQL

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 90701 (9.7.1)
 Source Host           : localhost:3306
 Source Schema         : travis_monolith

 Target Server Type    : MySQL
 Target Server Version : 90701 (9.7.1)
 File Encoding         : 65001

 Date: 25/08/2026 10:47:14
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
  `lock_version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`),
  UNIQUE KEY `uk_app_user_mobile` (`mobile`),
  KEY `idx_app_user_status` (`status`,`is_deleted`),
  KEY `idx_app_user_avatar_file_id` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端用户';

-- ----------------------------
-- Records of app_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for app_user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `app_user_login_log`;
CREATE TABLE `app_user_login_log` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `username` varchar(64) DEFAULT NULL COMMENT '登录用户名',
  `ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `location` varchar(100) DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `status` tinyint unsigned DEFAULT '1' COMMENT '登录状态 0-失败 1-成功',
  `message` varchar(255) DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_user_login_log_username` (`username`),
  KEY `idx_app_user_login_log_time` (`login_time`),
  KEY `idx_app_user_login_status_time_user` (`status`,`login_time`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户端用户登录日志表';

-- ----------------------------
-- Records of app_user_login_log
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
  `SERIALIZED_EVENT` mediumtext NOT NULL,
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
  `concurrent` tinyint NOT NULL DEFAULT '0',
  `misfire_policy` tinyint NOT NULL DEFAULT '0',
  `alert_user_ids` varchar(1000) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_ops_job_handler_name` (`handler_name`),
  KEY `idx_ops_job_status` (`status`)
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
  KEY `idx_ops_job_log_status` (`status`),
  KEY `idx_ops_job_log_create_time` (`create_time`),
  KEY `idx_ops_job_log_job_active_create` (`job_id`,`is_deleted`,`create_time` DESC,`id` DESC)
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
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
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
COMMIT;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL COMMENT '主键（雪花算法生成）',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态 0-禁用  1-启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint unsigned DEFAULT '0' COMMENT '是否删除 0-否  1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_type` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (1, '角色类型', 'sys_role_type', 1, 1, '0-用户创建\n1-系统内置', 0, '2026-06-16 03:46:25', 1, '2026-07-03 09:30:27', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2, '菜单类型', 'sys_menu_type', 2, 1, '0-目录\n1-菜单\n2-按钮', 0, '2026-05-24 01:54:11', 1, '2026-07-03 09:30:31', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (3, '参数类型', 'sys_config_type', 3, 1, '0-用户创建\n1-系统内置', 0, '2026-06-16 04:01:14', 1, '2026-07-03 09:30:30', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (4, '消息推送状态', 'sys_message_status', 4, 1, '0-待推送 1-已推送 2-已撤回', 0, '2026-06-26 09:59:51', 1, '2026-07-12 14:15:50', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (5, '消息推送方式', 'sys_message_push_type', 5, 1, '0-手动推送 1-定时推送 2-自动推送', 0, '2026-06-26 09:59:51', 1, '2026-07-13 22:33:12', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (6, '消息来源类型', 'sys_message_source_type', 6, 1, '后台人工推送等', 0, '2026-06-26 09:59:51', 1, '2026-07-03 09:31:34', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (7, '消息推送通道', 'sys_message_channel', 7, 1, '站内信、短信、微信公众号、微信小程序', 0, '2026-06-26 09:59:51', 1, '2026-07-04 17:44:00', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (8, '消息类型', 'sys_message_type', 8, 1, '1-系统消息 2-业务消息 3-系统公告 4-版本更新', 0, '2026-06-26 09:59:51', 1, '2026-07-13 20:18:56', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (9, '消息阅读状态', 'sys_message_read_status', 9, 1, '0-未读 1-已读', 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (10, '消息接收范围', 'sys_message_receiver_scope', 10, 1, '0-全部用户 1-指定用户 2-指定角色 3-指定部门', 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (11, '消息接收端', 'sys_message_receiver_type', 11, 1, 'admin-后台账号 app-客户端用户', 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (12, '是否置顶', 'is_pinned', 12, 1, '0-否 1-是', 0, '2026-06-25 15:42:59', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (13, '启用状态', 'enable_status', 13, 1, '0-禁用\n1-启用', 0, '2026-05-24 01:54:11', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (14, '在线状态', 'online_status', 14, 1, '0-离线 1-在线', 0, '2026-06-28 15:59:35', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (15, '操作结果', 'operation_status', 15, 1, '0-失败\n1-成功', 0, '2026-07-03 02:20:27', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (16, '操作业务类型', 'operation_business_type', 16, 1, '', 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (17, '请求方式', 'http_method', 17, 1, '', 0, '2026-07-03 13:12:03', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076400000000000001, '发布状态', 'sys_publish_status', 18, 1, '0-草稿 1-已发布 2-已撤回', 0, '2026-07-13 21:24:36', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000001, '错误日志处理状态', 'ops_error_log_handle_status', 19, 1, '0-待处理 1-已解决 2-已忽略', 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000005, '错误日志平台类型', 'ops_error_log_platform_type', 20, 1, 'ADMIN-管理端 APP-客户端 SYSTEM-系统任务', 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000009, '定时任务调度类型', 'ops_job_schedule_type', 21, 1, 'CRON-Cron表达式 INTERVAL-固定间隔 ONCE-单次执行', 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000013, '定时任务执行结果', 'ops_job_execution_status', 22, 1, '0-执行中 1-成功 2-失败', 0, '2026-08-16 00:00:00', 1, NULL, NULL);
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
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (9, 4, '已推送', '1', 'success', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-12 14:30:23', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (10, 4, '已撤回', '2', 'danger', 3, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-12 14:15:50', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (12, 5, '手动推送', '0', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:52', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (13, 5, '定时推送', '1', 'danger', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:53', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (14, 6, '后台手动创建', 'MANUAL', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:54', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (15, 7, '短信', 'SMS', 'info', 2, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-04 17:44:00', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (16, 7, '微信小程序', 'WECHAT_MP', 'lime', 4, 0, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:56', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (17, 7, '站内信', 'IN_APP', 'primary', 1, 1, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-03 10:03:57', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (18, 7, '微信公众号', 'WECHAT_OA', 'success', 3, 0, NULL, 0, '2026-06-26 09:59:51', 1, '2026-07-04 17:44:00', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (19, 12, '否', '0', 'default', 0, 1, '', 0, '2026-06-25 15:44:15', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (20, 12, '置顶', '1', 'primary', 1, 1, NULL, 0, '2026-06-25 15:44:28', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (21, 13, '启用', '1', 'success', 1, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (22, 13, '禁用', '0', 'danger', 2, 1, NULL, 0, '2026-05-24 01:54:11', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (23, 14, '离线', '0', 'default', 2, 1, NULL, 0, '2026-06-28 16:22:20', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (24, 14, '在线', '1', 'success', 1, 1, NULL, 0, '2026-06-28 16:22:31', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000101, 16, '新增', 'CREATE', 'success', 1, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000102, 16, '修改', 'UPDATE', 'primary', 2, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000103, 16, '删除', 'DELETE', 'danger', 3, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000104, 16, '授权', 'GRANT', 'warning', 4, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000105, 16, '上传', 'UPLOAD', 'cyan', 5, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000106, 16, '导入', 'IMPORT', '#5b8c00', 6, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000107, 16, '导出', 'EXPORT', 'volcano', 7, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2070500000000000108, 16, '其他', 'OTHER', 'default', 99, 1, NULL, 0, '2026-07-04 01:00:42', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2072873955806928897, 15, '成功', '1', 'success', 1, 1, NULL, 0, '2026-07-03 02:43:58', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2072873991148134402, 15, '失败', '0', 'danger', 2, 1, NULL, 0, '2026-07-03 02:44:06', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032177951928321, 17, 'PUT', 'PUT', 'success', 2, 1, NULL, 0, '2026-07-03 13:12:41', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032251595517954, 17, 'GET', 'GET', 'default', 1, 1, NULL, 0, '2026-07-03 13:12:59', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032315797729282, 17, 'POST', 'POST', 'primary', 3, 1, NULL, 0, '2026-07-03 13:13:14', 1, '2026-07-12 17:45:45', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073032357614940162, 17, 'DELETE', 'DELETE', 'danger', 4, 1, NULL, 0, '2026-07-03 13:13:24', 1, '2026-07-12 17:45:45', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2073627983050317826, 8, '系统消息', '1', 'primary', 1, 1, NULL, 0, '2026-07-05 04:40:12', 1, '2026-07-12 17:39:17', NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000003, 9, '未读', '0', 'default', 1, 1, NULL, 0, '2026-07-12 17:45:45', 1, '2026-07-13 14:42:11', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000004, 9, '已读', '1', 'success', 2, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000005, 10, '全部用户', '0', 'primary', 1, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000006, 10, '指定用户', '1', 'success', 2, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000007, 10, '指定角色', '2', 'warning', 3, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000008, 10, '指定部门', '3', 'info', 4, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000009, 11, '后台账号', 'admin', 'primary', 1, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076200000000000010, 11, '客户端用户', 'app', 'success', 2, 1, NULL, 0, '2026-07-12 17:45:45', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076300000000000001, 6, '系统公告', 'NOTICE', 'warning', 2, 1, NULL, 0, '2026-07-13 20:18:56', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076300000000000002, 6, '版本更新', 'VERSION', 'info', 3, 1, NULL, 0, '2026-07-13 20:18:56', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076300000000000003, 8, '系统公告', '2', 'info', 2, 1, NULL, 0, '2026-07-13 20:18:56', 1, '2026-08-09 14:09:46', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076300000000000004, 8, '版本更新', '3', 'volcano', 3, 1, NULL, 0, '2026-07-13 20:18:56', 1, '2026-08-09 14:09:48', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076400000000000002, 2076400000000000001, '草稿', '0', 'default', 1, 1, NULL, 0, '2026-07-13 21:24:36', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076400000000000003, 2076400000000000001, '已发布', '1', 'success', 2, 1, NULL, 0, '2026-07-13 21:24:36', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076400000000000004, 2076400000000000001, '已撤回', '2', 'danger', 3, 1, NULL, 0, '2026-07-13 21:24:36', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2076673058600243202, 5, '自动推送', '2', 'info', 3, 1, NULL, 0, '2026-07-13 14:20:15', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000002, 2088600000000000001, '待处理', '0', 'danger', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, '2026-08-16 09:21:33', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000003, 2088600000000000001, '已解决', '1', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000004, 2088600000000000001, '已忽略', '2', 'default', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000006, 2088600000000000005, '管理端', 'ADMIN', 'primary', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000007, 2088600000000000005, '客户端', 'APP', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000008, 2088600000000000005, '服务端', 'SYSTEM', 'default', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, '2026-08-16 10:10:09', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000010, 2088600000000000009, 'Cron表达式', 'CRON', 'primary', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000011, 2088600000000000009, '固定间隔', 'INTERVAL', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000012, 2088600000000000009, '单次执行', 'ONCE', 'warning', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000014, 2088600000000000013, '执行中', '0', 'warning', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, '2026-08-16 12:19:44', 1);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000015, 2088600000000000013, '成功', '1', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (2088600000000000016, 2088600000000000013, '失败', '2', 'danger', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_error_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_error_log`;
CREATE TABLE `sys_error_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `fingerprint` char(64) NOT NULL COMMENT '异常聚合指纹',
  `module_name` varchar(100) DEFAULT NULL COMMENT '模块名称',
  `platform_type` varchar(16) NOT NULL DEFAULT 'SYSTEM' COMMENT '平台类型',
  `user_id` bigint DEFAULT NULL COMMENT '登录用户ID',
  `username` varchar(100) DEFAULT NULL COMMENT '登录用户名快照',
  `source_type` varchar(32) NOT NULL DEFAULT 'WEB' COMMENT '异常来源类型',
  `source_name` varchar(1000) DEFAULT NULL COMMENT '异常来源名称',
  `business_key` varchar(500) DEFAULT NULL COMMENT '业务定位键',
  `request_id` varchar(64) DEFAULT NULL COMMENT '请求ID',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(10) DEFAULT NULL COMMENT 'HTTP方法',
  `controller_method` varchar(1000) DEFAULT NULL COMMENT '控制器方法',
  `request_params` text COMMENT '脱敏后的请求参数',
  `exception_class` varchar(500) NOT NULL COMMENT '异常类型',
  `message` text COMMENT '异常消息',
  `stack_trace` mediumtext COMMENT '异常堆栈',
  `ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0-待处理，1-已解决，2-已忽略',
  `occurrence_count` bigint NOT NULL DEFAULT '1' COMMENT '发生次数',
  `first_occurrence_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次发生时间',
  `last_occurrence_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后发生时间',
  `handled_by` bigint DEFAULT NULL COMMENT '处理人ID',
  `handled_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `application_name` varchar(100) DEFAULT NULL COMMENT '应用名称',
  `application_version` varchar(64) DEFAULT NULL COMMENT '应用版本',
  `instance_name` varchar(100) DEFAULT NULL COMMENT '实例名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_error_fingerprint` (`fingerprint`),
  KEY `idx_error_create_time` (`create_time`),
  KEY `idx_error_exception_class` (`exception_class`(191)),
  KEY `idx_error_request_id` (`request_id`),
  KEY `idx_error_trace_id` (`trace_id`),
  KEY `idx_error_source_type` (`source_type`),
  KEY `idx_error_last_occurrence_time` (`last_occurrence_time`),
  KEY `idx_error_status_last_time` (`status`,`last_occurrence_time` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错误日志表';

-- ----------------------------
-- Records of sys_error_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_error_log_occurrence
-- ----------------------------
DROP TABLE IF EXISTS `sys_error_log_occurrence`;
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
  KEY `idx_error_occurrence_log_time` (`error_log_id`,`occurred_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错误日志发生明细表';

-- ----------------------------
-- Records of sys_error_log_occurrence
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
  `uploader_id` bigint DEFAULT NULL COMMENT '上传主体ID',
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
  KEY `idx_file_storage` (`storage_config_id`),
  KEY `idx_file_create_time` (`create_time`),
  KEY `idx_file_uploader` (`uploader_type`,`uploader_id`),
  KEY `idx_sys_file_folder_active_create` (`folder_id`,`is_deleted`,`create_time` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件元数据表';

-- ----------------------------
-- Records of sys_file
-- ----------------------------
BEGIN;
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
INSERT INTO `sys_file_folder` (`id`, `parent_id`, `folder_name`, `sort`, `is_builtin`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES (6, 1, '消息推送', 5, 1, 0, '2026-06-26 15:21:26', 1, '2026-06-27 00:53:29', 1);
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
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
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
INSERT INTO `sys_file_storage_config` (`id`, `config_name`, `storage_type`, `storage_path`, `domain`, `endpoint`, `region`, `bucket_id`, `bucket_name`, `access_key`, `secret_key`, `meta`, `is_default`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `lock_version`) VALUES (1930000000001000, '本地存储', 'LOCAL', '${user.home}/data/uploads', 'http://127.0.0.1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, '', 0, '2026-06-11 16:38:41', 1, '2026-06-27 23:43:39', 1, 0);
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
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (4, 3, '菜单管理', 1, '/system/menu', 'system/menu/list', 'system:menu:query', 'lucide:menu', 4, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:42', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (5, 3, '角色管理', 1, '/system/role', 'system/role/list', 'system:role:query', 'lucide:shield', 2, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:56', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (6, 3, '用户管理', 1, '/system/user', 'system/user/list', 'system:user:query', 'lucide:user', 1, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:23:55', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (7, 3, '部门管理', 1, '/system/dept', 'system/dept/list', 'system:dept:query', 'lucide:building', 3, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:24:02', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (8, 3, '字典管理', 1, '/system/dict', 'system/dict/list', 'system:dict:query', 'lucide:book-open', 5, 1, '2026-05-27 09:40:25', 1, '2026-06-15 18:24:06', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (9, 3, '日志管理', 0, '/system/log', '', NULL, 'mdi:file-document-outline', 11, 1, '2026-05-27 09:40:25', 1, '2026-06-15 07:57:40', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (91, 9, '登录日志', 1, '/system/log/login', 'system/log/login-log/list', 'system:log:login:query', 'ion:log-in-outline', 1, 1, '2026-05-27 09:40:25', 1, '2026-07-03 08:05:25', NULL, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (92, 9, '操作日志', 1, '/system/log/operation', 'system/log/operation-log/list', 'system:log:operation:query', 'carbon:operations-record', 2, 1, '2026-05-27 09:40:25', 1, '2026-07-03 08:05:29', 1, '{\"keepAlive\": true}', 0);
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
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000001, 3, '参数配置', 1, '/system/config', 'system/config/list', 'system:config:query', 'ion:options-outline', 6, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:24:16', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000002, 1930000000000001, '新增', 2, NULL, NULL, 'system:config:create', NULL, 1, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:18', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000003, 1930000000000001, '修改', 2, NULL, NULL, 'system:config:update', NULL, 2, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:14', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000004, 1930000000000001, '删除', 2, NULL, NULL, 'system:config:delete', NULL, 3, 1, '2026-06-02 10:50:59', 1, '2026-06-15 00:08:21', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000012, 3, '版本管理', 1, '/system/version', 'system/version/list', 'system:version:query', 'carbon:version', 7, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:24:18', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000013, 1930000000000012, '新增', 2, NULL, NULL, 'system:version:create', NULL, 1, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:34', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000014, 1930000000000012, '修改', 2, NULL, NULL, 'system:version:update', NULL, 2, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:28', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000000015, 1930000000000012, '删除', 2, NULL, NULL, 'system:version:delete', NULL, 3, 1, '2026-06-02 10:50:59', 1, '2026-06-15 18:19:20', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001010, 3, '系统公告', 1, '/system/notice', 'system/notice/list', 'system:notice:query', 'ion:megaphone-outline', 8, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001011, 1930000000001010, '新增', 2, NULL, NULL, 'system:notice:create', NULL, 1, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001012, 1930000000001010, '修改', 2, NULL, NULL, 'system:notice:update', NULL, 2, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001013, 1930000000001010, '删除', 2, NULL, NULL, 'system:notice:delete', NULL, 3, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001014, 1930000000002000, '消息推送', 1, '/message-center/message', 'system/message/list', 'system:message:query', 'ion:paper-plane-outline', 1, 1, '2026-06-25 20:00:07', 1, '2026-06-26 09:59:51', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001015, 1930000000001014, '新增', 2, NULL, NULL, 'system:message:create', NULL, 1, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001016, 1930000000001014, '修改', 2, NULL, NULL, 'system:message:update', NULL, 2, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001017, 1930000000001014, '删除', 2, NULL, NULL, 'system:message:delete', NULL, 3, 1, '2026-06-25 20:00:07', 1, '2026-06-25 21:34:16', 1, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001020, 2066431140185759745, '错误日志', 1, '/ops/error-log', 'ops/error-log/list', 'ops:error-log:query', 'ion:bug-outline', 2, 1, '2026-06-11 16:38:41', 1, '2026-06-15 18:24:23', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001021, 1930000000001020, '处理', 2, '', '', 'ops:error-log:handle', '', 1, 1, '2026-08-13 00:00:00', 1, NULL, NULL, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001022, 1930000000001020, '删除', 2, '', '', 'ops:error-log:delete', '', 2, 1, '2026-08-13 00:00:00', 1, NULL, NULL, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001030, 3, '文件管理', 1, '/system/file-management', 'system/file-management/list', 'system:file:query', 'ion:folder-open-outline', 10, 1, '2026-06-11 16:38:41', 1, '2026-06-25 21:34:16', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001031, 1930000000001030, '上传', 2, NULL, NULL, 'system:file:upload', NULL, 1, 1, '2026-06-11 16:38:41', 1, '2026-06-15 00:08:45', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001032, 1930000000001030, '删除', 2, NULL, NULL, 'system:file:delete', NULL, 2, 1, '2026-06-11 16:38:41', 1, '2026-06-15 00:08:49', NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001040, 2066431140185759745, '任务调度', 0, '/ops/job', '', NULL, 'ion:timer-outline', 1, 1, '2026-06-11 16:38:41', 1, '2026-06-12 17:32:13', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001041, 1930000000001051, '编辑', 2, '', '', 'ops:job:update', '', 2, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:30:55', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001042, 1930000000001051, '启停', 2, '', '', 'ops:job:operation', '', 3, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:31:03', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001044, 1930000000001051, '执行', 2, '', '', 'ops:job:operation', '', 1, 1, '2026-06-12 17:02:59', 1, '2026-06-15 19:31:07', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001051, 1930000000001040, '任务管理', 1, '/ops/job/list', 'ops/job/list', 'ops:job:query', 'carbon:document-tasks', 1, 1, '2026-06-12 17:32:13', 1, '2026-06-15 19:30:29', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000001052, 1930000000001040, '执行日志', 1, '/ops/job/log', 'ops/job/log', 'ops:job:log:query', 'mdi:sticker-text-outline', 2, 1, '2026-06-12 17:32:13', 1, '2026-06-15 19:30:41', 1, '{\"keepAlive\": true}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002000, 0, '消息中心', 0, '/message-center', '', NULL, 'ion:chatbubbles-outline', 2, 1, '2026-06-26 09:59:51', 1, '2026-06-26 02:58:46', 1, '{}', 0);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`) VALUES (1930000000002001, 1930000000002000, '消息模板', 1, '/message-center/template', 'system/message-template/list', 'system:message:template:query', 'ion:document-text-outline', 2, 1, '2026-06-26 09:59:51', 1, NULL, NULL, '{\"keepAlive\": true}', 0);
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
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消息正文；引用型消息为空，预览时读取来源业务数据',
  `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1-系统消息 2-系统公告 3-版本更新',
  `push_type` tinyint NOT NULL DEFAULT '0' COMMENT '推送方式：0-手动推送 1-定时推送 2-自动推送',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT '来源类型：MANUAL-后台人工推送',
  `source_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源ID',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN_APP' COMMENT '推送通道：IN_APP/SMS/WECHAT_OA/WECHAT_MP',
  `jump_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接',
  `template_id` bigint DEFAULT NULL COMMENT '消息模板ID',
  `template_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '模板参数JSON',
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
  UNIQUE KEY `uk_sys_message_source` (`source_type`,`source_id`,`receiver_type`),
  KEY `idx_sys_message_status_time` (`status`,`is_deleted`,`publish_time`,`create_time`),
  KEY `idx_sys_message_push_type` (`push_type`,`status`,`is_deleted`),
  KEY `idx_sys_message_receiver` (`receiver_type`,`receiver_scope`,`status`,`is_deleted`),
  KEY `idx_sys_message_title` (`title`),
  KEY `idx_sys_message_channel` (`channel`,`status`,`is_deleted`),
  KEY `idx_sys_message_template` (`template_id`,`is_deleted`),
  KEY `idx_sys_message_inbox` (`receiver_type`,`channel`,`status`,`is_deleted`,`publish_time`,`id`),
  KEY `idx_sys_message_manual_create` (`source_type`,`is_deleted`,`create_time` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息推送主体';

-- ----------------------------
-- Records of sys_message
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
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
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
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布 2-已撤回',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `is_pinned` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶 0-否 1-是',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_notice_publish_time` (`publish_time`),
  KEY `idx_notice_order` (`is_pinned`,`sort`),
  KEY `idx_sys_notice_publish_order` (`status`,`is_deleted`,`is_pinned` DESC,`sort`,`publish_time` DESC,`create_time` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
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
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_menu_menu_id` (`menu_id`)
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
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2087450000000001021, 1, 1930000000001021, '2026-08-13 00:00:00', 1);
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES (2087450000000001022, 1, 1930000000001022, '2026-08-13 00:00:00', 1);
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
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_sys_user_avatar_file_id` (`avatar_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_file_id`, `email`, `mobile`, `dept_id`, `status`, `last_online_time`, `last_online_ip`, `last_online_location`, `last_offline_time`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`, `lock_version`) VALUES (1, 'admin718', '$2a$10$ZF6jptfCIC54xN6dF9kuc.OLiKJKSHzEJ7F4pL/e6AyNhkZSv3pL2', '管理员', NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, 0, '2026-05-01 00:00:00', 1, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_login_log`;
CREATE TABLE `sys_user_login_log` (
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
  KEY `idx_login_time` (`login_time`),
  KEY `idx_sys_user_login_status_time_user` (`status`,`login_time`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ----------------------------
-- Records of sys_user_login_log
-- ----------------------------
BEGIN;
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
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户与角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `create_by`) VALUES (2060945171012808705, 1, 1, '2026-05-31 12:43:14', 1);
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
  `status` tinyint(1) DEFAULT '0' COMMENT '状态（0=草稿 1=已发布 2=已撤回）',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `lock_version` int unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_version_version` (`version`),
  KEY `idx_sys_version_publish` (`status`,`publish_time` DESC,`create_time` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统版本日志表';

-- ----------------------------
-- Records of sys_version
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
