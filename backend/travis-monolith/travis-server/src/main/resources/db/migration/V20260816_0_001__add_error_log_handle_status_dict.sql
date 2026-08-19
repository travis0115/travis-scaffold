-- 错误日志处理状态统一通过系统字典维护文案与标签样式。
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES (2088600000000000001, '错误日志处理状态', 'ops_error_log_handle_status', 19, 1, '0-待处理 1-已解决 2-已忽略', 0, '2026-08-16 00:00:00', 1, NULL, NULL);

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES
  (2088600000000000002, 2088600000000000001, '待处理', '0', 'warning', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000003, 2088600000000000001, '已解决', '1', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000004, 2088600000000000001, '已忽略', '2', 'default', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
