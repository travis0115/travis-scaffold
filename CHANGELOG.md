# Changelog

本项目的重要变更记录在此文件。版本格式遵循语义化版本，日期使用 `YYYY-MM-DD`。

## [Unreleased]

### Added

- 首版模块化单体脚手架、管理端和单机多实例部署脚本。
- MySQL、Redis、Quartz、WebSocket、文件、系统管理与运维治理基础能力。
- 可选的 RocketMQ 基础设施封装；业务模块接入对应 starter 后生效。

### Security

- 上传白名单默认禁止 HTML、脚本、样式、Vue、SVG、XML 等主动内容。
- 管理端随机密码改用 Web Crypto 安全随机源。

### Changed

- 开发环境默认关闭 Flyway，生产环境仍在启动时执行已发布迁移。
- 本地初始管理员账号和密码统一为 `admin718`。

## [1.0.0] - 2026-08-25

- Travis Scaffold 第一个可发布版本。
