# v1 发布诊断清单

本清单用于脚手架首版发布前的最终核对。每项都应保留实际命令、结果和未验证边界，不能用“编译通过”代替运行时验证。

## 代码与依赖

- [ ] `wx-java-bom` 保留，依赖树中的 `redisson` 实际版本为项目显式声明版本。
- [ ] 后端编译、单元测试和 Spotless 检查通过。
- [ ] 前端类型检查、单元测试和生产构建通过。
- [ ] 工作区无误提交的密钥、账号、构建产物或个人数据。

## 数据库与初始数据

- [ ] dev Profile 默认关闭 Flyway；全新库初始化时已显式开启并核对结果。
- [ ] prod Profile 执行迁移前已完成备份、兼容性评审和回滚方案。
- [ ] 初始管理员仅为 `admin718/admin718`，文档明确要求业务项目立即替换。
- [ ] 已发布迁移不再修改；首版发布前合并的 SQL 在全新数据库完整执行成功。

## 安全

- [ ] 上传白名单不包含 HTML、CSS、JavaScript、TypeScript、Vue、XML 等主动内容；SVG 按业务要求允许，并已评估内容清洗或隔离域名方案。
- [ ] 管理端生成密码使用 Web Crypto，不使用 `Math.random()`。
- [ ] 生产 MySQL、Redis 和 JWT 使用独立强密钥，`.env` 权限为 `0600`。
- [ ] Actuator、Druid、登录限流和 Nginx 公网路径符合生产文档。

## 部署与恢复

- [ ] `validate.sh`、Compose 配置解析和所有 Shell 语法检查通过。
- [ ] 发布标识大小写规则、前端目录和 Nginx upstream 切换已实机验证。
- [ ] `backup.sh` 生成 MySQL、Redis RDB、上传目录和 `SHA256SUMS`。
- [ ] `verify-backup.sh` 通过，并在隔离环境完成一次完整恢复演练。
- [ ] 升级失败拒绝切流，旧 release 能在数据库兼容窗口内回滚。

## 运行时边界

- [ ] MySQL、Redis、Quartz JDBC 集群、WebSocket 多实例经过真实环境验证。
- [ ] RocketMQ 仅在业务实际引入 starter 后列为启动依赖并验证。
- [ ] 浏览器完成登录、权限、文件上传下载、消息和任务操作回归。

发布记录至少包含：Git commit、镜像 digest、数据库迁移版本、验证时间、验证人、已知限制和回滚窗口。
