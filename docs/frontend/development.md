# 管理端开发约定

本文只说明 `apps/travis-admin` 的项目自有约定。Vben 通用组件与包结构继续参考 `frontend/admin-vben/README.zh-CN.md` 及各 package README。

## 目录职责

| 目录 | 放什么 |
| --- | --- |
| `src/api` | 后端接口函数、请求/响应 TypeScript 类型 |
| `src/adapter` | 项目统一表单组件映射、VXE Table renderer 与默认行为 |
| `src/utils` | 权限、字典、业务选项、树等跨页面工具 |
| `src/views/system` | system 管理页面 |
| `src/views/ops` | ops 管理页面 |
| `src/views/_core` | 与框架布局直接关联的登录、个人中心、消息、公告、版本页面 |
| `src/store` | 应用级 Pinia store |

新增功能优先参考同类型现有页面：普通 CRUD 看 config/role，树表看 dept/menu，主从结构看 dict，独立编辑页看 message，复杂治理页看 ops/error-log 或 ops/job。

## API 请求

业务 API 统一使用 `requestClient`：

```ts
export function getOrderPage(params: OrderPageReq) {
  return requestClient.get<PageResp<Order>>('/order/page', { params });
}
```

它已经处理：

- `Authorization: Bearer ...`；
- `Accept-Language` 与 `Client-Type: web`；
- 后端 `{ code, msg, data }`，成功码为字符串 `'200'`；
- HTTP 401 和业务 code `'401'` 的重新认证；
- 服务端错误消息；
- 默认全屏 loading、`loading: 'nprogress'` 和 `loading: false`；
- 默认错误弹窗、`errorMessageType: 'message'` 和 `errorMessageType: false`。

`baseRequestClient` 不带上述业务响应处理，当前主要供登录等基础认证边界使用。普通页面不要使用它绕开统一错误处理。

后端 Long 默认作为 JSON 字符串返回，因此 ID 类型使用公共 `Id = number | string`，比较、路由和 Map key 时不要假设一定是 number。

上传使用 `FormData`，进度回调放在 API 函数中统一适配。上传前先读取后端 upload policy，不要把扩展名和大小仅硬编码在页面。

## API 类型组织

每个业务文件用 namespace 收拢类型，并同时导出接口函数。分页统一使用 `PageResp<T>`：

```ts
export namespace OrderApi {
  export interface Order {
    id: Id;
    orderNo: string;
  }

  export interface PageReq {
    pageNum?: number;
    pageSize?: number;
  }
}
```

DTO 字段要与后端 request/response、SQL projection 和页面消费保持一致。页面隐藏字段不等于后端不接受或不返回字段，安全边界仍在后端。

## 表单

从项目 adapter 引入：

```ts
import { useVbenForm, z } from '#/adapter/form';
```

adapter 已配置 Ant Design Vue 的 model prop、默认 label width，以及 `required`、`selectRequired` 国际化规则。不要从底层包直接创建第二套初始化配置。

常见结构：

```ts
const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});
```

动态依赖字段优先通过 schema 的 dependencies 表达。编辑回填遇到联动字段时，先设置决定显示结构的字段，再在下一个 tick 回填依赖值，避免隐藏字段被清空。

## 表格与查询

统一从项目 adapter 引入 `useVbenVxeGrid`：

```ts
import { useVbenVxeGrid } from '#/adapter/vxe-table';
```

adapter 已统一：

- 远程分页响应读取 `records`、`total`；
- 远程排序；
- 文本空值显示 `-`；
- 树表关闭 stripe，并通过单元格触发展开；
- 操作列居中；
- `CellAvatar`、`CellImage`、`CellTag`、`CellSwitch`、`CellOperation` renderer。

页面通常拆成 `data.ts`（查询 schema、表单 schema、columns）和 `list.vue`（请求与交互）。不要在每个页面重新注册 renderer 或重复做空值 formatter。

### CellOperation

```ts
{
  field: 'operation',
  title: '操作',
  cellRender: {
    name: 'CellOperation',
    options: [
      { code: 'detail', text: '详情' },
      'edit',
      'delete',
    ],
    attrs: {
      nameField: 'name',
      onClick: onActionClick,
    },
  },
}
```

- 字符串 `edit`、`delete`、`resetPassword` 有预设；
- delete 默认带二次确认，`popconfirm: false` 可关闭；其他操作设置 `popconfirm` 可启用确认；
- `show`、`disabled`、`text` 等可以是基于当前 row 的函数；
- `breakBefore` 从该操作前换到第二行；
- detail/preview/stats/view 使用查询语义色，危险操作使用 danger 语义；
- 不要在页面里单独重写操作链接 hover 样式。

### CellSwitch

状态切换通过 `beforeChange(newVal, row)` 调后端。renderer 会统一显示确认框、loading，并且只有回调不返回 `false` 时更新本地值。内置/不可修改资源通过 `disabled(row)` 回退显示状态 Tag。

## 权限

权限码集中在 `SYSTEM_PERMS`、`OPS_PERMS`：

```vue
<Button v-access:code="SYSTEM_PERMS.userCreate">新增</Button>
```

操作列使用 `filterAccessOptions` 把 operation code 映射到权限码：

```ts
options: filterAccessOptions(['edit', 'delete'], {
  edit: SYSTEM_PERMS.userUpdate,
  delete: SYSTEM_PERMS.userDelete,
})
```

需要命令式判断时使用 `hasAccessCode`。新增权限必须同步后端权限常量/接口校验、菜单数据和前端常量。前端权限只负责展示，不能替代服务端授权。

## 字典与业务选项

运行时字典统一通过：

```ts
const statusOptions = getDictOptions('enable_status');
const label = getDictLabel('enable_status', value);
```

字典树首次加载后缓存，`reloadDictOptions()` 用于管理端修改字典后主动刷新。`getTranslatedOptions` 用服务端字典覆盖代码内默认 label/color，因此页面在字典尚未返回时仍有可用默认值。

跨页面稳定选项放 `utils/business-options.ts`，不要在多个页面复制同一状态数组。字典数字字符串会规范化为 number；比较值时应与 API 类型保持一致。

## 公共工具

- 树选择器需要禁止当前节点及后代时使用 `disableTreeNodeAndDescendants`；
- 部门功能开关通过 `isDeptEnabled()` 读取 `VITE_BIZ_DEPT_ENABLED`；
- 富文本只读展示复用 `components/rich-text-preview`；
- 用户/角色等批量显示名查询优先复用 `utils/business-options` 中已有的 options loader，再决定是否新增工具。

## 新增管理页面检查表

1. 后端 API 是否已经有公开 DTO 和权限码；
2. `src/api` 是否定义了精确类型，ID 是否兼容字符串；
3. 是否复用 `requestClient`，并按场景选择 loading/error 类型；
4. 查询、表单、columns 是否复用 adapter 和字典选项；
5. 操作按钮是否同时有 `v-access`/`filterAccessOptions` 与后端权限；
6. 动态路由组件路径是否与页面实际文件匹配；
7. 执行 `pnpm --filter @travis/travis-admin typecheck`。
