# 阶段 2 业务原型 · 验收证据（批次 2-1、2-2a、2-2b-1、2-2b-2）

本目录保存批次 2-1（学生管理列表 + 新增/编辑抽屉样板页）、2-2a（教师管理列表样板页）、
2-2b-1（教师详情 + 新增教师 + 编辑教师，含放弃确认弹窗）、2-2b-2（教育角色分配 + 任教关系设置独立页）的截图证据。
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
| `student-list_drawer-create_1440x900.png` | 1440×900 | 新增学生抽屉（第 1 步） | 三步步骤条、系统发号只读、字段级必填标记 |
| `student-list_drawer-edit-homeroom_1440x900.png` | 1440×900 | 编辑学生抽屉（角色 = 班主任） | 字段级可编辑性矩阵：仅监护人 / 联系方式可写，其余灰底只读 |
| `student-list_platform-ops-school-switch_1440x900.png` | 1440×900 | 角色 = 平台运营 + 学校切到云溪外国语学校 | 平台运营协助视图 · 只读：页头显示只读与留痕提示、工具栏只剩"导出（需授权）"、操作列显示"只读"。**共享授权不适用于学生数据**，跨校共享的对象是题库习题与试卷资源 |
| `index_1440x1700.png` | 1440×1700 | 原型入口页 | 页面清单、演示步骤、规范文件、批次进度、已知缺口 |
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
| `teacher-create_step-1_1440x900.png` | 1440×900 | 新增教师抽屉（第 1 步 基本信息） | 三步步骤条、工号格式与必填校验、所属学校受数据范围约束 |
| `teacher-create_step-3_1440x900.png` | 1440×900 | 新增教师抽屉（第 3 步 任职信息） | 步骤 1 / 2 已完成、底部按钮切换为"上一步 / 保存"；教育角色多选与"保存后会发生什么"说明 |
| `teacher-edit_1440x900.png` | 1440×900 | 编辑教师抽屉（角色 = 教务主任） | 字段级可编辑性：工号与入职日期灰底只读，其余可写；所属学校永久只读 |
| `teacher-edit_role-subject-teacher_1440x900.png` | 1440×900 | 编辑教师抽屉（角色 = 任课教师） | 全部字段灰底只读并逐字段给出只读说明；并发保护说明 |
| `teacher-edit_1920x1080.png` | 1920×1080 | 编辑教师抽屉（宽屏） | 双列表单在宽屏下不拉伸变形 |
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

> 密度说明：`layout-spec.yaml` 原定"1440×900 可见 12 行"，批次 2-1 按三档实测修正为 8 行，
> 理由与计算过程写在 `prototypes/functional/v1/layout-spec.yaml` 的 `density.rules` 中。

## 3. 复现命令

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
