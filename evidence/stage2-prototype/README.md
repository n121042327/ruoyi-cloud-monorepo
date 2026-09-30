# 阶段 2 业务原型 · 验收证据（批次 2-1、2-2a、2-2b-1、2-2b-2、2-2b-2b）

本目录保存批次 2-1（学生管理列表 + 新增/编辑抽屉样板页）、2-2a（教师管理列表样板页）、
2-2b-1（教师详情 + 新增教师 + 编辑教师，含放弃确认弹窗）、2-2b-2（教育角色分配 + 任教关系设置独立页）、
2-2b-2b（年级管理列表样板页 + 删除确认弹窗）的截图证据。
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
| `panel` | `PAGE-STU-CREATE` / `PAGE-STU-EDIT` / `PAGE-TCH-DETAIL` / `PAGE-TCH-CREATE` / `PAGE-TCH-EDIT` / `DIALOG-TCH-LEAVE` / `DIALOG-TCH-DISCARD` | 直接打开抽屉或弹窗 |
| `school` | `201` / `202` | 切换学校；只有平台运营可切换，202 为他校的协助视图（只读 + 留痕） |
| `sample` | `1007`（跨校任教）/ `1003`（班主任 + 任课） | 教师详情抽屉的两种形态，只对 `teacher-list.html` 有效 |
| `step` | `1` / `2` / `3` | 向导类浮层的步骤序号，例：`teacher-list.html#panel=PAGE-TCH-CREATE&step=3` |
| `enroll` | `2021` / `2024` / `2026` … | 年级列表的入学年份筛选，例：`grade-list.html#enroll=2021` 只看空年级 |
| `leader` | 教师姓名 / `__none` | 年级列表的年级主任筛选，`__none` 表示"未指定年级主任" |
| `stage` | `primary` / `junior` / `senior`，逗号分隔 | 年级列表的学段筛选，例：`grade-list.html#stage=junior,senior` |

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
