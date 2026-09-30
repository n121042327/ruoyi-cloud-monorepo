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
| `docs/00-governance/change-requests/CR-004.md` | 变更申请：补齐教师模块字段字典并统一「校领导」对教师主体只读口径（已批准并执行） | `review` |
| `docs/00-governance/change-requests/CR-005.md` | 变更申请：补齐年级模块的权限口径与字段字典（待批准，关联 GAP-034 ~ GAP-037） | `review` |
| `docs/00-governance/change-requests/CR-006.md` | 变更申请：新增系统超级管理员角色 `super_admin`（已批准并执行，关联 D-057 / BR-ORG-014） | `review` |
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
| `docs/10-prd/06-field-dictionary.yaml` | 全局字段字典（枚举、字典、复用字段）；`CR-013` 补登记升班模块 6 个字段与 1 条部分唯一说明 | `frozen` |
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
| `docs/10-prd/modules/promotion/PRD.md` | 升班与学籍异动 PRD（66 条需求，v1.0.3-draft；CR-009 补登记调整弹窗、CR-012 收敛升班权限动作、CR-013 补登记取消确认片段） | `frozen` |
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
| `prototypes/functional/v1/pages/teacher-list.html` | 教师管理列表（批次 2-2a 样板）+ 教师详情 / 新增 / 编辑 / 离职 / 放弃确认五个浮层片段（批次 2-2b-1） | `review` |
| `prototypes/functional/v1/page-specs/teacher-list.md` | 教师管理列表页面规格 | `review` |
| `prototypes/functional/v1/page-specs/teacher-detail.md` | 教师详情页面规格（六分区、跨校任教与空角色两种形态） | `review` |
| `prototypes/functional/v1/page-specs/teacher-create.md` | 新增教师三步抽屉页面规格 | `review` |
| `prototypes/functional/v1/page-specs/teacher-edit.md` | 编辑教师抽屉页面规格（字段级可编辑性矩阵） | `review` |
| `prototypes/functional/v1/page-specs/teacher-role.md` | 教育角色分配弹窗页面规格（学校级角色 + 年级主任任职） | `review` |
| `prototypes/functional/v1/page-specs/teacher-assign.md` | 任教关系设置独立页页面规格（双栏：班级视角 / 教师视角） | `review` |
| `prototypes/functional/v1/pages/teacher-assign.html` | 任教关系设置（独立页，批次 2-2b-2） | `review` |
| ~~`prototypes/functional/v1/pages/student-form.html`~~ | 已由 `pages/student-list.html` 内的 `data-demo-panel="PAGE-STU-CREATE" / "PAGE-STU-EDIT"` 浮层片段取代，不再单独出文件 | `waived` |
| `prototypes/functional/v1/pages/grade-list.html` | 年级管理列表（批次 2-2b-2b 样板页）+ 删除年级二次确认浮层片段 `DIALOG-GRD-DELETE` | `review` |
| `prototypes/functional/v1/page-specs/grade-list.md` | 年级管理列表页面规格（字段 / 动作 / 状态 / 权限 / 样例数据 / 自查） | `review` |
| `prototypes/functional/v1/page-specs/grade-detail.md` | 年级详情抽屉页面规格（5 分区、两种样本） | `review` |
| `prototypes/functional/v1/page-specs/grade-create.md` | 新建 / 编辑年级弹窗页面规格（一窗两态、字段级只读） | `review` |
| `prototypes/functional/v1/page-specs/grade-batch.md` | 按学段批量生成弹窗页面规格（预览与冲突跳过） | `review` |
| `prototypes/functional/v1/page-specs/grade-leader.md` | 指定年级主任弹窗页面规格（任职清单、四条保存前检查） | `review` |
| `prototypes/functional/v1/page-specs/grade-archive.md` | 归档确认弹窗页面规格（引用情况、原因必填） | `review` |
| `docs/00-governance/change-requests/CR-007.md` | 变更申请：统一浮层载体为弹窗，与 apps/plus-ui 一致（已批准并执行，关联 D-058） | `review` |
| `docs/00-governance/change-requests/CR-008.md` | 变更申请：表单类改弹窗、详情类保留抽屉、含表格改独立页（已批准并执行，关联 GAP-039 / D-059） | `review` |
| `evidence/stage2-prototype/verify-carrier-change.html` | 载体变更回归 harness（14 条断言：表单是弹窗、详情仍是抽屉、弹窗内无 drawer-* 钩子） | `review` |
| `evidence/stage2-prototype/verify-grade-list.html` | 年级列表交互验证 harness（同源 iframe + 真实事件派发，38 条断言） | `review` |
| `evidence/stage2-prototype/verify-detail-entry.html` | 详情浮层入口验证 harness（年级 / 教师 / 学生三个列表页，6 条断言，关联 D-060） | `review` |
| `prototypes/functional/v1/pages/class-list.html` | 班级管理列表（批次 2-3a 样板页）+ 停用 / 删除二次确认片段 `DIALOG-CLS-DISABLE` / `DIALOG-CLS-DELETE` + 批次 2-3c 的四个弹窗片段 `PAGE-CLS-CREATE` / `PAGE-CLS-BATCH` / `PAGE-CLS-COPY` / `PAGE-CLS-LEADER` | `review` |
| `prototypes/functional/v1/page-specs/class-list.md` | 班级管理列表页面规格（字段 / 动作 / 状态 / 权限 / 样例数据 / 自查） | `review` |
| `docs/00-governance/change-requests/CR-009.md` | 变更申请：班级模块载体按 D-059 修正 + 补登记 5 个 PRD 要求的页面（已批准并执行，关联 GAP-040 / GAP-041 / D-061） | `review` |
| `docs/00-governance/change-requests/CR-010.md` | 变更申请：补齐班级模块三项公共前置（任课教师班级读权限 / 班级状态枚举 / 校区与教室字段）（已批准并执行，关联 GAP-043 ~ 045 / D-063） | `review` |
| `docs/00-governance/change-requests/CR-011.md` | 变更申请：把「在读名单」口径收敛为「只有在读状态计入」，并修正高二 (1) 班样例数（已批准并执行，关联 GAP-051 / D-068） | `review` |
| `docs/00-governance/change-requests/CR-012.md` | 变更申请：升班任务的权限动作收敛为 update、校领导收回 approve、年级主任补只读参与（已批准并执行，关联 GAP-052 / GAP-053 / D-069） | `review` |
| `docs/00-governance/change-requests/CR-013.md` | 变更申请：补齐升班模块的两项公共前置（6 个字段登记 + 取消确认片段登记）（已批准并执行，关联 GAP-054 / D-071） | `review` |
| `evidence/stage2-prototype/verify-class-list.html` | 班级列表交互验证 harness（同源 iframe + 真实事件派发，39 条断言，CL-01 ~ CL-39；CL-34 已覆盖批次 2-3c 新增的 ACT-CLS-030 ~ 047） | `review` |
| `prototypes/functional/v1/pages/class-detail.html` | 班级详情独立页（批次 2-3b：基本信息卡 + 花名册 / 任课教师 / 班主任任职历史 / 变更记录） | `review` |
| `prototypes/functional/v1/page-specs/class-detail.md` | 班级详情页面规格（字段 / 动作 / 状态 / 权限 / 样例数据 / 自查） | `review` |
| `evidence/stage2-prototype/verify-class-detail.html` | 班级详情交互验证 harness（同源 iframe + 真实事件派发，20 条断言，CD-01 ~ CD-20） | `review` |
| `evidence/stage2-prototype/class-detail_1440x900.png` | 班级详情截图（设计基准分辨率，花名册页签） | `review` |
| `evidence/stage2-prototype/class-detail_tab-*.png`（3 张，实际命名见 README 第 1 节） | 任课教师 / 班主任任职历史 / 变更记录三个页签截图 | `review` |
| `evidence/stage2-prototype/class-detail_verify-results.png` | 班级详情 harness 结果清单（CD-01 ~ CD-20 全部通过） | `review` |
| `evidence/stage2-prototype/class-list_1440x900.png` | 班级列表截图（设计基准分辨率，默认态） | `review` |
| `evidence/stage2-prototype/class-list_role-*.png`（8 张，实际命名见 README 第 1 节） | 班级列表的 8 个角色形态截图 | `review` |
| `evidence/stage2-prototype/class-list_dialog-disable_1440x900.png` | 停用班级二次确认截图 | `review` |
| `evidence/stage2-prototype/class-list_dialog-delete_1440x900.png` | 删除班级二次确认截图 | `review` |
| `evidence/stage2-prototype/class-list_more-menu_1440x900.png` | 行内「更多 ▾」下拉截图 | `review` |
| `evidence/stage2-prototype/class-list_state-*.png`（7 张，实际命名见 README 第 1 节） | 班级列表的七类页面状态截图 | `review` |
| `evidence/stage2-prototype/class-list_verify-results.png` | 班级列表 harness 结果清单（CL-01 ~ CL-39 全部通过） | `review` |
| `prototypes/functional/v1/page-specs/class-create.md` | 新建 / 编辑班级弹窗页面规格（一窗两态、字段级只读、唯一性冲突、三个后续动作） | `review` |
| `prototypes/functional/v1/page-specs/class-batch.md` | 批量生成班级弹窗页面规格（序号区间、预览与冲突跳过） | `review` |
| `prototypes/functional/v1/page-specs/class-copy.md` | 复制班级弹窗页面规格（9 项源班级摘要、不复制花名册与班主任、GAP-049） | `review` |
| `prototypes/functional/v1/page-specs/class-leader.md` | 指定 / 变更班主任弹窗页面规格（两态、候选人在职校验、四条保存前检查） | `review` |
| `evidence/stage2-prototype/verify-class-dialogs.html` | 班级四个弹窗交互验证 harness（同源 iframe + 真实事件派发 + 第二个 iframe 验证跨页跳转，36 条断言，CDL-01 ~ CDL-36） | `review` |
| `evidence/stage2-prototype/class-create_1440x900.png` | 新建班级弹窗截图（新建态默认值，两列表单） | `review` |
| `evidence/stage2-prototype/class-create_edit_1440x900.png` | 新建 / 编辑班级弹窗的编辑态截图（学期 / 年级 / 类型 / 班主任只读） | `review` |
| `evidence/stage2-prototype/class-batch_1440x900.png` | 批量生成班级弹窗截图（含 3 行「已存在，跳过」预览） | `review` |
| `evidence/stage2-prototype/class-copy_1440x900.png` | 复制班级弹窗截图（源班级 9 项摘要，3 项标为不复制） | `review` |
| `evidence/stage2-prototype/class-leader_1440x900.png` | 指定 / 变更班主任弹窗截图（变更态，含当前班主任与任职历史） | `review` |
| `evidence/stage2-prototype/class-leader_none_1440x900.png` | 指定班主任弹窗截图（未指定班主任的空态） | `review` |
| `evidence/stage2-prototype/class-dialogs_verify-results.png` | 班级四个弹窗 harness 结果清单（CDL-01 ~ CDL-36 全部通过） | `review` |
| `prototypes/functional/v1/pages/class-roster-add.html` | 编班（添加学生）独立页（批次 2-3d：左学生池 + 右待加入清单、冲突整体拒绝并给调班入口） | `review` |
| `prototypes/functional/v1/pages/class-move-students.html` | 批量迁学生独立页（批次 2-3d：选学生 → 选目标班 → 影响预览 → 执行） | `review` |
| `prototypes/functional/v1/page-specs/class-roster-add.md` | 编班页页面规格（字段裁剪、勾选可用性、冲突与整体拒绝、调班入口） | `review` |
| `prototypes/functional/v1/page-specs/class-move-students.md` | 批量迁学生页页面规格（可迁移范围、跨年级与停用班级校验、影响预览） | `review` |
| `prototypes/functional/v1/page-specs/class-transfer.md` | 调班弹窗与移出确认片段的页面规格（两块都随班级详情交付） | `review` |
| `evidence/stage2-prototype/verify-class-roster.html` | 编班 / 批量迁学生 / 移出与调班交互验证 harness（三个 iframe，34 条断言，RA-01 ~ RA-13、MV-01 ~ MV-10、TR-01 ~ TR-11） | `review` |
| `evidence/stage2-prototype/class-roster-add_1440x900.png` | 编班页截图（默认态：15 行学生池 + 空待加入清单） | `review` |
| `evidence/stage2-prototype/class-roster-add_conflict_1440x900.png` | 编班页截图（冲突态：待加入清单标红 + 冲突清单整体拒绝 + 调班入口） | `review` |
| `evidence/stage2-prototype/class-move-students_1440x900.png` | 批量迁学生页截图（默认态：源班级 2 名在读全选 + 影响预览） | `review` |
| `evidence/stage2-prototype/class-move-students_stopped_1440x900.png` | 批量迁学生页截图（目标班级已停用时的拦截形态） | `review` |
| `evidence/stage2-prototype/class-detail_dialog-remove_1440x900.png` | 移出确认片段截图（带入 2 名成员） | `review` |
| `evidence/stage2-prototype/class-detail_dialog-transfer_1440x900.png` | 调班弹窗截图（从编班页冲突行深链接带入该生与源 / 目标班级） | `review` |
| `evidence/stage2-prototype/class-roster_verify-results.png` | 编班 / 批量迁学生 / 移出与调班 harness 结果清单（RA/MV/TR 共 34 条全部通过） | `review` |
| `evidence/stage2-prototype/class-list_*.png`（21 张，见 README 第 1 节） | `CR-011` 后按新口径重拍：页头汇总「在读 148 人」、高二 (1) 班在读 1；含 verify-results | `review` |
| `evidence/stage2-prototype/verify-promotion-list.html` | 升班任务列表交互验证 harness（PRM-01 ~ PRM-36，36 条断言全部通过） | `review` |
| `evidence/stage2-prototype/promotion-list_*.png`（14 张，见 README 第 6 节） | 升班任务列表截图：3 个分辨率 + 4 种角色形态 + 2 类筛选 + 取消确认片段 + 4 类状态 | `review` |
| `evidence/stage2-prototype/interaction-verification.md` | 原型交互可点性验证报告（批次 2-2b-2b 的 GL-01 ~ GL-38、D-060 的 RD-01 ~ RD-06、批次 2-3e-s1 的 PRM-01 ~ PRM-36 实测结果） | `review` |
| `prototypes/functional/v1/pages/class-list.html` | 班级管理列表 | `planned` |
| `prototypes/functional/v1/pages/class-detail.html` | 班级详情与花名册 | `planned` |
| `prototypes/functional/v1/pages/promotion-list.html` | 升班任务列表（批次 2-3e-s1 首件样板：12 行样例覆盖 8 个状态 + 同页确认片段 DIALOG-PRM-CANCEL） | `review` |
| `prototypes/functional/v1/page-specs/promotion-list.md` | 升班任务列表页面规格（状态驱动的行内动作、权限与数据范围、样例数据、自查） | `review` |
| `prototypes/functional/v1/pages/promotion-wizard.html` | 升班向导（四步向导与结果页，批次 2-3e 剩余 6 页） | `planned` |
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
