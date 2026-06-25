-- 通知公告拆分为系统公告 + 消息推送
-- 适用基线：当前库已存在 sys_notice、sys_user_message、sys_menu、sys_role_menu。

START TRANSACTION;

-- 1. 原通知公告发布表迁移为消息推送源表。
RENAME TABLE `sys_notice` TO `sys_message`;

ALTER TABLE `sys_message`
    CHANGE COLUMN `notice_type` `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型 1-系统消息 2-业务消息',
    ADD COLUMN `source_type` varchar(50) DEFAULT NULL COMMENT '来源类型，预留业务来源入口' AFTER `message_type`,
    ADD COLUMN `source_id` varchar(100) DEFAULT NULL COMMENT '来源业务ID，预留业务来源入口' AFTER `source_type`,
    ADD COLUMN `channels` varchar(100) NOT NULL DEFAULT 'IN_APP' COMMENT '推送通道，逗号分隔，当前仅站内信' AFTER `source_id`,
    DROP INDEX `idx_notice_status`,
    DROP INDEX `idx_notice_publish_time`,
    ADD INDEX `idx_message_status` (`status`),
    ADD INDEX `idx_message_publish_time` (`publish_time`),
    ADD INDEX `idx_message_source` (`source_type`, `source_id`);

ALTER TABLE `sys_message` COMMENT = '消息推送源表';

-- 2. 原用户消息表迁移为消息接收表，receiver_type 为未来前端用户体系预留。
RENAME TABLE `sys_user_message` TO `sys_message_receiver`;

ALTER TABLE `sys_message_receiver`
    CHANGE COLUMN `notice_id` `message_id` bigint NOT NULL COMMENT '消息ID',
    CHANGE COLUMN `user_id` `receiver_id` bigint NOT NULL COMMENT '接收人ID',
    ADD COLUMN `receiver_type` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '接收人体系 ADMIN/APP/MEMBER 等' AFTER `message_id`,
    DROP INDEX `uk_notice_user`,
    DROP INDEX `idx_message_user_read`,
    ADD UNIQUE KEY `uk_message_receiver` (`message_id`, `receiver_type`, `receiver_id`),
    ADD INDEX `idx_message_receiver_read` (`receiver_type`, `receiver_id`, `read_status`);

ALTER TABLE `sys_message_receiver` COMMENT = '消息接收表';

-- 3. 新建系统公告表。公告是内容发布，不生成个人收件记录。
CREATE TABLE IF NOT EXISTS `sys_announcement` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `pinned` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶 0-否 1-是',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_announcement_status` (`status`),
  KEY `idx_announcement_publish_time` (`publish_time`),
  KEY `idx_announcement_order` (`pinned`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

-- 4. 菜单：原“通知公告”菜单改为“系统公告”，新增“消息推送”菜单。
UPDATE `sys_menu`
SET `menu_name` = '系统公告',
    `path` = '/system/announcement',
    `component` = 'system/announcement/list',
    `perms` = 'system:announcement:query',
    `icon` = 'ion:megaphone-outline',
    `sort` = 8,
    `update_time` = NOW(),
    `update_by` = 1
WHERE `id` = 1930000000001010;

UPDATE `sys_menu`
SET `perms` = 'system:announcement:create', `update_time` = NOW(), `update_by` = 1
WHERE `id` = 1930000000001011;

UPDATE `sys_menu`
SET `perms` = 'system:announcement:update', `update_time` = NOW(), `update_by` = 1
WHERE `id` = 1930000000001012;

UPDATE `sys_menu`
SET `perms` = 'system:announcement:delete', `update_time` = NOW(), `update_by` = 1
WHERE `id` = 1930000000001013;

UPDATE `sys_menu`
SET `sort` = 10, `update_time` = NOW(), `update_by` = 1
WHERE `id` = 1930000000001030;

INSERT INTO `sys_menu`
    (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `status`, `create_time`, `create_by`, `update_time`, `update_by`, `meta`, `is_deleted`)
VALUES
    (1930000000001014, 3, '消息推送', 1, '/system/message/push', 'system/message/push/list', 'system:message:query', 'ion:paper-plane-outline', 9, 1, NOW(), 1, NOW(), 1, '{}', 0),
    (1930000000001015, 1930000000001014, '新增', 2, NULL, NULL, 'system:message:create', NULL, 1, 1, NOW(), 1, NULL, NULL, NULL, 0),
    (1930000000001016, 1930000000001014, '修改', 2, NULL, NULL, 'system:message:update', NULL, 2, 1, NOW(), 1, NULL, NULL, NULL, 0),
    (1930000000001017, 1930000000001014, '删除', 2, NULL, NULL, 'system:message:delete', NULL, 3, 1, NOW(), 1, NULL, NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`),
    `sort` = VALUES(`sort`),
    `status` = VALUES(`status`),
    `update_time` = NOW(),
    `update_by` = 1,
    `is_deleted` = 0;

-- 5. 角色授权：拥有原系统公告菜单的角色，默认补上消息推送菜单权限。
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
SELECT 2070100000001014000 + `role_id` * 100 + 14, `role_id`, 1930000000001014, NOW(), 1
FROM `sys_role_menu` src
WHERE src.`menu_id` = 1930000000001010
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` dst
      WHERE dst.`role_id` = src.`role_id` AND dst.`menu_id` = 1930000000001014
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
SELECT 2070100000001015000 + `role_id` * 100 + 15, `role_id`, 1930000000001015, NOW(), 1
FROM `sys_role_menu` src
WHERE src.`menu_id` = 1930000000001011
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` dst
      WHERE dst.`role_id` = src.`role_id` AND dst.`menu_id` = 1930000000001015
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
SELECT 2070100000001016000 + `role_id` * 100 + 16, `role_id`, 1930000000001016, NOW(), 1
FROM `sys_role_menu` src
WHERE src.`menu_id` = 1930000000001012
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` dst
      WHERE dst.`role_id` = src.`role_id` AND dst.`menu_id` = 1930000000001016
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_by`)
SELECT 2070100000001017000 + `role_id` * 100 + 17, `role_id`, 1930000000001017, NOW(), 1
FROM `sys_role_menu` src
WHERE src.`menu_id` = 1930000000001013
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` dst
      WHERE dst.`role_id` = src.`role_id` AND dst.`menu_id` = 1930000000001017
  );

COMMIT;
