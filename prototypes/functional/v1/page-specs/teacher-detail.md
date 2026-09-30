# 教师详情

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-DETAIL` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | detail；容器为抽屉（`container: drawer`，尺寸 `lg` = 800px，取自 `layout-spec.yaml` 的 `drawer.sizes`） |
| 骨架模板 | `TPL-DETAIL` |
| 所属批次 | 2-2b-1 |
| 上游需求 | `REQ-TCH-009` ~ `REQ-TCH-013`；另被 `REQ-TCH-022`、`REQ-TCH-026` ~ `REQ-TCH-034`、`REQ-TCH-044`、`REQ-TCH-045` ~ `REQ-TCH-049`、`REQ-TCH-063` ~ `REQ-TCH-065`、`REQ-TCH-070` 引用 |
| 上游规则 | `BR-TEACHER-002`、`BR-TEACHER-003`、`BR-TEACHER-005`、`BR-TEACHER-006`、`BR-CLASS-004`、`BR-DATA-018`、`BR-AUDIT-001`、`BR-AUDIT-005`、`DS-DENY-07` |
| 权限资源 | `person.teacher` 的 `read`；分区内写入口取 `person.teacher:update`、`person.teaching_assignment:create`、`read_contact`、`org.class:read`、`audit.log:read` |
| 数据范围 | 按 `DS-DENY-07` 先把数据范围条件拼进查询再取数；教务主任与校领导 `DS-04`、年级主任 `DS-05`、班主任与任课教师 `DS-06` / `DS-07`（本人）、平台运营 `DS-01` |
| 原型文件 | `pages/teacher-list.html` 的 `data-demo-panel="PAGE-TCH-DETAIL"` |

> PRD 6.1 把本页写成"详情页（抽屉）"，PRD 6.3 与 `page-actions.yaml` 的 `ACT-TCH-010` 也都写明"打开详情抽屉"。
> 因此原型按列表页内的浮层片段实现，不单独出页面文件；`navigation.yaml` 已同步登记 `container: drawer`。

## 1. 页面目的

教务主任、租户管理员、年级主任在教师列表点击任意数据行后打开，用于在一个上下文里看全一名教师的六类信息：
基本信息、教育角色、任教关系、班主任任职、账号、变更记录。
读完之后要么原地发起写操作（编辑、角色、任教、停用账号、重置密码），要么关闭抽屉回到列表继续筛选。

跨校任教教师是本页的重点场景：归属校与任教校不同，两校各自只能看到本校范围内的任教明细（`BR-TEACHER-002`、`BR-DATA-018`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | 文本 + `el-tag` | 姓名、工号、在职状态、跨校任教标签、关闭按钮 |
| 2 | 范围与截止时间 | — | `.legend` | 数据范围、数据截止时间、模板名（对应"页面没标出当前在看谁的数据"这条根因） |
| 3 | 形态切换 | — | `.chip-group` | 形态 A（跨校任教）/ 形态 B（班主任 + 任课），用于演示两种数据形态 |
| 4 | 基本信息 | `section` | `el-descriptions` | 10 个字段；右上角"编辑"入口复用 `ACT-TCH-006` |
| 5 | 教育角色 | `section` | `el-descriptions` + `el-table` | 角色 / 任职范围 / 任职期间 / 状态 / 解除；空态给说明不给空表格 |
| 6 | 任教关系 | `section` | `el-descriptions` + `el-table` | 按学年学期分组，当前学年学期默认展开；含"复制上一学年""设置任教" |
| 7 | 班主任任职 | `section` | `el-descriptions` | 只读；全班入口在班级模块（`REQ-TCH-012`） |
| 8 | 教师账号 | `section` | `el-descriptions` | 登录名、账号状态、最近登录、创建时间；含停用与重置密码 |
| 9 | 变更记录 | `section` | `el-timeline` | 近 5 条时间线；"查看全部"跳审计模块（批次 2-8） |
| 10 | 底部操作条 | — | 固定 footer | 左侧说明"关闭后保留筛选与页码"，右侧"关闭"（`ACT-TCH-018`） |

## 3. 字段清单

只读展示字段（详情区一律用纯文本，不用 disabled 表单——`layout-spec.yaml` 的 `TPL-DETAIL` 规定只读优先用 `el-descriptions`）：

| 字段名 | 中文 | 组件 | 备注 |
|---|---|---|---|
| `teacher_no` | 工号 | 文本（等宽） | 租户内唯一（`BR-TEACHER-007`） |
| `teacher_name` | 姓名 | 文本 | 超长不截断 |
| `gender` | 性别 | 文本 | 取值来自 `gender` 枚举 |
| `school_id` | 所属学校 | 文本 | 归属校；跨校任教时与任教校并列说明 |
| `teacher_phone` | 联系电话 | 文本（掩码） | 默认掩码，查看完整值需 `read_contact` 且留痕 |
| `email` | 邮箱 | 文本 | 账号找回渠道，非登录名 |
| `hire_date` | 入职日期 | 文本 | 格式 YYYY-MM-DD |
| `employment_status` | 在职状态 | `el-tag` | 在职 / 离职 / 调离；取值来自 `employment_status` 枚举 |
| — | 最近更新 | 文本 | 时间 + 操作人 |
| `remark` | 备注 | 文本 | 详情页不截断 |
| `edu_role` | 教育角色 | `el-tag` | 取值来自 `edu_role` 枚举；提示"教育角色 ≠ 系统角色"（`REQ-TCH-034`） |
| `subject_code` | 任教学科 | 文本 | 取值来自 `subject_code` 枚举 |
| `class_type` | 班级类型 | 文本 | 取值来自 `class_type` 枚举（行政班 / 教学班） |
| `class_name` | 班级 | 文本 | 行政班复用 `class_name`；教学班同列 |
| — | 周课时 | 数字（右对齐） | 用于判断任教工作量 |
| `login_name` | 登录名 | 文本（等宽） | 全平台唯一（`REQ-TCH-045`） |

> `school_id`、`teacher_phone`、`email`、`hire_date`、`employment_status` 已由 `CR-004` 补登记进 `06-field-dictionary.yaml`（`GAP-033` 已关闭）。
> 展示名（学校名、学年学期名、学科名、任教学校名）由引用实体带出，不在字典里重复登记。

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-TCH-006` | 编辑 | 点击 | `person.teacher:update` | 打开 `PAGE-TCH-EDIT` 抽屉 | — |
| `ACT-TCH-007` | 分配角色 | 点击 | `person.teacher:update` | 打开 `PAGE-TCH-ROLE` 弹窗（批次 2-2b-2） | — |
| `ACT-TCH-008` | 设置任教 | 点击 | `person.teaching_assignment:read` | 进入 `PAGE-TCH-ASSIGN`（批次 2-2b-2） | — |
| `ACT-TCH-011` | 查看完整手机号 | 点击掩码旁链接 | `read_contact` | 就地展示并写访问日志 | — |
| `ACT-TCH-012` | 解除教育角色 | 点击角色行内"解除" | `person.teacher:update` | 二次确认后解除并刷新 | `removeTeacherEduRole` |
| `ACT-TCH-013` | 停用 / 启用账号 | 点击 | `person.teacher:update` | 二次确认后切换状态并写审计 | `changeTeacherAccountStatus` |
| `ACT-TCH-014` | 重置密码 | 点击 | `person.teacher:update` | 生成一次性初始凭据，不展示明文 | `resetTeacherPassword` |
| `ACT-TCH-015` | 复制上一学年的任教关系 | 点击 | `person.teaching_assignment:create` | 预览冲突后异步执行（`REQ-TCH-070`，批次 2-2b-2） | `copyTeachingAssignment` |
| `ACT-TCH-016` | 前往班级详情 | 点击 | `org.class:read` | 跳 `PAGE-CLS-DETAIL`（批次 2-3） | `getClass` |
| `ACT-TCH-017` | 展开 / 收起学年学期分组 | 点击 | 分组存在 | 折叠状态写入用户偏好 | — |
| `ACT-TCH-018` | 关闭详情 | 点击 | 始终可用 | 回到列表并保留筛选与页码 | — |
| `ACT-TCH-029` | 查看全部变更记录 | 点击 | `audit.log:read` | 跳 `PAGE-AUDIT-LOG-LIST`（批次 2-8） | `listAuditLog` |

## 5. 状态清单

本页是抽屉，不承载列表查询，页面级状态由宿主列表页 `PAGE-TCH-LIST` 承载。抽屉内部给出的形态：

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 不适用（宿主页已加载数据，抽屉直接渲染传入对象）；生产端打开抽屉时以骨架屏覆盖分区 | — |
| 空数据 | `empty` | 两个分区各自有空态：教育角色为"暂无学校级教育角色"、任教关系为"当前学年学期没有任教关系"，都不给空表格 | 分配角色 / 设置任教 |
| 查询失败 | `error` | 不适用（宿主页展示查询失败态与请求编号）；生产端按 `ACT-COM-005` 重试 | 重试 |
| 无权限 | `forbidden` | 无权字段不返回给前端（`REQ-TCH-010`）；对无 `person.teacher:read` 的账号整页不可见，入口不渲染 | — |
| 提交中 | `submitting` | 写动作（`ACT-TCH-012` ~ `015`）点击后按钮 loading 且禁用，禁止重复提交 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 列表点击行 | `PAGE-TCH-DETAIL` | 抽屉 | 关闭后回到列表，保留筛选条件与页码 |
| "编辑" | `PAGE-TCH-EDIT` | 抽屉 | 关回详情；保存后刷新详情与列表 |
| "分配角色" | `PAGE-TCH-ROLE` | 弹窗 | 关回详情 |
| "设置任教" | `PAGE-TCH-ASSIGN` | 独立页 | 返回后回到详情 |
| 班主任班级"前往班级详情" | `PAGE-CLS-DETAIL` | 独立页 | 返回后回到详情 |
| "查看全部"变更记录 | `PAGE-AUDIT-LOG-LIST` | 独立页 | 返回后回到详情 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（`academic_director`） | 本校全部教师的完整六类分区 | 编辑、分配角色、设置任教、复制上一学年、停用账号、重置密码、查看完整手机号、查看全部变更 | 无 |
| 租户管理员（`tenant_admin`） | 本租户组织配置范围内教师 | 编辑（含工号）、分配角色、停用账号、重置密码、查看完整手机号 | 任教关系只读（无 `person.teaching_assignment:create`） |
| 年级主任（`grade_leader`） | 负责年级范围内教师 | 设置任教（只读入口）、任职分组折叠 | 其余写入口不渲染 |
| 班主任 / 任课教师（`homeroom` / `subject_teacher`） | 仅本人信息 | 无任何写入口 | 手机号整块不出现；页面提示"未配置教育角色、未建立任教关系"时的空范围说明 |
| 校领导（`school_leader`） | 本校全部教师 | 查看完整手机号、查看全部变更 | 只读；无 `person.teacher:update`（`CR-004` / `GAP-032` 裁决 A） |
| 平台运营（`platform_ops`） | `DS-01` 全平台，只读 | 无写入口；导出需逐次授权 | 每次访问写访问日志并同步租户侧（`BR-AUDIT-005`） |

## 8. 样例数据

| 节点 | 用途 | 为什么用它 |
|---|---|---|
| `content-samples.json` → `teachers` 的 `teacher_id=1007`（苏睿） | 形态 A | 唯一一条跨校任教样本，用来验证"归属校 ≠ 任教校"时的分区渲染与只读口径 |
| `content-samples.json` → `teachers` 的 `teacher_id=1003`（邓丽娟） | 形态 B | 同时具备班主任任职（只读）与 2 条任教关系（含教学班），并且**没有学校级教育角色**，用来验证空态 |
| `content-samples.json` → `teachers_edge_cases` 的 `teacher_id=1003` 之外的 5 条 | 列表入口验证 | 无手机号显示 `—`、姓名含间隔号不截断、离职 / 调离行不提供离职入口 |
| `content-samples.json` → `teacher_detail_samples` | 两个形态的完整分区数据 | 保证详情字段与列表字段同源，不在页面里临时编造 |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-DETAIL`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-006` ~ `019`、`029`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`CR-004` 补登记 13 个教师模块字段，`GAP-033` 已关闭）
- [x] 五类状态按"抽屉 + 宿主页"分工给出形态（见第 5 节）
- [x] 1440×900 与 1366×768 下未出现横向滚动条与元素重叠（表格内部按需横向滚动）
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
