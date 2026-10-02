# 升班与学籍异动 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-PRM-*`（见 `docs/10-prd/modules/promotion/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：升班任务（`edu_promotion_task` / `edu_promotion_item`，预览 / 执行 / 重试 / 取消）、
学籍异动（`edu_enrollment_change`）、跨校转学单（`edu_transfer_order`）。

不负责：

- 年级定义与学段序号（年级管理）
- 班级与成员关系的表结构（班级管理）；升班写入的是班级模块的表，写入动作由本模块的
  升班任务执行器发起（唯一执行入口，`DP-01`）
- 学生主体字段维护（学生管理）

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_enrollment_change` | 学籍异动记录 | `school` | 5-2 | 13 | 0 | 1 |
| `edu_promotion_item` | 升班明细 | `school` | 5-2 | 9 | 1 | 2 |
| `edu_promotion_task` | 升班任务 | `school` | 5-2 | 14 | 1 | 2 |
| `edu_transfer_order` | 跨校转学单 | `school` | 5-2 | 18 | 1 | 1 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listPromotionTask` | GET | `/edu/promotion/task/list` | 任务列表 | `promotion.batch` | 同步 |
| `exportPromotionTask` | POST | `/edu/promotion/task/export` | 任务列表导出（`CR-037` 补登记：模块级导出，≤ 2000 行直接下载、超出转异步） | `promotion.batch` | 异步 |
| `getPromotionTask` | GET | `/edu/promotion/task/{id}` | 任务详情 | `promotion.batch` | 同步 |
| `addPromotionTask` | POST | `/edu/promotion/task` | 创建任务 | `promotion.batch` | 同步 |
| `previewPromotionTask` | POST | `/edu/promotion/task/{id}/preview` | 生成预览 | `promotion.batch` | 同步 |
| `updatePromotionItem` | PUT | `/edu/promotion/task/{id}/item` | 调整单条去向 | `promotion.batch` | 同步 |
| `batchUpdatePromotionItem` | PUT | `/edu/promotion/task/{id}/item/batch` | 批量调整 | `promotion.batch` | 同步 |
| `validatePromotionTask` | POST | `/edu/promotion/task/{id}/validate` | 校验 | `promotion.batch` | 同步 |
| `executePromotionTask` | POST | `/edu/promotion/task/{id}/execute` | 执行（异步） | `promotion.batch` | 异步 |
| `retryPromotionTask` | POST | `/edu/promotion/task/{id}/retry` | 只重试失败项 | `promotion.batch` | 同步 |
| `cancelPromotionTask` | POST | `/edu/promotion/task/{id}/cancel` | 取消 | `promotion.batch` | 同步 |
| `exportPromotionPreview` | POST | `/edu/promotion/task/{id}/preview/export` | 预览导出 | `promotion.batch` | 同步 |
| `exportPromotionResult` | POST | `/edu/promotion/task/{id}/result/export` | 结果报告导出 | `promotion.batch` | 同步 |
| `listEnrollmentChange` | GET | `/edu/enrollment/change/list` | 异动记录 | `enrollment.status` | 同步 |
| `addEnrollmentChange` | POST | `/edu/enrollment/change` | 发起异动 | `enrollment.status` | 同步 |
| `approveEnrollmentChange` | POST | `/edu/enrollment/change/{id}/approve` | 审批异动 | `enrollment.status` | 同步 |
| `listTransfer` | GET | `/edu/enrollment/transfer/list` | 转学单列表 | `enrollment.status` | 同步 |
| `addTransfer` | POST | `/edu/enrollment/transfer` | 发起转出 | `enrollment.status` | 同步 |
| `acceptTransfer` | POST | `/edu/enrollment/transfer/{id}/accept` | 转入校接收 | `enrollment.status` | 同步 |
| `checkInTransfer` | POST | `/edu/enrollment/transfer/{id}/check-in` | 报到 | `enrollment.status` | 同步 |
| `cancelTransfer` | POST | `/edu/enrollment/transfer/{id}/cancel` | 撤销接收 | `enrollment.status` | 同步 |

共 21 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 升班任务列表 | `PAGE-PRM-LIST` | `/edu/promotion/list` | 2-3 | 13 | 4 | `views/edu/promotion/prm_list/index.vue` |
| 新建升班任务 | `PAGE-PRM-CREATE` | `/edu/promotion/create` | 2-3 | 12 | 1 | `views/edu/promotion/prm_create/index.vue` |
| 升班预览与调整 | `PAGE-PRM-PREVIEW` | `/edu/promotion/preview` | 2-3 | 19 | 9 | `views/edu/promotion/prm_preview/index.vue` |
| 升班校验结果 | `PAGE-PRM-VALIDATE` | `/edu/promotion/validate` | 2-3 | 11 | 3 | `views/edu/promotion/prm_validate/index.vue` |
| 执行与进度 | `PAGE-PRM-EXECUTE` | `/edu/promotion/execute` | 2-3 | 7 | 2 | `views/edu/promotion/prm_execute/index.vue` |
| 执行结果与重试 | `PAGE-PRM-RESULT` | `/edu/promotion/result` | 2-3 | 12 | 6 | `views/edu/promotion/prm_result/index.vue` |
| 跨校转学 | `PAGE-PRM-TRANSFER` | `/edu/promotion/transfer` | 2-5 | 5 | 3 | `views/edu/promotion/prm_transfer/index.vue` |
| 异动历史 | `PAGE-PRM-HISTORY` | `/edu/promotion/history` | 2-5 | 10 | 3 | `views/edu/promotion/prm_history/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 创建升班任务 + 预览 | `edu_promotion_task` + `edu_promotion_item` | 预览只写 item 的预演结果，不改班级关系 |
| 执行升班 | 每 200 名学生一个事务 | 目标学期班级关系、`edu_promotion_item.item_status` 与任务进度一起提交 |
| 学籍异动 | `edu_enrollment_change` + `edu_student_enrollment` | 同事务；跨校同时创建 `edu_transfer_order` |
| 转学单审批 / 报到 | `edu_transfer_order` 状态流转 | 报到成功后写目标校在校记录 |
| 重试 / 取消 | `edu_promotion_task` 单表 | 取消仅允许 `queued`；重试复用 `task_no` |

## 6. 并发与幂等

- `uk_promotion_task_no (task_no)`、`uk_promotion_item (task_id, student_id)` 保证幂等
- 同一学校同一源 / 目标学期同时只允许一个 `running` 任务：执行前对任务表加行锁并检查状态，
  避免双执行造成重复写班级关系
- 升班按学年**追加**、不覆盖历史（`BR-*`），因此同一学生在新学期产生新记录而非改写旧记录
- 转学单 `uk_transfer_no` 幂等；重复报到以状态机拒绝

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 目标学期 | 必须存在、未归档，且与源学期连续 |
| 学段上限 | 小学 6 年级、初中 3 年级、高中 3 年级；到顶后转「毕业」而非升班 |
| 升班范围 | 只处理在籍在读学生；休学、转出等状态按学籍状态机跳过并记原因 |
| 学籍状态 | 在读 / 休学 / 转入未报到 / 转出 / 休学 / 复学 / 毕业 / 结业 / 肄业 / 出国 / 失踪 / 退学 / 开除 / 死亡 |
| 3+1+2 变更 | 首次选科与变更分别受截止时间约束，逾期需校级管理员审批（已确认） |
| 审批 | 退学 / 开除等终态异动要求审批人；审批通过后状态不可回退，需新建异动单 |

## 8. 失败恢复与补偿

- 升班执行失败：任务置 `failed`，已提交批次不回滚（按学年追加语义可续跑），重试只处理未完成项
- 部分成功：任务置 `partial_failed`，`edu_promotion_item` 保留逐条状态，可导出失败清单
- 取消：仅 `queued` 可取消；`running` 需先停止消费者再置 `cancelled`
- 转学单超期：可取消并新建，历史保留
- 任务重试耗尽：进死信，运维重放复用 `task_no`，写审计

## 9. 权限与数据范围

- 执行权限：校级教务主任与租户管理员；年级主任只读预览视图
- 学籍异动涉及跨校时，目标学校必须已存在且启用；跨校数据不因转学自动共享历史教学数据
- 平台运营 `DS-01` 只读并可导出（逐次授权 + 留痕）

## 10. 关联图与时序

- 时序图：`diagrams/sequence/promotion-execute.mmd`
- 状态机：`diagrams/state/promotion-task.mmd`、`diagrams/state/student-enrollment-status.mmd`
- 领域模型：`diagrams/class/promotion-domain.mmd`

## 11. 验收要点

1. 同一学校同一源 / 目标学期并发发起两个执行任务，第二个被拒绝
2. 升班后上一学年班级关系完整保留，历史可查
3. 到顶年级（小学 6 年级）执行升班被引导为「毕业」流程
4. 执行失败后重试只处理未完成学生，不重复写已完成项
5. 休学学生被跳过并在结果中标注原因

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
