-- 错误日志平台类型统一通过系统字典维护展示文案。
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES (2088600000000000005, '错误日志平台类型', 'ops_error_log_platform_type', 20, 1, 'ADMIN-管理端 APP-客户端 SYSTEM-系统任务', 0, '2026-08-16 00:00:00', 1, NULL, NULL);

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES
  (2088600000000000006, 2088600000000000005, '管理端', 'ADMIN', 'primary', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000007, 2088600000000000005, '客户端', 'APP', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000008, 2088600000000000005, '系统任务', 'SYSTEM', 'default', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
