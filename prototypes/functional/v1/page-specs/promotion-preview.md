# 升班预览与调整（升班向导第二步）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-PREVIEW`（取自 `navigation.yaml`） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | wizard（向导第二步，独立页，双栏） |
| 骨架模板 | `TPL-WIZARD`（内容区用 `split` 双栏） |
| 所属批次 | 2-3e-s3 |
| 上游需求 | `REQ-PRM-013` ~ `REQ-PRM-021` |
| 上游规则 | `BR-PROMO-001`（按学年追加）、`BR-PROMO-002`、`BR-PROMO-004`（默认同序号 +1）、`BR-PROMO-005`（毕业）、`BR-PROMO-006`（留级去向）、`BR-STU-012`（在读口径） |
| 权限资源 | `promotion.batch` 的 `read` / `update`；导出用 `data.export:export` |
| 数据范围 | 教务主任 / 校领导 `DS-04`；年级主任 `DS-05`（只读，只看本人负责年级的明细行）；平台运营 `DS-01`（只读）；超级管理员 `platform` |
| 原型文件 | `pages/promotion-preview.html`（含同页片段 `PAGE-PRM-ADJUST`） |
| 交付证据 | `evidence/stage2-prototype/verify-promotion-wizard.html`、`evidence/stage2-prototype/promotion-preview_*.png` |

## 1. 页面目的

教务主任在生成预览后核对每个学生的去向：左栏按源班级分组（`REQ-PRM-013`），可对某个源班级整体指定目标班级（`REQ-PRM-018`）；
右栏逐个学生看结果类型与目标班级，需要时点行内「调整」改留级 / 转班 / 毕业 / 跳过（`REQ-PRM-017`）。
预览不写入任何学生数据（`REQ-PRM-020`），只生成任务明细；完成后任务状态转为「已预览待确认」，可重新预览覆盖旧明细（`REQ-PRM-021`）。
年级主任与校领导只读：能看本范围内的去向、能导出核对，但没有调整与批量入口（`CR-012`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 页头 | — | `el-page-header` 等价物 | 标题 + 数据范围 + 「向导 2 / 4」+ 任务编号与状态迁移 |
| 2 | 步骤条 | `steps` | `el-steps` | 第 1 步已完成且可点返回，第 3 / 4 步可点（未到步骤由跳转确认） |
| 3 | 任务信息 | `task` | 5 个数值卡 + `el-alert` | 任务编号 / 源学期 / 目标学期 / 参与人数 / 源班级数与目标班级数 + 预览口径 |
| 4 | 双栏预览 | `preview` | `split`（左侧 `split-aside` 列表 + 右侧 `el-table`） | 左：源班级（在读人数、目标班级 select、应用到本班）；右：逐学生明细（学号 / 姓名 / 源班级 / 结果类型 / 目标班级 / 明细状态 / 操作） |
| 5 | 底部操作条 | `footer` | sticky 操作条 | 上一步（回 `PAGE-PRM-CREATE`）+ 下一步：升班校验 |
| 6 | 调整弹窗 | `dialog` | `el-dialog`（md） | `PAGE-PRM-ADJUST`：处理方式 / 目标班级 / 调整原因 |
| 7 | 状态片段 | — | 状态块 | 加载中 / 空数据 / 查询失败 / 无权限 / 提交中 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `student_no` | 学号 | 只读单元格 | — | — | 来自明细 | — | 平台级学生主体，不随升班变化 |
| `student_name` | 姓名 | 只读单元格 | — | — | — | — | — |
| `source_class_id` | 源班级 | 只读单元格 | — | — | — | — | 预览按它分组（`REQ-PRM-013`） |
| `target_class_id` | 目标班级 | `el-select`（左栏批量 + 弹窗内单选） | 升级 / 转班时必填 | 教务主任 / 超级管理员 | 默认同学段序号 +1 | 必须属于目标学年学期（`REQ-PRM-022`）；已停用班级不可选 | 留级 / 毕业 / 跳过时为空 |
| `result_type` | 处理方式 / 结果类型 | `el-radio-group`（弹窗内） | 是 | 教务主任 / 超级管理员 | `upgrade` | 五选一：升级 / 留级 / 转班 / 毕业 / 跳过 | 枚举 `promotion_result_type`（`CR-014`） |
| `status` | 明细状态 | 只读单元格 | — | — | `pending` | — | 枚举 `promotion_item_status`；执行后为成功 / 失败 / 跳过 |
| `remark` | 调整原因 | `el-input.textarea` | 留级 / 跳过时必填 | 教务主任 / 超级管理员 | 空 | 最长 500 字 | 写入审计（`REQ-PRM-058`） |

## 4. 动作清单

全部动作已在 `page-actions.yaml` 的 `promotion_preview`（`ACT-PRM-020` ~ `026`）与 `promotion_adjust`（`ACT-PRM-027` / `028`）登记。

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-PRM-020` | 上一步：选择学年学期 | 点击 | `update` | 回 `PAGE-PRM-CREATE` | — |
| `ACT-PRM-021` | 重新预览 | 点击 | `update` | 覆盖旧明细，状态回「已预览待确认」 | `previewPromotionTask` |
| `ACT-PRM-022` | 导出预览结果 | 点击 | `data.export:export` | 导出预览清单供线下核对（`REQ-PRM-019`） | `exportPromotionPreview` |
| `ACT-PRM-023` | 应用到本班 | 点击 | `update` | 该源班级全部明细统一写目标班级（`REQ-PRM-018`） | `batchUpdatePromotionItem` |
| `ACT-PRM-024` | 行内「调整」 | 点击 | `update` | 打开 `PAGE-PRM-ADJUST` 并带入该学生 | — |
| `ACT-PRM-025` | 下一步：升班校验 | 点击 | `update` | 进入 `PAGE-PRM-VALIDATE` | `validatePromotionTask` |
| `ACT-PRM-026` | 左栏源班级行 | 点击 | `read` | 右栏只显示该班明细（再点回到全部） | — |
| `ACT-PRM-027` | 调整弹窗「取消」 | 点击 | 始终可用 | 关闭片段，不写入 | — |
| `ACT-PRM-028` | 调整弹窗「保存调整」 | 点击 | 升级 / 转班需目标班级；留级 / 跳过需原因 | 只写明细字段，不改学生数据 | `updatePromotionItem` |
| `ACT-PRM-043` | 返回任务列表（错误 / 无权限态） | 点击 | `read` | 回 `PAGE-PRM-LIST` | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 正常 | `normal` | 7 行明细（教务主任 / 校领导 / 平台运营）或 5 行（年级主任 `DS-05`）；左栏 5 个源班级 | 调整 / 下一步 |
| 加载中 | `loading` | 预览生成骨架行 | — |
| 空数据 | `empty` | 「该学年没有可升班的学生」+ 写明只有在读计入 | 返回上一步调整范围 |
| 查询失败 | `error` | 请求编号 + 错误码 + 明说未写入学生数据 + 重新预览 | 重新预览 |
| 无权限 | `forbidden` | 缺 `promotion.batch:read`；不降级为全量、不渲染明细行 | 返回任务列表（只读） |
| 提交中 | `submitting` | 按钮 loading 并禁用 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 上一步 | `PAGE-PRM-CREATE` | 跳页 | 明细保留，重新预览才覆盖 |
| 下一步：升班校验 | `PAGE-PRM-VALIDATE` | 跳页 | 返回时保留调整结果 |
| 行内「调整」 | `PAGE-PRM-ADJUST` | 同页弹窗 | 关闭后回到本页，筛选与左栏选中保留 |
| 步骤条第 3 / 4 步 | `PAGE-PRM-VALIDATE` / `PAGE-PRM-EXECUTE` | 跳页 | — |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任 | 全部 7 行 + 5 个源班级 | 导出、重新预览、应用到本班、行内调整、下一步 | 全部可写 |
| 超级管理员 | 全部 | 全部 | 强制留痕（`BR-ORG-014`） |
| 校领导 | 全部 7 行（只读） | 导出 | 无调整入口，操作列显示「只读」 |
| 年级主任 | 本年级 5 行（只读） | 导出 | `DS-05`；提示写明不能调整（`CR-012`） |
| 平台运营 | 全部（只读） | 导出（需授权） | `DS-01` 只读并留痕 |
| 班主任 / 租户管理员 | 无 | 无 | 无 `promotion.batch:read`，进入无权限态 |

## 8. 样例数据

取自 `content-samples.json` 的 `promotion_preview`：任务 `PRM-20261001-0022`、5 个源班级（在读 2 / 2 / 1 / 1 / 1）、7 行明细
（升级 4：王梓萱、李俊逸、朱书瑶、孙悠然；转班 1：赵一诺；留级 1：吴雨桐；毕业 1：马嘉懿）；
休学（陈思远）与出国保留学籍（徐昊然）在左栏以「不参与」提示出现，不占明细行。

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-WIZARD` + `split` 
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）；动态渲染的行内「调整」同样带标记
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-PRM-020` ~ `028`、`043`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（含 `CR-014` 补的 `result_type` / `target_class_id` / `status`）
- [x] 五类状态齐全
- [x] 1366×768 下无整页横向滚动；表格列宽之和 1000 = `min-width`
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
