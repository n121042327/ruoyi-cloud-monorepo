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
| `docs/00-governance/traceability.yaml` | 追踪矩阵：626 条需求全量（生成物）；接口链 580 条、表链 537 条，178 个 operationId 全部可追溯到需求（GAP-081 已闭合） | `review` |
| `docs/00-governance/delivery-gap-remediation.md` | 五项交付缺口核查与补齐方案（样板方案已确认，验收待确认） | `review` |
| `docs/00-governance/filter-logic-audit.md` | 全部 45 页筛选顺序与学校上下文静态核查 | `review` |
| `docs/00-governance/change-requests/CR-020.md` | 学生追踪、映射与筛选 v2 样板变更记录 | `review` |
| `docs/00-governance/change-requests/CR-021.md` | 编班四列、组合定位与两类班级支持的补充版本 | `review` |
| `docs/10-prd/modules/class/import-template-v2.md` | 编班模板补充：用户已确认组合定位、姓名核对、行政班与教学班一起支持 | `review` |
| `prototypes/functional/v2/pages/student-list.html` | 学生业务样板：筛选级联、学校上下文复位与顺序调整，待人工验收 | `review` |
| `prototypes/functional/v2/assets/prototype-shell.js` | v2 业务外壳：增加角色变更通知，v1 保留 | `review` |
| `prototypes/functional/v2/layout-spec.yaml` | 学生筛选 v2 差异规则 | `review` |
| `prototypes/high-fidelity/v2/pages/student-list.html` | 同步业务样板的高保真 v2 页面 | `review` |
| `prototypes/high-fidelity/v2/assets/hifi-shell.js` | v2 高保真外壳：角色控件禁用与角色变更通知 | `review` |
| `prototypes/high-fidelity/v2/component-mapping.yaml` | 学生单文件及已有浮层 207 个元素映射样板 | `review` |
| `evidence/stage2-prototype-v2/README.md` | 本批产物、验证边界、验收方法与历史证据说明 | `review` |
| `evidence/governance/2026-10-01_filter-inventory.yaml` | 45 个页面筛选区域源码清单 | `review` |
| `tools/build_student_sample_mapping.py` | 从本批浏览器快照生成或核对组件映射 | `review` |
| `evidence/governance/2026-10-01_delivery-gap-audit.log` | 五项缺口静态核查与文档一致性检查证据，不含交互验收 | `review` |
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
| `docs/10-prd/06-field-dictionary.yaml` | 全局字段字典（枚举、字典、复用字段）；`CR-013` 补登记升班任务 6 个字段与 1 条部分唯一说明，`CR-014` 补登记升班明细 7 个字段与 3 个枚举 | `frozen` |
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
| `prototypes/functional/v1/page-specs/*.md` | 其余页面的规格：元素、状态、交互、跳转（45 个页面规格已随各批产出，含模板文件） | `review` |
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
| `docs/00-governance/change-requests/CR-014.md` | 变更申请：补齐升班明细的字段与枚举（7 个字段 + 3 个枚举 + `BR-PROMO-006` 的留级去向 + 升班 PRD 6.1 / 6.3 / 7.1 的字段口径）（已批准并执行，关联 GAP-055 / D-073） | `review` |
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
| `evidence/stage2-prototype/interaction-verification.md` | 原型交互可点性验证报告（批次 2-2b-2b 的 GL-01 ~ GL-38、D-060 的 RD-01 ~ RD-06、批次 2-3e-s1 的 PRM-01 ~ PRM-36、批次 2-3e-s2 的 PC-01 ~ PC-22 实测结果） | `review` |
| `prototypes/functional/v1/pages/class-list.html` | 班级管理列表 | `review` |
| `prototypes/functional/v1/pages/class-detail.html` | 班级详情与花名册 | `review` |
| `prototypes/functional/v1/pages/promotion-list.html` | 升班任务列表（批次 2-3e-s1 首件样板：12 行样例覆盖 8 个状态 + 同页确认片段 DIALOG-PRM-CANCEL） | `review` |
| `prototypes/functional/v1/page-specs/promotion-list.md` | 升班任务列表页面规格（状态驱动的行内动作、权限与数据范围、样例数据、自查） | `review` |
| `prototypes/functional/v1/pages/promotion-create.html` | 升班向导第一步：选择源 / 目标学年学期 + 目标年级班级齐备性 + 未结束任务冲突 + 在读规模与耗时预估（批次 2-3e-s2） | `review` |
| `prototypes/functional/v1/page-specs/promotion-create.md` | 升班向导第一步页面规格（四档校验样例、三类前置校验、权限与数据范围、自查） | `review` |
| `prototypes/functional/v1/pages/subject-list.html` | 学科与配置（列表 + 5 个弹窗：新建 / 编辑 / 选科角色 / 学段启用 / 批量初始化，批次 2-6c） | `review` |
| `evidence/stage2-prototype/verify-subject.html` | 学科与配置交互验证 harness（SB-01 ~ SB-14 共 14 条断言全部通过） | `review` |
| `evidence/stage2-prototype/subject-list_*.png`、`evidence/stage2-prototype/subject-dialog-*_1440x900.png`、`evidence/stage2-prototype/subject_verify-results.png` | 批次 2-6c 截图（见 README 第 6.9 节） | `review` |
| `prototypes/functional/v1/pages/stream-config.html` | 选科配置（开放期 / 截止时间 / 逾期审批 + 固定规则卡片，批次 2-7a） | `review` |
| `prototypes/functional/v1/pages/stream-selection.html` | 学生选科（首选二选一 + 再选 4 选 2 + 当前结果与提交，批次 2-7a） | `review` |
| `prototypes/functional/v1/pages/stream-list.html` | 选科清单（8 列 6 行 + PAGE-STR-HISTORY 时间线区块 + PAGE-STR-CHANGE 变更申请弹窗，批次 2-7a） | `review` |
| `evidence/stage2-prototype/verify-stream.html` | 选科三页交互验证 harness（ST-01 ~ ST-24 共 26 条断言全部通过） | `review` |
| `evidence/stage2-prototype/stream-*.png`、`evidence/stage2-prototype/stream_verify-results.png` | 批次 2-7a 截图（12 张，见 README 第 6.10 节） | `review` |
| `docs/00-governance/change-requests/CR-016.md` | 变更申请：选科模块的字段补登记、权限码对齐与组合分布统计载体修正（已批准并执行，关联 D-080 / GAP-057） | `review` |
| `docs/10-prd/06-field-dictionary.yaml` | 新增 `stream_open_from` / `overdue_requires_approval` / `subject_combination` 三个字段（CR-016） | `review` |
| `prototypes/functional/v1/pages/stream-stat.html` | 组合分布统计（总览统计卡 + 纯 CSS 柱条 + 组合明细与学科选择人数 + 下钻，批次 2-7b） | `review` |
| `prototypes/functional/v1/pages/stream-approve.html` | 变更审批待办 + 审批弹窗 DIALOG-STR-APPROVE（批次 2-7b） | `review` |
| `prototypes/functional/v1/pages/stream-generate-class.html` | 按组合生成教学班四步向导（预览 / 执行 / 核对，批次 2-7b） | `review` |
| `prototypes/functional/v1/pages/teaching-class-list.html` | 教学班管理 + 详情抽屉 DRAWER-CLS-TEACHING + 停用确认 DIALOG-TCL-DISABLE（批次 2-7b） | `review` |
| `evidence/stage2-prototype/verify-stream-b.html` | 批次 2-7b 交互验证 harness（SB-01 ~ SB-37 共 37 条断言全部通过） | `review` |
| `evidence/stage2-prototype/stream-stat_*.png`、`evidence/stage2-prototype/stream-approve_*.png`、`evidence/stage2-prototype/stream-generate-class_*.png`、`evidence/stage2-prototype/teaching-class-list_*.png`、`evidence/stage2-prototype/stream-b_verify-results.png` | 批次 2-7b 截图（13 张，见 README 第 6.11 节） | `review` |
| `docs/00-governance/change-requests/CR-017.md` | 变更申请：教学班管理的交付面补齐（片段登记 / 3 个 operationId / 创建入口唯一，已批准并执行，关联 D-081 / GAP-058） | `review` |
| `prototypes/functional/v1/pages/audit-log-list.html` | 操作日志 + 详情抽屉 + 对象变更时间线区块 + 导出配置弹窗（批次 2-8） | `review` |
| `prototypes/functional/v1/pages/audit-ops-access.html` | 运营访问记录（租户侧自助查询，批次 2-8） | `review` |
| `prototypes/functional/v1/pages/audit-sensitive-access.html` | 敏感数据访问记录（批次 2-8） | `review` |
| `prototypes/functional/v1/pages/audit-security-event.html` | 登录与安全事件（批次 2-8） | `review` |
| `prototypes/functional/v1/pages/audit-archive.html` | 归档管理 + 运维提醒（批次 2-8） | `review` |
| `evidence/stage2-prototype/verify-audit.html` | 批次 2-8 交互验证 harness（AU-01 ~ AU-28 共 28 条断言全部通过） | `review` |
| `evidence/stage2-prototype/audit-*.png` | 批次 2-8 截图（9 张，见 README 第 6.12 节） | `review` |
| `docs/00-governance/change-requests/CR-018.md` | 变更申请：审计模块补登记日志导出配置片段（已批准并执行，关联 D-082 / GAP-059） | `review` |
| `prototypes/functional/v1/pages/async-task-list.html` | 异步任务列表 + 任务详情抽屉 + 导出配置弹窗（批次 2-9） | `review` |
| `prototypes/functional/v1/pages/dead-letter-task.html` | 死信任务 + 重放确认片段（批次 2-9） | `review` |
| `evidence/stage2-prototype/verify-task.html` | 批次 2-9 交互验证 harness（AT-01 ~ AT-22 共 22 条断言全部通过） | `review` |
| `evidence/stage2-prototype/async-task-list_*.png`、`evidence/stage2-prototype/dead-letter-task_*.png`、`evidence/stage2-prototype/task_verify-results.png` | 批次 2-9 截图（9 张，见 README 第 6.13 节） | `review` |
| `docs/00-governance/change-requests/CR-019.md` | 变更申请：导入导出模块补登记死信重放确认片段（已批准并执行，关联 D-083 / GAP-060） | `review` |
| `prototypes/high-fidelity/v1/design-tokens.json` | 阶段 3 视觉 token（机器可读，权威值；阶段 6 搬进 apps/plus-ui） | `review` |
| `prototypes/high-fidelity/v1/visual-spec.yaml` | 阶段 3 视觉规范的人类可读摘要（与 design-tokens.json 同源） | `review` |
| `prototypes/high-fidelity/v1/component-spec.md` | 阶段 3 组件规格（变体 / 尺寸 / 状态 / Element Plus 映射 / 禁止事项） | `review` |
| `prototypes/high-fidelity/v1/component-mapping.yaml` | 阶段 3 组件映射表（4 个页面 32 个可交互元素 → Element Plus 组件） | `review` |
| `prototypes/high-fidelity/v1/visual-checklist.md` | 阶段 3 交批自查清单（32 条，含三档分辨率与可访问性） | `review` |
| `prototypes/high-fidelity/v1/assets/hifi.css`、`prototypes/high-fidelity/v1/assets/hifi-shell.js` | 阶段 3 高保真样式与外壳 | `review` |
| `prototypes/high-fidelity/v1/pages/*.html`（45 页） | 阶段 3 全量交付页（由阶段 2 派生：视觉层 + 外壳替换），覆盖 95 个页面编号 / 403 个动作编号 / 229 个状态片段 | `review` |
| `prototypes/high-fidelity/v1/reference/student-list-visual-reference.html` | 阶段 3 手工精修的视觉基准（不在交付清单内） | `review` |
| `prototypes/high-fidelity/v1/page-manifest.yaml` | 阶段 3 逐页清单（来源 / 页面编号 / 动作编号 / 状态片段 / 元素计数） | `review` |
| `tools/make_hifi_pages.py`、`tools/make_hifi_coverage.py` | 阶段 3 派生工具与覆盖度工具（幂等；集合差异为 0 才退出 0） | `review` |
| `evidence/stage3-highfidelity/verify-hifi-coverage.html` | 阶段 3 覆盖度 harness（CV-01 ~ CV-45 + 3 条集合级检查，48 / 48 通过） | `review` |
| `evidence/stage3-highfidelity/pages/*.png`（53 张） | 阶段 3 交付页截图：45 页 1440×900 + 4 个代表页 1366 / 1920 | `review` |
| `evidence/stage3-highfidelity/hifi-coverage_verify-results.png` | 阶段 3 覆盖度 harness 结果清单 | `review` |
| `prototypes/high-fidelity/v1/index.html`、`prototypes/high-fidelity/v1/README.md` | 阶段 3 入口与交付说明（3-0 ~ 3-9 批次表） | `review` |
| `evidence/stage3-highfidelity/verify-hifi-student.html` | 阶段 3 样板批交互验证 harness（HF-01 ~ HF-18 共 18 条断言全部通过） | `review` |
| `evidence/stage3-highfidelity/student-list_*.png`、`evidence/stage3-highfidelity/hifi-student_verify-results.png` | 阶段 3 样板批截图（10 张：三档分辨率 + 抽屉 / 弹窗 + 3 种角色 + 空态 / 无权限 + harness 结果） | `review` |
| `evidence/stage3-highfidelity/README.md` | 阶段 3 验收证据与「阶段 2 vs 阶段 3」差异表 | `review` |
| `prototypes/functional/v1/pages/school-campus.html` | 校区管理（列表 + 页内表单 + 停用二次确认，批次 2-6b） | `review` |
| `prototypes/functional/v1/pages/school-init.html` | 开通初始化四步向导（`initSchoolBaseline` 幂等，批次 2-6b） | `review` |
| `prototypes/functional/v1/pages/term-list.html` | 学年列表 + 新建学年 / 设为当前 / 归档三个弹窗（批次 2-6b） | `review` |
| `prototypes/functional/v1/pages/term-terms.html` | 学期管理（表格 + 页内表单 + 删除二次确认 + 引用检查，批次 2-6b） | `review` |
| `evidence/stage2-prototype/verify-org-config.html` | 批次 2-6b 交互验证 harness（CP / SI / TL / TT 共 23 条断言全部通过） | `review` |
| `evidence/stage2-prototype/school-campus_*.png`、`evidence/stage2-prototype/school-init_*.png`、`evidence/stage2-prototype/term-list_*.png`、`evidence/stage2-prototype/term-terms_*.png`、`evidence/stage2-prototype/org-config_verify-results.png` | 批次 2-6b 截图（见 README 第 6.8 节） | `review` |
| `prototypes/functional/v1/pages/school-list.html` | 学校管理列表 + 详情抽屉 + 4 个弹窗（批次 2-6a） | `review` |
| `prototypes/functional/v1/page-specs/school.md` | 学校管理页面规格（覆盖 6 个页面编号） | `review` |
| `docs/00-governance/change-requests/CR-015.md` | 变更申请：批次 2-6 的 9 条表单浮层由抽屉改为弹窗（已批准并执行，关联 D-077） | `review` |
| `evidence/stage2-prototype/verify-school.html` | 学校管理交互验证 harness（SC-01 ~ SC-16 共 16 条断言全部通过） | `review` |
| `evidence/stage2-prototype/school-list_*.png`、`evidence/stage2-prototype/school-detail_1440x900.png`、`evidence/stage2-prototype/school-dialog-*_1440x900.png`、`evidence/stage2-prototype/school_verify-results.png` | 批次 2-6a 截图（见 README 第 6.7 节） | `review` |
| `prototypes/functional/v1/pages/student-cross-transfer.html` | 跨校转学（转出校视角）四步向导（批次 2-5） | `review` |
| `prototypes/functional/v1/pages/promotion-transfer.html` | 跨校转学（转入校视角）四步向导：待接收 → 接收 → 报到（批次 2-5） | `review` |
| `prototypes/functional/v1/pages/promotion-history.html` | 异动历史列表（追加式记录 + 跨页登记入口，批次 2-5） | `review` |
| `prototypes/functional/v1/page-specs/student-detail.md` | 学生详情抽屉与变更记录区块的规格 | `review` |
| `prototypes/functional/v1/page-specs/student-status.md` | 学籍异动弹窗的规格（状态机、阶段与审批约束） | `review` |
| `prototypes/functional/v1/page-specs/student-transfer.md` | 调班弹窗的规格 | `review` |
| `prototypes/functional/v1/page-specs/promotion-change.md` | 升班口径异动登记的规格（同字段同接口） | `review` |
| `prototypes/functional/v1/page-specs/cross-school-transfer.md` | 跨校转学两侧向导的规格 | `review` |
| `prototypes/functional/v1/page-specs/promotion-history.md` | 异动历史的规格 | `review` |
| `evidence/stage2-prototype/verify-student-module.html` | 批次 2-5 交互验证 harness（SM / CT / PT / PH 共 28 条断言全部通过） | `review` |
| `evidence/stage2-prototype/student-detail_*.png`、`evidence/stage2-prototype/student-cross-transfer_*.png`、`evidence/stage2-prototype/promotion-transfer_*.png`、`evidence/stage2-prototype/promotion-history_*.png`、`evidence/stage2-prototype/student-module_verify-results.png` | 批次 2-5 截图（见 README 第 6.6 节） | `review` |
| `evidence/stage2-prototype/verify-promotion-create.html` | 升班向导第一步交互验证 harness（PC-01 ~ PC-22，22 条断言全部通过） | `review` |
| `evidence/stage2-prototype/promotion-create_*.png`（11 张，见 README 第 6.2 节） | 升班向导第一步截图：3 个分辨率 + 3 档校验样例 + 无权限形态 + 3 类状态 + harness 结果 | `review` |
| `prototypes/functional/v1/pages/promotion-preview.html` | 升班向导第二步（预览与调整：双栏源班级 + 逐学生明细，含 PAGE-PRM-ADJUST 片段） | `review` |
| `prototypes/functional/v1/pages/promotion-validate.html` | 升班向导第三步（通过 / 警告 / 错误三分类 + 下钻 + 标记跳过 + 执行被拦） | `review` |
| `prototypes/functional/v1/pages/promotion-execute.html` | 升班向导第四步（进度条 + 四类计数 + 处理时间线 + 排队中形态） | `review` |
| `prototypes/functional/v1/pages/promotion-result.html` | 升班结果与重试（四类清单 + 只重试失败项 / 继续执行剩余项 + 结果导出） | `review` |
| `prototypes/functional/v1/page-specs/promotion-preview.md` | 升班向导第二步的页面规格（双栏结构、字段、动作、状态、权限、样例、自查） | `review` |
| `prototypes/functional/v1/page-specs/promotion-adjust.md` | 调整学生去向弹窗的页面规格（处理方式驱动必填项、留级去向口径） | `review` |
| `prototypes/functional/v1/page-specs/promotion-validate.md` | 升班校验结果页的页面规格（三分类、下钻、标记跳过） | `review` |
| `prototypes/functional/v1/page-specs/promotion-execute.md` | 执行与进度页的页面规格（进度、计数、时间线、排队中） | `review` |
| `prototypes/functional/v1/page-specs/promotion-result.md` | 执行结果与重试页的页面规格（四类清单、重试、导出） | `review` |
| `prototypes/functional/v1/assets/wizard.css` | 向导类页面共用样式（步骤条、sticky 操作条、分组清单、数值卡、进度条、弹窗清单行） | `review` |
| `evidence/stage2-prototype/verify-promotion-wizard.html` | 升班向导第 2 ~ 4 步交互验证 harness（PV / ADJ / VD / EX / RS / ALL 共 39 条断言全部通过） | `review` |
| `evidence/stage2-prototype/promotion-preview_*.png`（5 张）、`promotion-validate_*.png`（3 张）、`promotion-execute_*.png`（3 张）、`promotion-result_*.png`（4 张）、`evidence/stage2-prototype/promotion-wizard_verify-results.png` | 升班向导第 2 ~ 4 步截图（见 README 第 6.4 节）；含调整弹窗、只读角色、错误下钻与四类清单切换 | `review` |
| `prototypes/functional/v1/pages/import-wizard.html` | 导入向导（四步 + 模板 / 校验 / 执行三个区块，`ACT-IMP-001` ~ `010`） | `review` |
| `prototypes/functional/v1/pages/student-import.html` | 学生批量导入向导（14 列模板，不含学号列） | `review` |
| `prototypes/functional/v1/pages/teacher-import.html` | 教师批量导入向导（9 列模板，工号租户内唯一） | `review` |
| `prototypes/functional/v1/pages/class-import-roster.html` | 编班表导入向导（4 列，含目标班级列；列清单见 GAP-056） | `review` |
| `prototypes/functional/v1/page-specs/import-wizard.md` | 导入向导页面规格（覆盖 PAGE-IMP-WIZARD + 三个区块） | `review` |
| `prototypes/functional/v1/page-specs/student-import.md` | 学生批量导入的页面规格（14 列模板、不含学号列、学号对照表） | `review` |
| `prototypes/functional/v1/page-specs/teacher-import.md` | 教师批量导入的页面规格（9 列模板、工号租户内唯一） | `review` |
| `prototypes/functional/v1/page-specs/class-import-roster.md` | 编班表导入的页面规格（4 列、两阶段、幂等、冲突修正提示） | `review` |
| `prototypes/functional/v1/pages/login.html` | 登录（不套管理外壳；多校切换、s + 学号、首登改密、锁定口径） | `review` |
| `prototypes/functional/v1/pages/403.html` | 无权限页（不降级为全量，`DS-DENY-03` / `NFR-SEC-05`） | `review` |
| `prototypes/functional/v1/page-specs/error-pages.md` | 异常页规格（403 / 404 / 500 合并一份规格，三页都指向同一口径） | `review` |
| `prototypes/functional/v1/pages/404.html` | 页面不存在（指向 navigation.yaml 的页面注册表） | `review` |
| `prototypes/functional/v1/pages/500.html` | 服务异常（请求编号 + 错误码 + 不产生部分写入） | `review` |
| `prototypes/functional/v1/page-specs/login.md` | 登录页规格（三类账号形态、四类状态、D-039 口径） | `review` |
| `evidence/stage2-prototype/verify-import-login.html` | 批次 2-4 交互验证 harness（IMP / MS / MT / MC / LG / ER / ALL 共 30 条断言全部通过） | `review` |
| `evidence/stage2-prototype/import-wizard*.png`（5 张）、`evidence/stage2-prototype/student-import*.png`（2 张）、`evidence/stage2-prototype/teacher-import_1440x900.png`、`evidence/stage2-prototype/class-import-roster_1440x900.png`、`evidence/stage2-prototype/login_*.png`（2 张）、`evidence/stage2-prototype/error-403_1440x900.png`、`evidence/stage2-prototype/error-404_1440x900.png`、`evidence/stage2-prototype/error-500_1440x900.png`、`evidence/stage2-prototype/import-login_verify-results.png` | 批次 2-4 截图（见 README 第 6.5 节）；含四步向导、模块导入、登录页与三个异常页 | `review` |
| `tools/run_harness.py` | 阶段 2 全量 harness 回归运行器（19 个 `verify-*.html`；兼容两种历史结果格式） | `review` |
| `evidence/stage2-prototype/harness-regression.log` | 全量回归结果：19 / 19 通过，累计断言 508 条 | `review` |

---

## 阶段 3：高保真原型

目录根：`prototypes/high-fidelity/v1/`（**独立目录，不复用阶段 2 的文件**）

| 路径 | 用途 | 状态 |
|---|---|---|
| `prototypes/high-fidelity/v1/index.html` | 高保真原型入口 | `review` |
| `prototypes/high-fidelity/v1/README.md` | 与业务原型的差异说明、视觉规范摘要 | `review` |
| `prototypes/high-fidelity/v1/design-tokens.json` | 色彩、字号、间距、圆角、阴影、层级 | `review` |
| `prototypes/high-fidelity/v1/visual-spec.yaml` | 视觉规范的人类可读摘要（与 design-tokens 同源） | `review` |
| `prototypes/high-fidelity/v1/component-spec.md` | 组件外观与状态规格（按钮、表格、表单、抽屉、徽标…） | `review` |
| `prototypes/high-fidelity/v1/component-mapping.yaml` | 原型元素 → Element Plus 组件 的映射 | `review` |
| `prototypes/high-fidelity/v1/business-components.yaml` | 业务组件清单（学生选择器、班级树…）与阶段 6 落点 | `review` |
| `prototypes/high-fidelity/v1/visual-checklist.md` | 视觉验收清单（对齐、间距、层级、响应式） | `review` |
| `prototypes/high-fidelity/v1/page-manifest.yaml` | 页面文件与页面编号 / 路由的对应 | `review` |
| `prototypes/high-fidelity/v1/pages/*.html` | 高保真页面（45 个） | `review` |
| `prototypes/high-fidelity/v1/screenshots/1366/*.png` | 1366×900 档截图（45 张） | `review` |
| `prototypes/high-fidelity/v1/screenshots/1440/*.png` | 1440×900 档截图（45 张） | `review` |
| `prototypes/high-fidelity/v1/screenshots/1920/*.png` | 1920×1080 档截图（45 张） | `review` |
| `prototypes/high-fidelity/v1/interaction-notes.md` | 交互说明：状态切换、加载、空态、错误 | `review` |
| `tools/capture_hifi_screenshots.ps1` | 三档截图生成脚本（headless Chrome） | `review` |
| `tools/make_hifi_pages_table.py` | 高保真入口页的全量清单生成器（45 页；数据来自 page-manifest + navigation） | `review` |
| `tools/check_hifi_layout.py` | 高保真布局检查（CDP 实测 45 页：搜索区行数、表格溢出、操作列 display、卡片内边距） | `review` |
| `tools/make_diagrams_doc.py` | 把全部 `*.mmd` 汇总成可预览的 `docs/diagrams.md` | `review` |

---

## 阶段 4：概要设计

目录根：`docs/30-architecture/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/30-architecture/00-index.md` | 索引 | `review` |
| `docs/30-architecture/01-system-context.md` | 系统上下文、外部系统、边界 | `review` |
| `docs/30-architecture/02-architecture.md` | 架构图（逻辑 + 部署 + 容器） | `review` |
| `docs/30-architecture/03-module-division.md` | 模块划分与服务边界 | `review` |
| `docs/30-architecture/04-tech-selection.md` | 技术选型与理由（引用 `docs/00-governance/stack-lock.md`） | `review` |
| `docs/30-architecture/05-data-ownership.md` | 数据归属：哪张表归哪个服务 | `review` |
| `docs/30-architecture/06-api-catalog.md` | 接口清单（模块 × 资源 × 操作 × operationId） | `review` |
| `docs/30-architecture/07-sync-async-boundary.md` | 同步 / 异步边界与消息清单 | `review` |
| `docs/30-architecture/08-cache-strategy.md` | Redis 缓存键、失效策略、隔离策略 | `review` |
| `docs/30-architecture/09-permission-architecture.md` | 数据权限落地架构（含租户 / 学校 / 年级 / 班级） | `review` |
| `docs/30-architecture/10-mobile-and-toc-extension.md` | 移动端与 ToC 扩展位置（不做实现） | `review` |
| `diagrams/*.mmd` | Mermaid 架构图源文件 | `review` |
| `docs/diagrams.md` | 全部 Mermaid 图汇总（阶段 4 架构图 + 阶段 5 ER / 时序 / 状态机 / 领域模型，共 41 张）；任何支持 Mermaid 的 Markdown 预览器都能直接看图 | `review` |

---

## 阶段 5：详细设计与建表

目录根：`docs/40-detailed-design/`

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/40-detailed-design/00-index.md` | 索引 | `review` |
| `docs/40-detailed-design/modules/*/design.md` | 模块详细设计：事务边界、并发、校验、失败恢复 | `review` |
| `docs/40-detailed-design/api/openapi.yaml` | OpenAPI 3 契约（全部首轮接口） | `review` |
| `docs/40-detailed-design/api/error-codes.yaml` | 错误码表 | `review` |
| `docs/40-detailed-design/api/error-codes.md` | 错误码可读版（与 YAML 同源生成） | `review` |
| `docs/40-detailed-design/diagrams/sequence/*.mmd` | 时序图 | `review` |
| `docs/40-detailed-design/diagrams/state/*.mmd` | 状态机（学生学籍状态、升班任务状态…） | `review` |
| `docs/40-detailed-design/diagrams/class/*.mmd` | 类图 / 领域模型图 | `review` |
| `docs/40-detailed-design/frontend-page-tree.yaml` | 前端页面树、路由、组件归属 | `review` |
| `docs/40-detailed-design/page-action-api-map.yaml` | 页面操作 → 组件 → API operationId 映射 | `review` |
| `docs/40-detailed-design/database/schema.yaml` | **表结构事实源（生成物理表 / ER / 迁移脚本的唯一样本）** | `review` |
| `docs/40-detailed-design/database/physical-schema.md` | **逐表说明：字段、类型、可空、默认值、注释** | `review` |
| `docs/40-detailed-design/database/er-diagram.mmd` | ER 图源文件 | `review` |
| `docs/40-detailed-design/database/domain-table-map.csv` | 领域对象 → 表 映射表 | `review` |
| `docs/40-detailed-design/database/keys-and-indexes.md` | 唯一键、外键、索引清单与理由 | `review` |
| `docs/40-detailed-design/database/check-sql.sql` | 结构与数据一致性检查 SQL | `review` |
| `docs/40-detailed-design/database/migration-plan.md` | 迁移顺序、回滚方案、存量升级路径 | `review` |
| `docs/40-detailed-design/runtime-design.md` | 异步任务、消息、缓存、幂等的运行时设计 | `review` |
| `docs/40-detailed-design/migrations/V1__edu_*.sql` | 教育域建表脚本 | `review` |

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

## 2026-10-01 字段布局与班级 v2 批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/page-field-layout.md` | 用户明确要求的字段布局约束 | `review` |
| `docs/00-governance/change-requests/CR-022.md` | 规则落盘与班级列表 v2 变更 | `review` |
| `prototypes/functional/v2/pages/class-list.html` | 班级查询布局与学校上下文修复 | `review` |
| `prototypes/high-fidelity/v2/pages/class-list.html` | 班级列表 v2 高保真 | `review` |

## 2026-10-01 教师、年级 v2 批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-023.md` | 教师与年级查询布局、学校上下文变更 | `review` |
| `prototypes/functional/v2/pages/teacher-list.html` | 教师业务原型 v2 | `review` |
| `prototypes/functional/v2/pages/grade-list.html` | 年级业务原型 v2 | `review` |
| `prototypes/high-fidelity/v2/pages/teacher-list.html` | 教师高保真 v2 | `review` |
| `prototypes/high-fidelity/v2/pages/grade-list.html` | 年级高保真 v2 | `review` |

## 2026-10-01 升班与异动历史 v2 批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-024.md` | 升班与异动历史查询变更 | `review` |
| `prototypes/functional/v2/pages/promotion-list.html` | 升班任务业务原型 v2 | `review` |
| `prototypes/functional/v2/pages/promotion-history.html` | 异动历史业务原型 v2 | `review` |
| `prototypes/high-fidelity/v2/pages/promotion-list.html` | 升班任务高保真 v2 | `review` |
| `prototypes/high-fidelity/v2/pages/promotion-history.html` | 异动历史高保真 v2 | `review` |

## 2026-10-01 班级、教师表格与编辑表单批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-025.md` | 班级、教师字段分组和列序变更 | `review` |
| `tools/align_table_form_layout.py` | 仅整理四个 v2 页面，避免重复处理 | `review` |

## 2026-10-01 教师编辑表单数据绑定批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-026.md` | 教师编辑按点击行绑定目标数据 | `review` |
| `evidence/stage2-prototype-v2/verify-teacher-edit-binding.html` | 教师编辑绑定回归（业务 / 高保真各 14 项） | `review` |

## 2026-10-01 学生、年级表格与表单布局批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-027.md` | 学生、年级表格列序与表单分区变更 | `review` |
| `tools/align_student_grade_layout.py` | 仅整理学生、年级四个 v2 页面，重复执行安全 | `review` |
| `evidence/stage2-prototype-v2/verify-student-grade-layout.html` | 学生、年级布局回归（两层各 17 项） | `review` |

## 2026-10-01 学生学籍异动、调班与异动登记浮层批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-028.md` | 三个学生浮层的字段分组与顺序变更 | `review` |
| `tools/align_student_overlay_layout.py` | 按分组配置重建三个浮层的字段块，重复执行结果一致 | `review` |
| `evidence/stage2-prototype-v2/verify-student-overlay-layout.html` | 浮层分组回归（业务 / 高保真各 13 项） | `review` |
| `evidence/stage2-prototype-v2/2026-10-01_student-overlay-layout.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 学生调班对齐与教师非在职行批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-029.md` | 学生调班与班级模块对齐（GAP-077 裁决 A） | `review` |
| `docs/00-governance/change-requests/CR-030.md` | 教师非在职行入口（GAP-075 裁决 A） | `review` |
| `evidence/stage2-prototype-v2/verify-student-transfer-alignment.html` | 学生调班字段与接口对齐回归（两层各 8 项） | `review` |
| `evidence/stage2-prototype-v2/verify-teacher-inactive-rows.html` | 教师非在职行入口回归（两层各 8 项） | `review` |
| `evidence/stage2-prototype-v2/verify-student-overlay-layout-cr029.html` | CR-028 浮层分组回归按新口径的副本（两层各 13 项） | `review` |
| `evidence/stage2-prototype-v2/verify-student-layout-list-cr029.html` | 学生模块回归按新接口名的副本（28 项） | `review` |
| `evidence/stage2-prototype-v2/verify-teacher-edit-binding-cr030.html` | 教师编辑绑定回归按新口径的副本（两层各 14 项） | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_transfer-teacher-batch-verification.log` | 本批通过计数、契约重生成与冻结检查日志 | `review` |

## 2026-10-02 年级浮层字段分组批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-031.md` | 年级详情 / 指定年级主任 / 归档 / 删除浮层分组 | `review` |
| `evidence/stage2-prototype-v2/verify-grade-overlay-layout.html` | 年级四个浮层的分组与校验回归（两层各 12 项） | `review` |
| `evidence/stage2-prototype-v2/verify-teacher-grade-detail-cr031.html` | 详情入口回归副本（修正失效断言与 iframe 就绪竞态，6 项） | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_grade-overlay-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 架构图 Mermaid 渲染修复

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-032.md` | Mermaid 渲染缺陷修复说明 | `review` |
| `tools/check_mermaid.py` | subgraph 标题写法静态检查（无 Node 依赖） | `review` |
| `evidence/governance/2026-10-02_mermaid-check.log` | 修复前后 103 张图的解析结果与负例自检 | `review` |

## 2026-10-02 学生详情抽屉字段分组

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-033.md` | 学生详情抽屉分组变更 | `review` |
| `evidence/stage2-prototype-v2/verify-student-detail-layout.html` | 学生详情分组回归（两层各 8 项） | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_student-detail-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 学校级联收窄与撤销离职口径批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-034.md` | GAP-047 下拉收窄与 GAP-078 撤销口径 | `review` |
| `evidence/stage2-prototype-v2/verify-school-cascade.html` | 学校变化后下拉收窄回归（两层各 8 项） | `review` |
| `evidence/stage2-prototype-v2/verify-teacher-inactive-rows-cr034.html` | 非在职行回归副本（TI-07 改按 REQ-TCH-071） | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_school-cascade-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 学生列表契约样板批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-035.md` | GAP-066 学生列表契约与批量调班收敛 | `review` |
| `tools/check_api_contract.py` | v2 原型 data-api 与 PRD 查询参数的契约一致性检查 | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_student-contract-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |
| `README.md` | 仓库根导航与上手说明（用户要求新增） | `review` |

## 2026-10-02 孤儿接口引用清理批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-036.md` | GAP-079 孤儿接口引用清理 | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_orphan-api-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 补齐缺失接口契约批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-037.md` | GAP-080 补 6 个缺失接口契约 | `review` |
| `evidence/governance/2026-10-02_contract-completion.log` | 生成器重跑与契约检查结果 | `review` |

## 2026-10-02 人工验收指南

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/acceptance-guide.md` | 回答「在哪验收、怎么验收」：打开方式、hash 参数、逐页核对清单、复跑命令 | `review` |

## 2026-10-02 学年学期样例批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-038.md` | GAP-067 学年学期样例确认与统一 | `review` |
| `evidence/stage2-prototype-v2/verify-term-sample.html` | 学期选项集合与归属标注回归（两层各 8 项） | `review` |
| `evidence/stage2-prototype-v2/2026-10-02_term-sample-batch-verification.log` | 本批通过计数与冻结检查日志 | `review` |

## 2026-10-02 追踪矩阵与组件映射全量批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-039.md` | GAP-063 / GAP-064 从样板扩展为全量 | `review` |
| `tools/gen_traceability.py` | 从 PRD / 验收用例 / 导航 / 页面树生成追踪矩阵骨架，保留人工字段 | `review` |
| `docs/00-governance/traceability.yaml` | 626 条需求追踪记录 + 模块接口索引（生成物） | `review` |
| `tools/build_component_mapping_all.py` | 解析 45 个高保真页面生成逐元素组件映射 | `review` |
| `prototypes/high-fidelity/v2/component-mapping-all-pages.yaml` | 45 页 / 1361 条元素映射（生成物） | `review` |
| `tools/check_gap_register.py` | 缺项表重复键检查（YAML 重复键会静默覆盖） | `review` |
| `evidence/governance/2026-10-02_traceability-mapping.log` | 本批生成结果与检查输出 | `review` |

## 2026-10-02 需求到接口与表的逐条对应批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-040.md` | GAP-081 学生与班级模块的逐条对应 | `review` |
| `docs/00-governance/requirement-links.yaml` | 人工维护的需求 → 接口 / 表对应表（159 条） | `review` |
| `evidence/governance/2026-10-02_requirement-links.log` | 本批生成结果与检查输出 | `review` |

## 2026-10-02 需求对应教师与年级批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-041.md` | GAP-081 教师与年级模块的逐条对应 | `review` |
| `evidence/governance/2026-10-02_requirement-links-teacher-grade.log` | 本批生成结果与检查输出 | `review` |

## 2026-10-02 需求逐条挂接收口与契约补齐批次

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-042.md` | GAP-081 剩余 7 个模块 356 条逐条挂接，并把 7 个漏挂接口挂回需求 | `review` |
| `docs/00-governance/change-requests/CR-043.md` | GAP-082 / GAP-083 补 6 个接口（学生激活码 4 + 教师 2） | `review` |
| `docs/00-governance/requirement-links.yaml` | 人工维护的需求 → 接口 / 表对应表（626 条全量，11 个模块） | `review` |
| `docs/00-governance/traceability.yaml` | 626 条需求追踪 + 需求级接口追溯（接口链 580 / 表链 537，生成物） | `review` |
| `evidence/governance/2026-10-02_requirement-links-all-modules.log` | 本批生成结果与四个检查脚本输出 | `review` |
| `evidence/governance/2026-10-02_contract-completion-gap082-083.log` | 契约补齐后的 operationId 数与检查输出 | `review` |

## 2026-10-02 阶段 6 首批：学生管理列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-044.md` | 阶段 6 首批（学生管理列表页）交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/index.vue` | 学生管理列表页（查询区 / 工具栏 / 表格 / 分页 / 无权限态） | `已实现` |
| `apps/plus-ui/src/views/edu/student/stu_list/composables/useStudentList.ts` | 列表页查询、分页、导出与级联逻辑 | `已实现` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentFormDialog.vue` | 新增 / 编辑弹窗（基础信息 + 教育信息） | `已实现` |
| `apps/plus-ui/src/api/edu/**` | 教育域接口层：student / school / term / grade / class | `已实现` |
| `apps/plus-ui/src/enums/edu/StudentEnum.ts` | 学段 / 性别 / 学籍状态枚举（取自字段字典） | `已实现` |
| `tools/check_fe_page_structure.py` | 生产页面 ←→ 原型的结构对照检查（阶段 6 交互对照的可执行部分） | `review` |
| `evidence/stage6-frontend/2026-10-02_student-list_verification.log` | 类型检查 / Lint / 构建 / 结构对照证据 | `review` |

## 2026-10-02 阶段 6 第二批：学生详情抽屉与三步向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-045.md` | 学生详情抽屉 + 新增编辑三步向导的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentDetailDrawer.vue` | 只读详情抽屉（四个字段分组 + 监护人 + 变更记录） | `已实现` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentFormDialog.vue` | 新增 / 编辑三步向导（学籍信息 / 证件与联系 / 监护人） | `已实现` |
| `evidence/stage6-frontend/2026-10-02_student-detail-and-form-wizard_verification.log` | 本批类型检查 / Lint / 构建 / 结构对照证据 | `review` |

## 2026-10-07 阶段 6 第三批：联系电话查看完整接口

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-046.md` | GAP-084 补 `viewStudentPhone` 并接上前端 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentDetailDrawer.vue` | 详情抽屉（本批补联系电话「查看完整」） | `已实现` |
| `evidence/stage6-frontend/2026-10-07_student-phone-reveal_verification.log` | 本批生成结果与门禁证据 | `review` |

## 2026-10-07 阶段 6 第四批：学生照片与监护人编辑 / 解绑

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-047.md` | 学生照片上传 / 查看原图与监护人编辑 / 解绑的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/GuardianTable.vue` | 可复用监护人编辑表格（向导与详情抽屉共用） | `已实现` |
| `evidence/stage6-frontend/2026-10-07_student-photo-and-guardian_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-07 阶段 6 第五批：学籍异动与调班浮层

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-048.md` | 学籍异动与调班弹窗的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentStatusDialog.vue` | 学籍异动弹窗（异动信息 / 复学报到安排 / 异动说明） | `已实现` |
| `apps/plus-ui/src/views/edu/student/stu_list/components/StudentTransferDialog.vue` | 调班弹窗（班级关系 / 调班说明） | `已实现` |
| `evidence/stage6-frontend/2026-10-07_student-status-and-transfer_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-07 阶段 6 第六批：批量导出 / 批量调班 + 异动登记入口

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-049.md` | 批量能力与异动登记入口的交付、GAP-085 登记与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_list/index.vue` | 学生列表页（本批补多选列与两个批量按钮） | `已实现` |
| `evidence/stage6-frontend/2026-10-07_student-batch-actions_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-07 阶段 6 第七批：跨校转学（转出校）向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-050.md` | GAP-085 裁决 A 的契约收敛与转出校向导交付记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_cross_transfer/index.vue` | 跨校转学（转出校）4 步向导 | `已实现` |
| `apps/plus-ui/src/api/edu/promotion/**` | 升班与学籍异动模块接口层（转学单发起 / 撤销 / 待接收清单） | `已实现` |
| `evidence/stage6-frontend/2026-10-07_cross-transfer-out_verification.log` | 本批生成结果与门禁证据 | `review` |

## 2026-10-07 阶段 6 第八批：跨校转学（转入校）向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-051.md` | 转入校侧向导的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/promotion/prm_transfer/index.vue` | 跨校转学（转入校）4 步向导 | `已实现` |
| `evidence/stage6-frontend/2026-10-07_cross-transfer-in_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第九批：班级管理列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-052.md` | 班级列表页交付 + GAP-086 登记与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/class/cls_list/index.vue` | 班级管理列表页（查询区 / 工具栏 / 表格 / 分页 / 无权限态） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_list/components/ClassFormDialog.vue` | 新建 / 编辑班级（教育信息 + 管理信息两组） | `已实现` |
| `apps/plus-ui/src/api/edu/teacher/**` | 教师接口层（班主任下拉） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_class-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十批：学生批量导入向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-053.md` | GAP-086 裁决 A 的契约收敛与导入向导交付记录 | `review` |
| `apps/plus-ui/src/views/edu/student/stu_import/index.vue` | 学生批量导入 4 步向导 | `已实现` |
| `apps/plus-ui/src/api/edu/importExport/**` | 导入导出模块接口层（模板 / 校验 / 执行 / 失败行 / 结果 / 任务详情） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_student-import_verification.log` | 本批生成结果与门禁证据 | `review` |

## 2026-10-08 阶段 7 起步：ruoyi-edu 服务模块

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-054.md` | 阶段 7 骨架与第一条纵切的交付记录 | `review` |
| `services/RuoYi-Cloud-Plus/ruoyi-modules/ruoyi-edu/**` | 教育业务服务模块（pom / 启动类 / 学生模块纵切 / application.yml） | `已实现` |
| `evidence/stage7-backend/2026-10-08_ruoyi-edu_compile.log` | 编译验证输出 | `review` |

## 2026-10-08 阶段 6 第十一批：年级管理列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-055.md` | 年级列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/grade/grd_list/index.vue` | 年级管理列表页（查询区 / 工具栏 / 表格 / 分页 / 无权限态） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_grade-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十二批：教师管理列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-056.md` | 教师列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/teacher/tch_list/index.vue` | 教师管理列表页（查询区 / 工具栏 / 表格 / 分页 / 无权限态） | `已实现` |
| `apps/plus-ui/src/api/edu/subject/**` | 学科接口层（`listSubjectOption`） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_teacher-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十三批：学科与配置列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-057.md` | 学科列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/subject/sub_list/index.vue` | 学科与配置列表页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_subject-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十四批：学年学期列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-058.md` | 学年学期列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/term/term_list/index.vue` | 学年学期列表页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_term-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十五批：学校管理列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-059.md` | 学校列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/school/sch_list/index.vue` | 学校管理列表页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_school-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十六批：操作日志列表页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-060.md` | 操作日志列表页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_log_list/index.vue` | 操作日志列表页 | `已实现` |
| `apps/plus-ui/src/api/edu/audit/**` | 审计接口层（操作日志查询与详情） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-log-list_verification.log` | 本批结构对照 / 类型检查 / Lint / 构建证据 | `review` |

## 2026-10-08 阶段 6 第十七批：操作日志详情抽屉

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-061.md` | 日志详情抽屉的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_log_list/components/LogDetailDrawer.vue` | 操作日志详情抽屉（操作信息 / 对象信息 / 变更明细） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-log-detail_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第十八批：对象变更时间线

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-062.md` | 对象变更时间线的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_log_list/components/ObjectTimelineDrawer.vue` | 对象变更时间线抽屉 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-object-timeline_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第十九批：运营访问记录

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-063.md` | 运营访问记录的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_ops_access/index.vue` | 运营访问记录页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-ops-access_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十批：敏感数据访问记录

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-064.md` | 敏感数据访问记录的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_sensitive_access/index.vue` | 敏感数据访问记录页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-sensitive-access_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十一批：登录与安全事件 + 归档管理

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-065.md` | 两页的交付与门禁记录（审计模块收官） | `review` |
| `apps/plus-ui/src/views/edu/audit/audit_security_event/index.vue` | 登录与安全事件页 | `已实现` |
| `apps/plus-ui/src/views/edu/audit/audit_archive/index.vue` | 归档管理页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_audit-security-event-and-archive_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十二批：异步任务 + 死信任务

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-066.md` | 异步任务与死信任务的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/import-export/imp_task_list/index.vue` | 异步任务列表页 | `已实现` |
| `apps/plus-ui/src/views/edu/import-export/imp_deadletter/index.vue` | 死信任务页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_async-task-and-deadletter_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十三批：异动历史

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-067.md` | 异动历史页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/promotion/prm_history/index.vue` | 异动历史页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_promotion-history_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十四批：校区管理

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-068.md` | 校区管理页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/school/sch_campus/index.vue` | 校区管理页 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_school-campus_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十五批：开通初始化向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-069.md` | 开通初始化向导的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/school/sch_init/index.vue` | 开通初始化 4 步向导 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_school-init_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十六批：学期管理页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-070.md` | 学期管理页的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/term/term_terms/index.vue` | 学期管理列表页（含删除必填原因） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_term-terms_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十七批：新建 / 编辑学年与学年归档弹窗

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-071.md` | 学年弹窗与归档弹窗的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/term/term_list/components/AcademicYearFormDialog.vue` | 新建 / 编辑学年弹窗（PAGE-TERM-CREATE） | `已实现` |
| `apps/plus-ui/src/views/edu/term/term_list/components/AcademicYearArchiveDialog.vue` | 学年归档弹窗（PAGE-TERM-ARCHIVE，含引用检查） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_term-year-dialogs_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十八批：升班任务列表与四步向导

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-072.md` | 升班列表与向导的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/promotion/prm_list/index.vue` | 升班任务列表页（PAGE-PRM-LIST） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_list/components/CancelPromotionDialog.vue` | 取消升班任务二次确认弹窗 | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_create/index.vue` | 新建升班任务（向导第 1 步） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_preview/index.vue` | 升班预览与调整（向导第 2 步） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_preview/components/AdjustItemDialog.vue` | 逐条调整弹窗（PAGE-PRM-ADJUST） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_validate/index.vue` | 升班校验（向导第 3 步） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_execute/index.vue` | 执行与进度（向导第 4 步） | `已实现` |
| `apps/plus-ui/src/views/edu/promotion/prm_result/index.vue` | 执行结果与重试 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_promotion-wizard_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第二十九批：添加学生 + 批量迁学生

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-073.md` | 添加学生 / 批量迁学生的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/class/cls_roster_add/index.vue` | 添加学生（PAGE-CLS-ROSTER-ADD） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_move/index.vue` | 批量迁学生（PAGE-CLS-MOVE） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_class-roster-add-and-move_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十批：编班表导入 + 教学班管理

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-074.md` | 编班表导入 / 教学班管理的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/class/cls_roster_import/index.vue` | 编班表导入四步向导（PAGE-CLS-ROSTER-IMPORT） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_teaching/index.vue` | 教学班管理（PAGE-CLS-TEACHING） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_teaching/components/TeachingClassDetailDrawer.vue` | 教学班详情抽屉（DRAWER-CLS-TEACHING） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_teaching/components/TeachingClassDisableDialog.vue` | 教学班停用弹窗（DIALOG-TCL-DISABLE） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_class-import-and-teaching_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十一批：选科配置 + 学生选科

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-075.md` | 选科配置 / 学生选科的交付与门禁记录 | `review` |
| `apps/plus-ui/src/api/edu/stream/index.ts` | 选科接口层（8 个 operationId） | `已实现` |
| `apps/plus-ui/src/api/edu/stream/types.ts` | 选科接口类型 | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_config/index.vue` | 选科配置（PAGE-STR-CONFIG） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_config/components/UnselectedStudentDialog.vue` | 未选科学生催办弹窗 | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_student/index.vue` | 学生选科（PAGE-STR-STUDENT） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_student/components/StreamHistoryDialog.vue` | 选科历史弹窗 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_stream-config-and-student_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十二批：选科清单 + 组合分布统计

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-076.md` | 选科清单 / 组合分布统计的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/stream/str_list/index.vue` | 选科清单（PAGE-STR-LIST） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_list/components/StreamChangeDialog.vue` | 选科变更申请弹窗（PAGE-STR-CHANGE） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_list/components/StreamHistorySection.vue` | 变更记录时间线（PAGE-STR-HISTORY） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_stat/index.vue` | 组合分布统计（PAGE-STR-STAT） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_stat/components/SubjectStatTable.vue` | 学科选择人数表 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_stream-list-and-stat_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十三批：选科变更审批 + 按组合生成教学班

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-077.md` | 选科变更审批 / 按组合生成教学班的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/stream/str_approve/index.vue` | 选科变更审批（PAGE-STR-APPROVE） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_approve/components/ApproveDialog.vue` | 审批弹窗（DIALOG-STR-APPROVE） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_gen_class/index.vue` | 按组合生成教学班四步向导（PAGE-STR-GEN-CLASS） | `已实现` |
| `apps/plus-ui/src/views/edu/stream/str_gen_class/components/CheckResultTable.vue` | 核对结果表 | `已实现` |
| `evidence/stage6-frontend/2026-10-08_stream-approve-and-gen-class_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十四批：教师任教关系 + 教师导入

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-078.md` | 教师任教关系 / 教师导入的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/teacher/tch_assign/index.vue` | 教师任教关系（PAGE-TCH-ASSIGN） | `已实现` |
| `apps/plus-ui/src/views/edu/teacher/tch_assign/components/AssignmentTable.vue` | 任教关系主表 | `已实现` |
| `apps/plus-ui/src/views/edu/teacher/tch_assign/components/AssignmentFormDialog.vue` | 新增 / 编辑任教关系弹窗 | `已实现` |
| `apps/plus-ui/src/views/edu/teacher/tch_assign/components/CopyAssignDialog.vue` | 复制上一学年任教关系弹窗（DIALOG-TCH-COPY） | `已实现` |
| `apps/plus-ui/src/views/edu/teacher/tch_import/index.vue` | 教师导入四步向导（PAGE-TCH-IMPORT） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_teacher-assign-and-import_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十五批：通用导入向导 + 班级合并

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-079.md` | 通用导入向导 / 班级合并的交付与门禁记录 | `review` |
| `apps/plus-ui/src/views/edu/import-export/imp_wizard/index.vue` | 通用导入向导（PAGE-IMP-WIZARD） | `已实现` |
| `apps/plus-ui/src/views/edu/class/cls_merge/index.vue` | 班级合并（PAGE-CLS-MERGE，无原型，按 GAP-088 实现） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_import-wizard-and-class-merge_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 6 第三十六批（收尾）：403 / 500 异常页

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-080.md` | 403 / 500 的交付、GAP-087 关闭与门禁记录 | `review` |
| `apps/plus-ui/src/views/error/403.vue` | 无权限页（PAGE-403，静态路由 `/403`） | `已实现` |
| `apps/plus-ui/src/views/error/500.vue` | 服务异常页（PAGE-500，静态路由 `/500`） | `已实现` |
| `evidence/stage6-frontend/2026-10-08_error-pages_verification.log` | 本批门禁证据 | `review` |

## 2026-10-08 阶段 7 第二批：班级模块纵切 + 学生纵切列名修正

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-081.md` | 班级模块纵切与学生列名修正的交付与编译证据 | `review` |
| `services/RuoYi-Cloud-Plus/ruoyi-modules/ruoyi-edu/src/main/java/org/dromara/edu/domain/EduClass.java` | 班级实体（edu_class） | `已实现` |
| `.../domain/EduClassMember.java` | 班级成员（花名册）实体（edu_class_member，无 del_flag） | `已实现` |
| `.../domain/bo/EduClassBo.java` / `EduClassMemberBo.java` | 班级 / 成员业务对象 | `已实现` |
| `.../domain/vo/EduClassVo.java` / `EduClassMemberVo.java` | 班级 / 成员视图对象 | `已实现` |
| `.../mapper/EduClassMapper.java` / `EduClassMemberMapper.java` | 班级 / 成员数据层 | `已实现` |
| `.../service/IEduClassService.java` + `impl/EduClassServiceImpl.java` | 班级服务层（12 个方法） | `已实现` |
| `.../controller/EduClassController.java` | 班级控制器（12 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-class_compile.log` | 本批编译证据（BUILD SUCCESS / 39 个 class） | `review` |

## 2026-10-08 阶段 7 第三批：年级模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-082.md` | 年级模块纵切的交付与编译证据 | `review` |
| `.../domain/EduGrade.java` | 年级实体（edu_grade） | `已实现` |
| `.../domain/EduGradeLeader.java` | 年级主任任职实体（edu_grade_leader，DS-05 权威来源） | `已实现` |
| `.../domain/bo/EduGradeBo.java` | 年级业务对象（含批量与任职字段） | `已实现` |
| `.../domain/vo/EduGradeVo.java` / `EduGradeLeaderVo.java` | 年级 / 任职视图对象 | `已实现` |
| `.../mapper/EduGradeMapper.java` / `EduGradeLeaderMapper.java` | 年级 / 任职数据层 | `已实现` |
| `.../service/IEduGradeService.java` + `impl/EduGradeServiceImpl.java` | 年级服务层（11 个方法） | `已实现` |
| `.../controller/EduGradeController.java` | 年级控制器（11 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-grade_compile.log` | 本批编译证据（BUILD SUCCESS / 59 个 class） | `review` |

## 2026-10-08 阶段 7 第四批：学年学期模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-083.md` | 学年学期模块纵切的交付与编译证据 | `review` |
| `.../domain/EduAcademicYear.java` | 学年实体（edu_academic_year，无 del_flag） | `已实现` |
| `.../domain/EduTerm.java` | 学期实体（edu_term，无 del_flag，is_current 为 char(1)） | `已实现` |
| `.../domain/bo/EduAcademicYearBo.java` / `EduTermBo.java` | 学年 / 学期业务对象（日期用 String 接收） | `已实现` |
| `.../domain/vo/EduAcademicYearVo.java` / `EduTermVo.java` / `TermReferenceVo.java` | 学年 / 学期 / 引用检查视图对象 | `已实现` |
| `.../mapper/EduAcademicYearMapper.java` / `EduTermMapper.java` | 学年 / 学期数据层 | `已实现` |
| `.../service/IEduTermService.java` + `impl/EduTermServiceImpl.java` | 学年学期服务层（12 个方法，含默认学期结构与引用检查） | `已实现` |
| `.../controller/EduTermController.java` | 学年学期控制器（12 个端点，term 模块全覆盖） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-term_compile.log` | 本批编译证据（BUILD SUCCESS / 83 个 class） | `review` |

## 2026-10-08 阶段 7 第五批：教师模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-084.md` | 教师模块纵切的交付、缺项与编译证据 | `review` |
| `.../domain/EduTeacher.java` | 教师主体实体（edu_teacher，带 delFlag） | `已实现` |
| `.../domain/EduUserRole.java` | 学校级教育角色实体（edu_user_role） | `已实现` |
| `.../domain/EduTeachingAssignment.java` | 任教关系实体（edu_teaching_assignment，无 delFlag，DS-07 权威来源） | `已实现` |
| `.../domain/bo/EduTeacherBo.java` / `EduUserRoleBo.java` / `EduTeachingAssignmentBo.java` | 三个业务对象 | `已实现` |
| `.../domain/vo/EduTeacherVo.java` / `EduUserRoleVo.java` / `EduTeachingAssignmentVo.java` | 三个视图对象 | `已实现` |
| `.../mapper/EduTeacherMapper.java` / `EduUserRoleMapper.java` / `EduTeachingAssignmentMapper.java` | 三个数据层 | `已实现` |
| `.../service/IEduTeacherService.java` + `impl/EduTeacherServiceImpl.java` | 教师服务层（15 个方法，含账号创建与任教关系复制） | `已实现` |
| `.../controller/EduTeacherController.java` | 教师控制器（15 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-teacher_compile.log` | 本批编译证据（BUILD SUCCESS / 116 个 class） | `review` |

## 2026-10-08 阶段 7 第六批：学校与校区 + 学科配置模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-085.md` | 学校与校区 / 学科配置的交付与编译证据 | `review` |
| `.../domain/EduSchool.java` / `EduCampus.java` / `EduSchoolStage.java` | 学校 / 校区 / 学校开设学段实体 | `已实现` |
| `.../domain/EduSubject.java` / `EduSubjectStage.java` | 学科 / 学科与学段启用实体 | `已实现` |
| `.../domain/bo/EduSchoolBo.java` / `EduCampusBo.java` / `EduSchoolStageBo.java` | 学校 / 校区 / 学段业务对象 | `已实现` |
| `.../domain/bo/EduSubjectBo.java` / `EduSubjectStageBo.java` | 学科 / 学科与学段业务对象 | `已实现` |
| `.../domain/vo/EduSchoolVo.java` / `EduCampusVo.java` / `EduSchoolStageVo.java` / `SchoolSummaryVo.java` | 学校 / 校区 / 学段 / 摘要视图对象 | `已实现` |
| `.../domain/vo/EduSubjectVo.java` / `EduSubjectStageVo.java` / `EduSubjectOptionVo.java` / `SubjectReferenceVo.java` | 学科相关视图对象 | `已实现` |
| `.../mapper/EduSchoolMapper.java` / `EduCampusMapper.java` / `EduSchoolStageMapper.java` | 学校 / 校区 / 学段数据层 | `已实现` |
| `.../mapper/EduSubjectMapper.java` / `EduSubjectStageMapper.java` | 学科 / 学段启用数据层 | `已实现` |
| `.../service/IEduSchoolService.java` + `impl/EduSchoolServiceImpl.java` | 学校服务层（15 个方法，含幂等开通初始化） | `已实现` |
| `.../service/IEduSubjectService.java` + `impl/EduSubjectServiceImpl.java` | 学科服务层（12 个方法，含标准学科模板） | `已实现` |
| `.../controller/EduSchoolController.java` | 学校与校区控制器（15 个端点） | `已实现` |
| `.../controller/EduSubjectController.java` | 学科控制器（12 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-school-and-subject_compile.log` | 本批编译证据（BUILD SUCCESS / 175 个 class） | `review` |

## 2026-10-08 阶段 7 第七批：学生档案、学籍异动与监护人纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-086.md` | 学生档案 / 学籍异动 / 监护人的交付、缺项与编译证据 | `review` |
| `.../domain/EduStudentEnrollment.java` | 在校记录实体（两段式取数第一段） | `已实现` |
| `.../domain/EduGuardian.java` | 监护人主体实体（平台级，不设 tenant_id） | `已实现` |
| `.../domain/EduStudentGuardian.java` | 监护人与学生关联实体（平台级） | `已实现` |
| `.../domain/EduEnrollmentChange.java` | 学籍异动记录实体（追加式，无 delFlag） | `已实现` |
| `.../domain/EduStudentFieldChange.java` | 学生资料变更申请实体（不映射生成列 pending_guard） | `已实现` |
| `.../domain/bo/EduStudentEnrollmentBo.java` 等 4 个 | 在校记录 / 监护人 / 异动查询 / 资料变更查询业务对象 | `已实现` |
| `.../domain/vo/EduStudentEnrollmentVo.java` / `EduGuardianVo.java` / `EduEnrollmentChangeVo.java` / `EduEnrollmentStatusOptionVo.java` / `EduStudentFieldChangeVo.java` | 五个视图对象（与前端契约字段对齐） | `已实现` |
| `.../mapper/EduStudentEnrollmentMapper.java` 等 5 个 | 五个数据层 | `已实现` |
| `.../service/IEduStudentProfileService.java` + `impl/EduStudentProfileServiceImpl.java` | 学生档案服务层（9 个方法，含学籍流转矩阵与敏感字段掩码） | `已实现` |
| `.../controller/EduStudentProfileController.java` | 学生档案控制器（9 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-student-profile_compile.log` | 本批编译证据（BUILD SUCCESS / 217 个 class） | `review` |

## 2026-10-08 阶段 7 第八批：学籍异动与跨校转学 + 学生激活码纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-087.md` | 学籍异动 / 跨校转学 / 激活码的交付与编译证据 | `review` |
| `.../domain/EduTransferOrder.java` | 跨校转学单实体（无 delFlag，只按状态流转） | `已实现` |
| `.../domain/EduActivationCode.java` | 激活码实体（with_audit=false，不继承 BaseEntity；生成列不映射） | `已实现` |
| `.../domain/bo/EduTransferOrderBo.java` / `EduActivationCodeBo.java` | 转学单 / 激活码业务对象 | `已实现` |
| `.../domain/vo/EduTransferOrderVo.java` / `EduActivationCodeVo.java` | 转学单 / 激活码视图对象 | `已实现` |
| `.../mapper/EduTransferOrderMapper.java` / `EduActivationCodeMapper.java` | 两个数据层 | `已实现` |
| `.../service/IEduEnrollmentService.java` + `impl/EduEnrollmentServiceImpl.java` | 学籍异动与跨校转学服务层（8 个方法） | `已实现` |
| `.../service/IEduActivationService.java` + `impl/EduActivationServiceImpl.java` | 激活码服务层（3 个方法） | `已实现` |
| `.../controller/EduEnrollmentController.java` | 学籍异动与跨校转学控制器（8 个端点） | `已实现` |
| `.../controller/EduStudentActivationController.java` | 学生激活码控制器（3 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-enrollment-and-activation_compile.log` | 本批编译证据（BUILD SUCCESS / 241 个 class） | `review` |

## 2026-10-08 阶段 7 第九批：升班模块 + 教学班模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-088.md` | 升班 / 教学班的交付与编译证据 | `review` |
| `.../domain/EduPromotionTask.java` | 升班任务实体（带 delFlag） | `已实现` |
| `.../domain/EduPromotionItem.java` | 升班明细实体（with_audit=false，不继承 BaseEntity） | `已实现` |
| `.../domain/EduTeachingClass.java` | 教学班实体（与行政班完全独立） | `已实现` |
| `.../domain/EduTeachingClassMember.java` | 教学班成员实体（无 delFlag） | `已实现` |
| `.../domain/bo/EduPromotionTaskBo.java` / `EduTeachingClassBo.java` | 升班 / 教学班业务对象 | `已实现` |
| `.../domain/vo/EduPromotionTaskVo.java` / `EduPromotionItemVo.java` / `EduPromotionReadinessVo.java` | 升班相关视图对象（含前端状态码映射） | `已实现` |
| `.../domain/vo/EduTeachingClassVo.java` / `EduTeachingClassMemberVo.java` | 教学班 / 成员视图对象 | `已实现` |
| `.../mapper/EduPromotionTaskMapper.java` 等 4 个 | 四个数据层 | `已实现` |
| `.../service/IEduPromotionService.java` + `impl/EduPromotionServiceImpl.java` | 升班服务层（10 个方法，含齐备性、预览、校验、执行、重试、取消） | `已实现` |
| `.../service/IEduTeachingClassService.java` + `impl/EduTeachingClassServiceImpl.java` | 教学班服务层（5 个方法，含幂等生成） | `已实现` |
| `.../controller/EduPromotionController.java` | 升班控制器（12 个端点） | `已实现` |
| `.../controller/EduTeachingClassController.java` | 教学班控制器（5 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-promotion-and-teaching-class_compile.log` | 本批编译证据（BUILD SUCCESS / 282 个 class） | `review` |

## 2026-10-08 阶段 7 第十批：选科模块纵切

| 路径 | 用途 | 状态 |
|---|---|---|
| `docs/00-governance/change-requests/CR-089.md` | 选科模块的交付与编译证据 | `review` |
| `.../domain/EduStreamConfig.java` | 选科配置实体（无 delFlag，失效写 config_status） | `已实现` |
| `.../domain/EduStudentStream.java` | 学生选科实体（再选科目存逗号分隔字符串） | `已实现` |
| `.../domain/EduStreamChangeRequest.java` | 选科变更申请实体（含审批轨迹与撤回时间） | `已实现` |
| `.../domain/EduStreamHistory.java` | 选科历史实体（with_audit=false，不继承 BaseEntity，追加式） | `已实现` |
| `.../domain/bo/EduStreamConfigBo.java` / `EduStreamSelectionBo.java` / `EduStreamChangeRequestBo.java` / `EduStreamHistoryBo.java` | 四个业务对象 | `已实现` |
| `.../domain/vo/EduStreamConfigVo.java` / `EduMyStreamVo.java` / `EduStreamOptionVo.java` / `EduStreamSelectionVo.java` | 配置 / 我的选科 / 选科选项 / 清单视图对象 | `已实现` |
| `.../domain/vo/EduStreamStatVo.java` / `EduUnselectedStudentVo.java` / `EduStreamChangeRequestVo.java` / `EduStreamHistoryVo.java` / `EduTeachingClassGenerateVo.java` | 统计 / 未选清单 / 变更申请 / 历史 / 教学班生成视图对象 | `已实现` |
| `.../mapper/EduStreamConfigMapper.java` / `EduStudentStreamMapper.java` / `EduStreamChangeRequestMapper.java` / `EduStreamHistoryMapper.java` | 四个数据层 | `已实现` |
| `.../service/IEduStreamService.java` + `impl/EduStreamServiceImpl.java` | 选科服务层（16 个方法，含开放期比较、变更审批、历史追加、教学班幂等生成） | `已实现` |
| `.../controller/EduStreamController.java` | 选科控制器（16 个端点） | `已实现` |
| `evidence/stage7-backend/2026-10-08_edu-stream_compile.log` | 本批编译证据（BUILD SUCCESS / 332 个 class） | `review` |
