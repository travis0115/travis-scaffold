-- travis-scaffold 平台能力增量脚本
-- 基线：/Users/travis/Desktop/travis_monolith.sql
-- 日期：2026-06-11

SET NAMES utf8mb4;

-- 1. 更新日志完整迁移为版本日志（保留原数据）
RENAME TABLE `sys_update_log` TO `sys_version_log`;
ALTER TABLE `sys_version_log` COMMENT = '系统版本日志表';

UPDATE `sys_menu`
SET `menu_name` = '版本日志',
    `path` = '/system/version-log',
    `component` = 'system/version-log/list',
    `perms` = 'system:versionLog:list'
WHERE `id` = 1930000000000012;
UPDATE `sys_menu` SET `perms` = 'system:versionLog:add' WHERE `id` = 1930000000000013;
UPDATE `sys_menu` SET `perms` = 'system:versionLog:edit' WHERE `id` = 1930000000000014;
UPDATE `sys_menu` SET `perms` = 'system:versionLog:delete' WHERE `id` = 1930000000000015;

-- 2. 字典项展示样式
ALTER TABLE `sys_dict_item`
    ADD COLUMN `tag_style` varchar(20) DEFAULT 'default' COMMENT '展示样式' AFTER `value`;

-- 3. 通知公告
CREATE TABLE IF NOT EXISTS `sys_notice` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `title` varchar(200) NOT NULL COMMENT '公告标题',
    `content` text NOT NULL COMMENT '公告内容',
    `notice_type` tinyint NOT NULL DEFAULT 1 COMMENT '类型 1-通知 2-公告',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布',
    `audience_type` tinyint NOT NULL DEFAULT 0 COMMENT '接收范围 0-全部用户 1-指定用户 2-指定角色 3-指定部门',
    `target_ids` text DEFAULT NULL COMMENT '接收对象ID，逗号分隔',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NOT NULL COMMENT '创建人ID',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_notice_status` (`status`),
    KEY `idx_notice_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS `sys_user_message` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `notice_id` bigint NOT NULL COMMENT '公告ID',
    `user_id` bigint NOT NULL COMMENT '接收用户ID',
    `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态 0-未读 1-已读',
    `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NOT NULL COMMENT '创建人ID',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
    KEY `idx_message_user_read` (`user_id`, `read_status`),
    KEY `idx_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户消息表';

-- 4. 操作日志
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
    `username` varchar(100) DEFAULT NULL COMMENT '操作用户名',
    `description` varchar(200) NOT NULL COMMENT '操作描述',
    `module` varchar(100) NOT NULL COMMENT '业务模块',
    `method` varchar(500) DEFAULT NULL COMMENT '请求方法全限定名',
    `request_url` varchar(500) DEFAULT NULL COMMENT '请求地址',
    `request_method` varchar(20) DEFAULT NULL COMMENT 'HTTP请求方法',
    `request_params` text DEFAULT NULL COMMENT '请求参数',
    `response_result` text DEFAULT NULL COMMENT '响应结果',
    `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
    `duration` bigint DEFAULT NULL COMMENT '执行耗时毫秒',
    `status` tinyint NOT NULL COMMENT '操作状态：0-失败 1-成功',
    `error_msg` text DEFAULT NULL COMMENT '错误信息',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operation_user_id` (`user_id`),
    KEY `idx_operation_module` (`module`),
    KEY `idx_operation_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后台操作日志表';

-- 5. 错误日志
CREATE TABLE IF NOT EXISTS `sys_error_log` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `user_id` bigint DEFAULT NULL COMMENT '登录用户ID',
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
    KEY `idx_error_exception_class` (`exception_class`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后端错误日志表';

-- 6. 文件存储服务、文件夹与文件元数据
CREATE TABLE IF NOT EXISTS `sys_file_storage_config` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `config_name` varchar(100) NOT NULL COMMENT '配置名称',
    `storage_type` varchar(30) NOT NULL COMMENT '存储类型',
    `base_path` varchar(500) NOT NULL COMMENT '本地存储目录',
    `access_prefix` varchar(200) NOT NULL COMMENT '访问路径前缀',
    `domain` varchar(500) DEFAULT NULL COMMENT '访问域名',
    `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NOT NULL COMMENT '创建人ID',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_storage_default` (`is_default`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件存储配置表';

CREATE TABLE IF NOT EXISTS `sys_file_folder` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父文件夹ID',
    `folder_name` varchar(100) NOT NULL COMMENT '文件夹名称',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NOT NULL COMMENT '创建人ID',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_file_folder_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分类文件夹表';

CREATE TABLE IF NOT EXISTS `sys_file` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `folder_id` bigint DEFAULT NULL COMMENT '文件夹ID',
    `storage_config_id` bigint NOT NULL COMMENT '存储配置ID',
    `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
    `original_name` varchar(500) DEFAULT NULL COMMENT '原始文件名',
    `path` varchar(1000) NOT NULL COMMENT '相对路径',
    `url` varchar(1500) NOT NULL COMMENT '访问地址',
    `extension` varchar(50) DEFAULT NULL COMMENT '扩展名',
    `mime_type` varchar(200) DEFAULT NULL COMMENT 'MIME类型',
    `size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NOT NULL COMMENT '创建人ID',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_file_folder` (`folder_id`),
    KEY `idx_file_storage` (`storage_config_id`),
    KEY `idx_file_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件元数据表';

INSERT IGNORE INTO `sys_file_storage_config`
(`id`, `config_name`, `storage_type`, `base_path`, `access_prefix`, `domain`, `is_default`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`)
VALUES
(1930000000001000, '默认本地存储', 'LOCAL', '${user.home}/data/uploads', '/files', NULL, 1, 1, '与 travis.web.file 默认配置一致', 0, NOW(), 1);

-- 6. 菜单
INSERT IGNORE INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `meta`, `is_deleted`)
VALUES
(1930000000001010, 3, '通知公告', 1, '/system/notice', 'system/notice/list', 'system:notice:list', 'ion:notifications-outline', 20, 1, NOW(), 1, NULL, 0),
(1930000000001011, 1930000000001010, '新增公告', 2, NULL, NULL, 'system:notice:add', NULL, 1, 1, NOW(), 1, NULL, 0),
(1930000000001012, 1930000000001010, '编辑公告', 2, NULL, NULL, 'system:notice:edit', NULL, 2, 1, NOW(), 1, NULL, 0),
(1930000000001013, 1930000000001010, '删除公告', 2, NULL, NULL, 'system:notice:delete', NULL, 3, 1, NOW(), 1, NULL, 0),
(1930000000001020, 1930000000000010, '错误日志', 1, '/system/logs/error-log', 'system/error-log/list', 'system:errorLog:list', 'ion:bug-outline', 3, 1, NOW(), 1, NULL, 0),
(1930000000001030, 3, '文件管理', 1, '/system/file-management', 'system/file-management/list', 'system:file:list', 'ion:folder-open-outline', 21, 1, NOW(), 1, NULL, 0),
(1930000000001031, 1930000000001030, '上传文件', 2, NULL, NULL, 'system:file:upload', NULL, 1, 1, NOW(), 1, NULL, 0),
(1930000000001032, 1930000000001030, '删除文件', 2, NULL, NULL, 'system:file:delete', NULL, 2, 1, NOW(), 1, NULL, 0),
(1930000000001050, 3, '我的消息', 1, '/system/my-message', 'system/my-message/list', 'system:message:list', 'ion:mail-unread-outline', 22, 1, NOW(), 1, NULL, 0),
(1930000000001040, 0, '任务调度', 1, '/ops/job', 'ops/job/index', 'ops:job:entry', 'ion:timer-outline', 30, 1, NOW(), 1, NULL, 0);

-- 7. 默认授予管理员角色
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`) VALUES
(1930000000001101, 1, 1930000000001010, NOW(), 1),
(1930000000001102, 1, 1930000000001011, NOW(), 1),
(1930000000001103, 1, 1930000000001012, NOW(), 1),
(1930000000001104, 1, 1930000000001013, NOW(), 1),
(1930000000001105, 1, 1930000000001020, NOW(), 1),
(1930000000001106, 1, 1930000000001030, NOW(), 1),
(1930000000001107, 1, 1930000000001031, NOW(), 1),
(1930000000001108, 1, 1930000000001032, NOW(), 1),
(1930000000001109, 1, 1930000000001040, NOW(), 1);

-- “我的消息”属于所有后台用户的个人能力，默认授予现有全部角色
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
SELECT 1930000000010000 + `id`, `id`, 1930000000001050, NOW(), 1
FROM `sys_role`
WHERE `is_deleted` = 0;
