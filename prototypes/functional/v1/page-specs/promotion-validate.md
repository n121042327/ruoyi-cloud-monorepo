# 升班校验结果（升班向导第三步）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-VALIDATE`（取自 `navigation.yaml`） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | wizard（向导第三步，独立页） |
| 骨架模板 | `TPL-WIZARD` |
| 所属批次 | 2-3e-s4 |
| 上游需求 | `REQ-PRM-022` ~ `REQ-PRM-026` |
| 上游规则 | `BR-PROMO-002`、`BR-CLASS-005`（容量只警告）、`BR-PROMO-006` |
| 权限资源 | `promotion.batch` 的 `read` / `update` |
| 数据范围 | `DS-04` / `DS-05`（只读）/ `DS-01`（只读）/ `platform` |
| 原型文件 | `pages/promotion-validate.html` |
| 交付证据 | `evidence/stage2-prototype/verify-promotion-wizard.html`、`promotion-validate_*.png` |

## 1. 页面目的

教务主任在执行前确认三个维度的结论：**通过 / 警告 / 错误**（`REQ-PRM-023`），警告不阻塞（容量超限只提示，`REQ-PRM-024`），
错误必须先修正或标记跳过（`REQ-PRM-025`）；结论可下钻到具体学生（`REQ-PRM-026`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | `el-page-header` 等价物 | 标题 + 数据范围 + 「向导 3 / 4」+ 任务编号 + 错误项标签 |
| 2 | 步骤条 | `steps` | `el-steps` | 第 1 ~ 2 步已完成可点，第 4 步可点 |
| 3 | 校验结论 | `summary` | 4 个数值卡 + 2 个 `el-alert` | 通过 / 警告 / 错误 / 已标记跳过；错误阻塞说明（含「回到预览修正」）；容量只警告说明 |
| 4 | 下钻明细 | `detail` | chips + `el-table` | 按级别过滤；列：学号 / 姓名 / 源班级 / 目标班级 / 校验级别 / 校验说明 / 操作（标记跳过） |
| 5 | 底部操作条 | `footer` | sticky 操作条 | 上一步 + 确认执行（异步） |
| 6 | 状态片段 | — | 状态块 | 加载中 / 空数据 / 校验失败 / 无权限 / 提交中 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `student_no` / `student_name` | 学号 / 姓名 | 只读单元格 | — | — | — | — | — |
| `source_class_id` | 源班级 | 只读单元格 | — | — | — | — | — |
| `target_class_id` | 目标班级 / 去向 | 只读单元格 | — | — | — | — | 留级显示「同学段同名年级」，毕业显示「—」 |
| `status`（校验级别） | 校验级别 | `el-tag` | — | — | 按校验结果 | 通过 / 警告 / 错误（`promotion_validation_level`） | `CR-014` 登记 |
| `error_msg` | 校验说明 | 只读单元格 | — | — | — | — | 错误项写明修正方式 |
| `result_type` | 处理方式（隐式） | 由「标记跳过」写入 | — | 教务主任 | — | 置 `skip` | `REQ-PRM-025` / `REQ-PRM-017` |

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-PRM-029` | 上一步 / 回到预览修正 | 点击 | `update` | 回 `PAGE-PRM-PREVIEW` | — |
| `ACT-PRM-030` | 重新校验 | 点击 | `update` | 刷新三分类结论 | `validatePromotionTask` |
| `ACT-PRM-031` | 按维度下钻 chips | 点击 | `read` | 明细按级别过滤 | — |
| `ACT-PRM-032` | 行内「标记跳过」 | 点击 | `update` 且该行非「通过」 | `result_type=skip` 放行错误项 | `updatePromotionItem` |
| `ACT-PRM-033` | 确认执行（异步） | 点击 | 无错误项 | 异步执行并返回任务编号 | `executePromotionTask` |
| `ACT-PRM-043` | 返回任务列表（无权限态） | 点击 | `read` | 回 `PAGE-PRM-LIST` | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 正常 | `normal` | 通过 4~5 / 警告 1 / 错误 1（按角色收窄） | 标记跳过 / 确认执行 |
| 加载中 | `loading` | 校验骨架行 | — |
| 空数据 | `empty` | 没有可校验的明细 | 回到预览与调整 |
| 校验失败 | `error` | 请求编号 + 错误码 + 重试校验；写明不会以跳过校验的方式继续 | 重试校验 |
| 无权限 | `forbidden` | 缺 `read`，不降级为全量 | 返回任务列表 |
| 提交中 | `submitting` | 按钮 loading 并禁用 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 上一步 / 回到预览修正 | `PAGE-PRM-PREVIEW` | 跳页 | 调整结果保留 |
| 确认执行 | `PAGE-PRM-EXECUTE` | 跳页（异步任务已受理） | 可从列表回到进度页 |
| 步骤条第 1 / 2 / 4 步 | `PAGE-PRM-CREATE` / `PREVIEW` / `EXECUTE` | 跳页 | — |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任 | 7 条明细 + 三分类统计 | 重新校验、标记跳过、确认执行、上一步 | — |
| 超级管理员 | 全部 | 全部 | 强制留痕 |
| 校领导 / 年级主任 | 本校 / 本年级明细（只读） | 无写入口 | 年级主任只看到 5 条 |
| 平台运营 | 只读 | 无 | `DS-01` 留痕 |
| 班主任 / 租户管理员 | 无 | 无 | 无权限面板 |

## 8. 样例数据

`content-samples.json` 的 `promotion_preview.rows` 与任务 `PRM-20261001-0022`：
通过 5（王梓萱、李俊逸、赵一诺、吴雨桐、马嘉懿）、警告 1（孙悠然：目标班级 46 / 45 超容量）、错误 1（朱书瑶：目标班级不存在）。

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-WIZARD`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-PRM-029` ~ `033`、`043`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [x] 五类状态齐全；错误项存在时「确认执行」被拦并给出原因
- [x] 样例数据取自 `content-samples.json`
