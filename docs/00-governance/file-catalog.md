# 交付物目录（File Catalog）

本文件是整个项目的"待办总表"。每批任务开始前看它，结束后更新它。

状态取值：

| 状态 | 含义 |
|---|---|
| `planned` | 已列入计划，未开工 |
| `draft` | 草稿，未经用户确认 |
| `review` | 已提交用户验收 |
| `frozen` | 已确认，禁止就地修改（改动走 `change-control.md`） |
| `superseded` | 已废弃，被新版本取代 |

首轮范围：年级管理、班级管理、教师管理、学生管理 + 支撑能力。
非首轮模块在表中标 `planned`，不进入本轮排期。

---

## 阶段 0：工程治理

| 路径 | 用途 | 状态 |
|---|---|---|
| `.gitignore` | 根级忽略规则 | `review` |
| `package.json` | monorepo 根包，定义 Turbo 脚本入口 | `review` |
| `pnpm-workspace.yaml` | pnpm 工作区定义 + 构建脚本白名单（`allowBuilds`） | `review` |
| `turbo.json` | Turbo 任务编排（build / dev / lint / typecheck / test） | `review` |
| `pnpm-lock.yaml` | 依赖锁定（根 + apps/plus-ui 两个 importer，630 个包） | `review` |
| `evidence/stage0-monorepo/**` | 基线检查原始日志 | `review` |
| `tools/check_docs.py` | 文档一致性核查脚本 | `review` |
| `AGENTS.md` | 根级 Codex 工程约定 | `review` |
| `docs/00-governance/README.md` | 治理目录索引 | `review` |
| `docs/00-governance/repo-baseline.md` | 仓库基线核查报告 | `review` |
| `docs/00-governance/stack-lock.md` | 技术栈锁定表 | `review` |
| `docs/00-governance/file-catalog.md` | 本文件 | `review` |
| `docs/00-governance/document-map.yaml` | 文档依赖图 | `review` |
| `docs/00-governance/stage-inputs.yaml` | 阶段输入 / 输出 / 门禁 | `review` |
| `docs/00-governance/gap-register.yaml` | 缺项登记 | `review` |
| `docs/00-governance/decisions.md` | 决策与裁决记录 | `review` |
| `docs/00-governance/change-control.md` | 变更流程 | `review` |
| `docs/00-governance/change-requests/CR-001.md` | 变更申请：修正已冻结模块的范围划分章节引用（已批准并执行） | `review` |
| `docs/00-governance/change-requests/CR-002.md` | 变更申请：把数据共享授权的对象限定为教学资源（题库习题、试卷），同步改写 2 条验收用例 | `review` |
| `docs/00-governance/change-requests/CR-003.md` | 变更申请：补齐敏感字段权限点归属、学生照片、批量导出与批量调班、字段字典补登记、学号修改口径（用户已批准） | `review` |
| `docs/00-governance/traceability.yaml` | 追踪矩阵骨架 | `review` |
| `docs/00-governance/task-packet.md` | 任务包模板 | `review` |
| `docs/00-governance/baseline-manifest.schema.json` | 冻结清单 Schema | `review` |
| `apps/plus-ui/AGENTS.md` | 前端子级约定（组件映射、代码写法、视觉规范） | `review` |
| `services/RuoYi-Cloud-Plus/AGENTS.md` | 后端子级约定（分层、接口形状、数据权限、测试） | `review` |
| `.agents/skills/edu-*/SKILL.md` | 项目专用 Codex 技能 | `planned` |

---

## 阶段 1：PRD

### 公共前置

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/10-prd/00-index.md` | PRD 索引与阅读顺序 | `frozen` |
| `docs/10-prd/01-product-context.md` | 产品背景、目标、边界、成功指标 | `frozen` |
| `docs/10-prd/02-personas-and-scenarios.md` | 角色画像与典型场景 | `frozen` |
| `docs/10-prd/03-glossary.md` | 术语表（行政班 / 教学班 / 选科 / 学年学期 …） | `frozen` |
| `docs/10-prd/04-business-rules.md` | 业务规则总表（编号 BR-xxx） | `frozen` |
| `docs/10-prd/05-permission-matrix.yaml` | 角色 × 资源 × 操作 权限矩阵 | `frozen` |
| `docs/10-prd/06-field-dictionary.yaml` | 全局字段字典（枚举、字典、复用字段） | `frozen` |
| `docs/10-prd/07-non-functional-requirements.md` | 非功能需求（性能、并发、安全、审计、可用性） | `frozen` |
| `docs/10-prd/08-data-scope-model.md` | 数据归属与数据权限模型 | `frozen` |
| `docs/10-prd/09-guardian-and-onboarding.md` | 家长绑定与学生数据采集模型（含表结构草案） | `frozen` |
| `docs/10-prd/10-data-permission-schema.md` | 数据权限表结构与关系（含范围解析与缓存失效） | `frozen` |

### 模块 PRD（首轮）

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/10-prd/modules/student/PRD.md` | 学生管理 PRD（**样板**，97 条需求，v1.0.2-draft） | `frozen` |
| `docs/10-prd/modules/student/acceptance.md` | 学生管理验收标准（97 条功能用例 + 15 条越权用例 + 30 条审计 / 性能 / 兼容用例） | `frozen` |
| `docs/10-prd/modules/teacher/PRD.md` | 教师管理 PRD（70 条需求，v1.0.1-draft） | `frozen` |
| `docs/10-prd/modules/teacher/acceptance.md` | 教师管理验收标准（84 条用例） | `frozen` |
| `docs/10-prd/modules/class/PRD.md` | 班级管理 PRD（62 条需求，v1.0.2-draft） | `frozen` |
| `docs/10-prd/modules/class/acceptance.md` | 班级管理验收标准（78 条用例） | `frozen` |
| `docs/10-prd/modules/grade/PRD.md` | 年级管理 PRD（40 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/grade/acceptance.md` | 年级管理验收标准（54 条用例） | `frozen` |
| `docs/10-prd/modules/school/PRD.md` | 学校管理 PRD（45 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/school/acceptance.md` | 学校管理验收标准（59 条用例） | `frozen` |
| `docs/10-prd/modules/term/PRD.md` | 学年学期管理 PRD（42 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/term/acceptance.md` | 学年学期管理验收标准（54 条用例） | `frozen` |
| `docs/10-prd/modules/subject/PRD.md` | 学科与学科配置 PRD（42 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/subject/acceptance.md` | 学科与学科配置验收标准（54 条用例） | `frozen` |
| `docs/10-prd/modules/promotion/PRD.md` | 升班与学籍异动 PRD（66 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/promotion/acceptance.md` | 升班与学籍异动验收标准（82 条用例） | `frozen` |
| `docs/10-prd/modules/stream/PRD.md` | 3+1+2 选科与教学班 PRD（69 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/stream/acceptance.md` | 3+1+2 选科与教学班验收标准（85 条用例） | `frozen` |
| `docs/10-prd/modules/import-export/PRD.md` | 导入导出与异步任务 PRD（52 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/import-export/acceptance.md` | 导入导出与异步任务验收标准（78 条用例，v1.0.1） | `frozen` |
| `docs/10-prd/modules/audit/PRD.md` | 审计与操作日志 PRD（40 条需求，v1.0.0-draft） | `frozen` |
| `docs/10-prd/modules/audit/acceptance.md` | 审计与操作日志验收标准（59 条用例，v1.0.1） | `frozen` |

---

## 阶段 2：业务原型

目录根：`prototypes/functional/v1/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `prototypes/functional/v1/README.md` | 原型说明、运行方式、批次对照 | `review` |
| `prototypes/functional/v1/prototype-quality-spec.md` | 原型质量规范：18 类"空洞"根因与约束、数据真实性、边界数据、角色视角、组件选择决策、交批验收清单 | `review` |
| `prototypes/functional/v1/layout-spec.yaml` | 栅格、区域划分、主内容区宽度、组件尺寸规则 | `review` |
| `prototypes/functional/v1/navigation.yaml` | 菜单树、87 项页面注册表、跳转关系、入口条件、批次 | `review` |
| `prototypes/functional/v1/page-actions.yaml` | 动作清单：按钮 → 触发 → 结果 → 权限 → 接口 | `review` |
| `prototypes/functional/v1/markup-contract.md` | 原型 HTML 必须携带的 `data-*` 语义标记与组件映射表 | `review` |
| `prototypes/functional/v1/content-samples.json` | 原型演示数据（真实感中文样例） | `review` |
| `prototypes/functional/v1/page-specs/_template.md` | 页面规格模板 | `review` |
| `prototypes/functional/v1/page-specs/student-list.md` | 学生管理列表页面规格（批次 2-1 样板） | `review` |
| `prototypes/functional/v1/page-specs/student-create.md` | 新增学生抽屉页面规格（批次 2-1 样板） | `review` |
| `prototypes/functional/v1/page-specs/student-edit.md` | 编辑学生抽屉页面规格（批次 2-1 样板） | `review` |
| `prototypes/functional/v1/page-specs/<page>.md` | 其余页面的规格：元素、状态、交互、跳转（随各批产出） | `planned` |
| `prototypes/functional/v1/assets/prototype-shell.css` | 原型外壳与组件样式（Element Plus 仿真，非生产代码） | `review` |
| `prototypes/functional/v1/assets/prototype-shell.js` | 原型演示引擎：外壳注入、角色 / 状态切换、浮层、提交模拟、校验 | `review` |
| `prototypes/functional/v1/index.html` | 原型入口与导航（批次 2-1） | `review` |
| `prototypes/functional/v1/pages/student-list.html` | 学生管理列表 + 新增 / 编辑抽屉浮层（**样板**） | `review` |
| `prototypes/functional/v1/pages/teacher-list.html` | 教师管理列表（批次 2-2a 样板） | `review` |
| `prototypes/functional/v1/page-specs/teacher-list.md` | 教师管理列表页面规格 | `review` |
| ~~`prototypes/functional/v1/pages/student-form.html`~~ | 已由 `pages/student-list.html` 内的 `data-demo-panel="PAGE-STU-CREATE" / "PAGE-STU-EDIT"` 浮层片段取代，不再单独出文件 | `waived` |
| `prototypes/functional/v1/pages/teacher-list.html` | 教师管理列表 | `planned` |
| `prototypes/functional/v1/pages/grade-list.html` | 年级管理列表 | `planned` |
| `prototypes/functional/v1/pages/class-list.html` | 班级管理列表 | `planned` |
| `prototypes/functional/v1/pages/class-detail.html` | 班级详情与花名册 | `planned` |
| `prototypes/functional/v1/pages/promotion-wizard.html` | 升班向导 | `planned` |
| `prototypes/functional/v1/pages/import-wizard.html` | 批量导入向导 | `planned` |
| `prototypes/functional/v1/pages/login.html` | 登录 | `planned` |
| `prototypes/functional/v1/pages/403.html` | 无权限页 | `planned` |
| `prototypes/functional/v1/pages/404.html` | 页面不存在 | `planned` |
| `prototypes/functional/v1/pages/500.html` | 服务异常 | `planned` |

---

## 阶段 3：高保真原型

目录根：`prototypes/high-fidelity/v1/`（**独立目录，不复用阶段 2 的文件**）

| 路径 | 用途 | 状态 |
|---|---|---|
| `index.html` | 高保真原型入口 | `planned` |
| `README.md` | 与业务原型的差异说明、视觉规范摘要 | `planned` |
| `design-tokens.json` | 色彩、字号、间距、圆角、阴影、层级 | `planned` |
| `component-spec.md` | 组件外观与状态规格（按钮、表格、表单、抽屉、徽标…） | `planned` |
| `component-mapping.yaml` | 原型元素 → Element Plus 组件 的映射 | `planned` |
| `business-components.yaml` | 业务组件清单（学生选择器、班级树…） | `planned` |
| `visual-checklist.md` | 视觉验收清单（对齐、间距、层级、响应式） | `planned` |
| `pages/**` | 高保真页面 | `planned` |
| `screenshots/**` | 1366 / 1440 / 1920 三档截图 | `planned` |
| `interaction-notes.md` | 交互说明：状态切换、加载、空态、错误 | `planned` |

---

## 阶段 4：概要设计

目录根：`docs/30-architecture/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `00-index.md` | 索引 | `planned` |
| `01-system-context.md` | 系统上下文、外部系统、边界 | `planned` |
| `02-architecture.md` | 架构图（逻辑 + 部署 + 容器） | `planned` |
| `03-module-division.md` | 模块划分与服务边界 | `planned` |
| `04-tech-selection.md` | 技术选型与理由（引用 `stack-lock.md`） | `planned` |
| `05-data-ownership.md` | 数据归属：哪张表归哪个服务 | `planned` |
| `06-api-catalog.md` | 接口清单（模块 × 资源 × 操作 × operationId） | `planned` |
| `07-sync-async-boundary.md` | 同步 / 异步边界与消息清单 | `planned` |
| `08-cache-strategy.md` | Redis 缓存键、失效策略、隔离策略 | `planned` |
| `09-permission-architecture.md` | 数据权限落地架构（含租户 / 学校 / 年级 / 班级） | `planned` |
| `10-mobile-and-toc-extension.md` | 移动端与 ToC 扩展位置（不做实现） | `planned` |
| `diagrams/*.mmd` | Mermaid 架构图源文件 | `planned` |

---

## 阶段 5：详细设计与建表

目录根：`docs/40-detailed-design/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `00-index.md` | 索引 | `planned` |
| `modules/<module>/design.md` | 模块详细设计：事务边界、并发、校验、失败恢复 | `planned` |
| `api/openapi.yaml` | OpenAPI 3 契约（全部首轮接口） | `planned` |
| `api/error-codes.yaml` | 错误码表 | `planned` |
| `diagrams/sequence/*.mmd` | 时序图 | `planned` |
| `diagrams/state/*.mmd` | 状态机（学生学籍状态、升班任务状态…） | `planned` |
| `diagrams/class/*.mmd` | 类图 / 领域模型图 | `planned` |
| `frontend-page-tree.yaml` | 前端页面树、路由、组件归属 | `planned` |
| `page-action-api-map.yaml` | 页面操作 → 组件 → API operationId 映射 | `planned` |
| `database/physical-schema.md` | **逐表说明：字段、类型、可空、默认值、注释** | `planned` |
| `database/er-diagram.mmd` | ER 图源文件 | `planned` |
| `database/domain-table-map.csv` | 领域对象 → 表 映射表 | `planned` |
| `database/keys-and-indexes.md` | 唯一键、外键、索引清单与理由 | `planned` |
| `database/check-sql.sql` | 结构与数据一致性检查 SQL | `planned` |
| `database/migration-plan.md` | 迁移顺序、回滚方案、存量升级路径 | `planned` |
| `runtime-design.md` | 异步任务、消息、缓存、幂等的运行时设计 | `planned` |
| `migrations/V1__edu_*.sql` | 教育域建表脚本 | `planned` |

---

## 阶段 6：生产前端

目录根：`apps/plus-ui/src/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `api/edu/<module>/index.ts` | 接口层 | `planned` |
| `api/edu/<module>/types.ts` | 请求 / 响应类型 | `planned` |
| `types/edu/**` | 领域类型 | `planned` |
| `views/edu/<module>/index.vue` | 列表页 | `planned` |
| `views/edu/<module>/components/*.vue` | 页面级组件 | `planned` |
| `views/edu/<module>/composables/*.ts` | 页面状态与逻辑 | `planned` |
| `components/Edu/**` | 业务通用组件 | `planned` |
| `router/edu.ts` | 教育模块路由（如需本地路由） | `planned` |
| `__tests__/**` | 组件与工具测试 | `planned` |

---

## 阶段 7：生产后端

目录根：`services/RuoYi-Cloud-Plus/ruoyi-modules/ruoyi-edu/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `pom.xml` | 模块 POM | `planned` |
| `src/main/java/org/dromara/edu/controller/**` | REST 接口 | `planned` |
| `src/main/java/org/dromara/edu/service/**` | 业务接口与实现 | `planned` |
| `src/main/java/org/dromara/edu/mapper/**` | MyBatis-Plus Mapper | `planned` |
| `src/main/java/org/dromara/edu/domain/**` | 实体 | `planned` |
| `src/main/java/org/dromara/edu/domain/bo/**` | 请求 BO | `planned` |
| `src/main/java/org/dromara/edu/domain/vo/**` | 响应 VO | `planned` |
| `src/main/java/org/dromara/edu/enums/**` | 枚举 | `planned` |
| `src/main/java/org/dromara/edu/datascope/**` | 数据权限扩展 | `planned` |
| `src/main/resources/mapper/edu/**/*.xml` | 复杂 SQL | `planned` |
| `src/test/java/org/dromara/edu/**` | 单元与集成测试 | `planned` |

---

## 阶段 8：联调验收

| 路径 | 用途 | 状态 |
|---|---|---|
| `evidence/stage0/**` … `evidence/stage8/**` | 各阶段命令输出与截图 | `planned` |
| `docs/50-delivery/acceptance-checklist.md` | 验收清单 | `planned` |
| `docs/50-delivery/test-plan.md` | 测试计划（含越权、并发、幂等用例） | `planned` |
| `docs/50-delivery/deployment-guide.md` | Docker 部署说明 | `planned` |
| `docs/50-delivery/known-issues.md` | 已知问题与延后项 | `planned` |
