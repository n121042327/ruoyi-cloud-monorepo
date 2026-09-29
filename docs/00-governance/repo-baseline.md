# 仓库基线核查报告

核查时间：2026-09-29
核查方式：只读扫描 + 经用户授权的目录迁移（`plus-ui` → `apps/plus-ui`，`RuoYi-Cloud-Plus` → `services/RuoYi-Cloud-Plus`）
本文档中的路径除第 9 节事件记录外，均指迁移后的路径。

## 1. 版本控制状态

| 项 | 值 |
|---|---|
| 根仓库 | `D:\work\person_work\ruoyi-cloud-monorepo` |
| 当前分支 | `main` |
| 工作区状态 | 有未提交改动：1622 个重命名已暂存、68 个 `.git1` 删除已暂存、根 `AGENTS.md` 与 `docs/` 未跟踪 |
| 远端 | `git@github.com:n121042327/ruoyi-cloud-monorepo.git` |

近期提交：

```
4b79a46 Merge branch 'codex/monorepo-init'
ec67c94 install superpowers-zh
214a11d Merge branch 'codex/monorepo-init'
6dab382 回退到2.6.2，这版有租户相关逻辑
b394ab5 chore(仓库): 初始化 monorepo，引入 RuoYi-Cloud-Plus 与 plus-ui 代码基线
```

## 2. 顶层结构

| 路径 | 类型 | 说明 |
|---|---|---|
| `.agents/skills/` | 目录 | superpowers-zh 技能集，22 个技能，含 `executing-plans`、`writing-plans`、`test-driven-development` |
| `.git/` | 目录 | 根仓库 Git 元数据 |
| `apps/plus-ui/` | 目录 | Vue 3 前端，2026-09-29 由 `plus-ui/` 迁移而来 |
| `services/RuoYi-Cloud-Plus/` | 目录 | Java 后端，2026-09-29 由 `RuoYi-Cloud-Plus/` 迁移而来 |
| `docs/` | 目录 | 全部设计与治理文档 |
| `AGENTS.md` | 文件 | 根级 Codex 工程约定 |

`prototypes/` 与 `evidence/` 尚未创建，将在阶段 2、阶段 8 建立。

## 3. 前端事实

来源：`apps/plus-ui/package.json`

| 项 | 值 |
|---|---|
| 包名 | `ruoyi-vue-plus` |
| 版本 | `5.6.2-2.6.2` |
| 包管理器 | 声明 `engines`，仓库未见 `pnpm-lock.yaml`（待核实），实际使用 pnpm |
| 构建工具 | Vite 7.3.2 |
| 框架 | Vue 3.5.30 |
| 语言 | TypeScript ~5.9.3 |
| 状态管理 | Pinia 3.0.4 |
| 路由 | Vue Router 5.0.3 |
| UI 库 | Element Plus 2.13.5 + `@element-plus/icons-vue` 2.3.2 |
| 表格 | vxe-table 4.18.1（现有功能，需在设计阶段确认是否引入教育模块） |
| 样式 | UnoCSS 66.6.6 + Sass 1.98.0 |
| 请求 | Axios 1.13.6 |
| 测试 | Vitest 4.0.18（已安装，是否已有测试用例待核实） |
| Lint | ESLint 9.39.1 + `@vue/eslint-config-typescript` 14.6.0 + Prettier 3.8.1 |
| Node 要求 | `>=20.19.0` |

`src/` 目录：`api`、`assets`、`components`、`directive`、`enums`、`hooks`、`lang`、`layout`、`plugins`、`router`、`store`、`types`、`utils`、`views`

`src/api/` 已按模块分层：`system/{user,role,dept,dict,tenant,tenantPackage,oss,...}`、`monitor`、`tool`、`workflow`、`demo`

`src/views/` 现有：`system`、`monitor`、`tool`、`workflow`、`demo`、`error`、`redirect`

结论：前端**已具备**教育模块落地所需的目录骨架与依赖，无需重建项目。

## 4. 后端事实

来源：`services/RuoYi-Cloud-Plus/pom.xml`、目录结构

| 项 | 值 |
|---|---|
| 版本 | `<revision>2.6.2</revision>` |
| JDK | `<java.version>17</java.version>`，用于 Maven compiler `release` |
| Spring Boot | 3.5.15 |
| Spring Cloud | 2025.0.3 |
| MyBatis | 3.5.19 |
| MyBatis-Plus | 3.5.16 |
| Hutool | 5.8.43 |
| FastJSON | 1.2.83 |

顶层模块：`ruoyi-api`、`ruoyi-auth`、`ruoyi-common`、`ruoyi-example`、`ruoyi-gateway`、`ruoyi-gateway-mvc`、`ruoyi-modules`、`ruoyi-visual`

业务模块（`ruoyi-modules/`）：`ruoyi-system`、`ruoyi-resource`、`ruoyi-job`、`ruoyi-workflow`、`ruoyi-gen`、`ruoyi-ai`

`ruoyi-common/` 下 40 个子模块，与本项目直接相关的包括：
`ruoyi-common-tenant`（多租户上下文）、`ruoyi-common-mybatis`、`ruoyi-common-redis`、
`ruoyi-common-security`、`ruoyi-common-idempotent`、`ruoyi-common-excel`、
`ruoyi-common-elasticsearch`、`ruoyi-common-log`、`ruoyi-common-doc`、`ruoyi-common-job`

`ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/` 结构：
`controller` / `domain` / `dubbo` / `listener` / `mapper` / `service`

## 5. 本机环境

| 项 | 值 |
|---|---|
| Java | `openjdk 17.0.0.1`（符合要求） |
| Maven | Apache Maven 3.9.9，运行在 JDK 17 |

## 6. 数据库与脚本现状

- 现有建库脚本：`services/RuoYi-Cloud-Plus/script/sql/ry-cloud.sql`（MySQL）、另有 `oracle/`、`postgres/` 方言
- 增量升级脚本：`services/RuoYi-Cloud-Plus/script/sql/update/`，版本区间 2.0 → 2.6.0
- 另有 `ry-config.sql`（Nacos 配置库）、`ry-job.sql`（SnailJob）、`ry-seata.sql`、`ry-workflow.sql`
- `sys_tenant` / `sys_tenant_package` 已在 `ry-cloud.sql` 中定义，说明**租户管理功能现有系统已实现**
- **教育业务表在本仓库中尚不存在**，需要新建迁移脚本

## 7. 已确认可直接复用与必须新建的部分

| 能力 | 现状 | 结论 |
|---|---|---|
| 租户管理 | 已有 `sys_tenant` + `ruoyi-common-tenant` | 复用，不重建 |
| 用户 / 角色 / 菜单 / 部门 | 已有 `ruoyi-system` | 复用系统能力 |
| 数据权限框架 | 已有 `ruoyi-common-mybatis` 数据权限 | **必须扩展**，见下节风险 |
| 教育领域表与业务 | 无 | 新建 `ruoyi-modules/ruoyi-edu` |
| 学校 / 年级 / 班级 / 学生 / 教师 | 无 | 新建，落在 `ruoyi-edu` |
| 前端教育页面 | 无 | 新建，落在 `apps/plus-ui/src/views/edu/**` |

## 8. 风险与未核实项

| 编号 | 项 | 影响 | 状态 |
|---|---|---|---|
| R-01 | 内层 `.git` / `.git1` 已由用户删除并暂存（2026-09-29 15:06） | 迁移不再需要处理嵌套仓库；内层历史以 pack 形式保留在根仓库对象库 | 已确认，保持删除 |
| R-02 | 现有租户动态切换依赖上下文，多学校 / 多标签页并发时的请求级隔离未核实 | 可能造成跨校数据串读 | 阶段 5 必须验证 |
| R-03 | `ruoyi-common-tenant` 中租户管理员可能存在放行逻辑 | 若直接套用，教育数据权限会被绕过 | 阶段 5 必须验证并改造 |
| R-04 | 根 `pom.xml` 默认 `<skipTests>true</skipTests>` | 误判测试通过 | 已在根 `AGENTS.md` 记录 |
| R-05 | Jackson 对 `Long` / `BigDecimal` 有特殊序列化配置 | 前端 ID 精度问题 | 阶段 4 定 API 契约时明确 |
| R-06 | 前端**没有** `pnpm-lock.yaml`；根目录没有 `pnpm-workspace.yaml` / `turbo.json` / `package.json` | Turbo + workspace 需要从零建立 | 已核实，待建 |
| R-07 | `.claude/agents` 目录不存在 | 用户确认不再考虑，改用根 `.agents/skills` | 已关闭 |
| R-08 | 15:06 的分支切换由用户本人操作，不是未知进程 | 不存在并发干扰 | 已澄清 |

## 9. 事件记录：内层 Git 元数据消失

> 本节路径一律指**迁移前**的仓库根路径。
> 结论：由用户本人操作导致，非异常进程，已按用户决定保持删除。

### 发生了什么

本阶段执行期间，`plus-ui` 与 `RuoYi-Cloud-Plus` 下的四个目录从工作区消失：

| 路径 | 是否被 Git 跟踪 | 能否恢复 |
|---|---|---|
| `apps/plus-ui/.git` | 否 | 不能从本仓库恢复 |
| `apps/plus-ui/.git1` | 是（33 个文件，含完整 pack） | 能，`git restore --source=HEAD -- apps/plus-ui/.git1` |
| `services/RuoYi-Cloud-Plus/.git` | 否 | 不能从本仓库恢复 |
| `services/RuoYi-Cloud-Plus/.git1` | 是（35 个文件，含完整 pack） | 能，`git restore --source=HEAD -- services/RuoYi-Cloud-Plus/.git1` |

`git status` 显示 68 个删除项，**全部位于 `.git1/` 内**，其余文件无删除。

### 时间线证据

| 时间 | 事件 | 证据来源 |
|---|---|---|
| 15:05:10 | 本阶段开始写入 `docs/00-governance/**` | 文件创建时间 |
| 15:06:22 | `plus-ui` 与 `RuoYi-Cloud-Plus` 目录内容变化；同一时刻写入 `services/RuoYi-Cloud-Plus/AGENTS.md` | 目录修改时间 |
| 15:06:24 | HEAD 从 `main` 切到 `6dab382` | `git reflog` |
| 15:06:40 | HEAD 从 `6dab382` 切到 `codex/install-skills` | `git reflog` |
| 15:06:44 | HEAD 从 `codex/install-skills` 切到 `codex/monorepo-init` | `git reflog` |
| 15:06:47 | HEAD 从 `codex/monorepo-init` 切回 `main` | `git reflog` |
| 15:07:12 | 状态稳定不再变化 | 两次间隔 4 秒的 `git status` 对比 |

这四次分支切换**不是本阶段发起的**。本阶段只做了只读扫描与新增文件，
未执行任何 `git checkout`、`git restore`、`git clean` 或删除命令。
分支切换的时间窗口与文件消失高度重合。

### 影响

1. 目录迁移不再需要处理嵌套仓库，比原计划简单
2. 内层仓库历史以 pack 形式保留在根仓库对象库中，没有丢失
3. 有未知进程在并发操作本仓库，会与本项目"小批验收"的节奏冲突

### 结论

用户确认：15:06 的分支切换与 `.git` / `.git1` 的删除均由用户本人操作。
`.git1` 保持删除状态（已由用户加入暂存区），内层仓库历史以 pack 形式归档在根仓库对象库中。
如需恢复：`git restore --source=HEAD -- apps/plus-ui/.git1 services/RuoYi-Cloud-Plus/.git1`。

## 10. 目录迁移记录（2026-09-29）

| 项 | 值 |
|---|---|
| 授权 | 用户明确批准 |
| 方式 | `git mv`，Git 识别为改名，提交历史连续 |
| 结果 | `apps/plus-ui/`、`services/RuoYi-Cloud-Plus/` |
| Git 识别重命名条目 | 1622 |
| 后端文件数 | 2328 |
| 遗留问题 | 源目录 `RuoYi-Cloud-Plus/` 残留 625 个空目录壳，待用户确认后删除 |

执行细节：

1. `plus-ui` 整体一次 `git mv` 成功
2. `RuoYi-Cloud-Plus` 整体重命名被占用（Windows 返回 Permission denied），改为逐顶层条目 `git mv`，全部成功
3. `.github`、`target`、`AGENTS.md` 三项因是空目录或未跟踪文件，改用文件系统移动
4. 各模块的 `target` 构建产物随所属模块目录一并迁移

## 11. 结论

仓库可直接作为工程基线：前端骨架、后端骨架、租户能力都已就绪。
首轮工作重心不是"搭架子"，而是**在既有骨架上新增教育域能力并补上数据权限这一层**。

## 12. 前端基线检查结果（2026-09-29）

在目录迁移、monorepo 骨架建立、依赖安装完成后执行。
原始日志保存在 `evidence/stage0-monorepo/`。

| 检查项 | 命令 | 结果 | 说明 |
|---|---|---|---|
| 依赖安装 | `pnpm install` | 通过 | 539 个包。需在 `pnpm-workspace.yaml` 用 `allowBuilds` 放行 `esbuild` 与 `@parcel/watcher` 的构建脚本 |
| 生产构建 | `pnpm --filter @edu/plus-ui build:prod` | 通过 | 首次因 Windows 文件占用失败，重试后通过；产物体积正常 |
| 类型检查 | `pnpm --filter @edu/plus-ui typecheck` | **失败** | 49 个错误，全部位于上游既有代码 |
| Lint | `pnpm --filter @edu/plus-ui lint:eslint` | **失败** | 125 个问题，全部位于上游既有代码 |

### 类型检查错误分布（49 项）

| 目录 | 数量 |
|---|---|
| `src/views/workflow` | 18 |
| `src/layout/components` | 11 |
| `src/views/system` | 9 |
| `src/components/Process` | 3 |
| `src/components/DictTag` | 2 |
| `src/views/monitor` | 2 |
| `src/views/tool` | 2 |
| `src/components/FileUpload` | 1 |
| `src/views/register.vue` | 1 |

错误码分布：`TS2322` 19、`TS2345` 10、`TS2307` 8、`TS2551` 5、`TS2339` 2、`TS2367` 2、`TS2769` 2、`TS2353` 1。

典型成因：

1. `@/components/UserSelect` 一类路径式组件引用无法被 `components.d.ts` 覆盖（8 个 TS2307）
2. `<el-select>` 的 `v-model` 推导为 `(string | number)[]`，与组件声明的 `string[]` 冲突
3. 上游主题相关代码对标签页 `paneName` 的扩展未在类型层声明

### Lint 问题分布（125 项）

| 规则 | 数量 |
|---|---|
| `prettier/prettier` | 124 |
| `vue/block-lang` | 1 |

涉及 17 个文件，其中 124 项可用 `--fix` 自动修复。

### 结论

这三项属于**上游既有代码的基线状态**，不是目录迁移或 monorepo 改造引入的。
教育模块的验收门禁按"新增代码零错误 + 不扩大既有错误数"执行，见 `decisions.md` D-024。
上游遗留问题的清理单独排期，不与首轮交付混在一起。

### 补充：行尾符是 Lint 失败的重要因素

本机 `core.autocrlf = true`，检出时 LF 被替换为 CRLF，而仓库中实际保存的是 LF。
`apps/plus-ui/.prettierrc` 的 `endOfLine` 为 `auto`，遇到混合行尾的文件就会报格式错误。
这解释了 124 个 `prettier/prettier` 问题中的大部分。

待决：是否新增仓库根 `.gitattributes`（例如 `* text=auto eol=lf`）统一行尾。
该改动会在下次检出时对全仓库做一次行尾归一化，产生大范围 diff，因此需要单独确认后再做。
