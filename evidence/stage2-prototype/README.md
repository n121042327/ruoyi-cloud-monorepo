# 阶段 2 业务原型 · 验收证据（批次 2-1、2-2a、2-2b-1、2-2b-2、2-2b-2b、2-3a、2-3b、2-3c、2-3d、2-3e-s1、2-3e-s2）

本目录保存批次 2-1（学生管理列表 + 新增/编辑抽屉样板页）、2-2a（教师管理列表样板页）、
2-2b-1（教师详情 + 新增教师 + 编辑教师，含放弃确认弹窗）、2-2b-2（教育角色分配 + 任教关系设置独立页）、
2-2b-2b（年级管理列表样板页 + 删除确认弹窗）、2-3a（班级管理列表样板页 + 停用/删除确认）、
2-3b（班级详情与花名册）、2-3c（新建/编辑、批量生成、复制、指定班主任四个弹窗）、
2-3d（编班、批量迁学生、移出确认与调班）、2-3e-s1（升班任务列表样板 + 取消确认片段）、
2-3e-s2（升班向导第一步）的截图证据。
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

## 6. 批次 2-3e-s1（升班任务列表样板页）的交付说明

页面：`prototypes/functional/v1/pages/promotion-list.html`（`PAGE-PRM-LIST`，含同页确认片段 `DIALOG-PRM-CANCEL`）。
规格：`prototypes/functional/v1/page-specs/promotion-list.md`。harness：`verify-promotion-list.html`（36 / 36 通过）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `promotion-list_1440x900.png` | 1440×900 | 默认（教务主任，正常态） | 设计基准分辨率；10 列、8 个状态样例、状态驱动的行内动作 |
| `promotion-list_1366x768.png` | 1366×768 | 默认（教务主任） | 最小分辨率无整页横向滚动、无文字截断（列宽之和 = 表格 `min-width` = 1108） |
| `promotion-list_1920x1080.png` | 1920×1080 | 默认（教务主任） | 宽屏下主内容区不无限拉伸；全年份列表与口径说明可见 |
| `promotion-list_role-grade-leader.png` | 1440×900 | 角色 = 年级主任 | DS-05 只读：只剩本年级有学生的 6 行、计数按范围收窄（512 → 128）、无任何写入口（`CR-012`） |
| `promotion-list_role-school-leader.png` | 1440×900 | 角色 = 校领导 | 本校 11 行只读；工具栏只有导出（升班无审批环节） |
| `promotion-list_role-platform-ops.png` | 1440×900 | 角色 = 平台运营 | 只读 + 「导出（需授权）」入口；行内无写入口 |
| `promotion-list_role-homeroom-forbidden.png` | 1440×900 | 角色 = 班主任 | 无 `promotion.batch:read` → 无权限面板；写明不降级为全量（`DS-DENY-03`） |
| `promotion-list_filter-status-partial-failed.png` | 1440×900 | 状态筛选 = 部分失败 | 2 行样例；失败数为红色（保留失败原因 `title`） |
| `promotion-list_filter-draft.png` | 1440×900 | 状态筛选 = 草稿 | 草稿没有升班明细，三个计数列显示「—」（`REQ-PRM-011`） |
| `promotion-list_panel-cancel.png` | 1440×900 | 取消二次确认片段 | 原因必填 + 「保留已完成部分、不做整批回滚」的口径说明（已确认 4） |
| `promotion-list_state-partial.png` | 1440×900 | 状态 = 部分失败 | 两条任务的失败原因清单与重试口径（只重试失败项） |
| `promotion-list_state-error.png` | 1440×900 | 状态 = 查询失败 | 错误说明 + 请求编号 + 重试 |
| `promotion-list_state-empty.png` | 1440×900 | 状态 = 空数据 | 两档空态（筛选无结果 / 本校还没有任务） |
| `promotion-list_state-queued.png` | 1440×900 | 状态 = 排队中 | 任务编号、队列位置、异步执行依据（`REQ-PRM-027`） |

复现命令（本机 PowerShell，改文件名即可换场景；`#role=` / `#state=` / `#status=` / `#panel=` 四个深链接参数都可用）：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
& $chrome --headless=new --disable-gpu --no-first-run --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-chrome-prm" --window-size=1440,900 --virtual-time-budget=6000 `
  --screenshot="D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype\promotion-list_1440x900.png" `
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/promotion-list.html"
```

### 6.1 本批暴露的两个缺口已在 `CR-013` 关闭

1. 升班任务的 6 个字段（`source_term_id` / `target_term_id` / `promotion_task_status` /
   `total_count` / `success_count` / `failed_count`）已登记进 `06-field-dictionary.yaml`
   （外加 `edu_promotion_task` 的一条部分唯一说明，对应 `REQ-PRM-005`）。
2. 取消确认片段 `DIALOG-PRM-CANCEL` 已写进升班 PRD 6.1 / 6.3，并在 `navigation.yaml` 登记为
   `type: dialog`、`parent: PAGE-PRM-LIST` 的同页浮层片段，取消跳转也补齐（批次 2-3 声明页数 16 → 17）。

原型的 `data-field="status"` 同步改为 `promotion_task_status`；`data-field` 不参与渲染，
因此本目录的 14 张截图无需重拍，`verify-promotion-list.html` 重跑仍为 36 / 36。

### 6.2 批次 2-3e-s2（升班向导第一步）的交付说明

页面：`prototypes/functional/v1/pages/promotion-create.html`（`PAGE-PRM-CREATE`，骨架 `TPL-WIZARD`，独立页）。
规格：`prototypes/functional/v1/page-specs/promotion-create.md`。
动作组：`page-actions.yaml` 的 `promotion_create`（`ACT-PRM-011` ~ `ACT-PRM-019`）。
样例数据：`content-samples.json` 的 `promotion_create_samples`（学年学期选项 + 四档校验样例）。
harness：`verify-promotion-create.html`（`PC-01` ~ `PC-22`，22 / 22 通过）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `promotion-create_1440x900.png` | 1440×900 | 默认（教务主任，齐备样例） | 设计基准分辨率；步骤条 1 / 4、表单两列、齐备性清单、底部 sticky 操作条可见 |
| `promotion-create_1366x768.png` | 1366×768 | 默认（教务主任） | 最小分辨率无整页横向滚动、无文字截断；主按钮仍在视口内（sticky） |
| `promotion-create_1920x1080.png` | 1920×1080 | 默认（教务主任） | 宽屏下主内容区不无限拉伸，表单两列不被拉长 |
| `promotion-create_sample-missing_1440x900.png` | 1440×900 | 校验样例 = 缺年级 / 班级 | 高中部行标红「缺 3 项」（缺 高二、高三 + 高一 (4) 班）；阻塞条常驻，主按钮 `data-blocked=true` |
| `promotion-create_sample-conflict_1440x900.png` | 1440×900 | 校验样例 = 已有未结束任务 | 指向 `PRM-20260930-0012`（与升班列表同一编号，两页可互相印证）；给「查看已有任务」入口 |
| `promotion-create_sample-large_1440x900.png` | 1440×900 | 校验样例 = 超出 1 万阈值 | 在读 10,240 人只给耗时预估警告，不阻止创建（`REQ-PRM-010`） |
| `promotion-create_role-school-leader-forbidden_1440x900.png` | 1440×900 | 角色 = 校领导 | 无 `promotion.batch:create` → 无权限面板；表单与步骤内容整体不可见（`DS-DENY-03`） |
| `promotion-create_state-submitting_1440x900.png` | 1440×900 | 状态 = 提交中 | 主按钮置 loading 并禁用；说明「创建成功后任务状态为草稿」（`REQ-PRM-011`） |
| `promotion-create_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 源学年学期在读 0 人：写明只有在读计入，给「去学生管理核对学籍状态」与「返回任务列表」 |
| `promotion-create_state-error_1440x900.png` | 1440×900 | 状态 = 校验失败 | 请求编号 + 错误码 + 数据截止时间 + 重试；写明不会以跳过校验的方式继续 |
| `promotion-create_verify-results.png` | 1500×1250 | harness 结果清单 | `合计 22 / 22 条，通过 22 条，不通过 0 条 —— 全部通过` |
| `index_1440x1700.png` | 1440×2800（文件名沿用批次 2-1 的命名，未改名） | 原型入口页 | 已交付页面表 24 行（含 2-3b ~ 2-3e-s2）；批次 2-3 行 17 页 / 2-3e-s2 待验收；`GAP-055` 已关闭 |

复现命令（`#sample=` / `#role=` / `#state=` 三个深链接参数都可用）：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
& $chrome --headless=new --disable-gpu --no-first-run --hide-scrollbars --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-chrome-pc" --virtual-time-budget=9000 --window-size=1440,900 `
  --screenshot="D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype\promotion-create_1440x900.png" `
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/promotion-create.html"
```

本批的两条工程决策（若不认可可回退）：

| 事项 | 做法 | 理由 |
|---|---|---|
| 向导四步的载体 | 每步一个独立页（`promotion-create / preview / validate / execute / result`），不合成 `promotion-wizard.html` | `navigation.yaml` 给每步注册了独立 `route`，`jump_map` 也把「下一步」写成页面跳转；合成单文件会让 `data-page` 与 `jump_map` 两个契约同时失真 |
| 阻塞项存在时的主按钮 | 不置灰，改为 `data-blocked=true` + 常驻阻塞条 + 点击给原因 | 置灰后点击不再触发事件，用户拿不到「为什么不能建」的说明；与 2-3d「目标班级已停用点执行被拦」同口径（harness `PC-06` 断言点击后仍有提示且不发请求） |

本批暴露一个**阻塞**缺项：`GAP-055`（升班明细的 7 个字段与「调整方式」枚举未登记进 `06-field-dictionary.yaml`），
阻塞向导第 2 ~ 4 步的 `data-field` 回溯与阶段 5 的升班建表；建议按选项 A 立 `CR-014` 一次补全后再铺 2-3e-s3。

### 6.3 `CR-014`：升班明细的字段与枚举补齐（`GAP-055` 选项 A，`D-073`）

用户 2026-10-01 对 `GAP-055` 的「推荐 A」答复「同意」后执行：

| 变更 | 内容 |
|---|---|
| `06-field-dictionary.yaml` | 新增 7 个字段（`task_id` / `student_id` / `source_class_id` / `target_class_id` / `result_type` / `status` / `error_msg`）与 3 个枚举（`promotion_result_type` 升级/留级/转班/毕业/跳过、`promotion_item_status` 待处理/成功/失败/跳过、`promotion_validation_level` 通过/警告/错误）；字段总数 86 → 93 |
| `04-business-rules.md` | `BR-PROMO-006` 补「留级的去向是目标学年学期的同学段同名年级，仍不改写源学年记录」 |
| 升班 `PRD.md` | 6.1 补字段口径（调整弹窗写 `result_type` + `target_class_id`；任务 / 明细两层状态的区分）、6.3 补「指定留级」「指定转班」两行、7.1 修正 `result_type` 取值（补「转班」）；版本 `1.0.3-draft` → `1.0.4-draft` |
| 治理文件 | `gap-register.yaml` 关闭 `GAP-055`、`stage-inputs.yaml` 从 `stage3` / `stage5` 的 `blocking_gaps` 移出、`decisions.md` 追加 `D-073` 并关闭 `R-034`、`file-catalog.md` 登记 `CR-014` |

本批**不改动任何原型页面**，因此本目录的截图除入口页（`index_1440x1700.png`，`GAP-055` 一行由「待拍板」改为「已关闭」）
以外都没有变化；9 个 harness 全部重跑作为回归证据：

### 6.4 批次 2-3e-s3 / s4（升班向导第 2 ~ 4 步）的交付说明

| 页面 | 文件 | 规格 | 说明 |
|---|---|---|---|
| `PAGE-PRM-PREVIEW` | `pages/promotion-preview.html` | `page-specs/promotion-preview.md` | 双栏：左源班级（按 `REQ-PRM-013` 分组、可整体指定目标班级 `REQ-PRM-018`）+ 右逐学生明细 |
| `PAGE-PRM-ADJUST` | 同上（同页片段） | `page-specs/promotion-adjust.md` | 处理方式驱动必填项；留级去向＝同学段同名年级（`BR-PROMO-006`） |
| `PAGE-PRM-VALIDATE` | `pages/promotion-validate.html` | `page-specs/promotion-validate.md` | 通过 / 警告 / 错误三分类 + 下钻 + 标记跳过 + 错误时执行被拦 |
| `PAGE-PRM-EXECUTE` | `pages/promotion-execute.html` | `page-specs/promotion-execute.md` | 进度条 + 四类计数 + 处理时间线 + 排队中形态 |
| `PAGE-PRM-RESULT` | `pages/promotion-result.html` | `page-specs/promotion-result.md` | 四类清单切换 + 只重试失败项 / 继续执行剩余项 + 结果导出 |

harness：`verify-promotion-wizard.html`（`PV` / `ADJ` / `VD` / `EX` / `RS` / `ALL` 共 39 条，39 / 39 通过）。
动作组：`page-actions.yaml` 的 `promotion_preview`（020 ~ 026）、`promotion_adjust`（027 ~ 028）、`promotion_common`（043）、
`promotion_validate`（029 ~ 033）、`promotion_execute`（034 ~ 036）、`promotion_result`（037 ~ 041）。
样例数据：`content-samples.json` 的 `promotion_preview`。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `promotion-preview_1440x900.png` | 1440×900 | 默认（教务主任） | 双栏布局、7 行明细、结果类型统计、sticky 操作条 |
| `promotion-preview_1366x768.png` | 1366×768 | 最小分辨率 | 无整页横向滚动；左栏收窄后仍可读 |
| `promotion-preview_dialog-adjust_1440x900.png` | 1440×900 | 调整弹窗（深链接带入赵一诺） | 处理方式 / 目标班级 / 调整原因；弹窗内不放表格 |
| `promotion-preview_role-grade-leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 只剩本年级 5 行、无调整与批量入口（`DS-05` / `CR-012`） |
| `promotion-preview_state-forbidden_1440x900.png` | 1440×900 | 角色 = 班主任 | 无权限面板，内容区整体隐藏（`DS-DENY-03`） |
| `promotion-validate_1440x900.png` | 1440×900 | 默认 | 通过 5 / 警告 1 / 错误 1、错误阻塞条、下钻 chips |
| `promotion-validate_filter-error_1440x900.png` | 1440×900 | 下钻 = 错误 | 只剩 1 行错误项，行内给「标记跳过」（`REQ-PRM-025`） |
| `promotion-validate_state-error_1440x900.png` | 1440×900 | 状态 = 校验失败 | 请求编号 + 错误码 + 重试，写明不会跳过校验继续 |
| `promotion-execute_1440x900.png` | 1440×900 | 默认 | 进度 5 / 7 = 71%、成功 4 / 失败 1、处理时间线 |
| `promotion-execute_state-queued_1440x900.png` | 1440×900 | 状态 = 排队中 | 队列位置与并发上限口径（异步不阻塞） |
| `promotion-execute_processed-0_1440x900.png` | 1440×900 | 深链接 `#processed=0` | 进度归零形态（用于演示刚受理） |
| `promotion-result_1440x900.png` | 1440×900 | 默认（失败清单） | 四类计数、失败原因、只重试失败项 |
| `promotion-result_list-succeeded_1440x900.png` | 1440×900 | 清单 = 成功 6 | 四类清单切换（`REQ-PRM-034`） |
| `promotion-result_role-grade-leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 成功清单 4 行（高二 / 高三不可见）、重试按钮隐藏 |
| `promotion-result_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 未执行 / 已取消任务给「继续执行剩余项」 |
| `promotion-wizard_verify-results.png` | 1500×1700 | harness 结果清单 | `合计 39 / 39 条，通过 39 条，不通过 0 条 —— 全部通过` |

**截图踩坑（值得记住）**：把 URL 拼成 `.../pages//promotion-preview.html#...`（变量末尾多一个 `/`）时，
Chrome 无头模式会加载文件但**不执行 JS**：截图看起来"有内容"，实际是没有外壳的原始 HTML，
而且同一页所有 `#` 深链接变体的截图字节数完全相同。判据就是「不同深链接的截图字节数一模一样」，
发现后按单斜杠 URL 重拍了 15 张截图（本节的 16 张全部为单斜杠 URL 产物）。

复现命令：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$b = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages"
& $chrome --headless=new --disable-gpu --no-first-run --hide-scrollbars --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-wiz" --virtual-time-budget=9000 --window-size=1440,900 `
  --screenshot="D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype\promotion-preview_1440x900.png" `
  "$b/promotion-preview.html"
```

> 9 个 harness 的全部重跑结果见本文件下方（`CR-014` 一节）与 `interaction-verification.md` 第 14 节。

### 6.5 批次 2-4（导入向导 + 登录 + 异常页）的交付说明

| 页面 | 文件 | 规格 | 说明 |
|---|---|---|---|
| `PAGE-IMP-WIZARD` + `PAGE-IMP-TEMPLATE` / `PAGE-IMP-VALIDATE` / `PAGE-IMP-EXECUTE` | `pages/import-wizard.html` | `page-specs/import-wizard.md` | 四步向导：选模板（14 / 9 / 4 列清单）→ 上传并同步校验（5000 行 / 10 MB / 30 秒）→ 校验结果（120 / 118 / 2 + 失败明细可下载）→ 异步执行（任务号 + 进度 + 配额） |
| `PAGE-STU-IMPORT` | `pages/student-import.html` | `page-specs/student-import.md` | 学生管理「导入」快捷入口：14 列模板不含学号列（`BR-STU-019`），导入后默认不给行政班（`REQ-STU-062`），生成学号对照表 |
| `PAGE-TCH-IMPORT` | `pages/teacher-import.html` | `page-specs/teacher-import.md` | 教师管理「导入」快捷入口：9 列模板、工号租户内唯一（`BR-TEACHER-002`）、教育角色列可多值 |
| `PAGE-CLS-ROSTER-IMPORT` | `pages/class-import-roster.html` | `page-specs/class-import-roster.md` | 编班表导入：4 列（含目标班级列，`REQ-CLS-035`）、两阶段校验、导入幂等；冲突行给调班 / 移出提示 |
| `PAGE-LOGIN` | `pages/login.html` | `page-specs/login.md` | 登录：多校切换、学生 `s` + 学号（`BR-ACCOUNT-002`）、首登强制改密（`D-039`）、连续 5 次错误锁定 5 分钟 |
| `PAGE-403` / `PAGE-404` / `PAGE-500` | `pages/403.html` / `404.html` / `500.html` | `page-specs/error-pages.md` | 三个异常页各回答一个问题：为什么没权限（不降级为全量）、地址为什么不对（指向页面注册表）、有没有部分写入（异步任务不产生部分写入） |

**载本决策（`D-075` 第 4 条）**：登录页与三个异常页**不套管理外壳**（与真实系统一致，无左侧菜单 / 顶部导航 / 演示面板），
因此这四页没有角色与状态切换；页面自身的状态（校验失败 / 登录失败 / 锁定 / 请求编号）在页内直接可见，由 harness 断言覆盖。

harness：`verify-import-login.html`（8 个 iframe，`IMP` / `MS` / `MT` / `MC` / `LG` / `ER` 共 30 条断言，30 / 30 通过）。
动作组：`import_common`（`ACT-IMP-001` ~ `010`、`ACT-IO-001` / `002`）、`auth`（`ACT-AUTH-001` ~ `003`）、`error_page`（`ACT-ERR-001` ~ `005`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `import-wizard_1440x900.png` | 1440×900 | 默认（第 1 步） | 四步步骤条、模板版本与过期强提示、14 列清单 |
| `import-wizard_step-3_1440x900.png` | 1440×900 | 深链接 `#step=3` | 校验结果：120 / 118 / 2 + 失败明细 2 行 + 学号对照表说明 |
| `import-wizard_step-4_1440x900.png` | 1440×900 | 深链接 `#step=4` | 任务号 `TASK-20260928-000312`、进度 62%、结果文件 7 天 |
| `import-wizard_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 无 `data.import:import` → 无权限面板，内容区隐藏（`DS-DENY-03`） |
| `import-wizard_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 「文件里没有数据行」+ 重新下载模板 |
| `student-import_1440x900.png` | 1440×900 | 学生导入第 1 步 | 14 列模板与「不含学号列」口径 |
| `student-import_step-3_1440x900.png` | 1440×900 | 学生导入第 3 步 | 与导入向导一致的失败明细（证件号重复 / 年级不存在） |
| `teacher-import_1440x900.png` | 1440×900 | 教师导入第 1 步 | 9 列模板与工号唯一口径 |
| `class-import-roster_1440x900.png` | 1440×900 | 编班表导入第 1 步 | 4 列（含目标班级列）与导入幂等口径 |
| `login_1440x900.png` / `login_1366x768.png` | 1440×900 / 1366×768 | 登录页 | 无管理外壳；多校切换、登录名占位符（`s2026000001`）、首登改密与锁定口径 |
| `error-403_1440x900.png` / `error-404_1440x900.png` / `error-500_1440x900.png` | 1440×900 | 三个异常页 | 403 的 `DS-DENY-03` / `NFR-SEC-05`、404 的页面注册表口径、500 的请求编号与「不产生部分写入」 |
| `import-login_verify-results.png` | 1500×1500 | harness 结果清单 | `合计 30 / 30 条，通过 30 条，不通过 0 条 —— 全部通过` |

复现命令（深链接 `#step=` / `#role=` / `#state=` 可用）：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$b = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages"
& $chrome --headless=new --disable-gpu --no-first-run --hide-scrollbars --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-imp" --virtual-time-budget=9000 --window-size=1440,900 `
  --screenshot="D:\work\person_work\ruoyi-cloud-monorepo\evidence\stage2-prototype\import-wizard_step-3_1440x900.png" `
  "$b/import-wizard.html#step=3"
```

### 6.6 批次 2-5（学生模块剩余：详情 / 学籍异动 / 调班 / 跨校转学 / 异动历史）的交付说明

| 页面 | 文件 | 规格 | 说明 |
|---|---|---|---|
| `PAGE-STU-DETAIL` + `PAGE-STU-HISTORY` | `pages/student-list.html`（抽屉 + 区块） | `page-specs/student-detail.md` | 只读详情抽屉：基本信息（敏感字段默认掩码）/ 监护人（上限 3、编辑与解绑）/ 变更记录时间线（「查看全部」跳审计）；四个动作分别开三个弹窗与跨校转学 |
| `PAGE-STU-STATUS` | 同上（弹窗） | `page-specs/student-status.md` | 学籍异动：类型由当前状态决定、开除在义务教育阶段不可用、退学 / 开除 / 死亡需校级管理员审批、原因必填 |
| `PAGE-STU-TRANSFER` | 同上（弹窗） | `page-specs/student-transfer.md` | 调班：目标班级必填、已停用班级不可选、只改班级关系 |
| `PAGE-PRM-CHANGE` | 同上（弹窗） | `page-specs/promotion-change.md` | 升班口径的异动登记：**与 `PAGE-STU-STATUS` 同字段集、同接口**，额外强制阶段与审批约束（`DP-01`） |
| `PAGE-STU-CROSS-TRANSFER` / `PAGE-PRM-TRANSFER` | `pages/student-cross-transfer.html` / `pages/promotion-transfer.html` | `page-specs/cross-school-transfer.md` | 跨校转学两侧向导（转出校发起 → 转入校接收 → 报到）：只暴露必要字段、学号不变、接收前不计入在读、未报到前可撤销接收 |
| `PAGE-PRM-HISTORY` | `pages/promotion-history.html` | `page-specs/promotion-history.md` | 异动历史列表：筛选 + 追加式记录表 + 行内「查看学生」+「异动登记」入口唯一（跨页 `data-panel-hash`） |

harness：`verify-student-module.html`（4 个 iframe，`SM` / `CT` / `PT` / `PH` 共 28 条断言，28 / 28 通过）。
动作组：`page-actions.yaml` 的 `student_detail`（`ACT-STU-023` ~ `029`、`034`）、`student_status`（`030` / `031`）、
`student_transfer`（`032` / `033`）、`promotion_change`（`ACT-PRM-044` / `045`）、
`student_cross_transfer`（`ACT-STU-040` ~ `043`）、`promotion_transfer`（`ACT-PRM-050` ~ `054`）、`promotion_history`（`ACT-PRM-046` ~ `048`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `student-detail_1440x900.png` / `student-detail_1366x768.png` | 1440×900 / 1366×768 | 学生详情抽屉（王梓萱） | 三个分区、监护人上限 3、变更记录时间线；最小分辨率无横向滚动 |
| `student-detail_dialog-status_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-STU-STATUS` | 异动类型（含不可用的「开除」）、生效日期、原因必填、审批提示 |
| `student-detail_dialog-transfer_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-STU-TRANSFER` | 目标班级（含已停用不可选项）、只改班级关系口径 |
| `student-detail_dialog-promotion-change_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-PRM-CHANGE` | 与学籍异动同字段集 / 同接口的升班口径登记 |
| `student-detail_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 敏感字段按钮对任课教师不可见（`role-hidden`） |
| `student-cross-transfer_step-1 / step-3 / step-4_1440x900.png` | 1440×900 | 转出校向导三步 | 可发起条件、必要字段口径、提交与撤销申请 |
| `promotion-transfer_step-1 / step-4_1440x900.png` | 1440×900 | 转入校向导 | 待接收清单（含转出校）、接收 / 报到 / 撤销接收 |
| `promotion-history_1440x900.png` / `promotion-history_role-platform-ops_1440x900.png` / `promotion-history_state-empty_1440x900.png` | 1440×900 | 异动历史默认 / 平台运营只读 / 空态 | 8 列记录表、只读范围提示、两档空态 |
| `student-module_verify-results.png` | 1500×1400 | harness 结果清单 | `合计 28 / 28 条，通过 28 条，不通过 0 条 —— 全部通过` |

| harness | 结果 |
|---|---|
| `verify-promotion-create.html` | 22 / 22 通过 |
| `verify-promotion-list.html` | 36 / 36 通过 |
| `verify-class-list.html` | 39 / 39 通过 |
| `verify-class-detail.html` | 20 / 20 通过 |
| `verify-class-roster.html` | 34 / 34 通过 |
| `verify-class-dialogs.html` | 36 / 36 通过 |
| `verify-grade-list.html` | 38 / 38 通过 |
| `verify-detail-entry.html` | 6 / 6 通过 |
| `verify-carrier-change.html` | 14 / 14 通过 |
| `python tools/check_docs.py` | 通过：未发现问题 |

### 6.7 批次 2-6a（学校管理）的交付说明

| 页面 | 文件 | 说明 |
|---|---|---|
| `PAGE-SCH-LIST` | `pages/school-list.html` | 学校列表：9 列（名称 / 编码 / 学段 / 校区数 / 班级数 / 在读学生 / 状态 / 所属租户 / 操作）；校区数可点跳校区管理（`GAP-046` 选项 A） |
| `PAGE-SCH-DETAIL` | 同上（抽屉） | 基本信息 / 规模概览（`getSchoolSummary`）/ 学段配置 + 底部四个写入口 |
| `PAGE-SCH-CREATE` / `EDIT` | 同上（弹窗） | 新建 / 编辑学校；编码与所属租户只读（编码变更走 `updateSchoolCode`） |
| `PAGE-SCH-STAGE` | 同上（弹窗） | 学段配置：学段序号固定映射（小学 1–6、初中 / 高中 1–3，`RV-GRD-03`） |
| `PAGE-SCH-DISABLE` | 同上（弹窗） | 停用二次确认：影响范围 + 原因必填并写审计 |

本批同时执行 **`CR-015`**：把批次 2-6 的 9 条表单类浮层登记由 `drawer` 改为 `dialog`（沿用 `D-059` / `CR-008` 的口径），
学校模块的 4 个浮层已按弹窗实现；学年学期与学科模块的 5 条在各自小批交付时落地。

harness：`verify-school.html`（`SC-01` ~ `SC-16`，16 / 16 通过）。动作组：`page-actions.yaml` 的 `school_list`（`ACT-SCH-001` ~ `018`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `school-list_1440x900.png` / `school-list_1366x768.png` | 1440×900 / 1366×768 | 学校列表默认（教务主任只读） | 9 列 + 列宽之和 = min-width = 1140；最小分辨率无表格内横向滚动 |
| `school-detail_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SCH-DETAIL` | 三个分区 + 底部四动作（编辑 / 校区管理 / 学段配置 / 停用） |
| `school-dialog-create_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SCH-CREATE` | 弹窗载体（`CR-015`）、名称 / 租户必填、三个学段勾选 |
| `school-dialog-stage_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SCH-STAGE` | 学段序号固定映射说明 |
| `school-dialog-disable_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SCH-DISABLE` | 影响范围 + 原因必填 |
| `school-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 只保留「详情」入口，新建 / 开通初始化隐藏 |
| `school-list_state-empty_1440x900.png` | 1440×900 | 状态 = 空数据 | 「还没有任何学校」+ 新建入口 |
| `school_verify-results.png` | 1500×1200 | harness 结果清单 | `合计 16 / 16 条，通过 16 条，不通过 0 条 —— 全部通过` |

### 6.8 批次 2-6b（校区管理 / 开通初始化 / 学年学期）的交付说明

| 页面 | 文件 | 说明 |
|---|---|---|
| `PAGE-SCH-CAMPUS` | `pages/school-campus.html` | 校区列表（7 列）+ 页内新增 / 编辑表单 + 停用二次确认（原因必填）；页面写明校区不参与权限判定（`BR-ORG-009`） |
| `PAGE-SCH-INIT` | `pages/school-init.html` | 四步开通初始化：学校基本信息 → 学段与年级（已存在跳过）→ 学年学期（`RV-TERM-08`）→ 执行与结果；`initSchoolBaseline` 幂等 |
| `PAGE-TERM-LIST` + `PAGE-TERM-CREATE` / `SETCURRENT` / `ARCHIVE` | `pages/term-list.html` | 学年列表（7 列）+ 三个弹窗：新建学年（日期连续不重叠）、设为当前（不改历史数据）、归档（引用检查 + 原因必填；已归档行按钮变「撤销归档」） |
| `PAGE-TERM-TERMS` | `pages/term-terms.html` | 学期管理独立页：学期表格 + 页内新增 / 编辑表单 + 删除二次确认（`checkTermReference` 有引用时禁用删除） |

harness：`verify-org-config.html`（4 个 iframe，`CP` / `SI` / `TL` / `TT` + `ROLE-01` 共 23 条断言，23 / 23 通过）。
动作组：`school_campus`（`ACT-SCH-030` ~ `042`）、`term_list`（`ACT-TERM-001` ~ `010`）、`term_terms`（`ACT-TERM-013` ~ `021`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `school-campus_1440x900.png` / `school-campus_role-tenant-admin_1440x900.png` | 1440×900 | 校区列表（教务主任只读 / 租户管理员可写） | 7 列 + 行内编辑 / 停用入口的角色差异 |
| `school-init_step-1_1440x900.png` / `school-init_step-3_1440x900.png` | 1440×900 | 初始化向导第 1 / 3 步（深链接 `#step=`） | 学段与年级预览、学年日期连续不重叠、执行初始化入口 |
| `term-list_1440x900.png` | 1440×900 | 学年列表 | 3 行（未开始 / 进行中 / 已归档）+ 当前学年学期标记 |
| `term-list_dialog-setcurrent_1440x900.png` / `term-list_dialog-archive_1440x900.png` | 1440×900 | 设为当前 / 归档弹窗 | 影响范围口径、引用检查结果、原因必填 |
| `term-terms_1440x900.png` / `term-terms_role-school-leader_1440x900.png` | 1440×900 | 学期管理（租户管理员 / 校领导） | 学期表格、页内表单、删除二次确认与引用检查；角色写入口差异 |
| `org-config_verify-results.png` | 1500×1400 | harness 结果清单 | `合计 23 / 23 条，通过 23 条，不通过 0 条 —— 全部通过` |

### 6.9 批次 2-6c（学科与配置）的交付说明

| 页面 | 文件 | 说明 |
|---|---|---|
| `PAGE-SUB-LIST` | `pages/subject-list.html` | 学科列表 8 列（编码 / 名称 / 启用学段 / 参与 3+1+2 / 选科角色 / 排序号 / 状态 / 操作），9 门样例；页面写明固定集合与三类引用检查（任教关系 / 教学班 / 学生选科） |
| `PAGE-SUB-CREATE` / `EDIT` | 同上（弹窗） | 新建（名称 / 编码 / 学段多选 / 排序号，角色默认 `none`）与编辑（名称 / 学段可改、编码只读，变更走 `updateSubjectCode`） |
| `PAGE-SUB-STREAM` | 同上（弹窗） | 选科角色：首选只允许物理 / 历史、再选只允许化学 / 生物 / 思想政治 / 地理；变更前校验已有选科数据（`REQ-SUB-022`）并失效缓存（`NFR-CACHE-02`） |
| `PAGE-SUB-STAGE` | 同上（弹窗） | 学段启用多选（未开设学段置灰）+ 停用影响提示（`REQ-SUB-028`） |
| `PAGE-SUB-BATCH` | 同上（弹窗） | 按学段批量初始化标准 9 学科清单，已存在项跳过（幂等），确认走 `batchInitSubject` |

harness：`verify-subject.html`（`SB-01` ~ `SB-14`，14 / 14 通过）。动作组：`subject_list`（`ACT-SUB-001` ~ `018`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `subject-list_1440x900.png` / `subject-list_1366x768.png` | 1440×900 / 1366×768 | 学科列表（教务主任只读） | 8 列 + 列宽之和 = `min-width` = 1120；9 门样例；固定集合与引用检查口径 |
| `subject-dialog-create_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SUB-CREATE` | 弹窗载体（`CR-015`）、名称 / 编码必填、三个学段勾选 |
| `subject-dialog-stream_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SUB-STREAM` | 固定集合说明 + 只有首选 / 再选两个角色选项 |
| `subject-dialog-stage_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SUB-STAGE` | 学段多选 + 未开设学段置灰 + 影响提示 |
| `subject-dialog-batch_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-SUB-BATCH` | 标准 9 学科清单预览 + 「已存在，跳过」 |
| `subject-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 只读（与教务主任形态一致，新建 / 批量初始化隐藏） |
| `subject_verify-results.png` | 1500×1200 | harness 结果清单 | `合计 14 / 14 条，通过 14 条，不通过 0 条 —— 全部通过` |

### 6.10 批次 2-7a（选科配置 / 学生选科 / 选科清单）的交付说明

| 页面 | 文件 | 说明 |
|---|---|---|
| `PAGE-STR-CONFIG` | `pages/stream-config.html` | 选科配置：开放期与截止时间（`stream_open_from` / `stream_deadline`）+ 逾期变更开关（默认「需校级管理员审批」`BR-STREAM-005`）+ 两张固定规则卡片（首选物理 / 历史、再选 4 选 2，学校与教务主任都不能增减）+ 查看选科清单 / 查看未选科学生 |
| `PAGE-STR-STUDENT` | `pages/stream-selection.html` | 学生选科：首选卡片二选一 + 再选 4 选 2（即时计数、超出不允许再勾、不足提交被拦）+ 当前选科结果三张统计卡 + 恢复当前结果 / 查看选科历史 / 提交；页内写明「选科结果决定教学班归属，行政班不变」 |
| `PAGE-STR-LIST` + `PAGE-STR-HISTORY` | `pages/stream-list.html` | 选科清单 8 列 6 行（学号 / 姓名 / 年级班级 / 首选 / 再选 / 组合 / 状态 / 操作），列宽之和 = `min-width` = 1080；页内 `PAGE-STR-HISTORY` 追加式时间线（3 条，旧组合 → 新组合 + 原因），全页无删除入口 |
| `PAGE-STR-CHANGE` | 同上（弹窗） | 发起变更申请：新的首选（2 选项）+ 新的再选（4 复选）+ 变更原因必填；审批通过前保持原组合不变，同一学生只允许一条待审批申请（`BR-STREAM-007`） |

harness：`verify-stream.html`（3 个 iframe，`ST-01` ~ `ST-24` 含两个子项，共 26 条断言，26 / 26 通过）。
动作组：`stream_config`（`ACT-STR-001` ~ `003`）、`stream_student`（`ACT-STR-010` ~ `014`）、`stream_list`（`ACT-STR-020` ~ `025`）。
本批同时执行 `CR-016`：字段名改 `stream_open_from` / `stream_deadline`、9 条权限码改用 `stream.config` / `stream.selection` / `stream.change_request`、
`PAGE-STR-STAT` 由 block 改为独立页、选科配置可写角色改为教务主任 + 校领导（租户管理员无选科教学数据范围，`DS-02`）。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `stream-config_1440x900.png` | 1440×900 | 选科配置（教务主任，可写） | 固定规则卡片、开放期状态条、四个字段与保存入口 |
| `stream-config_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 无权限形态（`REQ-STR-053` / `DS-DENY-03`），内容整体替换而非堆在下方 |
| `stream-config_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 校领导可写选科配置（`stream.config [read, update]`） |
| `stream-config_state-empty_1440x900.png` | 1440×900 | 深链接 `#state=empty` | 空数据形态（未配置开放期时的口径） |
| `stream-selection_1440x900.png` | 1440×900 | 学生选科（学生视角） | 首选卡片 + 再选 4 选 2 + 计数 2 / 2 + 当前结果三张卡 + 提交 |
| `stream-selection_state-empty_1440x900.png` | 1440×900 | 深链接 `#state=empty` | 「选科未开放」空态（按年级开放的口径） |
| `stream-list_1440x900.png` | 1440×900 | 选科清单（教务主任） | 8 列 6 行、列宽 1080、读口径提示、选科历史时间线、行内「发起变更」 |
| `stream-list_role-homeroom_1440x900.png` | 1440×900 | 角色 = 班主任 | 4 行可代发起变更、2 行待审批显示「审批中」 |
| `stream-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 全部行「只读」、无行内入口（审批在变更审批待办页） |
| `stream-list_state-forbidden_1440x900.png` | 1440×900 | 角色 = 任课教师 + `#state=forbidden` | 无权限卡片（含 `REQ-STR-053` 与 `DS-02` 的排除依据） |
| `stream-list_state-empty_1440x900.png` | 1440×900 | 深链接 `#state=empty` | 空数据形态 + 「去选科配置」入口 |
| `stream-list_dialog-change_1440x900.png` | 1440×900 | 角色 = 班主任 + `#panel=PAGE-STR-CHANGE` | 变更申请弹窗（候选载体 `el-dialog`、变更口径、三类字段） |
| `stream_verify-results.png` | 1100×1500 | harness 结果清单 | `合计 26 / 26 条，通过 26 条，不通过 0 条 —— 全部通过` |

### 6.11 批次 2-7b（组合分布统计 / 变更审批待办 / 按组合生成教学班 / 教学班管理）的交付说明

| 页面 | 文件 | 说明 |
|---|---|---|
| `PAGE-STR-STAT` | `pages/stream-stat.html` | 组合分布统计：总览五张统计卡（高中在读 15 / 已提交 12 / 未提交 3 / 待审批 2 / 生效组合 6）+ 6 行纯 CSS 柱条（合计 12 人）+ 组合明细 6 列（行内「查看学生」按组合下钻）+ 各学科选择人数（首选 12 人 / 再选 24 人次）；待审批不计入分布（`BR-STREAM-006`）、统计与清单同口径（`REQ-STR-050`） |
| `PAGE-STR-APPROVE` + `DIALOG-STR-APPROVE` | `pages/stream-approve.html` | 变更审批待办 8 列 4 行（待审批 2 / 已通过 1 / 已驳回 1，默认筛「待审批」、按提交时间升序）+ 行内「审批」打开审批弹窗：原 / 新组合对比、审批意见（驳回必填）、同意并生效 / 驳回；教务主任不能自审（`REQ-STR-034`） |
| `PAGE-STR-GEN-CLASS` | `pages/stream-generate-class.html` | 按组合生成教学班四步向导（选方式与范围 → 生成预览 → 执行与进度 → 核对结果）：支持按完整组合与按单学科（`REQ-STR-058`）、幂等跳过已存在（`REQ-STR-057`）、写入由班级管理模块执行（`REQ-STR-056`）、核对人数与统计一致性并给差异清单（`REQ-STR-059` / `060`） |
| `PAGE-CLS-TEACHING` + `DRAWER-CLS-TEACHING` + `DIALOG-TCL-DISABLE` | `pages/teaching-class-list.html` | 教学班管理 8 列 5 行（名称 / 学年学期 / 年级 / 组合或学科 / 人数 / 任课教师 / 状态 / 操作）+ 详情抽屉（基本信息 + 跨行政班成员 + 未同步变更入口）+ 停用二次确认（原因必填、写审计）；两套独立关系（`BR-CLASS-001`）、教学班不设班主任（`REQ-CLS-039`）、创建入口唯一（`DP-01`） |

harness：`verify-stream-b.html`（4 个 iframe，`SB-01` ~ `SB-37` 共 37 条断言，37 / 37 通过）。
动作组：`stream_stat`（`ACT-STR-030` ~ `034`）、`stream_approve`（`ACT-STR-040` ~ `047`）、
`stream_generate_class`（`ACT-STR-050` ~ `058`）、`teaching_class_list`（`ACT-TCL-001` ~ `012`）。
本批同时执行 `CR-017`：班级 PRD 升 1.0.5-draft（补 2 个同页片段 + 3 个教学班 operationId + 创建入口唯一口径）、
选科 PRD 升 1.0.1-draft（补 `DIALOG-STR-APPROVE` 与统计页 / 变更申请 / 选科历史的载体口径）；
`prototype-shell.css` 补统计卡与柱条共享样式，`layout-spec.yaml` 新增 `charts` 段。

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `stream-stat_1440x900.png` | 1440×900 | 组合分布统计（教务主任） | 总览五卡、柱条 6 行、组合明细与学科人数 |
| `stream-stat_role-grade-leader_1440x900.png` | 1440×900 | 角色 = 年级主任 | 范围收敛为 DS-05（统计只含本年级，DS-DENY-08） |
| `stream-stat_state-empty_1440x900.png` | 1440×900 | 深链接 `#state=empty` | 空数据形态（不画空坐标系） |
| `stream-approve_1440x900.png` | 1440×900 | 变更审批待办（教务主任） | 8 列 4 行、待审批行「只读」（不能自审） |
| `stream-approve_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 2 行待审批出现「审批」入口 |
| `stream-approve_dialog-approve_1440x900.png` | 1440×900 | 角色 = 校领导 + `#panel=DIALOG-STR-APPROVE` | 审批弹窗：影响面、原 / 新组合对比、审批意见、三个动作 |
| `stream-generate-class_step-1_1440x900.png` | 1440×900 | 生成向导第 1 步 | 两种生成粒度、年级范围必填、命名规则只读 |
| `stream-generate-class_step-2_1440x900.png` | 1440×900 | 深链接 `#step=2` | 生成预览 5 行（4 新建 + 1 已存在跳过）与汇总卡 |
| `stream-generate-class_step-4_1440x900.png` | 1440×900 | 深链接 `#step=4` | 核对结果 4 行一致 + 三个出口 |
| `teaching-class-list_1440x900.png` | 1440×900 | 教学班管理（教务主任） | 8 列 5 行、行内详情 / 停用、已停用行只读 |
| `teaching-class-list_drawer-detail_1440x900.png` | 1440×900 | 深链接 `#panel=DRAWER-CLS-TEACHING` | 详情抽屉：基本信息 + 跨行政班成员 + 未同步变更入口 |
| `teaching-class-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 只读（无停用入口，详情仍可用） |
| `stream-b_verify-results.png` | 1100×1800 | harness 结果清单 | `合计 37 / 37 条，通过 37 条，不通过 0 条 —— 全部通过` |
