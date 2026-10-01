# AGENTS.md — 前端（Vue 3 + TypeScript + Element Plus）

本文件用于 `apps/plus-ui/`。根 `AGENTS.md` 的第 3、4、8 节不可放宽。

## 1. 技术栈（以 `package.json` 实际版本为准）

Vue 3.5 + TypeScript 5.9 + Vite 7 + Pinia 3 + Vue Router 5 + Element Plus 2.13 +
UnoCSS 66 + Sass + Axios + `@vueuse/core` + `pnpm`。

禁止引入未在 `docs/00-governance/stack-lock.md` 登记的 UI 库、状态库或请求库。
需要新增依赖时，先说明理由、体积影响与替代方案，取得确认后再加。

## 2. 目录约定

新增教育模块代码按以下位置落盘：

| 内容 | 路径 |
|---|---|
| 接口请求 | `src/api/edu/<module>/index.ts` |
| 接口类型 | `src/api/edu/<module>/types.ts` |
| 领域类型 | `src/types/edu/**` |
| 页面 | `src/views/edu/<module>/index.vue` |
| 页面级组件 | `src/views/edu/<module>/components/*.vue` |
| 页面逻辑 | `src/views/edu/<module>/composables/*.ts` |
| 跨模块业务组件 | `src/components/Edu/**` |
| 单元测试 | 与被测文件同级的 `__tests__/**` |

不要往 `src/views/system/**` 里加教育页面；不要复用 system 模块的 api 目录。

## 3. 组件映射（对应 GAP-003）

生产代码的交互控件必须来自 Element Plus，且与高保真原型的
`prototypes/high-fidelity/v1/component-mapping.yaml` 逐条对应。

| 原型元素 | 生产组件 |
|---|---|
| 主操作按钮 | `<el-button type="primary">` |
| 普通操作 | `<el-button>` |
| 危险操作 | `<el-button type="danger">` |
| 文字链接操作 | `<el-button link type="primary">` |
| 下拉动作集合 | `<el-dropdown>` + `<el-dropdown-menu>` |
| 输入框 | `<el-input>` |
| 数字输入 | `<el-input-number>` |
| 下拉选择 | `<el-select>` + `<el-option>` |
| 单选组 | `<el-radio-group>` + `<el-radio>` |
| 多选组 | `<el-checkbox-group>` + `<el-checkbox>` |
| 日期 | `<el-date-picker>` |
| 开关 | `<el-switch>` |
| 级联（学校 → 年级 → 班级） | `<el-cascader>` |
| 树形选择（年级、部门） | `<el-tree-select>` |
| 列表 | `<el-table>` + `<el-table-column>` |
| 分页 | `<pagination>`（项目内既有组件） |
| 弹窗 | `<el-dialog>` |
| 抽屉表单 | `<el-drawer>` |
| 标签页 | `<el-tabs>` |
| 标签 / 状态徽标 | `<el-tag>` / `<el-badge>` |
| 提示 | `<el-tooltip>` |
| 气泡确认 | `<el-popconfirm>` |
| 全局提示 | `ElMessage` / `ElMessageBox` / `ElNotification` |
| 表单校验 | `<el-form>` + `rules` |
| 空态 | `<el-empty>` |
| 加载 | `v-loading` |
| 步骤条（升班向导、导入向导） | `<el-steps>` + `<el-step>` |
| 描述列表（详情） | `<el-descriptions>` |

规则：

1. 禁止用裸 `<div>`、`<a>`、`<span>` 承载可点击的主交互。若有例外，必须在映射文件中声明并说明原因。
2. 一个页面只允许一个 `type="primary"` 的主操作按钮。
3. 批量操作的入口统一放在表格工具栏，不放在行内。
4. 危险操作（删除、退学、毕业）必须二次确认，且文案包含对象数量。

## 4. 代码写法

- 单文件组件统一 `<script setup lang="ts">`，禁止 Options API
- 页面逻辑抽到 composable，`.vue` 中只保留模板与绑定
- 请求只在 `src/api/edu/**` 中定义，页面不直接使用 `axios`
- 组件间数据流用 props + emits，不通过修改 props 反向通信
- 枚举与常量集中定义，禁止在模板里写魔法字符串
- 提交前必须通过类型检查与 ESLint

## 5. 请求与响应约定

列表接口：`TableDataInfo<T>`，形状 `{ code, msg, rows, total }`
详情与操作接口：`R<T>`，形状 `{ code, msg, data }`

后端对 `Long` 有特殊序列化配置，**所有 ID 一律按字符串处理**：

```ts
// 正确
interface StudentVO {
  studentId: string;
  classId: string;
}

// 禁止
const id = Number(row.studentId);
```

## 6. 权限

- 按钮级权限使用项目既有的 `v-hasPermi` 指令
- 数据范围由后端决定，前端不得通过隐藏按钮代替鉴权
- 页面必须处理无权限状态：不显示入口，而不是显示报错

## 7. 列表页统一结构

首轮所有列表页按同一骨架实现，保证体验一致：

```
搜索区（可折叠）→ 工具栏（新增 / 批量 / 导入 / 导出 / 列设置）→ 表格 → 分页
```

必备状态：加载中、空数据、查询无结果、请求失败、无权限。

## 8. 视觉规范

视觉基线 = Element Plus 设计规范 + 现有 `apps/plus-ui` 既有风格，美观度在其之上。

具体取值由 `prototypes/high-fidelity/v1/design-tokens.json` 冻结，包括：

- 主色与语义色（成功 / 警告 / 危险 / 信息）
- 中性色阶（文字、边框、分隔、背景）
- 字号阶梯、行高、字重
- 间距阶梯（4 / 8 / 12 / 16 / 24 / 32）
- 圆角、阴影、层级

实现要求：

1. 颜色、间距、字号不硬编码，统一走样式变量
2. 列表页与表单页的信息密度、控件高度、对齐方式全模块一致
3. 表格行高、表头样式、分页位置在全部教育页面统一

## 9. 禁止事项

- 不在 `.vue` 中直接拼接 URL
- 不在组件里硬编码颜色与间距，取值必须来自 design-tokens 对应的样式变量
- 不使用 `any`，必要时用 `unknown` 再做类型收窄
- 不引入与 Element Plus 重复功能的组件库
- 不擅自升级依赖版本

## 页面字段布局约束

生成或修改页面、表单、表格、列表、详情和查询条件，必须遵守 `docs/00-governance/page-field-layout.md`。语义分组与依赖顺序优先；必填优先仅作建议。冻结版本通过新版本变更，逐批人工验收。
