# 原型标记契约（markup-contract）

本文件解决一个具体问题：**原型里用 `div` / `a` / `button` 画出来的东西，到生产代码时怎么准确变成 `el-button` 等组件。**

办法是：原型阶段的每一个可交互元素都携带一组 `data-*` 语义标记，标记值直接指向 Element Plus 组件与动作编号。
阶段 6 生成生产代码时，不靠"看样式猜组件"，而是**读标记**。

## 1. 一句话规则

> 原型里的元素，**样式可以是随意的，语义必须是明确的**。
> 任何"点了会有反应"的元素，必须同时具备 `data-role` 与 `data-action-id`（跳转类用 `data-nav`）。

## 2. 通用属性

| 属性 | 必填 | 取值 | 说明 |
|---|---|---|---|
| `data-page` | 页面容器必填 | `PAGE-*` | 挂在页面最外层容器上，取值来自 `navigation.yaml` 的 `pages.id` |
| `data-block` | 区块必填 | 自定义 | 页面内区块，如 `filter` / `toolbar` / `table` / `form` / `footer` |
| `data-role` | 元素必填 | 见第 3 节 | 元素的语义角色 |
| `data-component` | 建议 | 见第 4 节 | 目标 Element Plus 组件（含变体），如 `el-button.primary.plain` |
| `data-action-id` | 动作类必填 | `ACT-*` | 对应 `page-actions.yaml` 的动作编号 |
| `data-nav` | 跳转类必填 | `PAGE-*` | 点击后跳转或打开的页面编号 |
| `data-permission` | 动作类必填 | `资源:操作` | 与 `05-permission-matrix.yaml` 一致 |
| `data-api` | 有接口时必填 | `operationId` | 与 `page-actions.yaml` 的 `api` 一致 |
| `data-field` | 表单与列必填 | 字段名 | 取值来自 `06-field-dictionary.yaml` |
| `data-state` | 状态块必填 | 见第 5 节 | 页面状态 |
| `data-mask` | 敏感字段必填 | `mask` / `full` | 默认掩码还是全量 |
| `data-size` | 浮层必填 | `sm` / `md` / `lg` / `xl` | 与 `layout-spec.yaml` 的枚举一致 |

## 3. `data-role` 取值表

| 取值 | 含义 | 生产端对应 |
|---|---|---|
| `page` | 页面容器 | Vue 页面根节点 |
| `filter` | 筛选区 | `el-form :inline="true"` |
| `field` | 表单字段 | `el-form-item` + 对应输入组件 |
| `toolbar` | 工具条 | `el-card` 的 `#header` |
| `action` | 动作按钮 | `el-button` |
| `row-action` | 行内操作 | `el-table-column` 内的 `link` 按钮 |
| `table` | 表格 | `el-table` |
| `column` | 表格列 | `el-table-column` |
| `detail` | 只读详情区 | `el-descriptions` |
| `section` | 分组区块 | `el-card` 或 `el-divider` 分块 |
| `drawer` | 抽屉 | `el-drawer` |
| `dialog` | 弹窗 | `el-dialog` |
| `wizard` | 向导 | `el-steps` + 内容卡片 |
| `pagination` | 分页 | `Pagination` 组件 |
| `tag` | 状态标签 | `el-tag` |
| `timeline` | 时间线 | `el-timeline` |
| `empty` | 空态 | `el-empty` |
| `error` | 错误态 | 自定义提示块 |
| `forbidden` | 无权限态 | 自定义提示块 |
| `skeleton` | 加载骨架 | `el-skeleton` |
| `nav` | 菜单或跳转链接 | `router-link` 或路由跳转 |

## 4. 元素 → Element Plus 组件映射表

| 原型写法 | `data-component` | 生产代码写法 |
|---|---|---|
| `<button class="btn-primary">新增</button>` | `el-button.primary.plain` | `<el-button type="primary" plain icon="Plus" v-hasPermi="[...]" @click="handleAdd">新增</el-button>` |
| `<button class="btn-danger">删除</button>` | `el-button.danger.plain` | `<el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>` |
| `<a class="link">编辑</a>` | `el-button.link.primary` | `<el-button link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>`，外层套 `el-tooltip` |
| `<input class="input">` | `el-input` | `<el-input v-model="queryParams.studentNo" clearable @keyup.enter="handleQuery" />` |
| `<select class="select">` | `el-select` | `<el-select v-model="queryParams.status" clearable><el-option v-for="d in dict" ... /></el-select>` |
| `<input class="date-range">` | `el-date-picker.daterange` | `<el-date-picker type="daterange" value-format="YYYY-MM-DD" ... />` |
| `<div class="card">` | `el-card.hover` | `<el-card shadow="hover">` |
| `<table>` | `el-table` | `<el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange">` |
| `<th data-field="name">` | `el-table-column` | `<el-table-column prop="studentName" label="姓名" :show-overflow-tooltip="true" />` |
| `<div class="drawer">` | `el-drawer` | `<el-drawer v-model="visible" :size="'640px'" ...>` |
| `<div class="dialog">` | `el-dialog` | `<el-dialog v-model="visible" width="600px" append-to-body>` |
| `<ul class="pager">` | `pagination` | `<pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />` |
| `<span class="badge">在读</span>` | `el-tag.success` | `<el-tag type="success">在读</el-tag>` |
| `<ol class="steps">` | `el-steps` | `<el-steps :active="active" finish-status="success">` |
| `<ul class="timeline">` | `el-timeline` | `<el-timeline><el-timeline-item ... /></el-timeline>` |
| `<div class="empty">` | `el-empty` | `<el-empty description="...">` + 主动作按钮 |

### 4.1 状态标签的颜色映射（与 PRD 枚举对齐）

| 语义 | `data-component` | 示例 |
|---|---|---|
| 正常、在读、成功 | `el-tag.success` | 在读、已完成 |
| 进行中、待处理 | `el-tag.primary` | 执行中、待审批 |
| 警示、可能有问题 | `el-tag.warning` | 转入未报到、部分失败 |
| 终态、停用、失败 | `el-tag.info` | 已转出、毕业、停用 |
| 危险、异常 | `el-tag.danger` | 开除、失败、超限 |

## 5. `data-state` 取值表

每个页面必须为下列状态各给一个可见片段（可以放在设计稿的"状态展示"区，用标签切换）：

| 取值 | 含义 | 对应 `layout-spec.yaml` |
|---|---|---|
| `loading` | 加载中 | `required_states.loading` |
| `empty` | 空数据 | `required_states.empty` |
| `error` | 查询失败 | `required_states.error` |
| `forbidden` | 无权限 | `required_states.forbidden` |
| `submitting` | 提交中 | `required_states.submitting` |
| `partial` | 部分失败（导入 / 升班） | `BR-IMP-016` |
| `queued` | 排队中 | `FD-async_task_status` |

## 6. 与生产代码的对应链

```
原型元素 data-action-id="ACT-STU-001"
        ↓ 查 page-actions.yaml
动作记录：permission = academic_director / person.student / create
        ↓ 查 navigation.yaml + 详细设计的接口清单
operationId = addStudent
        ↓ 查 OpenAPI 与前端 api 层
apps/plus-ui/src/api/edu/student/index.ts 中的 addStudent()
        ↓
apps/plus-ui/src/views/edu/student/index.vue 中的 handleAdd()
```

四个环节缺一不可。阶段 6 的自查方式是：**从原型里抽出全部 `data-action-id`，逐个回溯到 API 函数与页面处理函数；找不到的即为缺项。**

## 7. 不允许出现的写法

| 反例 | 为什么不行 | 正确做法 |
|---|---|---|
| `<a href="#">保存</a>` | 用链接表达动作，映射时无法判断是按钮还是跳转 | `<button data-role="action" data-action-id="ACT-...">` |
| `<div onclick="save()">保存</div>` | 无语义、无权限、无接口标记 | 同上一行，并补 `data-permission` / `data-api` |
| `<button>` 不写 `data-action-id` | 无法回溯到动作清单 | 必须登记动作后再写原型 |
| 用不同样式表达同一语义（有时蓝按钮有时绿按钮表示"新增"） | 阶段 6 组件选型会漂移 | 统一用 `layout-spec.yaml` 的工具栏按钮规则 |
| 用纯文本展示只读字段 | 与可编辑字段难以区分 | 用 `data-role="field"` + `disabled` 形态 |
| 一个 `data-action-id` 出现在多个语义不同的元素上 | 动作编号失去唯一性 | 一个编号只对应一个动作 |
| 掩码字段不加 `data-mask` | 阶段 6 会漏掉脱敏 | 敏感字段加 `data-mask="mask"` |

## 8. 示例片段

### 8.1 列表页（节选）

```html
<div data-page="PAGE-STU-LIST">
  <section data-block="filter" data-role="filter" data-component="el-card.hover">
    <form data-role="field">
      <label for="studentNo">学号</label>
      <input id="studentNo" data-role="field" data-field="student_no" data-component="el-input" />
    </form>
    <button data-role="action" data-action-id="ACT-COM-001" data-component="el-button.primary" data-permission="person.student:read">搜索</button>
    <button data-role="action" data-action-id="ACT-COM-002" data-component="el-button" >重置</button>
  </section>

  <section data-block="toolbar" data-role="toolbar" data-component="el-card.hover">
    <button data-role="action" data-action-id="ACT-STU-001" data-component="el-button.primary.plain" data-permission="person.student:create" data-nav="PAGE-STU-CREATE">新增</button>
    <button data-role="action" data-action-id="ACT-STU-002" data-component="el-button.primary.plain" data-permission="data.import:import" data-nav="PAGE-STU-IMPORT">导入</button>
    <button data-role="action" data-action-id="ACT-STU-003" data-component="el-button" data-permission="data.export:export" data-api="exportStudent">导出</button>
  </section>

  <table data-role="table" data-component="el-table" data-api="listStudent">
    <th data-role="column" data-field="student_no">学号</th>
    <th data-role="column" data-field="student_name">姓名</th>
  </table>

  <div data-role="empty" data-state="empty" data-component="el-empty">当前筛选条件下没有学生</div>
  <div data-role="pagination" data-component="pagination"></div>
</div>
```

### 8.2 抽屉（节选）

```html
<aside data-role="drawer" data-component="el-drawer" data-size="md" data-page="PAGE-STU-CREATE" data-block="form">
  <form data-role="field">
    <div data-role="field" data-field="student_name">
      <label>姓名 <span class="required">*</span></label>
      <input data-component="el-input" data-field="student_name" />
    </div>
    <div data-role="field" data-field="enrollment_status">
      <label>学籍状态</label>
      <select data-component="el-select" data-field="enrollment_status" disabled></select>
    </div>
  </form>
  <footer data-block="footer">
    <button data-role="action" data-action-id="ACT-STU-014" data-component="el-button">取消</button>
    <button data-role="action" data-action-id="ACT-STU-013" data-component="el-button.primary" data-api="addStudent">保存</button>
  </footer>
</aside>
```

## 9. 交付前自查

1. 每个 `.html` 的页面根节点有 `data-page`，且该编号在 `navigation.yaml` 中存在
2. 每个可点元素有 `data-role` 与 `data-action-id`（跳转类有 `data-nav`）
3. 每个 `data-action-id` 在 `page-actions.yaml` 中能查到，且 `page` 字段与本页面一致
4. 每个 `data-permission` 都能在 `05-permission-matrix.yaml` 中找到
5. 每个 `data-field` 都能在 `06-field-dictionary.yaml` 中找到
6. 每个页面都给出第 5 节要求的全部状态片段
7. 不存在第 7 节列出的反例写法

## 10. 原型可交互约定

原型必须能**点**。静态截图不算原型——演示者要能在 2 分钟内点通主流程，不能靠翻图片。

### 10.1 演示脚本必须实现的五件事

每个原型页面底部自带一段内联 `<script>`（不引第三方库、不需构建），实现：

1. **角色切换**：切换后按 `data-role-visible` 显示或隐藏元素，同一页面演示多种角色形态
2. **状态切换**：在 `normal / loading / empty / error / forbidden / submitting` 之间切换，用于验收状态覆盖
3. **打开浮层**：点击带 `data-nav` 的元素，显示同页内 `data-demo-panel` 对应的浮层片段
4. **模拟提交**：点击带 `data-api` 的动作，进入 1 秒 loading，再按 `data-demo-outcome` 给出对应反馈
5. **表单校验**：必填项为空时给出字段级错误并阻止提交

### 10.2 演示专用属性

| 属性 | 取值 | 说明 |
|---|---|---|
| `data-demo` | `role-switcher` / `state-switcher` / `panel` / `toast` | 演示控件与演示容器 |
| `data-demo-panel` | `PAGE-*` | 被打开的浮层片段编号 |
| `data-demo-outcome` | `success` / `error` / `partial` / `validation` | 模拟提交的结果 |
| `data-role-visible` | 角色 code，逗号分隔 | 该元素对哪些角色可见；不写表示所有角色可见 |
| `data-empty-source` | `filter` / `scope` | 空态原因：筛选无结果 / 无数据范围 |
| `data-request-id` | 形如 `REQ-20260930-000123` | 错误态展示用的请求标识 |

### 10.3 脚本约定

- 内联在页面底部，最后一行注释 `<!-- demo script end -->`
- **不调用后端**；提交只做本地模拟，不产生真实数据
- 页面之间用相对路径真实跳转（如 `pages/student-list.html`）
- 演示控件统一放在页面右上角的浮动面板里，按钮文案用中文，标注"仅原型演示"

### 10.4 验收方式

打开 `index.html`，2 分钟内可按下列路径点通：

```
登录 → 学生管理列表 → 新增学生 → 保存成功 → 回到列表
      → 点击行打开详情 → 学籍异动 → 关闭 → 返回列表（筛选条件保留）
```

## 11. 反例补充（第一组见第 7 节）

| 反例 | 为什么不行 | 正确做法 |
|---|---|---|
| 交互只有 hover，没有 click | 无法演示流程，只能看图 | 按第 10 节实现点击行为 |
| 用浏览器 `alert()` 当成功提示 | 与真实系统反馈形态不一致，验收者无法判断最终样式 | 用页面内提示条或 `el-message` 形态的浮层 |
| 角色差异只写一句"（无权限）" | 差异没有落在按钮与字段上，等于没画 | 按 `page-actions.yaml` 的权限逐项显隐 |
| 状态靠注释掉 HTML 再截图 | 无法自动验收，交付物不自洽 | 用 `data-demo="state-switcher"` 真实切换 |
| 列表页只画 3 行数据 | 看不出列宽、省略、分页的真实表现 | 不少于 10 行，并显示总条数 |
