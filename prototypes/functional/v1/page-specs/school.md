# 学校管理（列表 / 详情 / 新建 / 编辑 / 学段配置 / 停用）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-SCH-LIST`（列表页）+ `PAGE-SCH-DETAIL`（详情抽屉）+ `PAGE-SCH-CREATE` / `PAGE-SCH-EDIT` / `PAGE-SCH-STAGE` / `PAGE-SCH-DISABLE`（四个弹窗） |
| 所属模块 | 学校管理（`school`） |
| 页面类型 | page（`TPL-LIST`）+ detail（抽屉）+ 4 × dialog（CR-015：表单类浮层用弹窗） |
| 所属批次 | 2-6a |
| 上游需求 | 学校 PRD 的列表 / 详情 / 新建 / 编辑 / 学段配置 / 停用章节 |
| 上游规则 | `BR-ORG-001`（租户 = 组织单元，层级 ≤3）、`BR-ORG-009`（校区不参与权限判定）、`BR-GRADE-006`（学段决定年级）、`RV-GRD-03`（学段序号固定映射） |
| 权限资源 | `org.school` 的 `read` / `create` / `update`；导出 `data.export:export` |
| 数据范围 | 租户管理员本租户（`DS-04`）可写；教务主任本校可写；校领导只读；平台运营只读并留痕（`DS-01`）；超级管理员不受限 |
| 交付证据 | `evidence/stage2-prototype/verify-school.html`、`school-list_*.png` |

## 1. 页面目的
维护学校这一「教学数据最小归属单位」：新建 / 编辑学校、配置学段、进校区管理、停用学校；
列表上的校区数入口复用校区管理页（`GAP-046` 选项 A，不新增页面）。

## 2. 页面结构
| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | 页头 | 标题 + 数据范围 + 学校总数 + 教学数据归属口径 |
| 2 | 筛选 | `filter` | `el-card` + `el-form inline` | 学校名称 / 学段 / 状态 |
| 3 | 工具条 | `toolbar` | `el-card#header` | 新建学校 / 开通初始化 / 导出 / 隐藏搜索 / 刷新 |
| 4 | 表格 | `table` | `el-table` | 9 列（名称 / 编码 / 学段 / 校区数 / 班级数 / 在读学生 / 状态 / 所属租户 / 操作） |
| 5 | 分页 | `pagination` | `Pagination` | 共 4 所学校（样例 4 行） |
| 6 | 详情抽屉 | `detail` | `el-drawer`（lg） | 基本信息 / 规模概览 / 学段配置 + 底部四动作 |
| 7 | 四个弹窗 | `form` | `el-dialog`（md / md / sm / sm） | 新建 / 编辑 / 学段配置 / 停用确认 |
| 8 | 状态片段 | — | 状态块 | 加载中 / 空数据 / 查询失败 / 无权限 / 提交中 |

## 3. 字段清单
`school_name`（必填）、`school_code`（系统生成，只读；变更走 `updateSchoolCode`）、`tenant_id`（必填、只读于编辑态）、
`stage_code`（多选，至少一个）、`school_type`、`campus_count`、`class_count`、`student_count`、`school_status`、`remark`（停用原因必填）。

## 4. 动作清单
`ACT-SCH-001`（新建）/`002`（开通初始化）/`003`（导出）/`004`（行内校区数）/`005`、`008`（学段配置）/`006`（编辑）/`007`（校区管理）/`009`（停用）/
`010`、`011`（新建取消 / 保存 addSchool）/`012`、`013`（编辑取消 / 保存 updateSchool）/`014`、`015`（学段取消 / 保存 saveSchoolStage）/
`016`、`017`（停用取消 / 确认 disableSchool）/`018`（行内详情）。

## 5. 状态清单
正常（4 行，含 1 行已停用）/ 加载中 / 空数据（新建学校入口）/ 查询失败（重试不放宽范围）/ 无权限 / 提交中。

## 6. 跳转关系
行点击或行内「详情」→ 详情抽屉；校区数 / 详情「校区管理」→ `PAGE-SCH-CAMPUS`；「开通初始化」→ `PAGE-SCH-INIT`；两个页面在 2-6a 的后续小批交付。

## 7. 权限与数据范围
租户管理员 / 超级管理员可写；教务主任本校可写；校领导 / 平台运营只读（平台运营额外留痕）；其他角色进入无权限态（`DS-DENY-03`）。

## 8. 样例数据
`content-samples.json` 的 `org`（学校与校区）与 `school_module_notes`；4 行学校样例含 1 行已停用。

## 9. 自查
- [x] 骨架属于 `TPL-LIST` / `TPL-DETAIL` / `TPL-OVERLAY`；每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 动作编号已在 `page-actions.yaml` 的 `school_list` 登记（`ACT-SCH-001` ~ `018`）
- [x] 四个浮层都是 `el-dialog`（CR-015：批次 2-6 的 9 条表单浮层由抽屉改为弹窗）
- [x] 表格 `min-width` 1140 = 列宽之和；每行带状态标签
- [x] 五类状态齐全；样例数据取自 `content-samples.json`
