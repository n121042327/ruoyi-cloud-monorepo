# AGENTS.md — K12 教育 ToB 平台（monorepo 根级约定）

本文件对 `D:\work\person_work\ruoyi-cloud-monorepo` 全仓库生效。
`apps/**` 与 `services/**` 下如存在自己的 `AGENTS.md`，以更靠近被修改文件的为准；
但本文件的第 3 节（阶段门禁）、第 4 节（缺项处理）、第 8 节（红线）任何下层文件都不得放宽。

## 1. 项目身份

- 产品：K12 教育 ToB 平台，PC 端 B/S 架构
- 客户形态：**租户即组织单元**。分学校租户、集团租户、运营方租户三类；集团租户下挂多个学校租户，层级为 运营方 → 集团 → 学校，不超过三级。运营方租户是超级租户，可查看全平台数据
- 用户群：学校（老师、学生、领导）、基教集团（专家、领导）、平台运营方
- 后续扩展：Pad 教师端、Pad 学生端、家长小程序端；ToC 校区外师生
- 首轮交付范围：**年级管理、班级管理、教师管理、学生管理**，以及支撑这四项所必需的能力——
  学校、学年学期、学科配置、教育角色、任教关系、租户接入、
  升班 / 调班 / 留级 / 毕业 / 结业 / 肄业 / 休复学 / 出国 / 失踪 / 转入未报到 / 退学 / 开除 / 死亡、3+1+2 选科、教学班、导入导出、异步任务、审计
- 首轮明确不做：自动排课、自动优化编班、移动端、题库、作业、考试、练习、错题、学情分析
- ToC 场景：只做架构设计、详细设计与预留 DDL，**不进入首轮迁移脚本**

## 2. 目录约定（目标态）

```
ruoyi-cloud-monorepo/
  apps/plus-ui/                     # Vue3 前端   ← 迁移已完成
  services/RuoYi-Cloud-Plus/        # Java 后端   ← 迁移已完成
    ruoyi-modules/ruoyi-edu/        # 新增教育业务服务
  docs/                             # 全部设计与约束文档
  prototypes/functional/v1/         # 业务原型
  prototypes/high-fidelity/v1/      # 独立高保真原型
  evidence/                         # 验收证据（命令输出、截图、报告）
  .agents/skills/                   # Codex 技能
```

目录迁移已于 2026-09-29 完成，Git 识别为改名（rename），提交历史连续。
内层 `.git` / `.git1` 由用户删除并暂存，其内容以 pack 形式保留在根仓库对象库中。
后续如再调整顶层目录结构，仍属红线操作，必须先征求确认。

## 3. 阶段门禁（不可跳步）

固定流水线：

```
项目核查与工程约束 → PRD → 业务原型 → 独立高保真原型 → 概要设计
→ 详细设计及建表 → 生产前端 → 生产后端 → 联调验收
```

| 阶段 | 输入 | 输出 | 通过条件 |
|---|---|---|---|
| 0 项目核查与工程约束 | 现有仓库代码 | 本文件、`docs/00-governance/**` | 用户确认约束文件 |
| 1 PRD | 阶段 0 产物 | `docs/10-prd/**` | 用户确认 PRD 样板 |
| 2 业务原型 | PRD + 原型约束 | `prototypes/functional/v1/**` | 样板页人工验收 |
| 3 高保真原型 | PRD + 业务原型 + 视觉规范 | `prototypes/high-fidelity/v1/**` | 视觉标准冻结 |
| 4 概要设计 | PRD + 原型 | `docs/30-architecture/**` | 用户确认架构与接口清单 |
| 5 详细设计及建表 | 概要设计 | `docs/40-detailed-design/**` + 迁移脚本 | 迁移脚本在 MySQL 8 验证通过 |
| 6 生产前端 | 详细设计 + 高保真原型 + 映射文件 | `apps/plus-ui/**` | 类型检查、Lint、构建、交互对照通过 |
| 7 生产后端 | 详细设计 | `services/**` | 编译 + 显式启用测试 + 接口测试通过 |
| 8 联调验收 | 全部 | `evidence/**` | 验收清单逐条有证据 |

上游产物未被用户确认前，不得开始下游阶段。
若下游设计必须回头修改已冻结的上游产物，先说明影响范围，取得确认后产生**新版本**，
不得就地覆盖已冻结版本。

## 4. 缺项处理（强制）

任何阶段发现下列情况，都必须**停下来先问**：

1. 计划中列出的前置文件不存在或内容为空
2. 需要判断但仓库中无依据的业务规则（例如"留级是否允许跨校""班主任能否修改学号"）
3. 两份上游文档互相矛盾
4. 技术选型与现有代码冲突

处理动作固定为四步，缺一不可：

```
记录缺项 → 说明影响范围 → 提出补全建议 → 等用户确认 → 补全并验证 → 继续
```

禁止在没有确认的情况下自行补造业务规则、接口、表结构或技术决策。
缺项一律登记到 `docs/00-governance/gap-register.yaml`，
状态只有 `open` / `answered` / `closed` / `waived`。

### 4.1 待用户拍板事项的写法（强制）

每一条需要用户拍板的事项，必须写全以下四部分，缺一不可：

1. **这是什么** —— 用业务语言描述问题，不使用只有实现者才懂的术语
2. **不选会怎样** —— 保持现状的后果，或选错方向的代价
3. **可选项** —— 每个选项一句话说明"选它之后会发生什么"
4. **推荐方案与理由** —— 明确推荐哪一个，以及为什么

只罗列选项、把判断直接丢给用户的写法不合格。
每条事项旁必须给出**所在文件的完整路径与行号**，方便用户直接翻看。

**用户未表态的项一律保持 `待确认`，不得替用户记为"已确认"。**
把未经确认的事项标成已确认，等于伪造验收结论，比漏问更严重。

## 5. 批量执行与人工验收

- 计划必须拆成可独立验收的小批，默认每批 **2—3 个页面或 1 个服务模块**
- 首批必须只做一个样板（业务原型、高保真原型、前端页面、后端模块各自的第一件）
- 样板验收通过后，用同一标准批量复制，其余批次仍逐批暂停验收
- 每批结束时输出：本批产物清单、验收方法、已知缺口、下一批建议
- 未经要求不自动继续下一批

## 6. 技术栈（锁定值）

完整版本与证据见 `docs/00-governance/stack-lock.md`。要点：

| 层 | 选型 |
|---|---|
| 前端 | Vue 3.5 + TypeScript 5.9 + Vite 7 + Pinia 3 + Vue Router 5 + Element Plus 2.13 + UnoCSS + Axios + `pnpm` |
| 前端写法 | `<script setup lang="ts">` + Composition API + composable + 独立 `api/` 层 |
| 后端 | JDK 17 + Spring Boot 3.5.15 + Spring Cloud 2025.0.3 + RuoYi-Cloud-Plus 2.6.2 + Dubbo + Sa-Token |
| 持久层 | MyBatis 3.5.19 + MyBatis-Plus 3.5.16 + MySQL 8 |
| 中间件 | Redis（热点缓存）、RabbitMQ（削峰、异步）、Elasticsearch（后续题库检索） |
| 构建 | Maven（后端）、pnpm workspace + Turbo（前端与 monorepo 编排） |
| 部署 | Docker |

**实际现有代码的版本优先于本表。** 若计划中的版本与 `pom.xml` / `package.json` 冲突，
以实际文件为准，并把差异记入 `docs/00-governance/decisions.md`。
首轮保留现有编辑器与前端基础设施；Tiptap + KaTeX 属于后续题库阶段，不在首轮引入。

## 7. 教学业务硬约束（AI 最容易做错的地方）

- 租户 = 组织单元本身（学校 / 集团 / 运营方三类），**不是"下辖多校的集团"**；层级不超过三级
- 教学数据落在学校租户上，学校数据各自维护；**跨校共享只能由运营方显式授权，且只读**
- 跨校共享授权的**对象只有教学资源**（题库习题、试卷等）；学生、班级、年级、教师、成绩等业务数据不跨校共享，跨校查看业务数据只有平台运营的 `DS-01` 全平台范围（只读并留痕），它不叫共享授权
- 学生主体与监护人主体是**平台级实体**，不参与学校租户隔离；学校侧读学生一律经在校记录 / 班级关系两段式取数
- 集团**不看**下属学校的教学数据，不存在按租户树自动下钻的默认行为
- 跨校数据共享由运营方创建**数据共享授权**实现，且**只读**（首轮仅 `read` / `export`），不设审批；授权对象仅限教学资源（`BR-DATA-018`）
- 数据权限：校领导看本校全部；年级主任看负责年级；班主任看负责班级；任课教师看本人任教班级的必要资料与本人所授学科数据
- 集团身份不自动获得学校教学数据的读取权；集团只能看集团自有数据
- 平台运营方全平台可见，但"查看 / 修改 / 导出"分别授权、分别审计
- 系统内置超级管理员（`super_admin`）是唯一不受数据范围与功能权限限制的账号，可执行全部功能、可视同任意角色；它的操作**强制留痕且不可关闭**，并禁止用于日常业务操作。它是「运营方三项独立授权」的唯一例外（`BR-ORG-014`）
- 教育数据权限**不得被现有租户管理员的放行逻辑绕过**；缺少租户、学校或执行人上下文时必须拒绝执行
- 列表、详情、批量操作、导出、文件访问、缓存、异步任务必须执行同一套权限规则
- 升班新增下一学年的班级与学生关系，**按学年追加、不覆盖历史**；升班的执行只在升班模块，年级管理只提供只读视图
- 行政班与教学班是两套独立关系，选科组合不等于行政班
- 同服务内核心关系使用物理外键，禁止级联删除；跨服务关系用逻辑引用 + 一致性检查
- **模块边界：每个字段只有一个写入入口。** 越界写入会把权限规则拆成两套，出问题时无法判断是谁改的
  - 班主任：唯一写入入口是班级管理（`edu_class.head_teacher_id`，`DP-01`）；教师管理只读并跳转
  - 学生班级归属：唯一写入入口是班级管理（编班 / 调班 / 移出）；学生管理只读展示
  - 升班：唯一执行入口是升班模块；年级管理只提供"学段内序号 +1"的只读视图

## 8. 红线

以下操作必须先征得用户确认：

1. 删除文件、目录或 Git 历史
2. 修改 `.env`、密钥、Token、证书、CI/CD 配置
3. `git push`、`git rebase`、`git reset --hard`、强制推送
4. 公开发布、生产部署
5. 移动现有代码目录（含 monorepo 目录结构调整）

另外：**不自动 commit、不自动 push**。提交前先展示变更摘要。
commit message 使用 Conventional Commits，scope 与描述用中文。

### 8.1 分支与集成策略（2026-10-01 起生效）

- **`main` 是唯一的长期分支**
- 每个阶段从 `main` 拉一条**短命分支**：`codex/stage<N>-<name>`，
  例如 `codex/stage6-frontend`、`codex/stage7-backend`、`codex/stage8-acceptance`
- 阶段完成、门禁通过后，用 **`git merge --no-ff`** 合并回 `main`，保留 merge commit ——
  这样每个阶段在 `git log --graph` 里是一个可识别的分叉 + 汇合；
  **不要用 `--ff-only`**，否则阶段边界在历史里消失
- 合并完成后删除该阶段分支，不留长命分支
- 阶段边界另有 annotated tag 可追溯：
  `stage2-prototype-end`、`stage3-highfidelity-end`、`stage4-architecture-end`、`stage5-detailed-design-end`
- 阶段 0 ~ 5 的合并痕迹已于 2026-10-01 补齐：此前用 `--ff-only` 合并，`main` 上是一条直线；
  现改为把 `main` 退回阶段 1 终点后逐阶段执行 `git merge --no-ff <阶段终点>`，
  每个阶段边界新增一个 merge commit，`git log --graph` 能看到 5 处分叉。
  **没有重放或改写任何已有提交**，原 SHA 全部保留，工作树内容与改写前 0 差异
- 改写前的状态保留在 `backup/pre-nomerge-rewrite` 分支，确认无误后可删除
- 原 `codex/prototype` 分支已改名为 `codex/archive-through-stage5`，仅作存档，不再使用

## 9. 质量与状态口径

功能状态只允许四选一：`已设计` / `已实现` / `已启用` / `已验证`。
检查结果只允许五选一：`通过` / `失败` / `未执行` / `不适用` / `已失效`。
不得把"编译通过"写成"测试通过"。

后端特别注意：根 `pom.xml` 默认 `<skipTests>true</skipTests>`，
`mvn package` **不能**作为测试通过的证据；必须显式启用测试并核对实际执行的用例数量。

证据落盘在 `evidence/`，命名要能单独看懂，例如
`evidence/stage6-frontend/2026-09-29_student-list_tsc.log`。

## 10. 文档语言与命名

- **所有文件使用 UTF-8 编码、不带 BOM；换行统一用 LF**
  - 用 PowerShell 写文件时显式指定编码，避免落到 ANSI：
    `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`
  - 写 `.ps1` 脚本时，含中文的脚本必须存为「UTF-8 with BOM」，否则整个脚本只用 ASCII ——
    Windows PowerShell 5.1 按 ANSI 解析 `.ps1`，中文会导致语法错误（本仓库的
    `tools/capture_hifi_screenshots.ps1` 因此保持纯 ASCII，并在文件头注明了原因）
  - 生成器输出、提交信息、验收日志同样一律 UTF-8
- 文档、注释、提交信息用中文；代码标识符、文件路径、命令用英文
- 中文与英文之间加空格，中文标点用全角
- 文档编号遵循 `docs/00-governance/file-catalog.md` 的目录约定
- 每个交接者都是全新上下文：写文档时假设读者没看过对话
- 向用户或下游文档指出位置时，必须给出**相对仓库根的完整路径**；需要精确到条目时补行号，
  例如 `docs/10-prd/modules/class/PRD.md` 第 12 节（第 455–476 行）

## 11. 常用命令

前端（**在仓库根执行**，pnpm workspace + Turbo 编排；`pnpm-workspace.yaml` 覆盖 `apps/*` 与 `packages/*`）：

```bash
pnpm install            # 只在根目录执行；lockfile 也只有根目录一份
pnpm dev                # = turbo run dev        → apps/plus-ui 的 vite serve
pnpm build              # = turbo run build:prod
pnpm lint               # = turbo run lint:eslint
pnpm typecheck          # = turbo run typecheck
pnpm test               # = turbo run test（只覆盖前端，不等于后端测试通过）
```

只跑前端单个包（以后新增 Pad / 小程序端同样适用）：

```bash
pnpm --filter @edu/plus-ui dev
pnpm --filter @edu/plus-ui typecheck
pnpm --filter @edu/plus-ui preview      # turbo.json 未登记 preview 任务，只能用 --filter 形式
pnpm turbo run build:prod --filter=@edu/plus-ui
pnpm turbo run build:prod --dry=json    # 看缓存命中与任务图
```

后端（`services/**` 不在 pnpm workspace 内，Turbo 不参与编排，Java 侧一律走 Maven）：

```bash
cd services/RuoYi-Cloud-Plus
# 现状可用：ruoyi-modules/ruoyi-edu 尚未创建，先拿已有模块验证
mvn -q -DskipTests=false -pl ruoyi-modules/ruoyi-system -am test
# 阶段 7 建出 edu 服务后改用：
# mvn -q -DskipTests=false -pl ruoyi-modules/ruoyi-edu -am test
```

`services/RuoYi-Cloud-Plus/pom.xml` 默认 `<skipTests>true</skipTests>`，`mvn package` 不能作为测试通过的证据；
必须显式加 `-DskipTests=false` 并核对实际执行的用例数量。

## 12. 文件地图

| 文件 | 用途 |
|---|---|
| `docs/00-governance/stack-lock.md` | 技术栈锁定值与证据 |
| `docs/00-governance/file-catalog.md` | 全部交付物清单与状态 |
| `docs/00-governance/stage-inputs.yaml` | 每阶段输入 / 输出 / 门禁 |
| `docs/00-governance/gap-register.yaml` | 缺项登记 |
| `docs/00-governance/decisions.md` | 已确认决策与裁决 |
| `docs/00-governance/change-control.md` | 变更流程 |
| `docs/00-governance/traceability.yaml` | 需求 → 原型 → 接口 → 表 → 测试 追踪 |

## 页面字段布局约束

生成或修改页面、表单、表格、列表、详情和查询条件，必须遵守 `docs/00-governance/page-field-layout.md`。语义分组与依赖顺序优先；必填优先仅作建议。冻结版本通过新版本变更，逐批人工验收。
