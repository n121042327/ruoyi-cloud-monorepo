# 组件规格（阶段 3 · `component-spec.md`）

本文件把 `design-tokens.json` 落到**组件**：每个组件在高保真原型里的变体、尺寸、状态与对应的 Element Plus 组件。
阶段 6 的生产前端按第 3 列的映射直接实现，不需要再判断「这个 div 应该是什么」。

## 1. 组件清单

| # | 原型 class / data-\* | Element Plus 组件 | 变体 | 尺寸 | 必需状态 |
|---|---|---|---|---|---|
| 1 | `.card` | `el-card`（`:shadow="never"` + 自定义阴影） | 常规 / hoverable | 常规 | 默认、hover（仅 hoverable） |
| 2 | `.card-header` | `el-card` 的 `#header` 插槽 | 带标题 / 标题 + 右侧动作 | 高 52px | — |
| 3 | `.btn` | `el-button` | default / primary / danger / link | 高 36px | 默认、hover、active、disabled、loading |
| 4 | `.el-tag` | `el-tag` | primary / success / warning / danger / info | 高 22px | 默认 |
| 5 | `table.el-table` | `el-table`（`:border="false"` + 表头底色） | 常规 / 可勾选 / 行内动作 | 行高 48px | 默认、空数据、loading、hover 行 |
| 6 | `.pagination` | `el-pagination` | 常规 | 高 32px | 默认 |
| 7 | `.input` / `.select` / `textarea.input` | `el-input` / `el-select` / `el-input type="textarea"` | 常规 / 错误 / 只读 | 高 36px | 默认、focus、error、disabled、readonly |
| 8 | `.readonly-value` | `el-input` 的 `readonly` 或纯文本 | 常规 | 高 36px | 默认 |
| 9 | `.radio-row` | `el-radio-group` / `el-checkbox-group` | 单选 / 多选 | — | 默认、disabled |
| 10 | `.field` | `el-form-item` | 常规 / 带 hint / 错误 | — | 默认、error、readonly（`role-readonly` 灰底） |
| 11 | `.alert` | `el-alert`（`:closable="false"`） | info / warning / danger | — | 默认 |
| 12 | `.stat-card` | `el-card` + 自定义结构 | 常规 / 数值为文本 | 最小宽 150px | 默认 |
| 13 | `.bar-chart` / `.bar-row` | ECharts `bar` | 横向柱条 | 高 14px（柱） | 默认、空数据（不画空坐标系） |
| 14 | `.timeline` | `el-timeline` + `el-timeline-item` | 追加式 | — | 默认、空数据 |
| 15 | `.dialog` | `el-dialog` | sm 480 / md 560 / lg 720 | — | 默认、loading、footer 两 / 三按钮 |
| 16 | `.drawer` | `el-drawer` | md 560 / lg 720 | — | 默认 |
| 17 | `.state-block` | 自定义状态组件 | loading / empty / error / forbidden / submitting / 业务补充态 | 图标 40px | 六类（见第 4 节） |
| 18 | `.skeleton-row` / `.skeleton-bar` | `el-skeleton` | 行 / 卡片 | — | loading |
| 19 | `.toast` | `ElMessage` | success / warning / error / info | — | 默认（自动消失 3.6s） |
| 20 | `.demo-panel` | **原型专用，生产不实现** | — | — | — |

## 2. 载体规则（沿用阶段 2 的 D-059 / CR-015）

| 内容形态 | 载体 | 原型 class | Element Plus |
|---|---|---|---|
| 表单类（新建 / 编辑 / 配置 / 确认） | **弹窗** | `.dialog` | `el-dialog` |
| 详情类（只读展示、含时间线） | **抽屉** | `.drawer` | `el-drawer` |
| 含表格或分页的内容 | **独立页** | `.card` + `table.el-table` | 独立路由 + `el-table` |
| 危险动作二次确认 | 弹窗（原因必填） | `.dialog` + `.field.has-error` | `el-dialog` + `el-form` 校验 |

## 3. 尺寸与密度

| 项 | 值 | 依据 |
|---|---|---|
| 控件高度 | 36px | @element-plus default size |
| 表格行高 | 48px | 12px 上下 padding + 24px 行内容 |
| 卡片内边距 | 20px | `design-tokens.spacing.usage.card-padding` |
| 卡片间距 | 16px | 同上 |
| 弹窗宽度 | 480 / 560 / 720 | sm / md / lg |
| 抽屉宽度 | 560 / 720 | md / lg |
| 侧边栏宽 | 216px | `design-tokens.layout.sidebar-width` |

## 4. 状态规格

| 状态 | 图标 | 标题 | 说明 | 主行动 | 载体 |
|---|---|---|---|---|---|
| loading | 骨架屏（不占图标位） | — | — | — | `[data-demo-state-panel="loading"]` |
| empty | 📭 | 一句业务话术（例：当前条件下没有学生） | 说明范围为空不退化 + 调整筛选入口 | 1 个 | 同上 |
| error | ⚠️ | 失败对象 | 重试不放宽范围 + 请求编号 + 错误码 | 重试 | 同上 |
| forbidden | 🔒 | 缺少的权限点 | 需要哪个权限 + 哪个数据范围 + 不降级声明（DS-DENY-03） | 0–1 个 | 同上 |
| submitting | ⏳ | 正在提交 | 按钮已 loading 并禁用 | — | 同上 |
| partial / queued | ⚠️ / ⏳ | 业务补充态 | 逐条原因 + 下一步 | 1 个 | 同上（业务需要时） |

**载体硬约束**：状态片段必须**替换**主内容 —— 主内容包在 `[data-normal-view]` 内，
状态片段放在 `#page-root` 内、`[data-normal-view]` 之外。
（阶段 2 的 D-080 曾因状态片段堆在内容之后导致 forbidden 截图与默认态完全相同，本阶段把这条写进规范。）

## 5. 角色差异规格

| 机制 | 原型属性 | 生产实现 |
|---|---|---|
| 入口显隐 | `data-role-visible="academic_director,grade_leader"` | `v-if="hasRole([...])"` + 后端权限注解双重校验 |
| 字段可编辑 | `data-role-editable="academic_director,homeroom"` | 字段级 `:disabled` + 后端字段级校验 |
| 列级隐藏 | `data-col-hide-role="subject_teacher"` | 列配置按角色过滤（不要在模板里写死） |
| 数据范围 | 页头 `.scope-hint` 文案 | 后端数据范围解析（DS-\*），前端只展示 |

## 6. 禁止事项

| 禁止 | 原因 |
|---|---|
| 在原型里引 JS 图表库 | 高保真原型要能直接双击打开、无构建、无网络依赖；图表在原型用纯 CSS，生产用 ECharts |
| 状态只靠颜色区分 | 色弱用户无法分辨；标签必须带文字 |
| 用 `outline: none` 去掉焦点 | 键盘用户失去定位；必须保留 2px 主色焦点环 |
| 弹窗内放表格 | 窄屏会挤成不可读；含表格用独立页（D-059） |
| 未登记的色值 / 字号 / 阴影 | token 是唯一来源；新增先改 `design-tokens.json` |
