# PRD 索引

本目录是阶段 1 的产出。所有下游产物（原型、概要设计、详细设计、前后端代码、测试）都以这里为准。

## 阅读顺序

| 顺序 | 文件 | 回答什么问题 | 谁必读 |
|---|---|---|---|
| 1 | [01-product-context.md](01-product-context.md) | 做什么、不做什么、为什么做、怎么算成功 | 所有人 |
| 2 | [03-glossary.md](03-glossary.md) | 这些名词到底指什么 | 所有人 |
| 3 | [02-personas-and-scenarios.md](02-personas-and-scenarios.md) | 谁在什么场景下用它 | 产品、设计、前端、测试 |
| 4 | [08-data-scope-model.md](08-data-scope-model.md) | 谁能看到哪些数据 | 后端、测试、产品 |
| 5 | [05-permission-matrix.yaml](05-permission-matrix.yaml) | 具体到操作与字段的权限 | 后端、前端、测试 |
| 6 | [04-business-rules.md](04-business-rules.md) | 业务判断的硬规则 | 所有人 |
| 7 | [06-field-dictionary.yaml](06-field-dictionary.yaml) | 字段与枚举的标准定义 | 后端、前端、数据库设计 |
| 8 | [07-non-functional-requirements.md](07-non-functional-requirements.md) | 性能、安全、审计、可用性底线 | 后端、架构、测试 |
| 9 | [09-guardian-and-onboarding.md](09-guardian-and-onboarding.md) | 学生数据由谁录入、班级关系由谁定、家长如何绑定并补充资料 | 产品、设计、后端、测试 |
| 10 | [10-data-permission-schema.md](10-data-permission-schema.md) | 数据权限落到哪些表、范围怎么解析、缓存怎么失效 | 后端、测试、架构 |

以上 8 个文件是**公共前置**，不针对某个模块。
模块级 PRD 放在 `modules/<模块>/PRD.md`，必须引用公共前置中的编号，不重复定义。

## 当前状态

| 文件 | 状态 |
|---|---|
| `01-product-context.md` | 待验收 |
| `02-personas-and-scenarios.md` | 待验收 |
| `03-glossary.md` | 待验收 |
| `04-business-rules.md` | 待验收 |
| `05-permission-matrix.yaml` | 待验收 |
| `06-field-dictionary.yaml` | 待验收 |
| `07-non-functional-requirements.md` | 待验收 |
| `08-data-scope-model.md` | 待验收 |
| `09-guardian-and-onboarding.md` | 待验收 |
| `10-data-permission-schema.md` | 待验收 |

## 编号约定

下游文档通过编号引用本目录，不要复制正文。

| 前缀 | 含义 | 示例 |
|---|---|---|
| `CTX-` | 产品上下文中的目标与指标 | `CTX-GOAL-03` |
| `PER-` | 角色 | `PER-HOMEROOM` |
| `SCN-` | 场景 | `SCN-STU-01` |
| `BR-<域>-` | 业务规则 | `BR-STU-003` |
| `PM-` | 权限矩阵条目 | `PM-STU-EDIT-GUARDIAN` |
| `FD-` | 字段字典条目 | `FD-ENROLL-STATUS` |
| `NFR-` | 非功能需求 | `NFR-PERF-01` |
| `DS-` | 数据范围规则 | `DS-SCHOOL-01` |
| `REQ-` | 模块级需求（在模块 PRD 中定义） | `REQ-STU-001` |

## 首轮模块清单

| 模块 | PRD 路径 | 批次 |
|---|---|---|
| 学生管理 | `modules/student/PRD.md` | 批次 1-1（样板） |
| 教师管理 | `modules/teacher/PRD.md` | 批次 1-2 |
| 班级管理 | `modules/class/PRD.md` | 批次 1-2 |
| 年级管理 | `modules/grade/PRD.md` | 批次 1-2 |
| 学校管理 | `modules/school/PRD.md` | 批次 1-2 |
| 学年学期 | `modules/term/PRD.md` | 批次 1-3 |
| 学科与学科配置 | `modules/subject/PRD.md` | 批次 1-3 |
| 升班与学籍异动 | `modules/promotion/PRD.md` | 批次 1-3 |
| 3+1+2 选科与教学班 | `modules/stream/PRD.md` | 批次 1-3 |
| 导入导出与异步任务 | `modules/import-export/PRD.md` | 批次 1-4 |
| 审计与操作日志 | `modules/audit/PRD.md` | 批次 1-4 |

## 变更规则

公共前置文件一经用户验收即置为 `frozen`。
后续如需修改，走 `docs/00-governance/change-control.md`，并在 `document-map.yaml` 的
`impact_rules` 指引下检查所有受影响的下游产物。
