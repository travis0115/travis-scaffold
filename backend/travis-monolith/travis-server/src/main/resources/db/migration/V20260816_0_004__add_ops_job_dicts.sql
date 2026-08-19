-- 定时任务调度类型统一通过系统字典维护文案与标签样式。
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES (2088600000000000009, '定时任务调度类型', 'ops_job_schedule_type', 21, 1, 'CRON-Cron表达式 INTERVAL-固定间隔 ONCE-单次执行', 0, '2026-08-16 00:00:00', 1, NULL, NULL);

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES
  (2088600000000000010, 2088600000000000009, 'Cron表达式', 'CRON', 'primary', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000011, 2088600000000000009, '固定间隔', 'INTERVAL', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000012, 2088600000000000009, '单次执行', 'ONCE', 'warning', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);

-- 定时任务执行结果统一通过系统字典维护文案与标签样式。
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES (2088600000000000013, '定时任务执行结果', 'ops_job_execution_status', 22, 1, '0-执行中 1-成功 2-失败', 0, '2026-08-16 00:00:00', 1, NULL, NULL);

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `label`, `value`, `tag_style`, `sort`, `status`, `remark`, `is_deleted`, `create_time`, `create_by`, `update_time`, `update_by`)
VALUES
  (2088600000000000014, 2088600000000000013, '执行中', '0', 'processing', 1, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000015, 2088600000000000013, '成功', '1', 'success', 2, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL),
  (2088600000000000016, 2088600000000000013, '失败', '2', 'danger', 3, 1, NULL, 0, '2026-08-16 00:00:00', 1, NULL, NULL);
