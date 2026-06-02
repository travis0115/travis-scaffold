-- =====================================================
-- 新增菜单记录初始化
-- 包含：系统配置、登录日志、更新日志菜单
-- =====================================================

-- --------------------------------------------------
-- 1. 系统配置菜单
-- --------------------------------------------------

-- 系统配置菜单（挂在系统管理目录下，parentId 需替换为实际系统管理目录ID）
-- 此处假设系统管理目录的 ID 通过查询获取，如不确定可直接使用固定 ID
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time)
VALUES (1930000000000001, 0, '系统配置', '/system/config', 'system/config/list', 'system:config:list', 1, 'ion:options-outline', 9998, 1, NOW());

-- 系统配置按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time) VALUES
(1930000000000002, 1930000000000001, '新增配置', NULL, NULL, 'system:config:add', 2, NULL, 1, 1, NOW()),
(1930000000000003, 1930000000000001, '编辑配置', NULL, NULL, 'system:config:edit', 2, NULL, 2, 1, NOW()),
(1930000000000004, 1930000000000001, '删除配置', NULL, NULL, 'system:config:delete', 2, NULL, 3, 1, NOW());

-- --------------------------------------------------
-- 2. 日志管理目录及子菜单
-- --------------------------------------------------

-- 日志管理目录（顶级目录）
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time)
VALUES (1930000000000010, 0, '日志管理', '/system/logs', NULL, NULL, 0, 'ion:document-text-outline', 9999, 1, NOW());

-- 登录日志菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time)
VALUES (1930000000000011, 1930000000000010, '登录日志', '/system/logs/login-log', 'system/loginLog/list', 'system:loginLog:list', 1, 'ion:log-in-outline', 1, 1, NOW());

-- 更新日志管理菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time)
VALUES (1930000000000012, 1930000000000010, '更新日志', '/system/logs/update-log', 'system/updateLog/list', 'system:updateLog:list', 1, 'ion:newspaper-outline', 2, 1, NOW());

-- 更新日志按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort, status, create_time) VALUES
(1930000000000013, 1930000000000012, '新增日志', NULL, NULL, 'system:updateLog:add', 2, NULL, 1, 1, NOW()),
(1930000000000014, 1930000000000012, '编辑日志', NULL, NULL, 'system:updateLog:edit', 2, NULL, 2, 1, NOW()),
(1930000000000015, 1930000000000012, '删除日志', NULL, NULL, 'system:updateLog:delete', 2, NULL, 3, 1, NOW());

-- --------------------------------------------------
-- 3. 为 admin 角色关联所有新增菜单
-- 说明：以下 SQL 通过子查询找到 role_code='admin' 的角色，
--       为其插入所有新增菜单的关联记录
-- --------------------------------------------------

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
SELECT
    -- 使用简单递增 ID（生产环境建议用雪花算法）
    1930000000000201 + (@rownum := @rownum + 1),
    r.id,
    m.id,
    NOW()
FROM sys_role r
CROSS JOIN (
    SELECT id FROM sys_menu WHERE id IN (
        1930000000000001, 1930000000000002, 1930000000000003, 1930000000000004,
        1930000000000010, 1930000000000011, 1930000000000012,
        1930000000000013, 1930000000000014, 1930000000000015
    )
) m
CROSS JOIN (SELECT @rownum := 0) t
WHERE r.role_code = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );
