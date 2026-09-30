# 教师管理列表

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-LIST` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | page |
| 骨架模板 | `TPL-LIST` |
| 所属批次 | 2-2a（教师管理的标准校验页；其余 12 页在验收通过后按同一标准铺开） |
| 上游需求 | `REQ-TCH-001` ~ `REQ-TCH-008` |
| 上游规则 | `BR-TEACHER-001`、`BR-TEACHER-005`、`BR-TEACHER-007`、`DS-DENY-03`、`DS-DENY-06`、`BR-DATA-018` |
| 权限资源 | `person.teacher` 的 `read`；写入口取 `person.teacher:create` / `update`、`person.teaching_assignment:create`、`data.import:import`、`data.export:export` |
| 数据范围 | `DS-02` 本租户组织（租户管理员）/ `DS-04` 本校（校领导、教务主任）/ `DS-05` 本年级（年级主任）/ `DS-06`、`DS-07` 本人（班主任、任课教师）；平台运营 `DS-01` |
| 原型文件 | `pages/teacher-list.html` |

## 1. 页面目的

教务主任、租户管理员、年级主任进入教师模块的默认入口，用于按学校、教育角色、任教年级与班级、任教学科、在职状态检索教师。
从这里发起新增、导入、导出、批量设置任教，以及行内的编辑、角色分配、任教关系、离职登记。
教师数据按**任教学校**判定数据范围；跨校任教教师在不同学校各占一行并标明任教学校（`REQ-TCH-008`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | 文本 + `el-tag` | 页面标题、数据范围提示、总条数、数据截止时间 |
| 2 | 搜索区（两行） | `filter` | `el-card` + `el-form inline` | 第一行：学校、教育角色、任教年级、在职状态；第二行：任教学科、任教班级、关键字 + 搜索/重置 |
| 3 | 工具条 | `toolbar` | `el-card` 的 `#header` | 左侧：新增、导入、导出、下载模板、批量设置任教；右侧：隐藏搜索、列配置、刷新 |
| 4 | 表格 | `table` | `el-table` | 复选列 + 9 个数据列 + 固定右侧操作列 |
| 5 | 分页器 | `pagination` | `Pagination` | 总条数、每页条数、跳页 |

> 说明：PRD 6.2 第 3 项的"批量设置任教"已登记为 `ACT-TCH-005`；未勾选时点击会给"请先勾选"的提示。

## 3. 字段清单

列字段（`data-field` 均取自 `06-field-dictionary.yaml`）：

| 字段名 | 中文 | 组件 | 列宽 | 备注 |
|---|---|---|---|---|
| — | 复选列 | `el-table-column type="selection"` | 50px | 对应 `ACT-COM-008` |
| `teacher_no` | 工号 | 文本 | 96px | 等宽 13px；租户内唯一（`BR-TEACHER-007`） |
| `teacher_name` | 姓名 | 文本 | 104px | 超长姓名省略号 + 悬浮完整 |
| `gender` | 性别 | 文本 | 48px | |
| `school_id` | 所属学校 | 文本 | 150px | 跨校任教时显示归属学校 + "跨校任教"标签 |
| `edu_role` | 教育角色 | `el-tag` 组 | 132px | 多角色并排显示；取值来自 `edu_role` 枚举 |
| `subject_code` | 任教学科 | 文本 | 96px | 多学科用 `、` 连接；无任教关系显示 `—` |
| — | 任课班级数 | 数字（右对齐） | 88px | 受数据范围约束（`DS-DENY-08`） |
| `employment_status` | 在职状态 | `el-tag` | 84px | 在职 / 离职 / 调离；取值来自 `employment_status` 枚举 |
| `teacher_phone` | 联系电话 | 文本（掩码） | 104px | 空值显示 `—`；对任课教师整列隐藏（`data-col-hide-role`） |
| — | 操作 | `el-table-column`（固定右侧） | 150px | 编辑 / 角色 / 任教 / 离职，按权限与状态显隐 |

筛选项字段：`school_id`、`edu_role`、`grade_name`、`class_name`、`subject_code`、`employment_status`、`teacher_name`（关键字）。

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-COM-001` ~ `008` | 搜索 / 重置 / 隐藏搜索 / 列配置 / 刷新 / 分页 / 导出 / 批量选择 | — | 同通用动作 | 同通用动作 | `listTeacher` 等 |
| `ACT-TCH-001` | 新增 | 点击 | `person.teacher:create` | 打开 `PAGE-TCH-CREATE`（批次 2-2b） | — |
| `ACT-TCH-002` | 导入 | 点击 | `data.import:import` | 打开 `PAGE-TCH-IMPORT`（批次 2-4） | — |
| `ACT-TCH-003` | 导出 | 点击 | `data.export:export` | ≤ 2000 行直接下载，超出转异步 | `exportTeacher` |
| `ACT-TCH-004` | 下载模板 | 点击 | `data.import:import` | 下载教师导入模板 | `downloadTeacherImportTemplate` |
| `ACT-TCH-005` | 批量设置任教 | 勾选多行后点击 | `person.teaching_assignment:create` | 带入已选教师进入任教关系页；逐条校验范围 | `batchSaveTeachingAssignment` |
| `ACT-TCH-006` | 行内编辑 | 点击 | `person.teacher:update` | 打开 `PAGE-TCH-EDIT`（2-2b） | — |
| `ACT-TCH-007` | 行内角色 | 点击 | `person.teacher:update` | 打开 `PAGE-TCH-ROLE`；选年级主任必须选学年学期与年级（2-2b） | — |
| `ACT-TCH-008` | 行内任教 | 点击 | `person.teaching_assignment:read` | 打开 `PAGE-TCH-ASSIGN`，默认当前学年学期（2-2b） | — |
| `ACT-TCH-009` | 行内离职 / 调离 | 点击 | `person.teacher:update` 且在在职状态 | 打开本页的登记弹窗（`DIALOG-TCH-LEAVE`），确认后生效 | `saveTeacherLeave` |
| `ACT-TCH-010` | 点击行 | 单击数据行 | `person.teacher:read` | 打开 `PAGE-TCH-DETAIL`（2-2b） | `getTeacher` |
| `ACT-TCH-011` | 查看完整手机号 | 点击掩码 | `read_contact` | 就地展示完整值并写访问日志 | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 表格区域内骨架行 3 条，不用全屏遮罩 | — |
| 空数据 | `empty` | 两种空态可切换：筛选无结果 / 无数据范围（任课教师只能看本人） | 清空筛选条件 |
| 查询失败 | `error` | 超时说明 + 请求编号 `REQ-20260930-000208` + 错误码 DS-5001 | 重试 |
| 无权限 | `forbidden` | 说明范围为空集并给出 `DS-DENY-03` 依据，不显示空表格骨架 | 返回入口 |
| 提交中 | `submitting` | 保留列表 + 提示条 + 工具条写操作按钮置 loading 且禁用 | — |
| 部分失败 | `partial` | 批量设置任教部分失败：成功 / 失败分列 + 失败原因（离职教师不允许新增任教） | — |
| 排队中 | `queued` | 任务编号 + 队列位置 + 配额依据 `BR-IMP-015` | 查看任务中心 |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 点击"新增" | `PAGE-TCH-CREATE` | 抽屉 | 批次 2-2b；本批给占位提示 |
| 行内"编辑" / "角色" / "任教" | `PAGE-TCH-EDIT` / `ROLE` / `ASSIGN` | 抽屉 / 弹窗 / 独立页 | 批次 2-2b；本批给占位提示 |
| 行内"离职" | `DIALOG-TCH-LEAVE` | 本页弹窗 | **本批已实现**：登记后回列表并保留筛选 |
| 点击数据行 | `PAGE-TCH-DETAIL` | 抽屉 | 批次 2-2b |
| 点击"导入" | `PAGE-TCH-IMPORT` | 独立页 | 批次 2-4 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（郑雅琴） | 本校全部教师 | 新增、导入、导出、下载模板、批量设置任教、编辑、角色、任教、离职 | 手机号掩码；有 `read_contact` 可看全量 |
| 租户管理员（周敏） | 本租户组织内的教师 | 新增、导入、下载模板、编辑、角色、任教、离职；**无导出**（矩阵未授予 `data.export`） | 不含教学数据 |
| 年级主任（何文博） | 本年级教师 | 导出、批量设置任教、任教；无新增 / 编辑 / 角色 / 离职 | 监护人以外的教师信息只读 |
| 任课教师（谢明轩） | 本人信息 | 无任何写入口、无导出、无行内按钮 | 联系方式整列隐藏；操作列显示"只读" |
| 校领导（陆承志） | 本校全部教师 | 导出、任教 | 只读 + 审批类操作不在本页 |
| 平台运营（运维账号 A） | 全平台（按学校切换） | 默认只读；导出需逐次授权并留痕 | 操作列显示"只读"；切到他校显示"协助视图 · 只读"提示条 |

跨校任教：`苏睿`（`YXWY2017001`）归属云溪外国语学校、在实验学校有任教关系，因此**在实验学校视图里也单独占一行**并标注"跨校任教"（`REQ-TCH-008`）；平台运营切到外国语学校视图时同样能看到他。

## 8. 样例数据

| 数据 | 来源节点 | 用途 |
|---|---|---|
| 12 行教师列表 | `content-samples.json` 的 `teachers`（7 条）+ `teachers_edge_cases`（5 条） | 覆盖任职状态、跨校任教与 5 类边界样本 |
| 总条数 46 | `list_totals.teacher_list_total` | 分页器与总条数；页面只渲染 12 行样例 |
| 任教关系 | `teaching_assignments` | 任教学科与任课班级数；跨校任教样例（id 4） |
| 教育角色枚举 | `06-field-dictionary.yaml` 的 `edu_role` | 角色标签文案 |

选择理由：`何文博` 用来演示"年级主任但无任教关系"（任教学科与班级数为 `—`）；`韦志远` 演示"缺失手机号"；`阿依努尔·艾山` 演示间隔号姓名；`高洪波` / `苗雨欣` 演示离职与调离（不再出现在任教关系选择项中，历史关系保留）；`苏睿` 演示跨校任教必须标明任教学校。

## 9. 自查

- [x] 页面骨架属于 `TPL-LIST`
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-001` ~ `011` 本批新增）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`CR-004` 补登记 13 个教师模块字段与 `employment_status` 枚举，`GAP-033` 已关闭）
- [x] 七类状态齐全（loading / empty / error / forbidden / submitting / partial / queued）
- [x] 至少 2 种角色形态（本页给出 6 种，差异落在按钮与整列显隐上）
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
- [x] 1366×768 / 1440×900 / 1920×1080 三档截图无整页横向滚动与重叠

## 10. 本批（2-2a）范围说明

本批只交付**教师管理列表 1 页**，作为 2-2 的标准校验页。以下 12 页在验收通过后按同一标准铺开（2-2b）：

`PAGE-TCH-DETAIL`、`PAGE-TCH-CREATE`、`PAGE-TCH-EDIT`、`PAGE-TCH-ROLE`、`PAGE-TCH-ASSIGN`、`PAGE-TCH-LEAVE`、
`PAGE-GRD-LIST`、`PAGE-GRD-DETAIL`、`PAGE-GRD-CREATE`、`PAGE-GRD-BATCH`、`PAGE-GRD-LEADER`、`PAGE-GRD-ARCHIVE`。

本批已实现的交互：筛选与重置、角色/状态切换、批量设置任教的勾选校验、离职登记二次确认弹窗、导出与下载模板的提交模拟。
其余跳转按占位提示处理，用于验证入口与权限显隐。
