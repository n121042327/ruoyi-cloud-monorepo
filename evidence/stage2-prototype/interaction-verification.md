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
