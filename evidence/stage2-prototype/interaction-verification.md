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

复核命令（本机 PowerShell，任选一个 harness 替换文件名即可）：

```powershell
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
& $chrome --headless=new --disable-gpu --no-first-run --allow-file-access-from-files `
  --user-data-dir="$env:TEMP\codex-chrome-verify" --virtual-time-budget=120000 --dump-dom `
  "file:///D:/work/person_work/ruoyi-cloud-monorepo/evidence/stage2-prototype/verify-class-dialogs.html" |
  Select-String -Pattern 'class="sum'
```
