# 阶段 3（独立高保真原型）验收证据

## 1. 本批范围（样板批 3-1）

| 项 | 内容 |
|---|---|
| 输入 | 阶段 2 的功能原型（`prototypes/functional/v1`）+ `visual-spec.yaml`（本批同时产出） |
| 输出 | `prototypes/high-fidelity/v1`（视觉规范 + 外壳 + 学生管理样板页） |
| 交付页面 | `PAGE-STU-LIST`（列表）、`PAGE-STU-DETAIL`（详情抽屉）、`PAGE-STU-CREATE` / `PAGE-STU-EDIT`（新增 / 编辑弹窗） |
| 门禁 | 视觉标准冻结：样板页经人工验收后，`visual-spec.yaml` 从「冻结候选」转为「冻结」，其余批次按同一标准铺开 |

## 2. harness 结果

### 2.1 覆盖度 harness（全量）

`evidence/stage3-highfidelity/verify-hifi-coverage.html`（逐页在 iframe 里加载 45 个高保真页面）：**48 / 48 通过**。

| 分组 | 覆盖 |
|---|---|
| 逐页检查（`CV-01` ~ `CV-45`） | 每页断言：加载高保真覆盖层 `hifi.css`、高保真外壳生效（独立页除外）、`[data-normal-view]` 存在、状态片段数量不少于清单、卡片阴影生效（token 未回退）、清单里声明的页面编号都出现在该页 DOM 上 |
| 集合级检查（`CV-COVERAGE-1` ~ `3`） | 页面数量 45 / 45；DOM 渲染出的 95 个页面编号（含 `PAGE-*` / `DIALOG-*` / `DRAWER-*`）全部在清单内；DOM 渲染出的 378 个 `data-action-id` 全部在清单内 |

源码级全覆盖由 `python tools/make_hifi_coverage.py` 证明（退出码 0）：阶段 2 与阶段 3 的
页面编号集合、动作编号集合、逐页状态片段集合**差异为 0**（95 个页面编号 / 403 个动作编号 / 229 个状态片段）。
该脚本同时生成 `prototypes/high-fidelity/v1/page-manifest.yaml`（逐页清单）。

### 2.2 视觉基准 harness（3-1 样板批）

`evidence/stage3-highfidelity/verify-hifi-student.html`（单 iframe + 真实事件派发，`HF-01` ~ `HF-18`）：**18 / 18 通过**。

> 目标页面是手工精修的**视觉基准** `prototypes/high-fidelity/v1/reference/student-list-visual-reference.html`。
> `pages/` 下的 45 个交付页由阶段 2 派生（见 `tools/make_hifi_pages.py`），
> 用覆盖层 `assets/hifi.css` 表达同一套视觉 token。

| 分组 | 覆盖 |
|---|---|
| 结构与视觉（`HF-01` / `HF-02`） | 12 列 12 行、列宽之和 = `min-width` = 1128；主色 `rgb(47,107,255)`、卡片阴影、表头底色 `rgb(247,249,252)` 三处 token 实测生效 |
| 状态载体（`HF-03`） | 五类状态片段齐全，主内容包在 `[data-normal-view]` 内（状态替换内容） |
| 行内动作与详情（`HF-04` ~ `HF-07`） | 教务主任 12 行 × 3 个动作；抽屉带入姓名 / 学号 / 班级 / 状态 / 电话；证件号掩码与留痕口径；3 条追加式变更记录 |
| 表单与字段级权限（`HF-08` ~ `HF-10`） | 新增弹窗字段级拦截；`addStudent` + `person.student:create`；编辑弹窗带入学号且联系电话登记 `data-role-editable` |
| 角色形态（`HF-11` ~ `HF-15`） | 年级主任 `DS-05` 可写；班主任只读本班；任课教师隐藏联系电话整列（`DS-07` 字段裁剪）；租户管理员无权限（`DS-02`）；超管全放行（`BR-ORG-014`） |
| 深链接与一致性（`HF-16` ~ `HF-18`） | `#state=empty` 在已打开页面上生效；12 列列顺序与阶段 2 的 `student-list` 逐项一致；演示面板 8 角色 + 8 状态 |

## 3. 截图

### 3.1 全量交付页截图（53 张）

| 目录 / 文件 | 分辨率 | 场景 |
|---|---|---|
| `pages/*_1440x900.png`（45 张） | 1440×900 | 45 个高保真交付页的主视口 |
| `pages/teacher-list_1366x768.png` 等（8 张） | 1366×768 / 1920×1080 | 代表页三档检查（教师列表 / 年级列表 / 班级列表 / 选课统计） |

### 3.2 视觉基准与 harness 结果

| 截图 | 分辨率 | 场景 | 用于验证 |
|---|---|---|---|
| `student-list_1440x900.png` | 1440×900 | 视觉基准主视口（教务主任） | 高保真视觉基线：主色、卡片阴影、表头底色、标签、行内动作间距 |
| `student-list_1366x768.png` | 1366×768 | 最低支持分辨率 | 不出现整页横向滚动；表格列不挤压错位 |
| `student-list_1920x1080.png` | 1920×1080 | 次视口 | 大屏下的留白与列宽表现 |
| `student-list_drawer-detail_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-STU-DETAIL` | 详情抽屉（基本信息 + 证件号掩码口径 + 变更记录时间线） |
| `student-list_dialog-create_1440x900.png` | 1440×900 | 深链接 `#panel=PAGE-STU-CREATE` | 新增弹窗（系统发号只读、字段级校验） |
| `student-list_role-subject-teacher_1440x900.png` | 1440×900 | 角色 = 任课教师 | 字段裁剪：联系电话整列隐藏（`DS-07`） |
| `student-list_role-school-leader_1440x900.png` | 1440×900 | 角色 = 校领导 | 只读形态：无编辑 / 调班入口 |
| `student-list_state-empty_1440x900.png` | 1440×900 | 深链接 `#state=empty` | 空态（替换主内容 + 主行动按钮） |
| `student-list_state-forbidden_1440x900.png` | 1440×900 | 角色 = 租户管理员 | 无权限形态（`DS-02` 无教学数据范围，不进入空白页） |
| `hifi-student_verify-results.png` | 1100×1400 | 视觉基准 harness 结果 | 合计 18 / 18 条 —— 全部通过 |
| `hifi-coverage_verify-results.png` | 1100×2600 | 覆盖度 harness 结果 | 合计 48 / 48 条 —— 全部通过 |
| `hifi-index_1440x1200.png` | 1440×1200 | 高保真入口页 | 批次表与规范摘要 |

> 命名口径：根目录的 `student-list_*.png` 是**视觉基准**（`reference/`）的截图；
> `pages/student-list_1440x900.png` 是**交付页**（派生）的截图。两者用同一套 token，观感一致。

## 4. 与阶段 2 的差异（本批只做视觉）

| 维度 | 阶段 2 | 阶段 3 |
|---|---|---|
| 主色 | Element Plus 默认 `#409eff` | `#2f6bff`（与 `apps/plus-ui` 现有风格对齐并提亮一档） |
| 页面底色 | 纯白 | `#f3f5f9` 浅灰底 + 白色卡片（形成层次） |
| 表头 | 无底色 | `#f7f9fc` 底色 + 600 字重 |
| 卡片 | 1px 边框 | 无边框 + 双层阴影（hover 加深） |
| 行内动作 | 文字紧排 | `flex` + 10px 间距，固定不换行溢出 |
| 空态 / 错误态 | 图标 30px | 图标 40px、垂直居中、说明限制在 620px 内 |
| 骨架屏 | 静态灰条 | 渐变动画（1.4s 循环） |

> 结构与字段完全一致：由 `HF-17` 逐列比对阶段 2 的 `student-list`，列清单与顺序相同。

## 5. 待人工确认

1. 视觉方向是否认可（主色 `#2f6bff`、浅灰底 + 白卡、表头底色、行内动作间距）。
2. `design-tokens.json` 是否可以**冻结**（冻结后 3-2 ~ 3-9 按同一 token 铺开；如需改色，改一次全量重刷）。
3. 批次划分是否接受：3-0（规范四件套）与 3-1（样板）已产出，3-2 ~ 3-9 与阶段 2 的 2-2 ~ 2-9 一一对齐。

> 按用户授权「需要我拍板的默认选推荐」：以上三项若未另行回复，按当前实现继续铺开 3-2；
> 视觉标准在 `design-tokens.json` 上冻结，后续如需调整只改该文件并全量重刷。

## 6. 本批文件清单（3-0 + 3-1）

| 文件 | 作用 |
|---|---|
| `prototypes/high-fidelity/v1/design-tokens.json` | 视觉 token（机器可读，权威值） |
| `prototypes/high-fidelity/v1/visual-spec.yaml` | 视觉规范的人类可读摘要 |
| `prototypes/high-fidelity/v1/component-spec.md` | 组件规格（20 个组件的变体 / 尺寸 / 状态 / 映射） |
| `prototypes/high-fidelity/v1/component-mapping.yaml` | 组件映射表：样板批逐元素明细（4 页 / 32 个元素）+ 全量覆盖模型（规则表 + 逐页计数） |
| `prototypes/high-fidelity/v1/visual-checklist.md` | 交批自查清单（40 条，含全量覆盖度门禁） |
| `prototypes/high-fidelity/v1/page-manifest.yaml` | 逐页清单（45 页 / 95 个页面编号 / 403 个动作编号 / 229 个状态片段 + 逐页元素计数） |
| `prototypes/high-fidelity/v1/assets/hifi.css`、`hifi-shell.js` | 高保真样式与外壳 |
| `prototypes/high-fidelity/v1/pages/*.html`（45 页） | 全量交付页（由阶段 2 派生） |
| `prototypes/high-fidelity/v1/reference/student-list-visual-reference.html` | 手工精修的视觉基准（不在交付清单内） |
| `prototypes/high-fidelity/v1/index.html`、`README.md` | 入口与交付说明 |
| `tools/make_hifi_pages.py` | 派生工具（视觉层 + 外壳替换，幂等可复现） |
| `tools/make_hifi_coverage.py` | 覆盖度工具（生成 `page-manifest.yaml` 与覆盖度 harness，退出码 0 表示无功能删减） |

## 7. 3-2 ~ 3-9 的覆盖方式与剩余工作

阶段 3 的 45 个交付页已一次性覆盖 3-1 ~ 3-9 的全部范围（95 个页面编号）。
3-2 ~ 3-9 尚未做的是**逐批人工观感复核**，不是功能缺口：

| 剩余工作 | 说明 | 建议做法 |
|---|---|---|
| 逐批观感复核 | 每批抽 2–3 页看 1366 / 1440 / 1920 三档观感（列宽、换行、遮挡） | 从 `pages/` 取该批截图，按 `visual-checklist.md` 第 16 ~ 19 条过一遍 |
| 单页视觉精修 | 覆盖层无法表达的特殊版式（如图表页、向导页的步骤条） | 该页从 `tools/make_hifi_pages.py` 的派生清单移除，改为手工精修（参考 `reference/`） |
| token 冻结 | `design-tokens.json` 经用户确认后冻结 | 冻结后 3-2 ~ 3-9 的观感复核按同一 token 执行 |

确认后即按 3-2 起批量铺开，每批 ≤ 3 个页面并暂停验收。
