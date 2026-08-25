# 数据库迁移说明

本项目使用一个 Flyway 实例和一张 `flyway_schema_history` 历史表统一管理脚手架与业务系统的数据库迁移。

## 目录

所有迁移脚本统一放在：

```text
db/migration
```

Flyway 配置位于 `application.yml`：

```yaml
spring:
  flyway:
    locations: classpath:db/migration
    table: flyway_schema_history
    out-of-order: true
    validate-on-migrate: true
    validate-migration-naming: true
    clean-disabled: true
    baseline-on-migrate: false
```

公共配置启用 Flyway，`application-dev.yml` 默认将其关闭，方便脚手架维护阶段合并尚未发布的 SQL；`prod` Profile 使用公共配置并在启动时执行迁移。开发环境需要验证迁移时显式设置 `SPRING_FLYWAY_ENABLED=true`。

## 版本迁移命名

脚手架与业务系统共享一个全局版本空间，版本号由时间、来源和序号组成：

```text
V<时间>_<来源>_<序号>__<描述>.sql
```

来源编号：

- `0`：脚手架迁移
- `1`：业务系统迁移

```text
V20260801_0_001__add_user_lock_version.sql
V20260801_0_002__add_role_lock_version.sql
V20260801_1_001__create_order_table.sql
```

已经发布或执行过的迁移脚本不得修改、重命名或删除。数据库结构需要修正时，必须新增更高版本的迁移脚本。

## 基线迁移命名

Flyway 基线迁移使用 `B` 前缀：

```text
B<时间>_<来源>_<序号>__<描述>.sql
```

脚手架可以提供仅包含脚手架结构的基线：

```text
B20260812110000_0_001__scaffold_baseline.sql
```

业务系统发布的基线必须是完整聚合快照：

```text
B20260812120000_1_001__full_baseline.sql
```

完整聚合快照必须包含：

- 当前脚手架的全部数据库结构和初始化数据
- 当前业务系统的全部数据库结构和初始化数据
- 该基线版本之前所有迁移的最终结果

业务基线版本必须高于它已经包含的所有 `V` 和 `B` 版本，避免执行基线后再次执行已包含的迁移。

## 更新脚手架

业务系统拉取脚手架更新时，先合并并验证最新的脚手架迁移脚本。

出现以下任一情况时，需要重新生成一个版本更高的业务完整聚合基线：

- 脚手架新增了更高版本的基线迁移
- 新拉取的脚手架迁移版本低于或等于当前业务基线
- 无法确认当前业务基线是否已经包含最新脚手架结构

最稳妥的规则是：脚手架迁移目录发生变化后，业务系统重新验证并生成最新的完整聚合基线。

`out-of-order: true` 允许已有数据库补充执行后来拉取的低版本 `V` 迁移，但不会让全新数据库执行低于最新 `B` 基线的迁移，因此不能替代聚合基线更新。

## 新数据库与已有数据库

全新数据库执行迁移时，Flyway 会选择版本最高的 `B` 基线，然后执行版本高于该基线的 `V` 迁移。没有 `B` 基线时，会按版本顺序执行全部 `V` 迁移。

已有 Flyway 历史记录的数据库会忽略后来新增的 `B` 基线，只执行尚未执行的 `V` 迁移。

对于已经存在但尚未接入 Flyway 的数据库，不要直接开启 Flyway。应先核对实际数据库结构和已应用的迁移版本，完成一次基线接管，再将 `spring.flyway.enabled` 设置为 `true`。
