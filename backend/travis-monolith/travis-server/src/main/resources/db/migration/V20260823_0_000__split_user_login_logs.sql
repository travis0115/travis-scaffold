RENAME TABLE `sys_login_log` TO `sys_user_login_log`;

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
    KEY `idx_app_user_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户端用户登录日志表';
