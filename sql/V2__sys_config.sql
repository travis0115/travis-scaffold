-- =====================================================
-- sys_config 系统配置表
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_config (
  id          BIGINT        NOT NULL          COMMENT '主键ID（雪花算法）',
  config_group VARCHAR(100) DEFAULT ''        COMMENT '配置分组',
  config_key  VARCHAR(200)  NOT NULL          COMMENT '配置键（唯一标识）',
  config_value TEXT                             COMMENT '配置值',
  remark      VARCHAR(500)  DEFAULT ''        COMMENT '备注',
  create_time DATETIME      DEFAULT NULL      COMMENT '创建时间',
  create_by   BIGINT        DEFAULT NULL      COMMENT '创建人ID',
  update_time DATETIME      DEFAULT NULL      COMMENT '更新时间',
  update_by   BIGINT        DEFAULT NULL      COMMENT '更新人ID',
  is_deleted  TINYINT(1)    DEFAULT 0         COMMENT '逻辑删除（0=未删除 1=已删除）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================
-- 初始化配置数据（可选）
-- =====================================================

INSERT INTO sys_config (id, config_group, config_key, config_value, remark, create_time, is_deleted) VALUES
(1930000000000101, 'system', 'site_name', 'Travis Admin', '系统名称', NOW(), 0),
(1930000000000102, 'system', 'site_logo', '', '系统Logo路径', NOW(), 0),
(1930000000000103, 'system', 'site_copyright', 'Copyright © 2026 Travis', '版权信息', NOW(), 0);
