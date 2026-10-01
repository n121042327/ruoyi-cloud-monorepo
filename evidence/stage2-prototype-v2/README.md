# 学生列表 v2 样板验收证据

- 日期：2026-10-01
- 批次：学生列表一个样板；既有浮层随原文件保留，未开展其他列表实现。
- 功能状态：筛选级联、学校角色上下文复位及首位学校筛选已验证（限静态原型）；生产实现未执行。
- 人工验收：待确认；全量追踪、组件映射及其他列表缺口保持未关闭。

## 产物

- `prototypes/functional/v2/pages/student-list.html`：业务样板。
- `prototypes/high-fidelity/v2/pages/student-list.html`：同步的高保真样板。
- 两层 `v2/assets/*shell.js`：样板使用的新外壳；v1 原样保留。
- `prototypes/high-fidelity/v2/component-mapping.yaml`：207 个可操作元素，含原文件已有浮层；共享外壳单独维护。
- `docs/00-governance/traceability.yaml`：REQ-STU-001～010，10 条均标部分覆盖，未填写猜测实现与测试路径。
- `docs/10-prd/modules/class/import-template-v2.md`：四列、组合定位、姓名核对与两类班级支持的新增补充版本。
- `docs/00-governance/filter-logic-audit.md`：45 页静态核查，不等于全页浏览器回归。

## 当前有效验证

| 检查 | 结果 | 证据 |
|---|---|---|
| 20 条级联 / 上下文 / 顺序断言，业务层 | 通过，20 / 20 | `2026-10-01_student-cascade-functional-v2.html` |
| 同一套断言，高保真层 | 通过，20 / 20 | `2026-10-01_student-cascade-hifi-v2.html` |
| 旧版对照 | 失败，5 / 20 通过 | `2026-10-01_student-cascade-functional-v1.html` |
| 既有载体变化回归 | 通过，14 条 | `2026-10-01_verify-carrier-change-result.html` |
| 既有详情入口回归 | 通过，6 条 | `2026-10-01_verify-detail-entry-result.html` |
| 既有学生模块回归 | 通过，28 条 | `2026-10-01_verify-student-module-result.html` |
| 元素映射与当前快照比对 | 通过，207 个元素 | `2026-10-01_student-elements.json` 与 `tools/build_student_sample_mapping.py --check` |
| 后端权限、实际接口请求、SQL 与生产实现 | 未执行 | 不以原型断言替代 |

当前浏览器回归共 88 条断言通过（20＋20＋14＋6＋28），不包括旧版失败对照。
三档截图为 `2026-10-01_student-list-cascade_1366x900.png`、`1440x900.png`、`1920x900.png`，展示平台运营学校 202 的样例。

历史文件：带 `before`、`after` 且未带版本号的 HTML 是修复过程中的旧证据；其中 17 / 19 条阶段结果已失效，
保留用于追溯，不用于本批最终通过结论。以本节列出的最终 v2 文件及 `2026-10-01_student-cascade.log` 为准。

## 人工验收方法

1. 打开高保真 v2 学生列表，确认学校在筛选区首位，其后为学年学期、年级、班级。
2. 切平台运营：选择学校 202，年级与班级仅含该校选项；切回 201 后选高一，保留高一 (3) 空班。
3. 先选高二班级，再改高一，失效班级清空；列表、页头与分页的样例行数一致。
4. 学校 202 下切教务主任，学校恢复 201 且禁用；深链接指定他校也不能遗留他校上下文。
5. 核对追踪样板的来源与接口证据，并抽查元素映射；不把设计路径当成实际代码。

## 已知缺口与下一批

学期样例没有学校归属（GAP-067），本批未编造学期收窄；筛选、排序参数及批量调班契约缺项（GAP-066）。
排序、列配置、分页和偏好保留是既有演示或设计，未被本批完整验证。非样板导航仍指向 v1。
教学班导入已确认组合不符阻止、重新生成保留合规关系，具体接口及关系存储未实现。班级、教师、年级的角色上下文问题及班级校区位置见核查报告。

下一批建议先班级列表一个页面：校区紧跟学校、学校角色上下文复位及对应回归。
本批结束暂停人工验收；未自动提交、推送、合并或进入生产阶段。
# 班级列表追加批次（CR-022）

用户要求加入统一字段布局约束并继续下一步。本批业务与高保真班级列表新增 v2，查询区分为教育信息、检索信息，操作独立；学校、校区相邻。学校角色切换及深链接恢复本校；同校角色切换保留编辑草稿。

- `verify-class-context.html`：新增 10 项，上下文回归业务与高保真各 10/10 通过。
- `verify-class-list.html`、`verify-class-dialogs.html`：复制既有验证器，仅班级列表入口改为 v2；列表 39/39、弹窗 36/36 通过。关联详情仍为 v1。
- 最终共 95 项通过；结果见 `2026-10-01_verify-class-context-functional.html`、`2026-10-01_verify-class-context-hifi.html`、`2026-10-01_verify-class-list-result.html`、`2026-10-01_verify-class-dialogs-result.html` 和 `2026-10-01_class-batch-verification.log`。
- `2026-10-01_class-context-before.html` 是 v1 基线（9 项中 5 项失败）；`2026-10-01_class-draft-before.html` 为审查发现草稿重置的修复前证据。早期 `class-context-functional.html` / `class-context-hifi.html` 是 9 项历史结果，已失效，不用于最终计数。
- `2026-10-01_class-list-layout_1440x900.png` 为最终布局截图，已查看。独立审查无阻断项。

人工验收待确认；未改变冻结 v1；表格与弹窗字段整体排序仍待后续批次按新约束处理，不声称全页全部字段通过。后端鉴权、学校学期选项归属未验证。下一批建议教师、年级两页，本批验收后再执行。未提交、推送或合并。
# 教师、年级追加批次（CR-023）

新增业务及高保真教师、年级列表 v2；仅调整查询区分组、字段顺序、独立操作区和学校上下文。表格列与浮层全部字段尚未重新分组，不声称全页新规范验收通过。

最终证据：`2026-10-01_teacher-grade-batch-verification.log`；新增验证器 `verify-teacher-grade-context.html` 的 13 项在两页两层均通过，结果文件为 `2026-10-01_verify-teacher-grade-context-<teacher|grade>-<functional|hifi>.html`。首次加载、参数更新、关闭旧浮层、同校保留浮层均覆盖。年级 38、载体 14、详情入口 6 项通过，本批共 110 项。

共享 v2 外壳调整参数顺序后，学生 88 项和班级 95 项同步回归通过，结果文件带 `shared-` 后缀，总执行 293 项。`teacher-context-before.html` / `grade-context-before.html` 为 v1 基线；`*-panel-before.html` / `*-panel-initial-before.html` 为审查修复前失败证据。早期 `*-context-functional.html` / `*-context-hifi.html` 为 8 项历史结果，已失效。两页最终 1440 × 900 截图已查看。

人工验收待确认；全量下拉范围 GAP-047、升班上下文 GAP-069、全量追踪及映射仍未完成，后端鉴权未执行。下一批建议升班列表与升班历史两页，验收后另行执行。未提交、推送或合并。
# 升班任务、异动历史追加批次（CR-024）

用户授权继续两页，并选择当前样例展示、暂禁历史学期。两页业务与高保真生成 v2，查询分组和操作独立；升班学校上下文统一复位，历史生效日期精确筛选。

最终验证：`verify-promotion-layout.html` 的任务 12 项、历史 9 项，在两层共 42 项通过；既有任务列表 36 项和历史关联模块 28 项通过，共 106 项。最终结果为 `2026-10-01_verify-promotion-layout-<list|history>-<functional|hifi>.html`、`2026-10-01_verify-promotion-list-result.html`、`2026-10-01_verify-promotion-history-module-result.html`，计数和核查见 `2026-10-01_promotion-batch-verification.log`。

`promotion-<list|history>-before.html` 为 v1 基线；`*-permission-before.html` 为权限状态组合修复前失败证据。早期 `promotion-<list|history>-<functional|hifi>.html` 为历史 10 / 7 项结果，已失效。两页 1440 × 900 截图已查看。

学期真实归属、完整下拉级联、表格与浮层全部字段分组、全量追踪与映射仍待补齐，后端权限未验证；人工验收待确认。下一批建议班级、教师表格与编辑表单字段分组、列序，验收后另行执行。未提交、推送或合并。
# 班级、教师表格与编辑表单批次（CR-025）

用户继续授权本批两页。班级表格教育字段先按校区、年级、班级排列，管理字段相邻；教师保留既有列序，补分组标识。表单按 CR-025 业务分节。按字段名读取替代数字列位置，保留权限和控件。

最终 171 项通过：新布局两层共 30 项，班级列表 39、弹窗 36、教师载体 14、详情 6，两页上下文两层共 46。结果见 `2026-10-01_table-form-batch-verification.log` 与带 `table-form-` 的最终结果文件；截图为两页 table / edit 的 1440 × 900 版本，均已查看。独立代码审查无阻断项。

新验证器为 `verify-table-form-layout.html`；复制班级验证器 `verify-class-layout-list.html`、`verify-class-layout-dialogs.html` 按字段名寻找单元格，CL-04 采用新列序。旧 `verify-class-list.html` 及其历史结果采用旧列序，已失效，不作为当前布局的回归证据。`*-table-form-before.html` 为修改前历史结果；`*-table-form-v1-baseline.html` 为冻结 v1 对照。教师旧 before 结果包含早期验证器选错表格 / 非完整样例的问题，不用于证明产品回归。

GAP-073 记录非完整教师样例的编辑绑定缺口，本批仅验证苏睿完整样例；列配置和导出仍是演示。其他浮层、全量追踪与映射、完整级联未完成，后端权限未验证。人工验收待确认；未提交、推送或合并。

# 教师编辑绑定批次（CR-026）

用户要求继续，优先修 GAP-073。教师列表行内「编辑」与「详情→编辑」改按当前行工号绑定目标；冻结来源缺失的字段清空并标注「样例未提供」；教育角色选中态按目标教师重置；非完整详情不再残留他人账号与关系清单。业务与高保真两个 `v2/pages/teacher-list.html` 同步修改，冻结 v1 保留。

最终验证：`verify-teacher-edit-binding.html` 业务与高保真各 14 项通过；教师载体 14、教师详情 6、教师上下文两层 26、教师表格与表单布局两层 14，均通过，本批合计 88 项。结果文件 `2026-10-01_teacher-edit-binding-<functional|hifi>.html`、`2026-10-01_teacher-binding-*-result.html`，计数与命令见 `2026-10-01_teacher-binding-verification.log`。修改前失败证据 `2026-10-01_teacher-edit-binding-before.html`（该文件为早期 10 项版本）。`2026-10-01_teacher-edit-binding-functional.html` / `-hifi.html` 亦为过程版本，最终以 14 项日志计数为准。

GAP-073 关闭；GAP-075（离职、调离行是否保留编辑、角色、任教入口）登记为待确认，本批不改该行为。全量追踪与映射、完整级联、其他浮层未完成，接口与数据权限未执行。人工验收待确认；未提交、推送或合并。

# 学生、年级表格与表单布局批次（CR-027）

用户要求继续，按 `docs/00-governance/page-field-layout.md` 整理学生、年级两页的表格列序与新建 / 编辑表单分区；两页原有按列号读取统一改为按字段名读取，避免调序后串列。业务与高保真四个 `v2/pages/*.html` 由 `tools/align_student_grade_layout.py` 生成，冻结 v1 保留。

最终 140 项通过：新布局两层各 17 项，学生模块 28 项，年级列表 38 项，学生级联两层 40 项。结果文件 `2026-10-01_cr027-final-<tag>.html`，计数与命令见 `2026-10-01_student-grade-layout.log`。

基线证据：`2026-10-01_student-grade-layout-before.html`（冻结 v1，17 项中 7 项失败）；过程证据 `2026-10-01_student-grade-layout-after-functional.html` / `-after-hifi.html`。`2026-10-01_grade-cr027-debug.html` 与 `2026-10-01_grade-page-cr027-debug.html` 是排查验证器停在「运行中」时的中间快照，仅作过程留档。`verify-student-layout-list.html`、`verify-grade-layout-list.html` 由既有验证器复制并改为按字段名读取，原 `verify-student-module.html`、`verify-grade-list.html` 保持不变。

截图：`2026-10-01_student-list-layout-cr027_1440x900.png`、`2026-10-01_student-create-layout-cr027_1440x900.png`、`2026-10-01_grade-list-layout-cr027_1440x900.png`、`2026-10-01_grade-create-layout-cr027_1440x900.png`，另附 v1 对照 `2026-10-01_student-list-layout-v1-compare.png`、`2026-10-01_grade-list-layout-v1-compare.png`，均已查看。列宽与格内换行与 v1 一致，本批未调整宽度。

GAP-076（学生、年级旧默认列序与新约束的差异）按 CR-027 处理并关闭；GAP-075 仍未获表态，保持 open。学生详情 / 学籍异动 / 调班浮层、年级详情 / 指定年级主任 / 归档弹窗、列配置持久化与导出未整理，接口与数据权限未执行。人工验收待确认；未提交、推送或合并。

# 学生学籍异动、调班与异动登记浮层批次（CR-028）

用户要求继续，按 `docs/00-governance/page-field-layout.md` 整理学生页三个未整理的浮层：学籍异动（`PAGE-STU-STATUS`）、调班（`PAGE-STU-TRANSFER`）、异动登记（`PAGE-PRM-CHANGE`）。三个弹窗改为语义分区，条件字段单独成组并紧跟触发组，提交与取消固定在页脚；字段集、组件、接口、权限与演示脚本逐字未改，业务与高保真两个 `v2/pages/student-list.html` 由 `tools/align_student_overlay_layout.py` 重建字段块，冻结 v1 保留。字段原文与冻结 v1 的同名块逐行一致（另对「原因」补 `span-2`、把校验汇总移出字段网格）。

最终 128 项通过：新验证器 `verify-student-overlay-layout.html` 业务与高保真各 13 项，学生模块既有回归 28 项，学生级联两层 40 项，学生与年级布局两层 34 项。结果文件 `2026-10-01_student-overlay-<v1|v2>-<functional|hifi>.html`、`2026-10-01_student-layout-list-cr028-result.html`、`2026-10-01_student-cascade-cr028-*-result.html`、`2026-10-01_student-grade-layout-cr028-*-result.html`，计数与命令见 `2026-10-01_student-overlay-layout.log`。

基线证据：`2026-10-01_student-overlay-v1-functional.html`（冻结 v1，13 项中 8 项失败：SO-01 / 02 / 03 / 04 / 07 / 08 / 09 / 10）。截图 `2026-10-01_student-status-layout-cr028_1440x900.png`、`2026-10-01_student-transfer-layout-cr028_1440x900.png`、`2026-10-01_student-prm-change-layout-cr028_1440x900.png` 及对应 `-hifi` 版本均已查看。

核对学生 PRD 6.1（第 586 行）、7.3（第 671 行）与班级模块同名弹窗后新增 GAP-077（调班是否补「生效日期」、两个 operationId 如何统一），保持现状并在弹窗内就地标注，未自行补字段。GAP-075 仍未获表态，保持 open。批量调班、学生详情、跨校转学向导、列配置与导出未整理，接口与数据权限未执行。人工验收待确认；未提交、推送或合并。

# 学生调班对齐与教师非在职行批次（CR-029 / CR-030，2026-10-02）

用户对两个挂起缺项分别批注选择 A：GAP-077 学生调班与班级模块对齐（补必填「生效日期」、原因字段名改 `remark`、统一到一个 operationId）；GAP-075 非在职行只保留查看与撤销离职登记。

CR-029：v2 学生调班弹窗班级关系组改为 目标班级 → 生效日期（默认 2026-09-01），调班说明组为调班原因（`data-field="remark"`），确认调班改走班级模块的 `transferClass`；学生 PRD 升 1.0.6（7.3 审计补生效日期、第 8 节删除 `transferStudentClass`），`06-api-catalog.md` / `openapi.yaml` / `modules/student/design.md` / `00-index.md` 由既有生成器重跑（173 → 172 个 operationId），生成器新增别名把冻结 v1 原型的历史写法映射到 `transferClass`。

CR-030：v2 教师列表按在职状态分叉渲染，非在职行只留「撤销离职登记」（`ACT-TCH-040` / `revokeTeacherLeave` / 新增弹窗 `DIALOG-TCH-REVOKE`），查看仍走行点击进详情；`page-actions.yaml` 同步 `ACT-TCH-006` / `007` / `008` 的 condition 并新增 `ACT-TCH-040`，`navigation.yaml` 登记片段并把批次 2-2 页面数改为 14，教师 PRD 升 1.0.4-draft。撤销后的账号与教育角色恢复口径上游未定义，登记 GAP-078 并就地标注。仅改动两个注册表文件，未改动任何 v1 页面。

最终 184 项通过：`verify-student-transfer-alignment.html` 两层各 8 项（v1 基线 8 项中 6 项失败）、`verify-student-overlay-layout-cr029.html` 两层各 13 项、学生模块 28 项、`verify-teacher-inactive-rows.html` 两层各 8 项（v1 基线 8 项中 7 项失败）、教师编辑绑定 14 项 × 2、教师载体 14 项 × 2、教师与年级上下文 13 项 × 2、表格与表单布局 8 项 × 2。计数、契约重生成与命令见 `2026-10-02_transfer-teacher-batch-verification.log`。

截图：`2026-10-02_student-transfer-effective-cr029_1440x900.png`（含 `-hifi`）、`2026-10-02_teacher-inactive-rows-cr030-full_1440x1900.png`（含 hifi 版）、`2026-10-02_teacher-revoke-dialog-cr030_1440x900.png`，均已查看。CR-028 版的 `verify-student-overlay-layout.html` 中 SO-08 / SO-09 断言已被本批取代，保留为历史基线。批量调班、跨校转学、教师导入、详情抽屉内分区不在本批范围；接口与数据权限未执行。人工验收待确认；未提交、推送或合并。

# 年级浮层字段分组批次（CR-031，2026-10-02）

用户要求继续，把年级管理列表页剩余四个浮层按 `page-field-layout.md` 整理：删除确认为 删除影响 / 删除说明，年级详情为 教育信息（所属学校 → 学段 → 入学年份 → 学段内序号 → 年级名称）/ 管理信息（状态），指定年级主任为 任职信息（学年学期 → 当前任职清单）/ 新增任职（教师 → 主要负责人 → 生效日期），归档确认为 归档影响 / 归档说明。详情卡片由「基本信息」改名「年级信息」；详情字段仍按 `data-detail-field` 取值，重排不影响填充。删除确认补上校验汇总（与同页其余三个弹窗一致）；为该页补一条例外，使 `.form-inline` 行内新增行不被分区宽度规则拆行。

最终 68 项通过：`verify-grade-overlay-layout.html` 两层各 12 项（v1 基线 12 项中 7 项失败）、年级列表回归 38 项、详情入口 6 项。计数与命令见 `2026-10-02_grade-overlay-batch-verification.log`。

截图：`2026-10-02_grade-detail-layout-cr031_1440x900.png`、`2026-10-02_grade-leader-layout-cr031_1440x900.png`、`2026-10-02_grade-archive-layout-cr031_1440x900.png`、`2026-10-02_grade-delete-layout-cr031_1440x900.png`，均已查看。

同时定位了 CR-030 起挂起的 `verify-teacher-grade-detail.html` 停在「运行中」的两处原因（按第一列匹配年级名称、iframe 就绪竞态），复制为 `verify-teacher-grade-detail-cr031.html` 修正后 RD-01 ~ RD-06 全部通过；原版保留为历史文件。本批未新增缺项，GAP-047 / 063 / 064 / 066 / 067 / 078 仍 open；未改动冻结 v1 页面。人工验收待确认；未提交、推送或合并。

# 学生详情抽屉字段分组批次（CR-033，2026-10-02）

用户要求继续，把学生列表页最后一个未整理浮层——学生详情抽屉——按 `page-field-layout.md` 整理：卡片标题由「基本信息」改为「学生信息」，卡片内分四组 基础信息（学号 → 全国学籍号 → 姓名 → 性别）/ 教育信息（入学年份 / 学段 → 年级 / 班级 → 学籍状态）/ 证件信息（证件号码）/ 联系方式（联系电话），顺序与 CR-027 的学生表单一致。证件号码与联系电话各自成组、改为 `span-2`，字段级「查看完整」（`ACT-STU-023`）随字段留在本组内；监护人（上限 3）与变更记录两张卡片保持原样。详情字段仍按 `sd-f-*` 的 id 填充，重排不影响取值。

最终 44 项通过：`verify-student-detail-layout.html` 两层各 8 项（v1 基线 8 项中 7 项失败）、学生模块回归 28 项。计数与命令见 `2026-10-02_student-detail-batch-verification.log`。截图 `2026-10-02_student-detail-layout-cr033_1440x1100.png`（含 hifi 版）已查看。

本批未新增缺项；GAP-047 / 063 / 064 / 066 / 067 / 078 仍 open；未改动冻结 v1 页面。人工验收待确认；未提交、推送或合并。

# 学校级联收窄与撤销离职口径批次（CR-034，2026-10-02）

用户对六条挂起缺项批注「按推荐走」。本批先落实两条：GAP-047（年级列表的 学段 / 入学年份 / 年级主任、教师列表的 任教年级 / 任教班级 按所选学校收窄，失效值清空、失效学段 chip 隐藏并取消选中；任教学科是平台级配置不收窄）与 GAP-078（教师 PRD 升 1.0.5-draft 新增 `REQ-TCH-071` / `AC-TCH-071`：撤销离职后账号自动重新启用、教育角色不自动恢复，撤销弹窗与 `ACT-TCH-040` 文案同步）。学年学期是唯一仍未按学校收窄的筛选项，`calendar` 样例没有 `school_id`，按 GAP-067 不编造。

最终 176 项通过：`verify-school-cascade.html` 两层各 8 项（v1 基线 8 项中 5 项失败）、`verify-teacher-inactive-rows-cr034.html` 两层各 8 项，加教师与年级上下文 13 × 2、年级浮层分组 12 × 2、学生级联 20 × 2、表格与表单布局 8 × 2、年级列表 38 项。计数见 `2026-10-02_school-cascade-batch-verification.log`；截图 `2026-10-02_grade-filter-school-202-cr034_1440x560.png`、`2026-10-02_teacher-filter-school-202-cr034_1440x560.png` 已查看。

其余四条已把「按推荐走」记入 `gap-register.yaml` 的 `answer` 并保持 open：GAP-063 / 064 的样板已交付，下一步是人工验收后逐模块与逐页扩展；GAP-066 需单独成批补学生列表契约并收敛批量调班 operationId；GAP-067 需用户提供两校学年学期样例。未改动冻结 v1 页面；未提交、推送或合并。

# 学生列表契约样板批次（CR-035，2026-10-02）

按 GAP-066 的选项 A，学生 PRD 新增「8.1 `listStudent` 查询参数」小节（12 个业务参数），由 `tools/gen_api_and_map.py` 解析后写入 OpenAPI——`listStudent` 从只有 `pageNum` / `pageSize` 变成 14 个参数；批量调班与单条调班统一收敛到班级模块的 `transferClass`（v2 学生页 `ACT-STU-020`、`page-actions.yaml`，以及生成器的历史字符串别名表）。新增 `tools/check_api_contract.py` 校验「v2 原型 data-api 命中 OpenAPI + PRD 查询参数与 OpenAPI 对齐」。

本批 68 项回归通过（学生模块 28 项、学生级联两层各 20 项），`check_api_contract.py` / `check_docs.py` / `check_mermaid.py` 均通过，冻结 v1 页面未改。计数见 `2026-10-02_student-contract-batch-verification.log`。

契约检查顺带发现 8 个此前未登记的孤儿引用（`exportClass` / `exportGrade` / `exportPromotionTask` / `uploadStudentPhoto` / `getStudentPhoto` / `viewStudentIdCard` / `saveTeacherEduRole` / `saveTeacherLeave`），登记为 **GAP-079**，本批只登记与提示，未擅自改指。未提交、推送或合并。
