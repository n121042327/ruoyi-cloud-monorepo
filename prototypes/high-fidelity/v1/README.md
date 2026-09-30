# 高保真原型 v1（阶段 3）

本目录是**独立于阶段 2 的高保真原型**：不改 `prototypes/functional/v1/` 的任何文件，
只把阶段 2 已确认的信息架构、字段、动作与状态片段按视觉规范精修一遍。

## 1. 回答什么问题

| 目录 | 回答的问题 |
|---|---|
| `prototypes/functional/v1`（阶段 2） | 长什么样、怎么操作、页面怎么跳 |
| `prototypes/high-fidelity/v1`（阶段 3，本目录） | **最终长什么样**：色彩、字体、间距、组件状态、密度、空态与错误态的具体形态 |

阶段 3 的产物是阶段 6 生产前端的视觉基准；`visual-spec.yaml` 的 token 会被搬进 `apps/plus-ui` 的设计 token。

## 2. 目录结构

```
prototypes/high-fidelity/v1/
  README.md              # 本文件
  index.html             # 入口：已交付页面清单 + 视觉规范摘要 + 演示路径
  design-tokens.json     # 视觉 token（机器可读，权威值；阶段 6 搬进 apps/plus-ui）
  component-spec.md      # 组件规格（变体 / 尺寸 / 状态 / Element Plus 映射）
  component-mapping.yaml # 组件映射表（每个可交互元素 → Element Plus 组件与属性）
  visual-checklist.md    # 交批自查清单（32 条，含三档分辨率与可访问性）
  visual-spec.yaml       # 视觉规范的人类可读摘要（与 design-tokens.json 同源）
  assets/hifi.css        # 高保真样式（token 落地；组件命名与阶段 2 一致）
  assets/hifi-shell.js   # 高保真外壳（侧边菜单 / 顶部导航 / 页签 / 演示面板 / 状态与角色引擎）
  pages/*.html           # 页面
```

## 3. 与阶段 2 的关系（三条硬约束）

1. **不改结构**：列清单、字段、筛选项、行内动作与阶段 2 一一对应；只允许视觉增强。
   由 `evidence/stage3-highfidelity/verify-hifi-student.html` 的 `HF-17` 看住（列顺序逐项比对）。
2. **不改阶段 2 的目录**：阶段 2 的文件保持原样，阶段 3 全部是新增文件。
3. **沿用同一套 data-\* 约定**：`data-action-id` / `data-field` / `data-demo-panel` / `data-demo-state-panel` /
   `data-normal-view` / `data-role-visible` / `data-role-editable` / `data-nav` / `data-overlay` 与阶段 2 同名同义，
   阶段 6 可以用同一份 `markup-contract.md` 映射表把两套原型一起替换成 Element Plus 组件。

## 4. 视觉规范摘要（完整见 `visual-spec.yaml`）

| 项 | 值 |
|---|---|
| 主色 | `#2f6bff`（hover `#1f5bf0`、active `#1a4fd6`、浅底 `#ecf2ff`） |
| 语义色 | 成功 `#0f9d58`、警告 `#e6a23c`、危险 `#d93026`、信息 `#6b7280` |
| 中性色 | 正文 `#1f2937`、常规 `#4b5563`、次级 `#8a94a6`、边框 `#dcdfe6` / `#ebeef5`、页面底 `#f3f5f9` |
| 字体 | 系统字体栈；数字与 ID 用等宽字体 |
| 字号 | 页标题 20/600、卡片标题 15/600、正文 14、说明 12、指标 26/600 |
| 间距 | 4 的倍数；页面 24、卡片 20、卡片间距 16、表单项 20 |
| 圆角 | 小 4、基础 8、大 12 |
| 阴影 | 卡片 `0 1px 2px rgba(31,41,55,.04), 0 4px 12px rgba(31,41,55,.06)` |
| 动效 | 150–180ms，`cubic-bezier(.4,0,.2,1)`，只用于 hover / focus / 展开 |
| 视口 | 主 1440×900、次 1920×1080、最低 1366×768（不出现整页横向滚动） |

## 5. 演示方式

```text
打开 index.html → 点「学生管理」进入样板页
右下角「高保真演示」面板：
  角色 8 种（超管 / 租户管理员 / 教务主任 / 班主任 / 年级主任 / 任课教师 / 校领导 / 平台运营）
  状态 8 种（正常 / 加载中 / 空数据 / 查询失败 / 无权限 / 提交中 / 部分失败 / 排队中）
深链接：#role=school_leader  #state=forbidden  #panel=PAGE-STU-DETAIL
```

## 6. 交付口径

阶段 3 按批推进，每批 ≤ 3 个页面并暂停验收。批次编号沿用 `docs/00-governance/stage-inputs.yaml` 的登记
（**3-0 先出规范，3-1 出样板**，与最初的计划一致，未自造批次编号）：

| 批次 | 内容 | 状态 |
|---|---|---|
| 3-0 | `design-tokens.json` + `component-spec.md` + `component-mapping.yaml` + `visual-checklist.md`（+ 人类可读摘要 `visual-spec.yaml`） | 已产出待验收 |
| 3-1 | 学生管理列表 + 详情抽屉 + 新增 / 编辑弹窗（样板）+ 高保真外壳 | 已产出待验收 |
| 3-2 | 教师 / 年级 | 待开始 |
| 3-3 | 班级 / 班级详情 / 升班向导 | 待开始 |
| 3-4 | 导入向导 / 登录 / 异常页 | 待开始 |
| 3-5 ~ 3-9 | 学生模块剩余 / 学校与配置 / 选科与教学班 / 审计 / 异步任务（与阶段 2 的 2-5 ~ 2-9 对齐） | 待开始 |

验收依据：`evidence/stage3-highfidelity/verify-hifi-student.html`（18 条断言）与同目录截图。
样板通过后再按同一标准铺开其余批次。
