# 前端配置

Travis Admin 的环境变量位于 `frontend/admin-vben/apps/travis-admin/.env*`。`backend-mock`、`playground` 下的变量属于 Vben 上游调试应用，不是管理端生产配置。

## 配置文件

| 文件 | 用途 |
| --- | --- |
| `.env` | 所有模式共享的应用、路由、上传和业务开关 |
| `.env.development` | 开发端口、API、WebSocket、Mock 和 DevTools |
| `.env.production` | 生产 API、WebSocket、压缩、PWA 和产物归档 |
| `.env.analyze` | 依赖分析构建，不用于部署 |
| `.env.development.local` | 本机覆盖，已被 Git 忽略，适合直连本地后端 |

## 基础变量

| 变量 | 当前值/作用 |
| --- | --- |
| `VITE_APP_TITLE` | 页面标题 `Travis Admin` |
| `VITE_APP_NAMESPACE` | 本地存储和应用命名空间 `travis-admin` |
| `VITE_APP_STORE_SECURE_KEY` | 本地持久化加密 key，正式项目应更换 |
| `VITE_BASE` | Vite 部署基础路径 `/` |
| `VITE_ROUTER_HISTORY` | `hash` 路由；切换模式时需要网关支持 SPA 回退 |
| `VITE_INJECT_APP_LOADING` | 是否注入首屏 loading |
| `VITE_UPLOAD_FILE_MAX_SIZE` | 前端上传提示上限 `20` MB，不替代后端限制 |
| `VITE_BIZ_DEPT_ENABLED` | 是否启用部门相关业务 UI |

## 开发变量

| 变量 | 当前值/作用 |
| --- | --- |
| `VITE_PORT` | 开发服务器端口 `5999` |
| `VITE_GLOB_API_URL` | `http://localhost/api/admin`，默认假设前面有本地网关 |
| `VITE_GLOB_WS_URL` | `ws://localhost/ws/admin` |
| `VITE_COMPRESS` | `none` |
| `VITE_NITRO_MOCK` | Nitro Mock 开关，当前 `false` |
| `VITE_DEVTOOLS` | Vue DevTools，当前 `true` |

直接连接快速开始中的本地后端时，创建 `.env.development.local`：

```dotenv
VITE_GLOB_API_URL=http://localhost:8082/api/admin
VITE_GLOB_WS_URL=ws://localhost:8082/ws/admin
```

## 生产变量

`.env.production` 当前仍使用示例 `localhost` 的 HTTPS/WSS 地址，部署前必须替换成真实网关：

| 变量 | 当前作用 |
| --- | --- |
| `VITE_GLOB_API_URL` | 管理端 API 根地址 |
| `VITE_GLOB_WS_URL` | 管理端 WebSocket 地址，HTTPS 页面应使用 WSS |
| `VITE_COMPRESS` | 当前为 `gzip` |
| `VITE_PWA` | 当前为 `false` |
| `VITE_ARCHIVER` | 当前为 `true`，构建后生成归档 |

## 修改检查表

1. API 地址是否已经包含 `/api/admin`，避免请求路径重复或缺失前缀；
2. WebSocket 协议、域名和 `/ws/admin` 是否与后端/网关一致；
3. `VITE_BASE` 与部署子路径、静态资源路径是否一致；
4. 上传大小是否同时核对后端 Multipart 和扩展名白名单；
5. 新增环境变量后是否补充类型声明和本文档。

后端端口、鉴权和 WebSocket 配置见 [后端配置](../backend/configuration.md)。
