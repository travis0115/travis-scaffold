# Implementation Plan

- [x] 1. 修复消息查询与输入校验
  - 接入管理端发布日期条件。
  - 限制并规范化接收 ID，校验人工定时时间。
  - 补请求与 service 测试。
  - _Requirements: 2, 3, 4_

- [x] 2. 串行化消息生命周期
  - 增加消息行锁查询。
  - 将编辑、发布、撤回、删除、来源生命周期和 Quartz 发送统一接入行锁。
  - 强化定时发送 CAS 条件并补竞态回归测试。
  - _Requirement: 1_

- [x] 3. 增加 message 缓存一致性兜底
  - 支持按缓存名 TTL。
  - 配置 message 缓存 TTL，并避免缓存解析后的文件 URL。
  - 补缓存行为测试。
  - _Requirement: 5_

- [x] 4. 将收件箱批量操作改为有界游标处理
  - Mapper 增加限量游标查询。
  - 修改全部已读和清空流程并补测试。
  - _Requirement: 6_

- [x] 5. 完善 Quartz 双向对账
  - 清理 message 任务组内孤儿 Job。
  - 保留缺失与时间漂移修复，补 Quartz 测试。
  - _Requirement: 7_

- [x] 6. 优化 WebSocket 收件箱通知
  - 基础设施增加 namespace 广播。
  - message 模块移除逐用户角色/部门匹配。
  - 补发送范围测试。
  - _Requirement: 8_

- [x] 7. 收尾契约与文档
  - 修正前端局部 ID 类型。
  - 修正 SQL 状态注释。
  - _Requirements: 9, 10_

- [x] 8. 完成回归验证
  - 运行 system 模块测试、聚焦格式检查、admin 类型检查和模块边界检查。
  - 复核工作区差异只包含本任务文件。
  - _Requirement: 11_
