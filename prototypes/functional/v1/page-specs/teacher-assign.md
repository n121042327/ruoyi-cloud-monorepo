# 任教关系设置

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-ASSIGN` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | page（独立页；路由 `/edu/teacher/assignment`，双栏：左侧主体选择器 + 右侧任教关系） |
| 骨架模板 | `TPL-LIST`（列表 + 内联表单；新增表单不跳页） |
| 所属批次 | 2-2b-2 |
| 上游需求 | `REQ-TCH-035` ~ `REQ-TCH-044`、`REQ-TCH-070`；导出复用 `REQ-TCH-056` ~ `REQ-TCH-058` |
| 上游规则 | `BR-TEACHER-002`、`BR-TEACHER-003`、`BR-TEACHER-004`、`BR-TEACHER-006`、`BR-CLASS-008`、`DS-DENY-02`、`DS-DENY-06` |
| 权限资源 | `person.teaching_assignment` 的 `read` / `create` / `update`；导出用 `data.export:export` |
| 数据范围 | 学年学期、学科、班级都必须在操作人范围内（`DS-DENY-02`）；批量提交逐条校验，任一条越权整体拒绝（`DS-DENY-06`） |
| 原型文件 | `pages/teacher-assign.html` |

## 1. 页面目的

教务主任与年级主任在这里维护**任教关系**：谁在哪个学年学期、教哪个学科、带哪个班（行政班或教学班）。
两个入口视角：**班级视角**解决"这个班还缺哪些学科"，**教师视角**解决"这位老师这学期带哪些班"（`REQ-TCH-044`）。
学年切换时不再手工重录：用"复制上一学年"整批复制并在预览阶段修正冲突（`REQ-TCH-070`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | 文本 + `el-tag` | 数据范围、有效任教关系总数、数据截止时间 |
| 2 | 工具条 | `toolbar` | `el-card` 的 `#header` | 新增任教关系、复制上一学年、导出任教关系；右侧列配置与刷新 |
| 3 | 左栏：选择主体 | `picker` | `el-card` + `el-table` | 视角切换、学年学期、关键字；班级列表 8 行 / 教师列表 11 行，点击即切换主体 |
| 4 | 主体页头 | `subject` | `el-card` | 当前主体、类型、班主任或教育角色；"前往班级详情" |
| 5 | 任教关系表 | `table` | `el-table` | 学科、任教教师、班级、班级类型、周课时、状态、操作；含历史学年学期只读分组 |
| 6 | 新增任教关系 | `form` | `el-card` + `el-form` + `el-checkbox` | 学年学期、学科、任教教师、班级类型、班级多选、周课时、生效期间 |
| 7 | 复制弹窗 | — | `el-dialog`（`DIALOG-TCH-COPY`） | 源 / 目标学年学期 + 冲突预览 + 确认复制 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `term_id` | 学年学期 | `el-select` | 是 | 教务主任、年级主任 | 当前学期 | 历史学期拒绝新增（`REQ-TCH-043`） | 任教关系必须绑定学期 |
| `subject_id` | 学科 | `el-select` | 是 | 教务主任、年级主任 | 空 | 必填；取值来自 `subject` 列表 | 与 `subject_code` 枚举对应 |
| `teacher_id` | 任教教师 | `el-select` | 是 | 教务主任、年级主任 | 空 | 必填；离职 / 调离教师不出现（`REQ-TCH-060`） | 跨校任教教师可被本校选中 |
| `class_type` | 班级类型 | `el-select` | 是 | 教务主任、年级主任 | 行政班 | 必填；取值来自 `class_type` 枚举 | 行政班 / 教学班 |
| `class_id` | 班级 | 多选（`el-select multiple` 形态） | 是 | 教务主任、年级主任 | 当前主体班级 | 至少 1 个；只列数据范围内的班级 | 唯一键：`(term_id, teacher_id, subject_id, class_type, class_id)` |
| `weekly_hours` | 周课时 | `el-input` | 否 | 教务主任、年级主任 | 5 | 数字，允许 0.5 步长 | 用于工作量统计 |
| `start_date` | 生效期间 | `el-date-picker` | 否 | 教务主任、年级主任 | 学期起始日 | 格式 `YYYY-MM-DD` | 留空表示随学期 |

列字段：`subject_code`、`teacher_name`、`class_id`、`class_type`、周课时、状态、操作。
左栏还使用 `class_name`、`grade_name`、`head_teacher_id`（班主任）与 `edu_role`（教师视角的角色标签）。

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-TCH-033` | 切换视角 | 点击班级 / 教师视角 | `person.teaching_assignment:read` | 切换左栏与右栏组织方式，保留学期与筛选 | — |
| `ACT-TCH-034` | 选择主体 | 点击左侧列表行 | 主体在数据范围内 | 右栏加载该主体的任教关系 | `listTeachingAssignment` |
| `ACT-TCH-035` | 打开新增任教关系表单 | 点击工具条 / 空态 / "待补充"行 | `person.teaching_assignment:create` | 定位到新增表单 | — |
| `ACT-TCH-039` | 保存任教关系 | 点击 | 必填与范围校验通过 | 逐条写入，已存在跳过，越权整体拒绝 | `saveTeachingAssignment` |
| `ACT-TCH-036` | 失效任教关系 | 点击行内"失效" | `person.teaching_assignment:update` | 二次确认后置为失效并写审计 | `disableTeachingAssignment` |
| `ACT-TCH-037` | 导出任教关系 | 点击 | `data.export:export` | 按视角与范围导出，写导出审计 | `exportTeachingAssignment` |
| `ACT-TCH-015` | 复制上一学年 | 点击 | `person.teaching_assignment:create` | 打开 `DIALOG-TCH-COPY` 预览 | `copyTeachingAssignment` |
| `ACT-TCH-038` | 确认复制 | 点击弹窗内"确认复制" | 完成冲突预览 | 异步执行并返回任务编号 | `copyTeachingAssignment` |
| `ACT-TCH-016` | 前往班级详情 | 点击主体页头 | `org.class:read` | 跳 `PAGE-CLS-DETAIL`（批次 2-3） | `getClass` |
| `ACT-TCH-017` | 展开 / 收起学年学期分组 | 点击 | 分组存在 | 展开历史学期（只读） | — |
| `ACT-COM-002` / `ACT-COM-004` / `ACT-COM-005` | 重置 / 列配置 / 刷新 | 点击 | 同通用动作 | 同通用动作 | `listTeachingAssignment` |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 表格内骨架行，不用全屏遮罩 | — |
| 空数据 | `empty` | "该班级在当前学年学期还没有任教关系"，并给"新增任教关系"主动作 | `ACT-TCH-035` |
| 查询失败 | `error` | 错误说明 + 请求编号 + 重试 | `ACT-COM-005` |
| 无权限 | `forbidden` | 说明任课教师与班主任只读、年级主任限本年级、空范围不放行数据 | 返回教师管理 |
| 提交中 | `submitting` | 写操作按钮 loading 且禁用 | — |
| 部分失败 | `partial` | 任务编号 + 成功 / 失败分列 + 失败原因 | — |
| 排队中 | `queued` | 任务编号、队列位置、配额依据（复制上一学年转异步） | 查看任务中心 |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 列表行内"任教" / 详情"设置任教" | `PAGE-TCH-ASSIGN` | 独立页 | 返回列表 / 详情并保留筛选 |
| 左栏选择班级或教师 | 同页 | 区块刷新 | 不跳页，保留学年学期与筛选 |
| 主体页头"前往班级详情" | `PAGE-CLS-DETAIL` | 独立页 | 返回本页并保留当前主体 |
| 工具条"复制上一学年" | `DIALOG-TCH-COPY` | 弹窗 | 关闭后回到本页 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（`academic_director`） | 全校班级与教师的任教关系 | 新增、保存、失效、复制上一学年、导出、前往班级详情 | 无 |
| 年级主任（`grade_leader`） | 本年级的班级与教师 | 新增、保存、失效、导出（限本年级） | 不能复制上一学年、不能跨年级选择班级 |
| 班主任（`homeroom`） | 只读本人班级 | 无写入口 | 新增表单整体只读 |
| 任课教师（`subject_teacher`） | 只读本人任教关系 | 无写入口 | 同上 |
| 校领导（`school_leader`） | 只读本校全部 | 导出 | 无写入口 |
| 租户管理员（`tenant_admin`） | 只读（`person.teaching_assignment` 无 create / update） | 无写入口 | 教师主体维护在其职责内，教学数据不在 |
| 平台运营（`platform_ops`） | `DS-01` 全平台，只读 | 无写入口，导出需逐次授权 | 访问留痕并同步租户侧 |

## 8. 样例数据

| 节点 | 用途 | 为什么用它 |
|---|---|---|
| `content-samples.json` → `teaching_assignment_board.class_picker` | 左栏班级视角 8 行 | 覆盖"班主任未指定""教学班""尚未排课（0 条）""外校班级"四类边界 |
| `content-samples.json` → `teaching_assignment_board.teacher_picker` | 左栏教师视角 11 行 | 覆盖跨校任教、纯管理角色 0 任教、离职 / 调离两类终态 |
| `content-samples.json` → `teaching_assignment_board.assignments_of_class_20260101` | 右栏任教关系 8 行 | 覆盖生效中、已失效（离职 / 调离）、待补充三类状态，且与 `teaching_assignments` 主数据同源 |
| `content-samples.json` → `teaching_assignment_board.copy_preview` | 复制上一学年的冲突预览 | 演示"复制 / 跳过 / 待处理"三种处理方式 |
| `content-samples.json` → `subjects` / `classes` / `grades` / `calendar.terms` | 表单下拉选项 | 选项口径与班级、年级、学科模块保持同一份数据 |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-LIST` + 双栏工作台；新增表单不跳页）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-015` ~ `017`、`033` ~ `039`、`ACT-COM-002/004/005`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`term_id`、`subject_id`、`teacher_id`、`class_type`、`class_id`、`weekly_hours`、`start_date` 均已登记）
- [x] 七类状态给出可见形态（见第 5 节）
- [x] 1440×900 与 1366×768 下未出现整页横向滚动条与元素重叠（表格内部按需横向滚动）
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
