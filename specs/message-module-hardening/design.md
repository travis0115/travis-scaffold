# Message 模块修复设计

## 1. 并发状态机

为 `sys_message` 增加按主键及按来源查询的 `SELECT ... FOR UPDATE` Mapper 方法。所有会改变既有消息状态的方法在事务内先锁定消息行，包括人工编辑/推送/撤回/删除、来源发布/撤回/删除和 Quartz 到期发送。

保留现有 `claimForPublish` 作为发送占用的第二层保护，并让定时发送占用同时校验 `push_type = SCHEDULED` 与读取到的原发布时间，防止陈旧任务发送重排后的消息。该方案不新增版本列，也不需要数据库迁移。

## 2. 查询与校验

- 管理端分页增加 `publish_time >= start.atStartOfDay()` 和 `publish_time < end.plusDays(1).atStartOfDay()`。
- 三类消息请求的接收 ID 增加最大数量、非空元素和正数约束；service 再做防御性校验、去重，并在 `ALL` 范围清空接收值。
- 后台用户、角色、部门范围在现有公开 API 能力内校验存在性；app 用户不允许 system 模块反向依赖 app 模块，因此只做结构校验，由 app 侧公开 API 未来补充存在性能力。
- 人工定时消息要求发布时间严格晚于当前时间；来源消息仍允许过去时间表达立即发布。

## 3. 缓存

- 为 Redis CacheManager 增加可配置的按缓存名 TTL，不改变其他缓存默认行为；模板详情和未读数配置有限 TTL。
- 人工消息详情不再缓存，避免缓存解析后的文件 URL；详情读取保持数据库实时状态。
- 保留事务感知失效；全局未读失效暂保留，但有限 TTL 防止漏失效永久化。

## 4. 收件箱批处理与查询

`markAllRead/clear` 改为按固定大小重复查询一批 ID 并 upsert，直到没有新记录。查询使用稳定的 ID 游标，避免 offset 在状态更新后漂移。

现有 JSON 接收范围不做数据模型迁移；通过接收 ID 上限、复合索引现状和批处理限制控制当前风险。

## 5. Quartz 对账

对账先读取全部有效待发送消息并维护预期 JobKey 集合，再遍历 `system-message` 组的 JobKey，删除不在预期集合中的任务。仍保留分布式锁和 Redis 分钟槽。

## 6. WebSocket

在 WebSocket 基础设施增加按 namespace 广播能力。message 模块只按 `admin/app` namespace 发送“收件箱可能变化”事件，不再枚举所有连接并逐用户查询角色/部门。事件不携带敏感正文，收件箱 HTTP 查询继续执行最终可见性判断。

## 7. 测试

- Service/Mapper 单元测试：日期条件、接收值规范化、过去时间、行锁调用、陈旧发布时间占用。
- Receiver 测试：游标批处理终止和分批行为。
- Quartz 测试：缺失/漂移任务修复和孤儿任务删除。
- 前端：独立 `travis-admin` 类型检查。
- 后端：system 模块测试、聚焦 Spotless；MySQL/Redis/JDBC JobStore 未启动时明确报告未验证边界。
