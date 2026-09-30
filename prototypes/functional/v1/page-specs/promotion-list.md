# 升班任务列表

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-LIST`（取自 `navigation.yaml`） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | page（列表页） |
| 骨架模板 | `TPL-LIST` |
| 所属批次 | 2-3e 首件样板（`2-3e-s1`） |
| 上游需求 | `REQ-PRM-001` ~ `REQ-PRM-006`、`REQ-PRM-010`、`REQ-PRM-019`、`REQ-PRM-027`、`REQ-PRM-032`、`REQ-PRM-034`、`REQ-PRM-036`、`REQ-PRM-058` |
| 上游规则 | `BR-PROMO-001`（按学年追加）、`BR-PROMO-003`（幂等）、`BR-STU-012`、`BR-GRADE-006`（跨学段） |
| 权限资源 | `promotion.batch` 的 `read` / `create` / `update`（`05-permission-matrix.yaml`，`CR-012` 收敛） |
| 数据范围 | 教务主任 / 校领导 `DS-04`；年级主任 `DS-05`（只读且计数收窄）；平台运营 `DS-01`（只读）；超级管理员 `platform` |
| 原型文件 | `pages/promotion-list.html`（含同页确认片段 `DIALOG-PRM-CANCEL`） |
| 交付证据 | `evidence/stage2-prototype/verify-promotion-list.html`、`evidence/stage2-prototype/promotion-list_*.png` |

## 1. 页面目的

教务主任在这里查看历次学年升级任务，按状态决定下一步：草稿去预览、已预览待确认去执行、失败 / 部分失败去重试失败项、执行中看进度、已取消继续执行剩余项。
校领导与年级主任只读：升班没有审批环节，他们的价值是核对去向（`CR-012` / `GAP-052` / `GAP-053`）。
平台运营用于排查，全平台只读、访问留痕；班主任与租户管理员没有 `promotion.batch:read`，直接进入无权限态。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | `el-page-header` 等价物 | 标题 + 数据范围提示 + 「共 17 个升班任务」+ 本页样例实时汇总 |
| 2 | 搜索区 | `filter` | `el-card` + `el-form inline` | 学校 / 源学年学期 / 目标学年学期 / 状态 / 创建人 / 任务编号 |
| 3 | 工具条 | `toolbar` | `el-card#header` | 新建任务、导出、导出（需授权）、隐藏搜索、列配置、刷新 |
| 4 | 表格 | `table` | `el-table` | 10 列，`min-width: 1108px`，列宽之和 = `min-width` |
| 5 | 分页 | `pagination` | `Pagination` | 「共 17 条 · 当前第 1 页 · 每页 20 条（本页显示 N 行样例）」 |
| 6 | 口径说明 | — | 文本块 | 状态口径 / 追加与幂等 / 按钮与权限（CR-012）/ 数据范围 / 边界样本 5 条 |
| 7 | 状态片段 | — | 状态块 | 加载中 / 空数据 / 查询失败 / 无权限 / 提交中 / 部分失败 / 排队中 |

### 2.1 状态驱动的行内动作（`REQ-PRM-006`）

| 任务状态 | 允许的行内动作（有 `update` 权限时） | 说明 |
|---|---|---|
| 草稿 | 预览、取消 | 没有升班明细，三个计数列显示「—」 |
| 已预览待确认 | 执行（先校验）、重新预览、取消 | 重新预览会覆盖旧明细（`REQ-PRM-021`） |
| 校验中 | 查看校验、取消 | 校验维度见 `REQ-PRM-022` |
| 执行中 | 查看进度、取消 | 取消后保留已完成部分（已确认 4） |
| 已完成 | 查看结果、结果导出 | 结果报告含成功 / 失败 / 留级 / 毕业清单（`REQ-PRM-034`） |
| 部分失败 | 重试失败项、查看结果 | 只重试失败项（`REQ-PRM-032`） |
| 失败 | 重试、查看结果 | 全量失败 |
| 已取消 | 继续执行剩余项、查看结果 | 不做整批回滚（已确认 4） |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `school_id` | 学校 | `el-select` | 是 | 平台运营 / 超级管理员 | 201（本校） | — | 非多校角色下拉禁用；升班任务不跨校共享 |
| `source_term_id` | 源学年学期 | `el-select` | 否 | — | 全部 | — | 筛选条件 |
| `target_term_id` | 目标学年学期 | `el-select` | 否 | — | 全部 | — | 筛选条件 |
| `promotion_task_status` | 状态 | `el-select` | 否 | — | 全部 | 取值来自 `promotion_task_status` 枚举 | 8 个状态；字段字典条目见 `CR-013` |
| `create_by` | 创建人 | `el-select` | 否 | — | 全部 | — | 筛选条件 |
| `task_no` | 任务编号 | `el-input` | 否 | — | 空 | — | 关键字检索，`varchar(32)`，全局唯一 |
| `total_count` | 学生总数 | 只读单元格 | — | — | — | — | 受数据范围约束（`DS-DENY-08`） |
| `success_count` | 成功 | 只读单元格 | — | — | — | — | 同上 |
| `failed_count` | 失败 | 只读单元格 | — | — | — | — | 与失败原因成对出现 |
| `create_time` | 创建时间 | 只读单元格 | — | — | — | — | 列表默认显示到分钟 |

## 4. 动作清单

全部动作已在 `page-actions.yaml` 的 `promotion_list` 动作组登记（`ACT-PRM-001` ~ `ACT-PRM-010`）。

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-PRM-001` | 新建任务 | 点击 | `promotion.batch:create` | 进入 `PAGE-PRM-CREATE` | — |
| `ACT-PRM-002` | 行内「预览」/「重新预览」 | 点击 | `update` 且状态为草稿 / 已预览待确认 | 生成预览，进入 `PAGE-PRM-PREVIEW` | `previewPromotionTask` |
| `ACT-PRM-003` | 行内「执行」/「查看校验」 | 点击 | `update` 且状态为已预览待确认 / 校验中 | 进入 `PAGE-PRM-VALIDATE` | `executePromotionTask` |
| `ACT-PRM-004` | 行内「重试失败项」/「重试」/「继续执行剩余项」 | 点击 | `update` 且状态为部分失败 / 失败 / 已取消 | 只重试失败项或剩余项 | `retryPromotionTask` |
| `ACT-PRM-005` | 行内「取消」 | 点击 | `update` 且任务未结束 | 打开 `DIALOG-PRM-CANCEL` | — |
| `ACT-PRM-006` | 行内「查看结果」/「查看进度」 | 点击 | `read` | 进入 `PAGE-PRM-RESULT` | `getPromotionTask` |
| `ACT-PRM-007` | 导出（任务台账） | 点击 | `data.export:export` | ≤2000 行直接下载，否则转异步 | `exportPromotionTask` |
| `ACT-PRM-008` | 行内「结果导出」 | 点击 | `data.export:export` 且任务已完成 / 部分失败 | 下载结果报告 | `exportPromotionResult` |
| `ACT-PRM-009` | 确认片段「返回」 | 点击 | 始终可用 | 关闭片段，不写入 | — |
| `ACT-PRM-010` | 确认取消任务 | 点击 | 已填取消原因（≥5 字） | 任务置为已取消并写审计 | `cancelPromotionTask` |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 正常 | `normal` | 8 个状态的任务共 12 行样例（本校 11 行 + 他校 1 行） | 按状态决定 |
| 加载中 | `loading` | 表格式骨架行 | — |
| 空数据 | `empty` | 两档：筛选无结果 / 本校还没有任务 | 清空筛选条件 |
| 查询失败 | `error` | 请求编号 + 错误码 + 重试 | 重试 |
| 无权限 | `forbidden` | 班主任 / 租户管理员自动进入；写明不降级为全量 | 去学生详情 |
| 提交中 | `submitting` | 写操作按钮 loading 并禁用 | — |
| 部分失败 | `partial` | 两条任务的失败原因清单 | 重试失败项 |
| 排队中 | `queued` | 队列位置 + 异步说明 | 查看任务中心 |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 新建任务 | `PAGE-PRM-CREATE` | 跳页 | 返回列表保留筛选与页码 |
| 行内预览 / 重新预览 | `PAGE-PRM-PREVIEW` | 跳页 | 同上 |
| 行内执行 / 查看校验 | `PAGE-PRM-VALIDATE` | 跳页 | 同上 |
| 行内重试 / 继续执行剩余项 | `PAGE-PRM-RESULT` | 跳页 | 同上 |
| 行内查看结果 / 查看进度 | `PAGE-PRM-RESULT` | 跳页 | 同上 |
| 行内取消 | `DIALOG-PRM-CANCEL` | 同页弹窗 | 关闭后不改变筛选 |

> 向导四步页（`PAGE-PRM-CREATE` / `PREVIEW` / `VALIDATE` / `EXECUTE` / `RESULT`）与 `PAGE-PRM-ADJUST` 属本批次后续小批，
> 本页先验证入口、权限显隐与状态驱动的按钮集合；点击时外壳给出「在批次 2-3 交付」的提示，不越批渲染页面内容。

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任 | 本校 11 行 | 新建任务、导出 + 全部状态对应的行内写操作 | — |
| 校领导 | 本校 11 行（只读） | 导出；行内无写入口 | — |
| 年级主任 | 本年级有学生的 6 行（只读） | 导出；计数按 `DS-05` 收窄 | 调整去向不开放（`CR-012`） |
| 平台运营 | 所选学校的任务（只读），本校 11 行 / 他校 1 行 | 导出（需授权） | 无任何写入口 |
| 超级管理员 | 全部，含他校 | 全部 | 操作强制留痕（`BR-ORG-014`） |
| 班主任 / 租户管理员 | 无 | 无 | 进入无权限态（`DS-DENY-03`） |

## 8. 样例数据

取自 `content-samples.json` 的 `promotion_tasks`（12 条）与 `promotion_tasks_notes`：

| 用途 | 样例 |
|---|---|
| 8 个状态各 1 条 | `PRM-20260620-0001`（已完成）、`002` / `0019`（部分失败）、`003`（失败）、`0011`（草稿）、`0012` / `0017`（已预览待确认）、`0013`（校验中）、`0014`（执行中）、`0015`（已取消） |
| 草稿无明细 | `0011` 的三个计数列显示「—」（`REQ-PRM-011`） |
| 跨学段 | `0016` 小学六年级 → 初中一年级（`BR-GRADE-006`） |
| 超阈值 | `0017` 10240 人 > 10000（`REQ-PRM-010`） |
| 他校 | `0018` 云溪外国语学校（平台运营切校后可见） |
| 范围收窄 | `grade_scope` 记录 2026 级 高一的子集计数，年级主任视图按它渲染（`DS-DENY-08`） |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-LIST`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-PRM-001` ~ `010` + `ACT-COM-001` ~ `007`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`CR-013` 补齐 `source_term_id` / `target_term_id` / `promotion_task_status` / `total_count` / `success_count` / `failed_count` 六个字段，`GAP-054` 已关闭）
- [x] 五类状态齐全（另加部分失败与排队中）
- [x] 1366×768 与 1920×1080 下未出现整页横向滚动（列宽之和 = 表格 `min-width` = 1108px；学年学期与创建时间两行展示）
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
