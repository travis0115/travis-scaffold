# 前端文档

管理端位于 `frontend/admin-vben/apps/travis-admin`，基于 Vue 3、Vite 和 Vben Admin 的 pnpm workspace。

## 建议阅读顺序

1. [前端配置](configuration.md)：理解 `.env*`、API、WebSocket、上传和构建变量；
2. [管理端开发约定](development.md)：使用请求、表单、VXE Grid、权限、字典和公共组件；
3. 涉及接口契约时回到 [后端文档](../backend/README.md) 核对 DTO、权限和业务边界。

## 不要重复封装

新增页面前先搜索：

- `requestClient` 和现有 API 模块；
- `useVbenForm`、`useVbenVxeGrid`；
- `CellOperation`、状态开关和公共 renderer；
- `v-access`、`AccessControl` 和路由权限；
- 字典、空值、图片、头像等共享工具。

前端隐藏按钮不是安全边界，后端仍必须校验权限。

返回 [文档总入口](../README.md)。
