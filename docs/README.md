# Travis Scaffold 文档

这是项目文档的唯一总入口。第一次使用不需要逐个翻文件，按下面的顺序阅读即可。

## 第一次使用

1. [快速开始](getting-started.md)：准备环境，启动 MySQL、Redis、RocketMQ、后端和管理端；
2. [项目能力总览](project-overview.md)：了解脚手架已经具备什么、哪些公共入口应直接复用；
3. 根据开发方向进入 [后端文档](backend/README.md) 或 [前端文档](frontend/README.md)。

准备生产环境时，再阅读 [单机双实例生产部署](production-deployment.md)。

## 我应该去哪里

| 需求 | 入口 |
| --- | --- |
| 启动项目或排查本地环境 | [快速开始](getting-started.md) |
| 查看整体架构、业务功能和能力边界 | [项目能力总览](project-overview.md) |
| 后端模块、starter、配置、文件、消息、Quartz | [后端文档](backend/README.md) |
| 管理端配置、请求、表单、表格、权限和字典 | [前端文档](frontend/README.md) |
| 查看后端全部 yml 和环境变量 | [后端配置](backend/configuration.md) |
| 查看管理端 `.env*` | [前端配置](frontend/configuration.md) |
| 部署 HTTPS、双实例、备份和滚动升级 | [生产部署](production-deployment.md) |

## 目录结构

```text
docs/
├── README.md                 # 唯一总入口
├── getting-started.md        # 从零启动
├── production-deployment.md  # 跨前后端生产部署
├── project-overview.md       # 项目能力总览
├── backend/
│   ├── README.md             # 后端二级目录
│   └── ...                   # 后端专题
└── frontend/
    ├── README.md             # 前端二级目录
    └── ...                   # 前端专题
```

## 文档维护约定

- 根目录只放跨前后端的入门和总览，不再堆放具体技术专题。
- 后端和前端专题分别放入对应目录，并从各自 `README.md` 提供入口。
- 文件名使用英文小写 kebab-case；每个新增公共能力必须更新所属目录。
- 文档说明当前可用能力和稳定公共入口，预留枚举、注释或未实现策略不能写成已支持功能。
- 业务模块之间只依赖公开 `api`、`event` 或 `@NamedInterface`，不依赖其他模块的 `internal` 包。
