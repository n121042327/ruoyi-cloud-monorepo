# 页面规格模板

复制本文件为 `page-specs/<页面编号小写>.md`，逐项填写。
**填不出来的项不要留空，写"待确认"并按 `docs/00-governance/change-control.md` 记录缺项。**

---

# <页面中文名>

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-XXX-YYY`（取自 `navigation.yaml`） |
| 所属模块 | <模块名>（`<module>`） |
| 页面类型 | page / detail / drawer / dialog / wizard / block |
| 骨架模板 | `TPL-LIST` / `TPL-DETAIL` / `TPL-WIZARD` / `TPL-OVERLAY` |
| 所属批次 | <批次号> |
| 上游需求 | `REQ-XXX-001` ~ `REQ-XXX-0NN`（PRD 中的需求编号） |
| 上游规则 | `BR-XXX-001`、`BR-XXX-002` … |
| 权限资源 | `<resource>` 的 `<action>`（取自 `05-permission-matrix.yaml`） |
| 数据范围 | `DS-0X`（说明该页面按哪个范围过滤） |
| 原型文件 | `pages/<file>.html` |

## 1. 页面目的

用两三句话说明：谁在什么情况下打开它、要完成什么、完成后去哪里。

## 2. 页面结构

按自上而下顺序列出区块，并注明每块用的组件：

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 搜索区 | `filter` | `el-card` + `el-form inline` | |
| 2 | 工具条 | `toolbar` | `el-card#header` | |
| 3 | 表格 | `table` | `el-table` | |
| 4 | 分页 | `pagination` | `Pagination` | |

## 3. 字段清单

每个字段都要能对应到 `06-field-dictionary.yaml`：

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| | | | | | | | |

## 4. 动作清单

每个动作必须已在 `page-actions.yaml` 登记：

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-XXX-001` | | | | | |

## 5. 状态清单

按 `markup-contract.md` 第 5 节，逐条给出可见形态：

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | | |
| 空数据 | `empty` | | |
| 查询失败 | `error` | | |
| 无权限 | `forbidden` | | |
| 提交中 | `submitting` | | |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| | | 抽屉 / 弹窗 / 跳页 | |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任 | | | |
| 班主任 | | | |
| 任课教师 | | | |
| 平台运营 | | | |

## 8. 样例数据

说明本页用到 `content-samples.json` 里的哪些节点，以及选择它们的理由
（例如"用 2026000003 演示休学状态，因为它保留了行政班关系但不计入在读"）。

## 9. 自查

- [ ] 页面骨架属于四种模板之一
- [ ] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [ ] 每个 `data-action-id` 已在 `page-actions.yaml` 登记
- [ ] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [ ] 五类状态齐全
- [ ] 1366×768 与 1920×1080 下未出现横向滚动条与元素重叠
- [ ] 样例数据取自 `content-samples.json`，未出现占位人名
