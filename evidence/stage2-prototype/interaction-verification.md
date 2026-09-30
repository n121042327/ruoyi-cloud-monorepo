# 阶段 2 原型 · 交互可点性验证（批次 2-2b-1 补做）

批次 2-2a 的验收反馈是"点行内『编辑』没反应"。定位结果是**原型外壳的事件委托缺陷**，不是单个页面的样式问题。
本文件记录根因、修复与验证证据。

## 1. 根因

`assets/prototype-shell.js` 的全局点击处理按以下顺序判断：

1. `[data-close-panel]` → 2. `[data-step-btn]` → 3. `[data-demo-toast]` → 4. `[data-api]` → 5. `[data-nav]`

第 4 步用的是 `node.closest('[data-api]')`。`markup-contract.md` 第 2 节要求**表格本身**带 `data-api`（如 `listTeacher`），
于是点击行、行内按钮、复选框时 `closest` 都会命中表格，被误判为"调接口"，`simulateApi` 直接 `return`，第 5 步的跳转永远走不到。

叠加第二个缺陷：页面脚本给行内按钮加了 `event.stopPropagation()`（本意是"点按钮不要打开行详情"），
而外壳的托管监听挂在 `document` 上，阻止冒泡后连外壳都收不到事件 —— 所以行内按钮**完全无响应**。

同类缺陷一并存在：表头全选复选框会触发一次假的"查询完成"提示；批量按钮未勾选时的提示依赖 `stopPropagation` 才不被误判。

## 2. 修复

| 位置 | 修复 |
|---|---|
| `assets/prototype-shell.js` | `data-api` 只在**动作元素**上才作为触发点（`BUTTON` / `A` / `data-role="action"`）；容器上的 `data-api` 仅作为接口标注 |
| `assets/prototype-shell.js` | 落在表单控件（`input` / `select` / `textarea` / `label`）上的点击不触发所在行或卡片的跳转 |
| `assets/prototype-shell.js` | 未提供页面片段的浮层改为提示"在本批未提供页面片段（原型按批交付）"，不再报"未找到页面片段"错误 |
| `assets/prototype-shell.js` | 校验汇总改为在整个浮层内查找（原先只在当前步骤内找，导致汇总永远不显示）；校验通过时自动收起汇总 |
| `assets/prototype-shell.js` | 步骤条初始化改按整篇文档查找 `[data-demo-panel]`，覆盖挂在 `#page-root` 之外的抽屉与弹窗 |
| `pages/teacher-list.html` | 行内按钮去掉 `stopPropagation`；行点击改用 `event.target.closest('[data-role="row-action"], input, select, button, label')` 守卫 |
| `pages/student-list.html` | 行内"编辑 / 异动 / 调班"三个按钮去掉 `stopPropagation`（批次 2-1 的同源缺陷） |

## 3. 验证方法与结果

验证方式：用一个临时页面把目标页放进同源 `<iframe>`，对真实 DOM 派发 `MouseEvent`，
再读回浮层 `open` 状态与字段值；通过 `chrome --headless=new --dump-dom` 输出结果。
命令模板与截图命令同源，见本目录 `README.md` 第 3 节。

### 3.1 教师管理列表（`pages/teacher-list.html`）

| 用例 | 操作 | 期望 | 实测 |
|---|---|---|---|
| TC-01 | 点第 1 行行内「编辑」 | 编辑抽屉打开 | `TC01_edit_open=true` |
| TC-02 | 编辑抽屉点「取消」 | 放弃确认弹窗打开 | `TC02_discard_open=true` |
| TC-03 | 放弃确认点「放弃并关闭」 | 两个浮层同时关闭 | `TC03_all_closed=true` |
| TC-04 | 点第 3 行姓名单元格 | 详情抽屉打开且头部切换为该教师 | `TC04_detail_by_row=true, name=邓丽娟` |
| TC-05 | 点工具条「新增」 | 新增抽屉打开 | `TC05_create_open=true` |
| TC-06 | 空表单点「下一步」 | 停在第 1 步、3 个字段标红、顶部汇总出现 | `TC06_validate_block=1, errors=3, summary=true` |
| TC-07 | 填工号 / 姓名 / 性别后点「下一步」 | 进入第 2 步，汇总自动收起 | `TC07_step2=2, summaryHidden=true` |
| TC-08 | 填手机号后点「下一步」 | 进入第 3 步，底部按钮变为「上一步 / 保存」 | `TC08_step3=3, saveVisible=true` |
| TC-09 | 点「保存」 | 按钮置 loading 并禁用（禁止重复提交） | `TC09_save_loading=true` |
| TC-10 | 点行内「角色」 | 给出批次 2-2b-2 的提示（页面本体未交付） | `TC10_role_toast=2` |
| TC-11 | 点行内「离职」 | 离职登记弹窗打开 | `TC11_leave_open=true` |
| TC-12 | 点行内复选框 | 不打开详情抽屉 | `TC12_checkbox_no_detail=true` |
| TC-13 | 点「刷新」 | 触发查询模拟 | `TC13_refresh_clicked=1` |

### 3.2 学生管理列表（`pages/student-list.html`）· 同源缺陷回归

| 用例 | 操作 | 期望 | 实测 |
|---|---|---|---|
| SC-01 | 点行内「编辑」 | 编辑抽屉打开 | `SC01_edit_open=true` |
| SC-02 | 点工具条「新增」 | 新增抽屉打开 | `SC02_create_open=true` |
| SC-03 | 点行内「异动」 | 未交付页面给出批次提示，不再静默无响应 | `SC03_undelivered_toast=1` |

## 4. 结论

- 行内按钮、行点击、复选框、步骤条、校验汇总五类交互在教师与学生两个列表页均可用。
- 该缺陷属于外壳级问题，后续批次的新页面不需要再各自加 `stopPropagation` 之类的补丁。
- 本文件与 `teacher-*.png` 截图一起构成本批的可点性与形态证据。

## 5. 批次 2-2b-2 的补充验证

新增两名角色与任教关系设置页后，追加以下用例（方式同上）：

### 5.1 教育角色分配弹窗（`PAGE-TCH-ROLE`，挂在 `pages/teacher-list.html`）

| 用例 | 操作 | 期望 | 实测 |
|---|---|---|---|
| ROLE-01 | 点行内「角色」 | 弹窗打开 | `ROLE01_open=true` |
| ROLE-02 | — | 头部跟随点击行 | `ROLE02_header=郑雅琴/YX2019001` |
| ROLE-03 | — | 年级主任任职表单默认收起 | `ROLE03_formHidden=true` |
| ROLE-04 | 点「新增年级主任任职」 | 表单展开 | `ROLE04_formShown=true` |
| ROLE-05 | 点「收起」 | 表单收起 | `ROLE05_formHiddenAgain=true` |
| ROLE-06 | 点角色行内「解除」 | 给出影响说明提示 | `ROLE06_unbind_toast=2` |
| ROLE-07 | 点「保存」 | 按钮置 loading 并禁用 | `ROLE07_save_loading=true` |

### 5.2 任教关系设置（`pages/teacher-assign.html`，独立页）

| 用例 | 操作 | 期望 | 实测 |
|---|---|---|---|
| AS-01 | 打开页面 | 默认班级视角 | `AS01_classVisible=true`，主体 `高一 (1) 班` |
| AS-02 | 点「教师视角」 | 左栏切换为 11 名教师 | `AS03_teacherView=true, count=11 名教师` |
| AS-03 | 点左栏某位教师 | 右侧主体切换 | `AS04_afterPick=邓丽娟/任课教师` |
| AS-04 | — | 失效行无写入口、终态行给出只读说明 | `AS05_rowActions=5, readonlyRows=4` |
| AS-05 | 表单未填学科直接保存 | 顶部汇总出现且不发请求 | `AS06_summaryShown=true` |
| AS-06 | 补齐学科与教师后保存 | 汇总收起、按钮置 loading | `AS10_summaryHidden=true, AS11_save_loading=true` |
| AS-07 | 点「复制上一学年」 | 冲突预览弹窗打开，可确认 | `AS08_copy_open=true` |
| AS-08 | 教师列表点行内「任教」 | 跳转到独立页并渲染选择器 | `phase1_page=PAGE-TCH-LIST → phase2_page=PAGE-TCH-ASSIGN, phase2_picker=ok` |

> 行内按钮的 `data-overlay` 语义补充：`编辑=drawer`、`角色=dialog`、`任教=无（跳独立页）`、`离职=dialog`。
> 外壳按 `data-overlay` 是否存在决定"打开同页浮层"还是"跳独立页"，`pages/teacher-list.html` 的 `link()` helper 已按此约定收口。

## 6. 批次 2-2b-2b 的验证（年级管理列表样板页）

验证方式与前两批不同：改用**可复现的 harness 页面**而不是一次性临时文件，
harness 把 iframe 里的每一步断言渲染成一张结果清单，再用无头 Chrome 截图留档，
结果可被任何人重跑复核。

- harness：`evidence/stage2-prototype/verify-grade-list.html`
- 复现命令：`evidence/stage2-prototype/README.md` 第 3.2 节
- 留档截图：`evidence/stage2-prototype/grade-list_verify-results.png`

### 6.1 用例与结果（22 条，全部通过）

| 用例 | 操作 / 断言 | 期望 | 实测 |
|---|---|---|---|
| GL-01 | 默认（教务主任）读取当前角色可见的工具条按钮 | 新增 / 按学段批量生成 / 导出；不含「导出（需授权）」 | `新增 \| 按学段批量生成 \| 导出` |
| GL-02 | 统计默认可见行与已归档行 | 本校 13 行，其中已归档 1 行 | `可见 13 行 / 已归档 1 行` |
| GL-03 | 统计全表 `ACT-GRD-007` 的位置 | 只在空年级（2021 级 小学六年级）出现 | `共 1 处` |
| GL-04 | 读已归档行的操作列 | 无任何写操作按钮 | `（无按钮）` |
| GL-05 | 切「年级主任」 | 无新增 / 批量生成，可导出，操作列无可见写操作 | `导出 / 可见行内操作 0 个` |
| GL-06 | 切「班主任」 | 数据范围提示为 `DS-06` 且只读 | `数据范围：本班所属年级（DS-06）· 只读年级摘要` |
| GL-07 | 切「校领导」 | 只读并审批（`GAP-034` 裁决 B），无写入口 | `导出` |
| GL-08 | 切「超级管理员」 | 全部按钮可见 + 专用提示 + 删除入口仍有 1 处 | `新增 \| 按学段批量生成 \| 导出 \| 导出（需授权）` |
| GL-09 | 平台运营把学校切到云溪外国语学校后搜索 | 出现协助视图提示，只剩他校 1 行，下拉可用 | `可见 1 行 / 学校下拉可用 true` |
| GL-10 | 切回非多校角色后点重置 | 学校下拉禁用且回到本校，恢复 13 行 | `禁用=true 值=201` |
| GL-11 | 切回本校后搜索 | 恢复 13 行 | `可见 13 行` |
| GL-12 | 点学段筛选「初中」 | 只剩 4 行 | `可见 4 行` |
| GL-13 | 点「全部学段」 | 恢复 13 行 | `可见 13 行` |
| GL-14 | 点「新增」 | 提示中文页面名与批次，不是死按钮 | 「新建 / 编辑年级」在本批未提供页面片段 |
| GL-15 | 点高一行的「班级数」 | 提示在批次 2-3 交付并带入年级筛选 | 「班级管理」在批次 2-3 交付（PAGE-CLS-LIST） |
| GL-16 | 点高一数据行 | 提示打开年级详情 | 「年级详情」在批次 2-2 交付（PAGE-GRD-DETAIL） |
| GL-17 | 点空年级行的「删除」 | 二次确认弹窗打开且标题写入目标年级 | `标题目标：2021 级 小学六年级` |
| GL-18 | 不填原因点「确认删除」 | 字段级错误 + 弹窗不关闭 | 已给出字段级错误 |
| GL-19 | 填写原因后点「确认删除」 | 按钮置 loading 并禁用 | 按钮状态：处理中 |
| GL-20 | 等待模拟提交返回 | 给出反馈并关闭弹窗 | 已删除该年级（逻辑删除）……可以重新创建。 |
| GL-21 | 点「隐藏搜索」 | 折叠筛选区并把按钮改为「显示搜索」 | 显示搜索 |
| GL-22 | 用状态切换器切到空态 | 六类状态真实存在、可切换 | 空态面板可见 |

### 6.2 harness 自身修过的两处误判（记录以免后人重复踩）

| 用例 | 第一次为什么判失败 | 修正 |
|---|---|---|
| GL-01 / GL-05 / GL-06 / GL-07 | harness 用 `textContent` 统计工具条按钮，把被 `role-hidden` 隐藏的按钮也算进去了 | 统计前先过滤 `.role-hidden` |
| GL-05 | 用操作列的 `textContent` 判断"没有写操作"，行内按钮虽被隐藏但文本仍在 DOM 里 | 改成统计"未被 `role-hidden` 的行内按钮"个数 |

> 这两处都是**断言写法**的问题，不是页面缺陷；页面在四种只读角色下的渲染结论在 GL-05 ~ GL-07、GL-10 中已由实际点击与可见性共同验证。

## 7. 批次 2-2b-2b 收口：深链接缺陷与其余 5 个浮层

### 7.1 用户反馈的"没有达到预期"：根因是深链接只在整页加载时生效

用户在已打开的年级列表地址栏里补 `#enroll=2021`，但页面**不会重新加载**，
而原实现只在 `DOMContentLoaded` 里读一次参数，于是筛选没有生效，
也就看不到唯一带「删除」入口的空年级行——整条操作路径因此失败。

复现与修复验证都留在 `evidence/stage2-prototype/_debug-grade-delete.html`（截图 `_debug-grade-delete.png`）：

| 步骤 | 修复前 | 修复后 |
|---|---|---|
| 初始加载 | 可见 13 行 | 可见 13 行 |
| 在已打开页面上把 hash 改成 `#enroll=2021` | **可见 13 行、下拉为空（不生效）** | 可见 1 行、下拉 `2021`（立即生效） |
| 整页重新加载同一条 URL | 可见 1 行（作为对照，说明参数本身没错） | 可见 1 行 |
| 空年级行的「删除」入口 | 找到（`ACT-GRD-007` 1 处） | 找到 |
| 不填原因点「确认删除」 | 字段级错误 + 弹窗不关 + 未发请求 | 同左 |

修复方式：外壳把参数解析抽成 `parseParams()` / `applyParams()`，新增 `hashchange` 监听，
并把参数通过 `prototype:params` 事件派发给页面；年级列表、学生列表、教师列表三处同步改为监听该事件。
外壳同时补了 `PENDING_PAGES`，让"未交付页面"的提示显示中文名而不是 `PAGE-XXX` 编号。

一并做的可用性修正：年级列表行序由「高中 → 初中 → 小学」改为「小学 → 初中 → 高中」，
空年级从第 13 行移到第 6 行，1440×900 首屏可见，验收时不必先改 URL。

### 7.2 其余 5 个浮层的用例（GL-23 ~ GL-33，全部通过）

| 用例 | 操作 / 断言 | 期望 | 实测 |
|---|---|---|---|
| GL-23 | 打开年级详情（样本 A） | 3 个行政班 + 基本信息为首行年级 | `班级行 3 / 2026 级 高一` |
| GL-24 | 切「样本 B · 空年级」 | 班级 / 学生统计 / 年级主任三张子表都走空态 | `班级 1 / 统计 1 / 主任 1`（各 1 行为空态说明） |
| GL-25 | 点其他数据行 | 基本信息跟随该行 | `2025 级 高二 / 2025` |
| GL-26 | 点「新增」 | 名称按规则生成、高中序号 3 项、新建态可编辑 | `2027 级 高一 / 序号 3 项 / 新建` |
| GL-27 | 学段改成小学 | 序号选项变 6 项、名称同步重算 | `2027 级 小学一年级 / 序号 6 项` |
| GL-28 | 输入已存在的名称后点保存 | 前端拦重名、弹窗不关、不发请求 | 已提示唯一性冲突 |
| GL-29 | 行内「编辑」 | 复用同一弹窗的编辑态，学段与序号只读 | `编辑 / 学段禁用 true / 序号禁用 true` |
| GL-30 | 批量生成预览 | 高中 3 项、小学 6 项、标出冲突 | `高中 3 / 小学 6 / 冲突 1` |
| GL-31 | 指定年级主任添加第三人 | 清单 1 → 2 条 | `1 → 2` |
| GL-32 | 重复添加同一人 | 被拒绝且清单不变 | 提示"已有一条任职" |
| GL-33 | 归档确认不填原因 | 字段级错误 + 弹窗不关 | `2024 级 初中三年级 / 字段错误 true` |
| GL-34 | 在已打开页面上改 hash | 深链接立即生效 | `可见 1 行 / 下拉 2021` |

harness 总结果：`合计 38 / 38 条，通过 38 条，不通过 0 条 —— 全部通过`（D-060 后新增 GL-35 ~ GL-38，详见第 9 节）。
其中 GL-14 的断言在本次一并更新：年级表单交付后，「新增」不再给"本批未提供页面片段"的提示，
而是直接打开弹窗，因此该用例改为断言弹窗打开与新建态。

## 8. CR-008 载体变更后的回归（批次 2-1 / 2-2 的四个表单浮层）

`GAP-039` 裁决为"表单改弹窗、详情保留抽屉"后，学生与教师的四个表单浮层载体发生变化。
前两批的用例（TC-01 ~ TC-13、SC-01 ~ SC-03）当时是用一次性临时文件跑的，无法直接复跑，
因此本次补一个可复现的回归 harness：`evidence/stage2-prototype/verify-carrier-change.html`
（截图 `carrier-change_verify-results.png`）。

| 用例 | 操作 / 断言 | 期望 | 实测 |
|---|---|---|---|
| SC-01 | 学生点「新增」 | 打开的是 `.dialog` 而不是 `.drawer` | 弹窗 |
| SC-02 | 检查新增弹窗内部 | 不再出现 `drawer-header` / `drawer-body` / `drawer-footer` | 0 处 |
| SC-03 | 学生行内「编辑」 | 打开的是弹窗 | 弹窗 |
| SC-04 | 检查编辑弹窗内部 | 不再出现 `drawer-*` 钩子 | 0 处 |
| SC-05 | 编辑弹窗内「取消」 | 放弃确认仍是弹窗（sm） | 弹窗 |
| TC-01 | 教师点「新增」 | 三步表单仍在**一个弹窗**内 | 弹窗 |
| TC-02 | 检查新增弹窗内部 | 不再出现 `drawer-*` 钩子 | 0 处 |
| TC-03 | 空表单点「下一步」 | 仍被校验拦住，停在第 1 步（载体变更不影响步骤条） | 当前步骤 1 |
| TC-04 | 教师行内「编辑」 | 打开的是弹窗 | 弹窗 |
| TC-05 | 检查编辑弹窗内部 | 不再出现 `drawer-*` 钩子 | 0 处 |
| TC-06 | 教师行内「角色」 | 教育角色分配仍是弹窗 | 弹窗 |
| TC-07 | 检查角色分配弹窗 | 内部 header 钩子已改为 `dialog-header` | 0 处遗留 |
| TC-08 | 教师点数据行 | 教师详情**仍然是抽屉**（详情类不改载体） | 抽屉 |
| TC-09 | 详情内「编辑」 | 打开弹窗（不是抽屉套抽屉） | 弹窗 |

总结果：`合计 14 条，通过 14 条，不通过 0 条 —— 全部通过`。

过程中 harness 自身出过一次写法错误：把 `window.PrototypeShell` 当成了 harness 自己的全局对象，
实际它在 iframe 的 window 上，导致 `Cannot read properties of undefined (reading 'closePanel')`。
已改为 `frame.contentWindow.PrototypeShell`，并给 harness 加了 `window.onerror` 与超时兜底，
出错时也会把已跑完的结果渲染出来，便于下次一眼定位断点。

## 9. D-060：详情抽屉打不开（点行只弹「批次交付」提示）

### 9.1 现象与根因

用户在验收年级批次时问"怎么打开抽屉"：列表行有 `cursor: pointer`（看起来可点），
但点下去只弹「「年级详情」在批次 2-2 交付（PAGE-GRD-DETAIL），本批只验证入口与权限显隐」，抽屉不出现。

根因不在页面数据，而在点击委托的分支判定：

| 位置 | 内容 | 后果 |
|---|---|---|
| `prototypes/functional/v1/pages/grade-list.html` 第 174 行起 | 数据行写的是 `data-nav="PAGE-GRD-DETAIL"`，**没有** `data-overlay` | — |
| `prototypes/functional/v1/assets/prototype-shell.js` 点击委托 | 只有 `data-overlay === 'drawer' / 'dialog' / 'block'` 才走 `openPanel()` | 否则走"跳独立页"分支 |
| 同文件"跳独立页"分支 | `PAGE-GRD-DETAIL` 不在 `MENUS` / `EXTRA_PAGES` 里（它是本页浮层片段） | 落到"批次交付"提示 |

同一缺陷存在于**学生列表**（`PAGE-STU-DETAIL`）与**教师列表**（`PAGE-TCH-DETAIL`）的数据行——
三个列表页的数据行都只写了 `data-nav`。年级列表更严重：操作列原来没有「详情」入口，
所以除了在地址栏敲 `#panel=PAGE-GRD-DETAIL` 之外，界面上没有任何办法打开抽屉。
`markup-contract.md` 当时也只登记了 `data-nav`，没登记 `data-overlay`，属于规范缺口。

### 9.2 修复

1. 外壳（一次修三类页面）：未显式写 `data-overlay` 时，若当前文档存在 `[data-demo-panel="<data-nav>"]`，
   按浮层打开；显式 `data-overlay` 优先级不变。
2. 年级列表补可见入口：操作列新增只读「详情」（复用 `ACT-GRD-009`）；年级名称列加链接色；
   操作列宽 216 → 280px、年级名称 264 → 200px，表格 `min-width` 仍为 1122px。
3. `markup-contract.md` 补 `data-overlay` 属性行与第 10.1 节的判定规则。

### 9.3 验证

`evidence/stage2-prototype/verify-grade-list.html` 新增 4 条（GL-35 ~ GL-38），另调整 2 条断言口径
（GL-05 年级主任可见行内操作由"0 个"改为"只剩只读「详情」"；GL-16 由"点行弹批次提示"改为"点行直接开抽屉"——
原断言把缺陷当成预期，属于断言写错）：

| 用例 | 操作 / 断言 | 期望 | 实测 |
|---|---|---|---|
| GL-35 | 点「2024 级 小学三年级」数据行 | 抽屉打开、标题跟随该行、不出现"交付"提示 | 通过 |
| GL-36 | 点该行操作列「详情」 | 打开同一抽屉、标题跟随该行 | 通过 |
| GL-37 | 关闭抽屉 | 无残留浮层，列表行数与筛选不变 | 通过 |
| GL-38 | 查空年级行操作列 | 5 个入口不换行、1440 下不横向滚动 | 5 个 / 1190px vs 1190px |

新增 `verify-detail-entry.html`（截图 `detail-entry_verify-results.png`）覆盖三个列表页：

| 用例 | 操作 / 断言 | 期望 | 实测 |
|---|---|---|---|
| RD-01 | 年级列表点数据行 | 打开详情抽屉且标题跟随该行 | 通过 |
| RD-02 | 年级列表点操作列「详情」 | 打开同一抽屉 | 通过 |
| RD-03 | 关闭年级抽屉 | 无残留浮层 | 通过 |
| RD-04 | 教师列表点数据行 | 打开教师详情抽屉（外壳自动识别已有片段） | 通过 |
| RD-05 | 教师列表点「编辑」（显式 `data-overlay="dialog"`） | 仍开弹窗，不被抽屉规则覆盖 | `PAGE-TCH-EDIT` |
| RD-06 | 学生列表点数据行 | 学生详情属批次 2-5，只给提示、不开空抽屉 | 通过 |

总结果：`合计 38 / 38 条`（年级）与 `合计 6 / 6 条`（三个列表页入口），均全部通过。

---

## 10. 批次 2-3c：班级四个弹窗（`verify-class-dialogs.html`）

新增 `evidence/stage2-prototype/verify-class-dialogs.html`（36 条断言，`CDL-01` ~ `CDL-36`），
覆盖 `pages/class-list.html` 的四个弹窗片段与一条跨页链路。
harness 用**第二个 iframe** 加载 `pages/class-detail.html`，验证「详情页 → 列表页」的跨页带入，
其余断言复用批次 2-3a / 2-3b 的同源 iframe + 真实事件派发方式。

### 10.1 用例分组与结果（全部通过）

| 分组 | 用例 | 关键断言 |
|---|---|---|
| 载体与入口 | CDL-01 ~ 07 | 四个片段均为 `el-dialog` 且无 `drawer-*` 钩子；弹窗内 0 个 `<table>`；行内「编辑 / 指定班主任」带 `data-nav`；教学班行不给班主任入口且点「编辑」给批次 2-7 提示；校领导下行内只剩「详情」 |
| 新建 / 编辑班级 | CDL-08 ~ 15 | 默认值（当前学期 / 高一 / 行政班 / `高一 (4) 班` / 容量 45）；教学班选项置灰并指向 `addTeachingClass`；必填校验不关弹窗；重名拦截且 1.3 秒后仍无「已保存」；保存成功后弹窗不关并给出三个后续动作；编辑态学期 / 年级 / 类型 / 班主任只读；编辑保存后按编辑口径关闭弹窗 |
| 批量生成 | CDL-16 ~ 20 | 高一 1–6 班预览 6 行、3 行「已存在，跳过」；冲突提示写明跳过数量；区间倒置被拦；1–100 只展开 30 行 + 说明行；7–8 无冲突两行「将生成」 |
| 复制班级 | CDL-21 ~ 25 | 源班级 9 项摘要且花名册 / 班主任 / 任职历史三行标红；默认目标学期 / 年级 / 名称；重名拦截；教学班只给提示不开弹窗；`data-api="addClass"` |
| 指定 / 变更班主任 | CDL-26 ~ 32 | 有在任时标题「变更班主任」、历史 1 行 + 说明；未指定时两处空态；离职 / 调离 / 非本校三项列出但不可选；历史学年学期不能保存；未选新任触发必填校验；`data-api="assignHeadTeacher"`；保存前四条检查写明规则编号 |
| 角色与跨页 | CDL-33 ~ 36 | 校领导打开新建弹窗 8 字段全禁用且保存不渲染；平台运营看不到新建 / 批量生成 / 直接导出；深链接 `class=<名称>` 直接打开该班编辑态；班级详情「编辑班级」跳到列表页并带入该班 |

### 10.2 本批顺带修掉的 harness 缺陷

1. `verify-class-list.html` 的 `CL-34` 原来只允许 `ACT-CLS-001 ~ 016`；本批页面新增四个弹窗后必然失败。
   已按「`common_actions` + `class_*` 各动作组」扩到 001 ~ 047，断言语义不变。
2. `verify-grade-list.html` 的 `GL-15` 断言在 D-064 之后过期（「班级数」由提示改为真实跨页跳转），
   且该 harness 的 `load` 监听缺少启动守卫，跳转触发的第二次 load 会重启一条断言链并在 `GL-04` 抛异常，
   页面长期停在「运行中…」（实测 `--dump-dom` 只有 1 个 `li`）。
   已按当前形态改断言 + 补 `FRAME_SRC` 与守卫 + 4 秒兜底，回归 `38 / 38`，见 `GAP-050`。

### 10.3 全量回归结果

| harness | 结果 |
|---|---|
| `verify-class-dialogs.html` | 36 / 36 通过 |
| `verify-class-list.html` | 39 / 39 通过 |
| `verify-class-detail.html` | 20 / 20 通过 |
| `verify-grade-list.html` | 38 / 38 通过 |
| `verify-detail-entry.html` | 6 / 6 通过 |
| `verify-carrier-change.html` | 14 / 14 通过 |
| `python tools/check_docs.py` | 通过：未发现问题 |

---

## 13. 批次 2-3e-s2：升班向导第一步（`pages/promotion-create.html`）

新增 harness `evidence/stage2-prototype/verify-promotion-create.html`（`PC-01` ~ `PC-22`，真实事件派发：
点角色按钮 / 点样例 chip / 改下拉 / 点「下一步」/ 改 hash，全部走真实 DOM 事件）。

| 分组 | 覆盖 |
|---|---|
| 结构与字段 | 四步向导第 1 步进行中、其余 3 步可点（`data-nav`）；学校只读、源 / 目标学年学期必填、范围说明选填（`REQ-PRM-007`） |
| 字段级校验 | 未选学年学期点「下一步」→ 两个字段标红 + 校验汇总 + 不提交；源 2026-2027 第二学期 / 目标 2026-2027 第一学期 → 目标起始日期的字段级拦截（`REQ-PRM-008` / `BR-TERM-005`） |
| 三类前置校验 | 缺年级与班级 → 阻塞条列 3 项并给「去创建年级 / 去创建班级」（`REQ-PRM-009`）；同一源→目标已有未结束任务 → 阻塞并指向 `PRM-20260930-0012`（`REQ-PRM-005`）；在读 10,240 → 只警告不阻塞（`REQ-PRM-010` / `NFR-PERF-07`） |
| 阻塞时的点击行为 | 缺项样例下点「下一步」：`data-blocked=true`、给出「存在阻塞项」提示、按钮不进入 loading（不发请求） |
| 角色形态 | 校领导 / 年级主任 / 平台运营 / 班主任 / 租户管理员（矩阵未授予 `promotion.batch:create`）→ 全部进无权限态且表单区不可见；超级管理员 → 正常态 + `BR-ORG-014` 强制留痕提示 |
| 状态片段 | 加载中 / 空数据 / 校验失败 / 无权限 / 提交中五类齐全；无权限态写明「不降级为全量」（`DS-DENY-03` / `NFR-SEC-05`）；校验失败态含请求编号、错误码与「不会跳过校验继续」 |
| 深链接与登记 | `#sample=large` 在已打开的页面上立即生效（hashchange 重放）；页面 9 个 `data-action-id` 全部落在 `common_actions` 与 `promotion_create` 已登记集合内 |

| harness | 结果 |
|---|---|
| `verify-promotion-create.html` | 22 / 22 通过 |

同一轮回归（本批改了 `prototype-shell.js` 的 `EXTRA_PAGES` 与 `PENDING_PAGES`，8 个已交付 harness 全部重跑）：

| harness | 结果 |
|---|---|
| `verify-promotion-list.html` | 36 / 36 通过 |
| `verify-class-list.html` | 39 / 39 通过 |
| `verify-class-detail.html` | 20 / 20 通过 |
| `verify-class-roster.html` | 34 / 34 通过 |
| `verify-class-dialogs.html` | 36 / 36 通过 |
| `verify-grade-list.html` | 38 / 38 通过 |
| `verify-detail-entry.html` | 6 / 6 通过 |
| `verify-carrier-change.html` | 14 / 14 通过 |
| `python tools/check_docs.py` | 通过：未发现问题 |

本批在 harness 写法上补了两条防呆（与页面缺陷无关，但会掩盖真实结果）：

1. **异步断言必须返回 promise**：角色切换的断言用 `.then()` 串联，若不返回 promise，
   一旦某步抛错整条链会在 await 处静默断掉，结果清单长期停在「运行中…」。
   `run()` 现在会捕获 promise 的 rejection 并记为 `ERR-xx`，另加 30 秒看门狗兜底。
2. **元素级监听 vs document 级监听**：外壳的托管点击监听挂在 `document` 上，
   页面里同样挂 `document` 的拦截逻辑用 `stopPropagation()` 拦不住它（需要 `stopImmediatePropagation`）。
   「下一步」的校验拦截因此改挂在按钮自身（先于 document 执行），`PC-06` 断言在缺项样例下点击不会发请求。

---

## 14. 批次 2-3e-s3 / s4：升班向导第 2 ~ 4 步（5 个页面）

新增 harness `evidence/stage2-prototype/verify-promotion-wizard.html`：4 个同源 iframe
（`promotion-preview` / `promotion-validate` / `promotion-execute` / `promotion-result`）里真实派发
点击、下拉、单选、chip 与 hash 变更，跑 `PV` / `ADJ` / `VD` / `EX` / `RS` / `ALL` 共 39 条断言。

| 分组 | 覆盖 |
|---|---|
| 预览（`PV-01` ~ `PV-12`） | 7 行明细 + 5 个源班级；结果类型统计 4 / 1 / 1 / 1；留级行的「同学段同名年级」与毕业行的「不生成下一学年关系」（`BR-PROMO-006` / `REQ-PRM-015`）；点左栏按 `REQ-PRM-013` 过滤并可回到全部；「应用到本班」批量写目标班级（`REQ-PRM-018`）；行内「调整」入口标记；年级主任 5 行只读且无批量入口；校领导 7 行只读；班主任无权限面板；五类状态；深链接 `#class=` |
| 调整弹窗（`ADJ-01` ~ `ADJ-06`） | 打开并带入学生与当前处理方式；选「留级」→ 目标班级换成只读的同学段同名年级 + 原因必填；未填原因保存被字段级拦截且弹窗不关；保存走 `updatePromotionItem`；处理方式切换驱动必填项；弹窗内无表格 |
| 校验（`VD-01` ~ `VD-07`） | 通过 5 / 警告 1 / 错误 1（`REQ-PRM-023`）；错误项存在时「确认执行」被拦（`REQ-PRM-025`）；按维度下钻 1 / 5 / 7（`REQ-PRM-026`）；「标记跳过」只对非通过行出现且走 `updatePromotionItem`；年级主任 5 行只读（错误 1、警告 0）；五类状态与 `DS-DENY-03`；确认执行登记 `executePromotionTask` 且 `data-blocked=true` |
| 执行（`EX-01` ~ `EX-06`） | 进度 5 / 7 = 71% + 成功 4 + 时间线 5 条；深链接 `#processed=N` 生效；刷新走 `getPromotionTask`；「查看结果」指向 `PAGE-PRM-RESULT`；取消入口指向列表；六类状态（含排队中、部分失败） |
| 结果（`RS-01` ~ `RS-07`） | 默认失败清单 + 重试块 + 「部分失败」标签；四类清单切换 6 / 1 / 1 / 1（`REQ-PRM-034`）；失败行「查看学生」带 `person.student:read`；只重试失败项与继续执行剩余项分别登记；结果导出走 `exportPromotionResult`；年级主任按 `DS-05` 收窄（失败 1 / 成功 4）且重试按钮隐藏；六类状态 |
| 动作登记（`ALL-01`） | 四个页面用到的动作编号全部落在 `ACT-PRM-020` ~ `043` 已登记集合内 |

| harness | 结果 |
|---|---|
| `verify-promotion-wizard.html` | 39 / 39 通过 |

本批回归（同时改了 `prototype-shell.js` 的 `EXTRA_PAGES`）：

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

本批踩到的一个坑（已写进 README 6.4）：用 Chrome 无头截图时把 URL 拼成
`.../pages//promotion-preview.html#...`（变量末尾与文件名之间多了一个 `/`）会让页面**不执行 JS**——
截图看起来"正常"，实际是没有外壳的原始 HTML，而且所有 `#` 深链接变体渲染完全相同（文件大小一模一样）。
判据是「同一页不同深链接的截图字节数完全相同」，据此发现并重拍了 15 张截图。

---

## 15. 批次 2-4：导入向导 / 模块导入 / 登录 / 异常页

新增 harness `evidence/stage2-prototype/verify-import-login.html`：8 个同源 iframe
（`import-wizard` / `student-import` / `teacher-import` / `class-import-roster` / `login` / `403` / `404` / `500`），
真实派发点击、下拉、切换、深链接，跑 `IMP` / `MS` / `MT` / `MC` / `LG` / `ER` / `ALL` 共 30 条断言。

| 分组 | 覆盖 |
|---|---|
| 导入向导（`IMP-01` ~ `IMP-14`） | 第 1 步 14 列清单与模板版本（`BR-IMP-007`）；「过期仍可下载但强提示」（`IMP-Q-05`）；下载模板走 `downloadImportTemplate`；四步页内切换（1 → 2 → 3 → 4）与步骤条状态；第 2 步 5000 行 / 10 MB / 30 秒与并发配额（`IMP-Q-01` / `03`）；第 3 步 120 / 118 / 2 + 2 条失败明细；下载失败明细走 `downloadImportFailedRows` 与学号对照表说明（`BR-STU-019`）；第 4 步任务号 + 62% + 结果 7 天 / 元数据 90 天（`IMP-Q-02`）；「查看异步任务」指向 `PAGE-IMP-TASK-LIST`；「重新导入」回到第 1 步；任课教师与平台运营进无权限面板；五类状态齐全 |
| 学生导入（`MS-01` / `MS-02`） | 14 列模板 + 「不含学号列」口径；校验结果与导入向导同两条失败明细（证件号重复 / 年级不存在） |
| 教师导入（`MT-01`） | 9 列模板（`REQ-TCH-050`）+ 工号租户内唯一（`BR-TEACHER-002`） |
| 编班表导入（`MC-01`） | 4 列含目标班级列（`REQ-CLS-035`）+ 失败明细含「已有行政班关系」（`REQ-CLS-029`） |
| 登录（`LG-01` ~ `LG-06`） | 不套管理外壳（无侧边栏 / 演示面板）；空表单字段级校验且不发请求；密码错误给剩余次数（`NFR-SEC-04`）；三类动作齐备；登录走 `login` 且首登改密口径可见（`D-039`）；「忘记密码」给「联系管理员」口径 |
| 异常页（`ER-01` ~ `ER-05`） | 403 写明 `DS-DENY-03` / `NFR-SEC-05` 并给两个入口；三页都不套外壳；404 指向页面注册表；500 给出请求编号 `REQ-20261001-000701` 与错误码 `EDU-SYS-5001`、写明异步任务不产生部分写入（`NFR-MQ-02`）；500 三个入口齐备 |
| 动作登记（`ALL-01`） | 八个页面用到的动作编号全部落在 `import_common` / `auth` / `error_page` 已登记集合内 |

| harness | 结果 |
|---|---|
| `verify-import-login.html` | 30 / 30 通过 |

本批回归（修改了 `prototype-shell.js` 的 `MENUS.delivered` 与 `EXTRA_PAGES`）：

| harness | 结果 |
|---|---|
| `verify-promotion-wizard.html` | 39 / 39 通过 |
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

### 11.3 `CR-011`（在读口径收敛）后的重跑

高二 (1) 班样例数由 2 改为 1（`GAP-051` 取选项 A）后，7 个 harness 全部重跑，结果与上表一致：
class-list 39/39、class-detail 20/20、class-roster 34/34、class-dialogs 36/36、
grade-list 38/38、detail-entry 6/6、carrier-change 14/14；`check_docs.py` 通过。
没有任何断言依赖高二 (1) 班的具体在读人数（`verify-class-list` 只用 `data-enrolled` 判超容量），
因此这次口径收敛不需要改断言；受影响的只有 21 张 `class-list_*.png` 截图，已重拍。

复核命令（本机 PowerShell，任选一个 harness 替换文件名即可）：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
& $chrome --headless=new --disable-gpu --no-first-run --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-chrome-verify" --virtual-time-budget=120000 --dump-dom `
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/evidence/stage2-prototype/verify-class-dialogs.html" |
  Select-String -Pattern 'class="sum'
```

---

## 11. 批次 2-3d：编班 / 批量迁学生 / 移出与调班（`verify-class-roster.html`）

新增 `evidence/stage2-prototype/verify-class-roster.html`（34 条断言），用**三个 iframe** 分别加载
`pages/class-roster-add.html`（编班）、`pages/class-move-students.html`（批量迁学生）与
`pages/class-detail.html`（移出确认 / 调班弹窗），三份文档都加载完成后再按顺序跑断言。

| 分组 | 用例 | 关键断言 |
|---|---|---|
| 编班页 | RA-01 ~ RA-13 | 页头标签；学生池 15 行；逐行判定 可加入 2 / 已在本班 4 / 已有行政班 6 / 转入未报到 1 / 终态 2；勾选可用性 6 禁用 / 9 可用；「只看可加入」只剩 2 行；姓名检索；勾选后可加入与冲突两种清单形态；任一条冲突则整体拒绝且清单不清空；移除后冲突块收起；全部通过时保存成功；深链接切目标班级后「已在本班」判定跟着变；列宽之和 = min-width |
| 批量迁学生页 | MV-01 ~ MV-10 | 源班级在读 2 人默认全选；休学成员不可选；影响预览 5 列 2 行；清空选择给提示；目标班级已停用 → 提示块 + 每行不可迁入；跨年级 → 确认提示；停用目标点执行被拦；正常执行 1.2 秒后出结果块；深链接到空班给空态；预览列与 REQ-CLS-062 一致 |
| 移出与调班 | TR-01 ~ TR-11 | 花名册勾选列（3 行 / 表头 10 列）；行内「移出」带入该生；未勾选时只提示不开空弹窗；勾选后确认片段列出成员；确认移出走 `removeClassRoster`；行内「调班」带入该生与源班级且源班级选项禁用；必填校验不关弹窗；确认调班走 `transferClass` 且已停用班级不可选；编班页深链接三个参数全部生效；历史学年学期勾选列禁用且行内只剩「详情」；两块片段内都不含表格 |

### 11.1 与上一批的关系

本批改了 `pages/class-detail.html` 的花名册（新增勾选列，表格 min-width 1080 → 1122），
因此把 `verify-class-detail.html` 一起重跑：20 / 20 通过（它的断言用 `th[data-field=…]` 与 `td.actions` 选取，
不受新增列影响）。其余 4 个 harness 也全部重跑通过。

### 11.2 全量回归结果（批次 2-3d 结束时）

| harness | 结果 |
|---|---|
| `verify-class-roster.html` | 34 / 34 通过 |
| `verify-class-list.html` | 39 / 39 通过 |
| `verify-class-detail.html` | 20 / 20 通过 |
| `verify-class-dialogs.html` | 36 / 36 通过 |
| `verify-grade-list.html` | 38 / 38 通过 |
| `verify-detail-entry.html` | 6 / 6 通过 |
| `verify-carrier-change.html` | 14 / 14 通过 |
| `python tools/check_docs.py` | 通过：未发现问题 |

---

## 12. 批次 2-3e-s1：升班任务列表（`pages/promotion-list.html`）

新增 harness `evidence/stage2-prototype/verify-promotion-list.html`（`PRM-01` ~ `PRM-36`，真实事件派发：
在 iframe 里点按钮 / 改下拉 / 改 hash，不靠人工目视）。

| 分组 | 覆盖 |
|---|---|
| 结构 | 10 列与 `REQ-PRM-001` 一致；`FD-promotion_task_status` 的 8 个状态各至少 1 条样例；列宽之和 = 表格 `min-width` = 1108（≤ 1118，1366×768 下不出现表格内横向滚动） |
| 状态驱动的按钮集合 | 草稿 / 已预览待确认 / 校验中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 八套行内动作逐条断言（`REQ-PRM-006`） |
| 角色形态 | 教务主任（11 行 + 写入口）、校领导（11 行只读）、年级主任（6 行只读 + 计数 512 → 128）、平台运营（只读 + 授权导出 + 切他校 1 行）、班主任与租户管理员（无权限面板 + 表体 0 行） |
| 筛选与深链接 | 状态 / 源学年学期 / 任务编号三类筛选、重置、`#status=running` 在已打开的页面上立即生效（`hashchange`） |
| 取消确认片段 | 行内「取消」绑定当前行任务编号 → 原因必填被拦且弹窗不关 → 确认走 `cancelPromotionTask` 且权限点为 `promotion.batch:update`（`CR-012`） |
| 数据口径 | 草稿的三个计数列显示「—」；跨学段 / 超阈值标注；无权限态写明不降级为全量（`DS-DENY-03`） |
| 动作登记 | 页面 17 个 `data-action-id` 全部落在 `common_actions` 与 `promotion_list` 已登记集合内 |

| harness | 结果 |
|---|---|
| `verify-promotion-list.html` | 36 / 36 通过 |

同一轮回归（本批没有改动已交付页面，7 个 harness 重跑作为证据）：

| harness | 结果 |
|---|---|
| `verify-class-list.html` | 39 / 39 通过 |
| `verify-class-detail.html` | 20 / 20 通过 |
| `verify-class-roster.html` | 34 / 34 通过 |
| `verify-class-dialogs.html` | 36 / 36 通过 |
| `verify-grade-list.html` | 38 / 38 通过 |
| `verify-detail-entry.html` | 6 / 6 通过 |
| `verify-carrier-change.html` | 14 / 14 通过 |
| `python tools/check_docs.py` | 通过：未发现问题 |
