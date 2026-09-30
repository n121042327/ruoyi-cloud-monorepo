# 阶段 2 业务原型 · 验收证据（批次 2-1、2-2a、2-2b-1、2-2b-2、2-2b-2b、2-3a、2-3b、2-3c、2-3d）

本目录保存批次 2-1（学生管理列表 + 新增/编辑抽屉样板页）、2-2a（教师管理列表样板页）、
2-2b-1（教师详情 + 新增教师 + 编辑教师，含放弃确认弹窗）、2-2b-2（教育角色分配 + 任教关系设置独立页）、
2-2b-2b（年级管理列表样板页 + 删除确认弹窗）、2-3a（班级管理列表样板页 + 停用/删除确认）、
2-3b（班级详情与花名册）、2-3c（新建/编辑、批量生成、复制、指定班主任四个弹窗）、
2-3d（编班、批量迁学生、移出确认与调班）的截图证据。
截图由本机 Chrome 无头模式生成，命令可复现，见文末。

## 1. 截图清单

| 文件 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `student-list_1366x768.png` | 1366×768 | 默认（教务主任，正常态） | 最小分辨率无整页横向滚动、无重叠、无文字截断；可见 7 行 |
| `student-list_1440x900.png` | 1440×900 | 默认（教务主任，正常态） | 设计基准分辨率；可见 8 行；筛选项与工具条布局 |
| `student-list_1920x1080.png` | 1920×1080 | 默认（教务主任，正常态） | 宽屏下主内容区不无限拉伸；13 行样例全部可见；分页器与说明可见 |
| `student-list_role-homeroom_1440x900.png` | 1440×900 | 角色 = 班主任 | 无新增 / 导入 / 调班入口；行内仅"编辑""异动" |
| `student-list_role-grade-leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 有新增 / 编辑 / 异动 / 调班；无证件号后四位筛选 |
| `student-list_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 无任何写入口与导出；联系电话整列隐藏；行内无按钮 |
| `student-list_role-platform-ops_1440x900.png` | 1440×900 | 角色 = 平台运营 | 无写入口；证件号后四位可查；导出需授权 |
| `student-list_state-loading_1440x900.png` | 1440×900 | 状态 = 加载中 | 表格内骨架行，不用全屏遮罩 |
| `student-list_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 两种空态可切换（筛选无结果 / 无数据范围） |
| `student-list_state-error_1440x900.png` | 1440×900 | 状态 = 查询失败 | 错误说明 + 请求编号 + 重试 |
| `student-list_state-forbidden_1440x900.png` | 1440×900 | 状态 = 无权限 | 说明数据范围为空集，不显示空表格骨架 |
| `student-list_state-submitting_1440x900.png` | 1440×900 | 状态 = 提交中 | 写操作按钮置 loading 且禁用 |
| `student-list_state-partial_1440x900.png` | 1440×900 | 状态 = 部分失败 | 成功 / 失败分列与失败明细下载 |
| `student-list_state-queued_1440x900.png` | 1440×900 | 状态 = 排队中 | 任务编号、队列位置、配额依据 |
| `student-list_drawer-create_1440x900.png` | 1440×900 | ~~新增学生抽屉（第 1 步）~~ | **已失效**：CR-008 / D-059 后表单载体由抽屉改弹窗，请改看 `student-list_dialog-create_1440x900.png` |
| `student-list_drawer-edit-homeroom_1440x900.png` | 1440×900 | ~~编辑学生抽屉（角色 = 班主任）~~ | **已失效**：同上，请改看 `student-list_dialog-edit-homeroom_1440x900.png` |
| `student-list_dialog-create_1440x900.png` | 1440×900 | 新增学生弹窗（第 1 步） | 三步步骤条、系统发号只读、字段级必填标记；载体与 apps/plus-ui 的 dialog 一致 |
| `student-list_dialog-edit-homeroom_1440x900.png` | 1440×900 | 编辑学生弹窗（角色 = 班主任） | 字段级可编辑性矩阵：仅监护人 / 联系方式可写，其余灰底只读；弹窗内长内容可滚动、footer 固定 |
| `student-list_platform-ops-school-switch_1440x900.png` | 1440×900 | 角色 = 平台运营 + 学校切到云溪外国语学校 | 平台运营协助视图 · 只读：页头显示只读与留痕提示、工具栏只剩"导出（需授权）"、操作列显示"只读"。**共享授权不适用于学生数据**，跨校共享的对象是题库习题与试卷资源 |
| `index_1440x1700.png` | 1440×1900（文件名沿用批次 2-1 的命名，未改名） | 原型入口页 | 页面清单、演示步骤、规范文件、批次进度、已知缺口；批次 2-2b-2b 后条目变多，截图高度随内容放大到 1900 |
| `teacher-list_1366x768.png` | 1366×768 | 教师管理列表（默认） | 批次 2-2a 样板：最小分辨率无整页横向滚动 |
| `teacher-list_1440x900.png` | 1440×900 | 教师管理列表（默认） | 7 个筛选项、11 列、4 个行操作、跨校任教标注 |
| `teacher-list_1920x1080.png` | 1920×1080 | 教师管理列表（默认） | 宽屏下主内容区不无限拉伸；12 行样例全部可见 |
| `teacher-list_role-tenant-admin_1440x900.png` | 1440×900 | 角色 = 租户管理员 | 有新增 / 导入 / 编辑 / 角色 / 任教 / 离职；**无导出**（矩阵未授予 `data.export`） |
| `teacher-list_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 无写入口与导出；联系电话整列隐藏；操作列显示"只读" |
| `teacher-list_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 两种空态可切换 |
| `teacher-list_dialog-leave_1440x900.png` | 1440×900 | 离职登记弹窗 | 危险动作二次确认 + 影响范围说明（本批已实现） |
| `teacher-list_platform-ops-school-switch_1440x900.png` | 1440×900 | 平台运营切到他校 | 协助视图 · 只读提示条；学校筛选仅平台运营可切换 |
| `teacher-detail_1366x768.png` | 1366×768 | 教师详情抽屉（形态 A · 跨校任教） | 800px 抽屉在最小分辨率下不挤裂；六个分区可滚动；页头保留数据范围与数据截止 |
| `teacher-detail_1440x900.png` | 1440×900 | 教师详情抽屉（形态 A · 跨校任教） | 基本信息 10 字段、教育角色表、跨校任教说明、手机号掩码与查看留痕入口 |
| `teacher-detail_sample-b_1440x900.png` | 1440×900 | 教师详情抽屉（形态 B · 班主任 + 任课） | 教育角色空态（不给空表格）、跨校标签隐藏、范围提示随形态切换 |
| `teacher-detail_role-platform-ops_1440x900.png` | 1440×900 | 教师详情抽屉（角色 = 平台运营） | 无任何写入口；范围提示为全平台只读；访问留痕说明 |
| `teacher-create_step-1_1440x900.png` | 1440×900 | 新增教师弹窗（第 1 步 基本信息） | 三步步骤条在弹窗内；工号格式与必填校验、所属学校受数据范围约束 |
| `teacher-create_step-3_1440x900.png` | 1440×900 | 新增教师弹窗（第 3 步 任职信息） | 步骤 1 / 2 已完成、底部按钮切换为"上一步 / 保存"；弹窗内滚动、footer 固定 |
| `teacher-edit_1440x900.png` | 1440×900 | 编辑教师弹窗（角色 = 教务主任） | 字段级可编辑性：工号与入职日期灰底只读，其余可写；所属学校永久只读 |
| `teacher-edit_role-subject-teacher_1440x900.png` | 1440×900 | 编辑教师弹窗（角色 = 任课教师） | 全部字段灰底只读并逐字段给出只读说明；并发保护说明 |
| `teacher-edit_1920x1080.png` | 1920×1080 | 编辑教师弹窗（宽屏） | 双列表单在宽屏下不拉伸变形 |
| `teacher-dialog-discard_1440x900.png` | 1440×900 | 放弃已填写内容（二次确认） | 写清"对谁、做什么、影响范围"：关闭后不保存、不进草稿 |
| `interaction-verification.md` | — | 交互可点性验证报告 | 点行内「编辑」无反应的根因（外壳 `data-api` 误判 + 页面 `stopPropagation`）、修复点与 16 条用例实测结果 |
| `teacher-role_1440x900.png` | 1440×900 | 教育角色分配弹窗 | 学校级角色勾选 + 年级主任任职表（含历史只读行）+ 保存前四条检查；弹窗超高时可内部滚动 |
| `teacher-role_grade-form_1440x1400.png` | 1440×1400 | 教育角色分配（展开年级主任任职表单） | 学年学期与年级必填、是否主管单选、任职期间；用高视口保证整屏可见 |
| `teacher-assign_1440x900.png` | 1440×900 | 任教关系设置（班级视角） | 双栏布局；左栏 8 个班级，右栏 8 条任教关系（生效中 / 已失效 / 待补充）与行操作差异 |
| `teacher-assign_1366x768.png` | 1366×768 | 任教关系设置（最小分辨率） | 无整页横向滚动；左栏收窄后仍可读 |
| `teacher-assign_1920x1080.png` | 1920×1080 | 任教关系设置（宽屏） | 主内容区不无限拉伸，表格列按列宽分配 |
| `teacher-assign_view-teacher_1440x900.png` | 1440×900 | 任教关系设置（教师视角） | 左栏切换为 11 名教师，含跨校任教与 0 任教两类样本 |
| `teacher-assign_state-empty_1440x900.png` | 1440×900 | 任教关系设置（空数据态） | "该班级在当前学年学期还没有任教关系" + 主动作 |
| `teacher-assign_dialog-copy_1440x900.png` | 1440×900 | 复制上一学年（冲突预览） | 复制 / 跳过 / 待处理三种处理方式与异步口径 |
| `teacher-assign_role-subject-teacher_1440x900.png` | 1440×900 | 任教关系设置（角色 = 任课教师） | 只读角色：无新增与保存入口，左栏仍可浏览 |
| `grade-list_1366x768.png` | 1366×768 | 年级管理列表（默认） | 批次 2-2b-2b 样板：最小分辨率无整页横向滚动、无元素重叠；可见 7 行 |
| `grade-list_1440x900.png` | 1440×900 | 年级管理列表（默认） | 4 个筛选项、8 列 + 固定操作列（操作列 280px）、4 个行入口（详情 / 编辑 / 指定年级主任 / 归档；空年级行多一个「删除」）、13 行样例；可见 11 行 |
| `grade-list_1920x1080.png` | 1920×1080 | 年级管理列表（宽屏） | 主内容区不无限拉伸；14 行样例与分页器同屏可见 |
| `grade-list_role-tenant-admin_1440x900.png` | 1440×900 | 角色 = 租户管理员 | 有新增 / 批量生成 / 编辑 / 指定主任 / 归档；**无导出**（矩阵未授予 `data.export`） |
| `grade-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 只读并审批年级主任变更（`GAP-034` 裁决 B）；只有导出，全列只读 |
| `grade-list_role-grade-leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 数据范围提示 `DS-05`；无写入口；操作列显示"只读" |
| `grade-list_role-homeroom_1440x900.png` | 1440×900 | 角色 = 班主任 | 数据范围提示 `DS-06`；无写入口；只读年级摘要 |
| `grade-list_role-super-admin_1440x900.png` | 1440×900 | 角色 = 超级管理员 | 全部按钮与行内操作可见；页头数据范围为全平台并提示 `BR-ORG-014` 强制留痕 |
| `grade-list_platform-ops-school-switch_1440x900.png` | 1440×900 | 平台运营切到云溪外国语学校 | 协助视图 · 只读提示条；工具栏只剩"导出（需授权）"；默认筛选下他校行不出现 |
| `grade-list_filter-enroll-2021_1440x900.png` | 1440×900 | 入学年份 = 2021（空年级） | 空年级行（2021 级 小学六年级）是唯一带「删除」入口的行；班级数与在读数均为 0 |
| `grade-list_filter-leader-none_1440x900.png` | 1440×900 | 年级主任 = 未指定 | 5 行「未指定」，并提示该年级的数据范围解析不出来（`DS-05` / `DS-07`） |
| `grade-list_state-loading_1440x900.png` | 1440×900 | 状态 = 加载中 | 表格内骨架行，不用全屏遮罩 |
| `grade-list_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 两种空态（筛选无结果 / 本校还没建年级）与各自的主动作 |
| `grade-list_state-error_1440x900.png` | 1440×900 | 状态 = 查询失败 | 错误说明 + 请求编号 + 错误码 + 重试 |
| `grade-list_state-forbidden_1440x900.png` | 1440×900 | 状态 = 无权限 | 说明范围为空集且不降级为全量（`DS-DENY-03`） |
| `grade-list_state-partial_1440x900.png` | 1440×900 | 状态 = 部分失败 | 按学段批量生成的部分冲突：3 条中 2 成功 1 冲突 |
| `grade-list_dialog-delete_1440x900.png` | 1440×900 | 删除年级二次确认 | 写清对象、影响范围、删除是逻辑删除；原因必填并写审计 |
| `grade-list_verify-results.png` | 1500×1250 | 交互验证 harness 结果清单 | GL-01 ~ GL-38 共 38 条用例**全部通过** |
| `verify-grade-list.html` | — | 年级列表交互验证 harness | 用同源 iframe + 真实事件派发跑 38 条断言，可重复执行 |
| `grade-detail_1440x900.png` | 1440×900 | 年级详情抽屉（样本 A · 2026 级 高一） | 5 个分区齐备；下辖班级 3 个、在读学生统计含合计行、任职历史含已解除的历史行 |
| `grade-detail_sample-b_1440x900.png` | 1440×900 | 年级详情抽屉（样本 B · 空年级） | 三个子分区同时给空态说明，不留空白表格 |
| `grade-create_1440x900.png` | 1440×900 | 新建 / 编辑年级弹窗 | 载体与 apps/plus-ui 一致用弹窗；名称按规则生成、序号按学段给 1–6 / 1–3 |
| `grade-batch_1440x900.png` | 1440×900 | 按学段批量生成弹窗 | 弹窗内不放表格：将生成的年级用列表渲染，已存在的标"已存在，跳过" |
| `grade-leader_1440x900.png` | 1440×900 | 指定年级主任弹窗 | 学年学期必填、任职清单（含主要负责人与移除）、保存前四条检查 |
| `grade-archive_1440x900.png` | 1440×900 | 归档确认弹窗 | 影响范围 + 引用情况（班级 / 在读 / 主任）+ 原因必填写审计 |
| `_debug-grade-delete.html` / `_debug-grade-delete.png` | — | 深链接与删除确认的根因复现 | 修前：在已打开页面上改 hash 不生效（13 行 / 下拉为空）；修后：立即生效（1 行 / 下拉 2021） |
| `verify-carrier-change.html` | — | 载体变更回归 harness | 同源 iframe + 真实事件派发跑 14 条断言：表单是弹窗、详情仍是抽屉、弹窗内无 drawer-* 钩子 |
| `carrier-change_verify-results.png` | 1500×620 | 载体变更回归结果 | `合计 14 条，通过 14 条，不通过 0 条 —— 全部通过` |
| `grade-list_platform-ops-school-switch_1440x900.png` | 1440×900 | 平台运营切到云溪外国语学校（年级） | 协助视图 · 只读提示条；工具栏只剩「导出（需授权）」；他校行只给「详情」可读，无任何写入口 |
| `verify-detail-entry.html` | — | 详情浮层入口验证 harness | 三个同源 iframe（年级 / 教师 / 学生）真实派发点击，跑 6 条断言：点行与操作列「详情」都能开抽屉、显式 dialog 不被覆盖、未交付的详情不开空抽屉 |
| `detail-entry_verify-results.png` | 1500×1150 | 详情浮层入口验证结果 | `合计 6 / 6 条，通过 6 条，不通过 0 条 —— 全部通过`（RD-01 ~ RD-06） |
| `class-list_1366x768.png` | 1366×768 | 班级管理列表（默认） | 批次 2-3a 样板：最小分辨率无整页横向滚动、无文字截断；表格列宽之和 1116px ≤ 可用 1118px |
| `class-list_1440x900.png` | 1440×900 | 班级管理列表（默认） | 6 个筛选项、10 列（在读与容量各自独立）、固定操作列 232px；行内 详情 / 编辑 / 指定班主任 / 更多▾ |
| `class-list_1920x1080.png` | 1920×1080 | 班级管理列表（宽屏） | 主内容区不无限拉伸，12 行样例与分页器同屏 |
| `class-list_grade_leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 数据范围收窄到本人负责年级（4 行，含教学班）；无「批量生成」，菜单里没有「删除」（矩阵只给 read / create / update） |
| `class-list_homeroom_1440x900.png` | 1440×900 | 角色 = 班主任 | 只看到本人担任班主任的 1 个班级；操作列只剩「详情」并显示「只读」（`DS-06`） |
| `class-list_subject_teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 只看到任教班级 3 行（含教学班）；顶部给出字段裁剪与禁止导出花名册的提示（`REQ-CLS-012` / `034`） |
| `class-list_school_leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 本校 11 行只读；工具栏只有「导出」 |
| `class-list_platform_ops-school-switch_1440x900.png` | 1440×900 | 平台运营切到云溪外国语学校 | 协助视图 · 只读提示条；工具栏只剩「导出（需授权）」；他校行只给「详情」，无任何写入口 |
| `class-list_super_admin_1440x900.png` | 1440×900 | 角色 = 超级管理员 | 全部按钮与行内操作可见；页头提示 `BR-ORG-014` 强制留痕 |
| `class-list_tenant_admin_1440x900.png` | 1440×900 | 角色 = 租户管理员 | 只读且无导出（矩阵显式禁止 `org.class.update`），行内只剩「详情」 |
| `class-list_more-menu_1440x900.png` | 1440×900 | 行内「更多 ▾」展开 | 低频操作下拉（编班 / 复制班级 / 批量迁学生 / 停用 / 删除），按可用条件过滤；右对齐并保持不出视口 |
| `class-list_dialog-disable_1440x900.png` | 1440×900 | 停用班级二次确认 | 写清对象与影响范围（历史花名册仍可查、有在读学生只能停用）；原因必填且写审计 |
| `class-list_dialog-delete_1440x900.png` | 1440×900 | 删除班级二次确认 | 只对在读人数为 0 的班级可用；原因必填；逻辑删除、不做物理删除（`REQ-CLS-046`） |
| `class-list_state-loading_1440x900.png` | 1440×900 | 状态 = 加载中 | 表格内骨架行，不用全屏遮罩 |
| `class-list_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 两种空态（筛选无结果 / 本校还没建班级）与各自主动作 |
| `class-list_state-error_1440x900.png` | 1440×900 | 状态 = 查询失败 | 错误说明 + 请求编号 + 错误码 + 重试 |
| `class-list_state-forbidden_1440x900.png` | 1440×900 | 状态 = 无权限 | 说明数据范围为空集且不降级为全量（`DS-DENY-03`） |
| `class-list_state-submitting_1440x900.png` | 1440×900 | 状态 = 提交中 | 写操作按钮置 loading 并禁用 |
| `class-list_state-partial_1440x900.png` | 1440×900 | 状态 = 部分失败 | 批量生成班级的部分冲突：6 条中 5 成功 1 冲突 |
| `class-list_state-queued_1440x900.png` | 1440×900 | 状态 = 排队中 | 任务编号、队列位置、并发配额（`BR-IMP-015`） |
| `class-list_verify-results.png` | 1500×1700 | 班级列表交互验证结果 | CL-01 ~ CL-39 共 39 条用例**全部通过**（含 GAP-046 的筛选级联 CL-38 / CL-39） |
| `verify-class-list.html` | — | 班级列表交互验证 harness | 同源 iframe + 真实事件派发跑 39 条断言（数据范围、筛选级联、行内入口、二次确认、七类状态），可重复执行 |
| `class-detail_1440x900.png` | 1440×900 | 班级详情（默认 · 花名册） | 批次 2-3b：页头第二行右对齐 6 个操作；基本信息 8 字段；花名册默认只显示在读成员 2 行 |
| `class-detail_1366x768.png` | 1366×768 | 班级详情（最小分辨率） | 无整页横向滚动、无元素重叠；页头按钮组自动换行 |
| `class-detail_1920x1080.png` | 1920×1080 | 班级详情（宽屏） | 主内容区不无限拉伸；页签与表格列宽分配 |
| `class-detail_tab-teachers_1440x900.png` | 1440×900 | 页签 = 任课教师 | 按学科分组只读展示（语文 邓丽娟 / 外语 苏睿跨校 / 7 个学科待设置） |
| `class-detail_tab-leader-history_1440x900.png` | 1440×900 | 页签 = 班主任任职历史 | 1 条生效中；写清学年切换重新指定与历史追加保留（REQ-CLS-050） |
| `class-detail_tab-changes_1440x900.png` | 1440×900 | 页签 = 变更记录 | 4 条时间线（含操作人与来源 IP）；「查看全部」跳操作日志 |
| `class-detail_role-subject_teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 监护人 / 联系电话整列隐藏；无导出花名册；行内只剩只读「详情」（REQ-CLS-012 / 034） |
| `class-detail_history-term_1440x900.png` | 1440×900 | 学年学期 = 2025-2026（已归档） | 只读提示条出现；编辑 / 指定班主任 / 编班 / 批量迁学生与行内写操作全部隐藏（REQ-CLS-032） |
| `class-detail_sample-empty_1440x900.png` | 1440×900 | 样本 = 高二 (2) 班（空班） | 花名册空态 + 在读 0 / 容量 45；页头跟随样本 |
| `class-detail_verify-results.png` | 1500×1450 | 班级详情交互验证结果 | CD-01 ~ CD-20 共 20 条用例**全部通过** |
| `verify-class-detail.html` | — | 班级详情交互验证 harness | 同源 iframe + 真实事件派发跑 20 条断言（页签 / 成员范围 / 字段裁剪 / 历史只读 / 七类状态） |

## 2. 截图里的关键事实（验收时可直接核对）

| 项 | 实测值 |
|---|---|
| 筛选项 | 9 个（学年学期 / 年级 / 班级 / 学籍状态 / 关键字 / 学校 / 性别 / 入学年份 / 证件号后四位；证件号后四位按角色显隐） |
| 表格列 | 12 列（复选列 + 10 个数据列 + 固定右侧操作列）；全国学籍号按 `REQ-STU-007` 属可选列，默认不渲染 |
| 行操作 | 3 个（编辑 / 异动 / 调班）；终态学生显示"终态 · 无异动入口" |
| 表格数据 | 14 行（`students` 10 条 + `students_edge_cases` 4 条），分页显示"共 137 条"（取自 `list_totals`） |
| 不含滚动可见行数 | 1366×768 → 7 行；1440×900 → 8 行；1920×1080 → 13 行 |
| 边界样本 | 超长姓名、间隔号、缺失（无全国学籍号 / 无手机号）、终态 2 条、待办中 1 条、他校学生 1 条（仅平台运营视图可见） |
| 角色形态 | 6 种（教务主任 / 班主任 / 年级主任 / 任课教师 / 校领导 / 平台运营） |
| 页面状态 | 正常 / 加载中 / 空数据 / 查询失败 / 无权限 / 提交中 / 部分失败 / 排队中 |
| 教师详情分区 | 6 个（基本信息 / 教育角色 / 任教关系 / 班主任任职 / 教师账号 / 变更记录） |
| 教师详情形态 | 2 种（跨校任教 苏睿；班主任 + 任课 邓丽娟，且无学校级教育角色） |
| 教师编辑可写角色 | 2 个（教务主任 / 租户管理员）；工号与入职日期仅租户管理员可改；所属学校永久只读 |
| 年级列表筛选项 | 4 个（学校 / 学段 / 入学年份 / 年级主任），与 `REQ-GRD-002` 逐项对应 |
| 年级列表列 | 8 列 + 固定操作列（年级名称 / 学段 / 入学年份 / 序号 / 班级数 / 在读学生数 / 年级主任 / 状态） |
| 年级列表行操作 | 4 个（编辑 / 指定年级主任 / 归档 / 删除），其中「删除」只在无班级无学生关系的空年级行出现 |
| 年级列表数据 | 14 行（`grades`），分页显示"共 18 条"（取自 `list_totals.grade_list_total`） |
| 年级列表可见行数 | 1366×768 → 7 行；1440×900 → 11 行；1920×1080 → 14 行 |
| 年级列表边界样本 | 空年级 1、已归档 1、未指定年级主任 5、毕业年级 2、他校 1、间隔号姓名 1 |
| 班级列表筛选项 | 6 个（学校 / 学年学期 / 年级 / 班级类型 / 班主任 / 校区）+ 关键字，与 `REQ-CLS-002` / `REQ-CLS-003` 对应 |
| 班级列表列 | 10 列（班级名称 / 年级 / 类型 / 班主任 / 在读 / 容量 / 校区 / 教室 / 状态）+ 固定右侧操作列 232px |
| 班级列表行内入口 | 高频 3 个（详情 / 编辑 / 指定班主任）+「更多 ▾」下拉（编班 / 复制班级 / 批量迁学生 / 停用 / 删除），下拉项按权限与可用条件过滤 |
| 班级列表数据 | 12 行（`classes` 7 条 + `class_edge_cases` 5 条），分页显示「共 34 条」（取自 `list_totals.class_list_total`） |
| 班级列表边界样本 | 空班 1（高二 (2) 班）、已停用 1（高三 (2) 班）、超容量 1（48 / 45）、超长班级名称 1、未指定班主任 2、教学班 1、毕业年级 2、他校 1 |
| 班级列表角色形态 | 8 种（教务主任 / 年级主任 / 班主任 / 任课教师 / 校领导 / 租户管理员 / 平台运营 / 超级管理员），差异落在可见行数与行内入口上 |
| 班级列表筛选级联 | 学校为第 1 级，校区 / 年级 / 班主任为第 2 级：本校 5 个年级 / 2 个校区 / 8 名班主任，他校 1 / 1 / 1（`GAP-046` 选项 A，规则见 `layout-spec.yaml` 的 `filter_cascade`） |
| 班级详情分区 | 4 个页签（花名册 / 任课教师 / 班主任任职历史 / 变更记录）+ 顶部基本信息卡 8 字段 |
| 班级详情花名册 | 在读成员 2 / 全部成员 3（含休学 1）；任课教师视角下监护人、联系电话整列隐藏；导出花名册对任课教师禁用 |
| 班级列表数据范围实测 | 教务主任 11 行、年级主任 4 行（`DS-05`）、班主任 1 行（`DS-06`）、任课教师 3 行（`DS-07`）、校领导 11 行只读（`DS-04`）、租户管理员 11 行只读（`DS-02`）、平台运营他校 1 行（`DS-01`） |

> 密度说明：`layout-spec.yaml` 原定"1440×900 可见 12 行"，批次 2-1 按三档实测修正为 8 行，
> 理由与计算过程写在 `prototypes/functional/v1/layout-spec.yaml` 的 `density.rules` 中。

## 3. 复现命令

### 3.1 截图

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$prof   = Join-Path $env:TEMP "codex-chrome-profile-shot"
$out    = "D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype"
$url    = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/student-list.html"

# 默认态
Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
  "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
  "--user-data-dir=$prof","--virtual-time-budget=3000",
  "--window-size=1440,900","--screenshot=$out\student-list_1440x900.png",$url)

# 深链接形态：角色 / 状态 / 浮层
# 例：班主任 + 编辑抽屉
$url2 = "$url#role=homeroom&panel=PAGE-STU-EDIT"
# 例：查询失败
$url3 = "$url#state=error"
# 例：平台运营切换到他校（只读协助视图）
$url4 = "$url?school=202#role=platform_ops"
```

深链接参数（原型自带，便于评审与截图）：

| 参数 | 取值 | 作用 |
|---|---|---|
| `role` | `academic_director` / `homeroom` / `grade_leader` / `subject_teacher` / `school_leader` / `platform_ops` | 切换角色形态 |
| `state` | `normal` / `loading` / `empty` / `error` / `forbidden` / `submitting` / `partial` / `queued` | 切换页面状态 |
| `panel` | `PAGE-STU-CREATE` / `PAGE-STU-EDIT` / `PAGE-TCH-DETAIL` / `PAGE-TCH-CREATE` / `PAGE-TCH-EDIT` / `DIALOG-TCH-LEAVE` / `DIALOG-TCH-DISCARD` / `DIALOG-GRD-DELETE` / `PAGE-GRD-DETAIL` / `PAGE-GRD-CREATE` / `PAGE-GRD-BATCH` / `PAGE-GRD-LEADER` / `PAGE-GRD-ARCHIVE` / `DIALOG-CLS-DISABLE` / `DIALOG-CLS-DELETE` | 直接打开抽屉、弹窗或同页确认片段 |
| `school` | `201` / `202` | 切换学校；只有平台运营可切换，202 为他校的协助视图（只读 + 留痕） |
| `sample` | `1007`（跨校任教）/ `1003`（班主任 + 任课） | 教师详情抽屉的两种形态，只对 `teacher-list.html` 有效 |
| `step` | `1` / `2` / `3` | 向导类浮层的步骤序号，例：`teacher-list.html#panel=PAGE-TCH-CREATE&step=3` |
| `enroll` | `2021` / `2024` / `2026` … | 年级列表的入学年份筛选，例：`grade-list.html#enroll=2021` 只看空年级 |
| `leader` | 教师姓名 / `__none` | 年级列表的年级主任筛选，`__none` 表示"未指定年级主任" |
| `stage` | `primary` / `junior` / `senior`，逗号分隔 | 年级列表的学段筛选，例：`grade-list.html#stage=junior,senior` |
| `term` | `202601` / `202602` / `202501` | 班级列表的学年学期筛选，例：`class-list.html#term=202501` 看已归档学年（样例集为空） |
| `grade` | `202601` / `202501` / `202401` / `202621` / `202423` / `202602` | 班级列表的年级筛选 |
| `type` | `administrative` / `teaching` | 班级列表的类型筛选，例：`class-list.html#type=teaching` 只看教学班 |
| `head` | 教师姓名 / `__none` | 班级列表的班主任筛选，`__none` 表示"未指定班主任" |
| `campus` | `2011` / `2012` / `2021` | 班级列表的校区筛选 |
| `keyword` | 班级名称或班主任姓名片段 | 班级列表的关键字检索 |
| `more` | 班级名称片段 | 直接展开某一行内「更多 ▾」下拉，例：`class-list.html#more=高二`（截图与评审用） |

### 3.2 交互验证 harness（批次 2-2b-2b 起）

harness 用一个同源 iframe 加载目标页，对真实 DOM 派发点击事件并逐条断言，
最后把结果渲染成一张清单；无头截图即结果留档。

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$prof   = Join-Path $env:TEMP "codex-chrome-verify"
$out    = "D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype"

Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
  "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
  "--allow-file-access-from-files",
  "--user-data-dir=$prof","--virtual-time-budget=30000",
  "--window-size=1500,1250","--screenshot=$out\grade-list_verify-results.png",
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/evidence/stage2-prototype/verify-grade-list.html")
```

结果解读：`grade-list_verify-results.png` 的最后一行为
`合计 38 / 38 条，通过 38 条，不通过 0 条 —— 全部通过`（D-060 后新增 GL-35 ~ GL-38 四条浮层入口用例）。
若某一行为红色"不通过"，该行会同时打印实际观测值，便于定位是页面缺陷还是断言写法问题。
`--allow-file-access-from-files` 是必需的：没有它，父页面无法读取 iframe 内文档。

详情浮层入口 harness（一次覆盖年级 / 教师 / 学生三个列表页）：

```powershell
Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
  "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
  "--allow-file-access-from-files",
  "--user-data-dir=$prof","--virtual-time-budget=20000",
  "--window-size=1500,1150","--screenshot=$out\detail-entry_verify-results.png",
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/evidence/stage2-prototype/verify-detail-entry.html")
```

结果解读：最后一行为 `合计 6 / 6 条，通过 6 条，不通过 0 条 —— 全部通过`。

批次 2-3a 的班级列表 harness（命令同上，换 URL 与输出文件名）：

```powershell
Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
  "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
  "--allow-file-access-from-files",
  "--user-data-dir=$prof","--virtual-time-budget=45000",
  "--window-size=1500,1600","--screenshot=$out\class-list_verify-results.png",
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/evidence/stage2-prototype/verify-class-list.html")
```

结果解读：最后一行为 `合计 39 / 39 条，通过 39 条，不通过 0 条 —— 全部通过`（CL-01 ~ CL-39）。
只取结论时可用 `--dump-dom` 代替截图：`& $chrome --headless=new ... --dump-dom <harness 路径> | Select-String 'class="sum'`。

批次 2-3a 的截图命令（18 张，含 3 档分辨率、8 个角色形态、7 类状态与 2 个二次确认）：

```powershell
$url = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/class-list.html"
$shots = @(
  @("class-list_1440x900.png",                          "1440,900", ""),
  @("class-list_1366x768.png",                          "1366,768", ""),
  @("class-list_1920x1080.png",                         "1920,1080",""),
  @("class-list_grade_leader_1440x900.png",             "1440,900", "#role=grade_leader"),
  @("class-list_homeroom_1440x900.png",                 "1440,900", "#role=homeroom"),
  @("class-list_subject_teacher_1440x900.png",          "1440,900", "#role=subject_teacher"),
  @("class-list_school_leader_1440x900.png",            "1440,900", "#role=school_leader"),
  @("class-list_super_admin_1440x900.png",              "1440,900", "#role=super_admin"),
  @("class-list_tenant_admin_1440x900.png",             "1440,900", "#role=tenant_admin"),
  @("class-list_platform_ops-school-switch_1440x900.png","1440,900","#role=platform_ops&school=202"),
  @("class-list_more-menu_1440x900.png",                "1440,900", "#more=高二"),
  @("class-list_dialog-disable_1440x900.png",           "1440,900", "#panel=DIALOG-CLS-DISABLE"),
  @("class-list_dialog-delete_1440x900.png",            "1440,900", "#panel=DIALOG-CLS-DELETE"),
  @("class-list_state-loading_1440x900.png",            "1440,900", "#state=loading"),
  @("class-list_state-empty_1440x900.png",              "1440,900", "#state=empty"),
  @("class-list_state-error_1440x900.png",              "1440,900", "#state=error"),
  @("class-list_state-forbidden_1440x900.png",          "1440,900", "#state=forbidden"),
  @("class-list_state-submitting_1440x900.png",         "1440,900", "#state=submitting"),
  @("class-list_state-partial_1440x900.png",            "1440,900", "#state=partial"),
  @("class-list_state-queued_1440x900.png",             "1440,900", "#state=queued")
)
foreach ($s in $shots) {
  Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
    "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
    "--allow-file-access-from-files",
    "--user-data-dir=$prof","--virtual-time-budget=20000",
    "--window-size=$($s[1])","--screenshot=$out\$($s[0])","$url$($s[2])")
}
```

## 4. 载体决策的核查命令（CR-007 / D-058 用）

用户要求"浮层载体与 apps/plus-ui 保持一致"，因此统计现有前端的 dialog / drawer 用量与宽度分布：

```powershell
$env:PYTHONIOENCODING='utf-8'
python -c "import os,re;from collections import Counter;c=Counter();[ c.update(['drawer']) for r in ['apps/plus-ui/src/views','apps/plus-ui/src/components'] for dp,dn,fn in os.walk(r) for f in fn if f.endswith('.vue') for t in re.findall('<el-drawer[^>]*>', open(os.path.join(dp,f),encoding='utf-8',errors='ignore').read()) ];print('el-drawer =',sum(c.values()))"
python -c "import os,re;from collections import Counter;q=chr(34);c=Counter();pat=re.compile('width='+q+'([^'+q+']+)'+q);[ c.update([pat.search(t).group(1) if pat.search(t) else '(未写)']) for r in ['apps/plus-ui/src/views','apps/plus-ui/src/components'] for dp,dn,fn in os.walk(r) for f in fn if f.endswith('.vue') for t in re.findall('<el-dialog[^>]*>', open(os.path.join(dp,f),encoding='utf-8',errors='ignore').read(), re.S) ];print('el-dialog =',sum(c.values()));print(c.most_common())"
```

实测结果：`el-dialog = 40`、`el-drawer = 0`；宽度分布 `500px ×13`、`800px ×4`、`700px ×3`、`600px ×2` 等。
据此确定：**表单与二次确认用弹窗**，抽屉只用于 PRD 显式要求的详情页；落地宽度映射 sm→500px、md→600px、lg→800px。

批次 2-2b-1 的截图命令（追加在批次 2-1 之后执行）：

```powershell
$url = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/teacher-list.html"
$shots = @(
  @{ u = "$url#panel=PAGE-TCH-DETAIL";                      s = "1440,900"; f = "teacher-detail_1440x900.png" },
  @{ u = "$url#panel=PAGE-TCH-DETAIL&sample=1003";           s = "1440,900"; f = "teacher-detail_sample-b_1440x900.png" },
  @{ u = "$url#role=platform_ops&panel=PAGE-TCH-DETAIL";     s = "1440,900"; f = "teacher-detail_role-platform-ops_1440x900.png" },
  @{ u = "$url#panel=PAGE-TCH-CREATE";                       s = "1440,900"; f = "teacher-create_step-1_1440x900.png" },
  @{ u = "$url#panel=PAGE-TCH-CREATE&step=3";                s = "1440,900"; f = "teacher-create_step-3_1440x900.png" },
  @{ u = "$url#panel=PAGE-TCH-EDIT";                         s = "1440,900"; f = "teacher-edit_1440x900.png" },
  @{ u = "$url#role=subject_teacher&panel=PAGE-TCH-EDIT";     s = "1440,900"; f = "teacher-edit_role-subject-teacher_1440x900.png" },
  @{ u = "$url#panel=DIALOG-TCH-DISCARD";                    s = "1440,900"; f = "teacher-dialog-discard_1440x900.png" }
)
foreach ($shot in $shots) {
  Start-Process $chrome -Wait -WindowStyle Hidden -ArgumentList @(
    "--headless=new","--disable-gpu","--no-first-run","--hide-scrollbars",
    "--user-data-dir=$prof","--virtual-time-budget=3000",
    "--window-size=$($shot.s)","--screenshot=$out\$($shot.f)",$shot.u)
}
```

## 4. 相关核查结果

```text
$ python tools/check_docs.py
=== 文档一致性核查 ===
通过：未发现问题
```

核查范围包含：`prototypes/**` 的 YAML / JSON 可解析、页面注册表编号唯一与批次计数一致、
跳转端点存在、占位词黑名单（`张三` / `李四` / `测试学校` / `xxx` 等）、HTML 中的
`data-page` 与 `data-action-id` 能回查到规范文件。

## 5. 批次 2-3a（班级管理列表样板页）的交付说明

### 5.1 本批产物

| 产物 | 路径 |
|---|---|
| 原型页面 | `prototypes/functional/v1/pages/class-list.html`（1 页 + 2 个二次确认浮层片段） |
| 页面规格 | `prototypes/functional/v1/page-specs/class-list.md` |
| 动作清单 | `prototypes/functional/v1/page-actions.yaml` 的 `class_list` 组（`ACT-CLS-001` ~ `ACT-CLS-016`） |
| 样例数据 | `prototypes/functional/v1/content-samples.json` 的 `classes`（补 `campus_id` / `campus_name`）、新增 `class_edge_cases`（5 条）、`list_totals.class_list_total` |
| 变更单 | `docs/00-governance/change-requests/CR-009.md`（载体修正 + 5 项页面登记） |
| 验证 harness | `evidence/stage2-prototype/verify-class-list.html`（39 条断言，含筛选级联 `CL-38` / `CL-39`） |

### 5.2 本批的工程决策（待确认，若不认可可回退）

| 事项 | 做法 | 理由 |
|---|---|---|
| 先出样板页 | 本批只交付 `PAGE-CLS-LIST` 一页，其余 15 页（详情 / 新建 / 批量生成 / 添加学生 / 调班 / 指定班主任 / 复制班级 / 批量迁学生 / 升班 6 页）按同一标准在 2-3b ~ 2-3e 铺开 | 用户要求"先生成一个页面，觉得好看了，用同样的标准生成其他剩下的一批页面"；也是 `AGENTS.md` 第 5 节的硬要求 |
| 行内操作超过 3 个的写法 | 高频 3 个（详情 / 编辑 / 指定班主任）直接渲染，其余进「更多 ▾」下拉；下拉挂到 `body` 做 fixed 定位并右对齐 | 表格 `.table-scroll` 是 `overflow-x:auto`，下拉放在单元格里会被裁切；`markup-contract.md` 已补 `el-dropdown` 映射与写法约定 |
| 班级表单的承载 | 编辑复用 `PAGE-CLS-CREATE` 的编辑态（与年级模块 `D-058` 的裁决一致），不另立编辑页 | 班级 PRD 6.1 只登记了"新建班级"；`GAP-038` 已就同一问题裁决过"新建与编辑共用同一表单" |
| 班级状态的取值 | 只使用**能从已冻结 PRD 文本推出**的两个值：`在读`（`REQ-CLS-004` / `007` 的"在读人数"）与 `已停用`（`REQ-CLS-043` / `044`） | PRD 7.1 的 `status` 没有给取值，`06-field-dictionary.yaml` 也没有 `class_status` 枚举；多造状态值属于补造业务规则，已登记 `GAP-044` 待你裁决 |
| 数量与边界的多样性来源 | 不用"多造状态"制造差异，改用：空班、已停用、超容量、超长名称、未指定班主任、教学班、毕业年级、他校数据 | 每一类都有 PRD 条款支撑（`BR-CLASS-005` / `006`、`BR-CLASS-001`、`BR-PROMO-005` 等） |
| 任课教师的读权限 | 页面按班级 PRD 2.1 与 `DS-07` 实现"只读、限任教班级、字段裁剪、禁止导出花名册"；但 `05-permission-matrix.yaml` 没有给 `subject_teacher` 授 `org.class:read` | 与 `GAP-035`（年级模块同类问题）一致的处理方式：先按上游 PRD 实现，权限缺口登记 `GAP-043` 待裁决 |
| 数据范围的实现位置 | 列表行按 `DS-04` / `05` / `06` / `07` / `02` / `01` 在原型里真实收窄（教务主任 11 行 / 年级主任 4 行 / 班主任 1 行 / 任课教师 3 行） | 班级是数据权限的权威来源，只画按钮显隐看不出越权风险；harness 的 CL-12 ~ CL-18 用实测行数留档 |
| 教室列的截断 | 教室列 104px，"本部教学楼 A203"这类值按 `layout-spec` 的长文本规则省略并给 `title` 悬浮全量 | 与年级 / 教师列表的既有做法一致；1366×768 下优先保证整页无横向滚动 |

### 5.3 本批暴露的缺口（待你裁决，见 `docs/00-governance/gap-register.yaml`）

| 缺口 | 内容 | 影响 |
|---|---|---|
| `GAP-043` | `subject_teacher` 对 `org.class` 的读权限未在权限矩阵定义 | 不补：任课教师打开班级列表只能看到"无权限"，与班级 PRD 2.1 的 `DS-07` 不一致 |
| `GAP-044` | `class_status` 字段与状态枚举未登记进字段字典；PRD 7.1 的 `status` 没有取值 | 不补：阶段 6 的 `el-tag` 映射与阶段 5 的建表缺少枚举依据；本轮原型只用"在读 / 已停用"两值 |
| `GAP-045` | `campus_id` 与 `classroom` 未登记进字段字典（PRD 7.1 已列出） | 不补：`data-field` 无法回溯到字段字典，check 清单第 5 条不满足 |

建议：走一次 `CR-010` 把 `GAP-043` ~ `GAP-045` 一起补完（与 `GAP-037` → `CR-005` 的做法一致），避免后续小批重复返工。

### 5.4 `GAP-046`（筛选级联与校区检索入口）的落地（D-064）

用户 2026-09-30 对「就按 A 改」答复「同意」，按选项 A 执行：

| 事项 | 落地 |
|---|---|
| 统一级联规则 | 写进 `prototypes/functional/v1/layout-spec.yaml` 的 `filter_cascade`（链路、四条规则、入口清单）与 `page-actions.yaml` 的 `state_rules` |
| 参考实现 | `pages/class-list.html`：学校为第 1 级，校区 / 年级 / 班主任为第 2 级；切学校后第 2 级只列该校选项、失效值清空；页头「共 N 个班级」与分页总数同步（`CL-38` 他校 2/2/2 项 + 共 6 个班级、`CL-39` 本校 6/3/10 项） |
| 校区检索入口 | `navigation.yaml` 的 `jump_map` 新增 `PAGE-SCH-LIST` 行内「校区数」→ `PAGE-SCH-CAMPUS`，复用批次 2-6 的校区管理页，不新增页面 |
| 待对齐 | 已交付的学生列表、年级列表、教师列表的筛选级联在各自下一批对齐（`GAP-047`，Codex 自有非阻塞项） |

### 5.5 批次 2-3b（班级详情与花名册）的交付说明

| 产物 | 路径 |
|---|---|
| 原型页面 | `prototypes/functional/v1/pages/class-detail.html`（独立页，`PAGE-CLS-DETAIL`） |
| 页面规格 | `prototypes/functional/v1/page-specs/class-detail.md` |
| 动作清单 | `page-actions.yaml` 新增 `class_detail` 组（`ACT-CLS-017` ~ `ACT-CLS-029`） |
| 外壳登记 | `prototype-shell.js` 的 `EXTRA_PAGES` 增加 `PAGE-CLS-DETAIL` → `pages/class-detail.html`；班级列表的行内「详情」由"只弹提示"改为真实跳转（`verify-class-list` 的 CL-36 已同步） |
| 样例口径修正 | 高一 (1) 班「在读」3 → **2**（陈思远休学，`BR-STU-012`），班级列表 / 年级详情样本 / `content-samples.json` 三处同步；受影响截图（班级列表 4 张、年级列表 3 张、年级详情 1 张）已重拍 |
| 验证 | `verify-class-detail.html` 20 / 20 通过；`verify-class-list.html` 39 / 39 通过；`python tools/check_docs.py` 通过 |

### 5.6 批次 2-3c（四个班级弹窗）的交付说明

| 产物 | 路径 |
|---|---|
| 原型片段 | `prototypes/functional/v1/pages/class-list.html` 内的 `PAGE-CLS-CREATE` / `PAGE-CLS-BATCH` / `PAGE-CLS-COPY` / `PAGE-CLS-LEADER`（均为 `el-dialog`，D-059） |
| 页面规格 | `page-specs/class-create.md` / `class-batch.md` / `class-copy.md` / `class-leader.md` |
| 动作清单 | `page-actions.yaml` 新增 `class_create` / `class_batch` / `class_copy` / `class_leader` 四组（`ACT-CLS-030` ~ `ACT-CLS-047`，共 18 条） |
| 跨页入口 | `pages/class-detail.html` 的「编辑班级 / 指定班主任 / 复制班级」改为跳转 `class-list.html#panel=PAGE-CLS-*&class=<班级名称>`，由列表页把弹窗打开在该班级上（写入入口仍只有班级模块一处，`DP-01`） |
| 表单布局 | 四个弹窗的表单改用两列 `form-grid`，1440×900 下 8 个字段与底部操作条一屏可见，不再需要滚动才能看到容量 / 校区 / 教室 |
| 截图 | `class-create_1440x900.png` / `class-create_edit_1440x900.png` / `class-batch_1440x900.png` / `class-copy_1440x900.png` / `class-leader_1440x900.png` / `class-leader_none_1440x900.png` / `class-dialogs_verify-results.png` |
| 验证 | `verify-class-dialogs.html` 36 / 36 通过（CDL-01 ~ CDL-36）；回归 `verify-class-list` 39 / 39、`verify-class-detail` 20 / 20、`verify-detail-entry` 6 / 6、`verify-carrier-change` 14 / 14、`verify-grade-list` 38 / 38；`python tools/check_docs.py` 通过 |

本批同时修掉一处上批遗留：

- `verify-class-list.html` 的 CL-34 原来只允许 `ACT-CLS-001 ~ 016`，本页新增四个弹窗后必然失败；
  已按「`common_actions` + `class_*` 各动作组」扩到 001 ~ 047，断言语义不变（仍是"页面用到的编号都已在 `page-actions.yaml` 登记"）。
- `verify-grade-list.html` 的 GL-15 断言在 D-064 之后过期（「班级数」已从"只弹提示"改成真实跨页跳转），
  且该 harness 的 `load` 监听没有启动守卫，跳转触发的第二次 load 会重启一条链并在 GL-04 抛异常，
  页面长期停在「运行中…」。已按当前形态修正断言并补守卫，回归 38 / 38，见 `GAP-050`。

待你拍板的两项（非阻塞，见 `gap-register.yaml`）：

- `GAP-048`：新建班级弹窗里「班级类型」只允许行政班（教学班创建在批次 2-7），是否需要改成弹窗内可切换。
- `GAP-049`：班级 PRD 第 8 节没有 `copyClass`，本批复制班级复用 `addClass`，是否需要补独立 operationId。

### 5.7 批次 2-3d（编班 / 批量迁学生 / 移出与调班）的交付说明

| 产物 | 路径 |
|---|---|
| 独立页 | `prototypes/functional/v1/pages/class-roster-add.html`（`PAGE-CLS-ROSTER-ADD`，左学生池 + 右待加入清单） |
| 独立页 | `prototypes/functional/v1/pages/class-move-students.html`（`PAGE-CLS-MOVE`，选学生 → 选目标班 → 影响预览 → 执行） |
| 片段（随班级详情） | `pages/class-detail.html` 的 `DIALOG-CLS-ROSTER-REMOVE`（移出确认）与 `PAGE-CLS-TRANSFER`（调班），花名册新增勾选列（表格 min-width 1080 → 1122） |
| 页面规格 | `page-specs/class-roster-add.md` / `class-move-students.md` / `class-transfer.md` |
| 动作清单 | `class_roster_add`（`ACT-CLS-053` ~ `058`）、`class_move_students`（`ACT-CLS-059` ~ `063`）、`class_detail` 补 `048` ~ `052` 与 `064` |
| 外壳登记 | `prototype-shell.js` 的 `EXTRA_PAGES` 增加 `PAGE-CLS-ROSTER-ADD` 与 `PAGE-CLS-MOVE` |
| 样例数据 | `content-samples.json` 新增 2 条「在读但无行政班」学生（2026000013 潘思彤 / 2026000014 蒋知远），`notes` 同步说明；总学生数 10 → 12 |
| 截图 | `class-roster-add_1440x900.png` / `class-roster-add_conflict_1440x900.png` / `class-move-students_1440x900.png` / `class-move-students_stopped_1440x900.png` / `class-detail_dialog-remove_1440x900.png` / `class-detail_dialog-transfer_1440x900.png` / `class-roster_verify-results.png` |
| 验证 | `verify-class-roster.html` 34 / 34 通过（RA-01 ~ RA-13、MV-01 ~ MV-10、TR-01 ~ TR-11，三个 iframe）；回归 class-list 39 / 39、class-detail 20 / 20、class-dialogs 36 / 36、grade-list 38 / 38、detail-entry 6 / 6、carrier-change 14 / 14；`python tools/check_docs.py` 通过 |

本批的关键点：

- 编班页允许勾选**已有行政班**的学生，提交时才整体拒绝并列出冲突清单——直接禁用冲突行就演示不了
  `REQ-CLS-029` 的「任一条冲突则整体拒绝」。
- 冲突行的「调班」按 class PRD 6.3 直接跳到班级详情页的调班弹窗，并带 `student` / `from` / `to` 三个参数预填。
- 批量迁学生只允许「学籍状态 = 在读」的成员迁移；休学 / 出国留学等保留关系成员显示但不可选（`BR-STU-012` 口径）。
- 待你拍板一项（非阻塞，见 `gap-register.yaml`）：`GAP-051`——「出国留学（保留学籍）」是否计入班级在读人数。
  样例里高二 (1) 班的 `student_count=2` 与「1 名在读 + 1 名出国留学」并存，推荐选项 A（与休学同口径不计入在读，并把该班样例数改成 1）。

下一批建议：2-3e 升班四步向导与结果页（`PAGE-PRM-LIST` / `CREATE` / `PREVIEW` / `VALIDATE` / `EXECUTE` / `RESULT`）
与预览里的「调整学生去向」弹窗（`PAGE-PRM-ADJUST`）。

### 5.8 `CR-011`：「在读名单」口径收敛与截图重拍

用户对 `GAP-051` 的「推荐 A」答复同意后，按 `CR-011` 落地：

- `BR-STU-012` 扩写为「**只有在读（`enrolled`）计入在读名单**」；休学、转入未报到、出国（保留学籍）都不计入；
  `06-field-dictionary.yaml` 的 `enrollment_status` 12 个枚举项各补 `counts_as_enrolled`（只有 `enrolled` 为 true）。
- 学生 PRD 的维度说明、`REQ-STU-037`、学籍状态表同步；学生验收新增 `AC-STU-408`。
- 样例修正：高二 (1) 班 `student_count` 2 → 1（该班只有孙悠然在读，徐昊然是出国保留学籍）。
- 受影响截图重拍 **21 张**：`class-list_1366x768` / `class-list_1440x900` / `class-list_1920x1080`、
  7 张角色形态、`class-list_more-menu`、2 张二次确认、7 张状态、`class-list_verify-results`。
  变化点是页头「在读 149 人」→「在读 148 人」与高二 (1) 班在读 2 → 1。

重拍命令与本文件第 3.1 节的 `class-list_*.png` 那一组完全一致（`#more=高二` 等深链接参数不变），
7 个 harness 重跑结果见 `interaction-verification.md` 第 11.2 节。
