# 学校与租户 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-SCH-*`（见 `docs/10-prd/modules/school/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：学校租户（`edu_school`）、校区（`edu_campus`）、学段启用（`edu_school_stage`）、
学校基线初始化（`initSchoolBaseline`）、数据共享授权（`edu_data_grant` / `edu_data_grant_scope`）。

不负责：租户体系本体（RuoYi 基线 `sys_tenant`）；集团与运营方租户的账号管理；
教学数据共享之外的数据流转（不存在的功能不要预留）。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_campus` | 校区 | `school` | 5-2 | 6 | 2 | 0 |
| `edu_data_grant` | 数据共享授权（仅教学资源） | `tenant` | 5-2 | 14 | 1 | 0 |
| `edu_data_grant_scope` | 授权范围明细 | `tenant` | 5-2 | 6 | 1 | 1 |
| `edu_school` | 学校 | `school` | 5-2 | 9 | 2 | 0 |
| `edu_school_stage` | 学校开设学段 | `school` | 5-2 | 2 | 1 | 0 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listSchool` | GET | `/edu/school/list` | 分页查询 | `org.school` | 同步 |
| `getSchool` | GET | `/edu/school/{id}` | 详情 | `org.school` | 同步 |
| `getCurrentSchool` | GET | `/edu/school/current` | 学校侧角色读取本校信息 | `org.school` | 同步 |
| `addSchool` | POST | `/edu/school` | 新建 | `org.school` | 同步 |
| `updateSchool` | PUT | `/edu/school` | 编辑 | `org.school` | 同步 |
| `updateSchoolCode` | PUT | `/edu/school/{id}/school-code` | 修改编码 | `org.school` | 同步 |
| `disableSchool` | POST | `/edu/school/{id}/disable` | 停用 | `org.school` | 同步 |
| `enableSchool` | POST | `/edu/school/{id}/enable` | 启用 | `org.school` | 同步 |
| `listCampus` | GET | `/edu/school/{id}/campus` | 校区列表 | `org.school` | 同步 |
| `saveCampus` | POST | `/edu/school/{id}/campus` | 新增 / 编辑校区 | `org.school` | 同步 |
| `removeCampus` | DELETE | `/edu/school/campus/{id}` | 删除校区（校验引用） | `org.school` | 同步 |
| `listSchoolStage` | GET | `/edu/school/{id}/stage` | 开设学段 | `org.school` | 同步 |
| `saveSchoolStage` | POST | `/edu/school/{id}/stage` | 保存学段配置 | `org.school` | 同步 |
| `getSchoolSummary` | GET | `/edu/school/{id}/summary` | 统计摘要 | `org.school` | 同步 |
| `initSchoolBaseline` | POST | `/edu/school/{id}/init` | 开通初始化 | `org.school` | 异步 |
| `exportSchool` | POST | `/edu/school/export` | 导出 | `org.school` | 同步 |

共 16 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 学校管理列表 | `PAGE-SCH-LIST` | `/edu/school/list` | 2-6 | 25 | 7 | `views/edu/school/sch_list/index.vue` |
| 校区管理 | `PAGE-SCH-CAMPUS` | `/edu/school/campus` | 2-6 | 9 | 4 | `views/edu/school/sch_campus/index.vue` |
| 开通初始化向导 | `PAGE-SCH-INIT` | `/edu/school/init` | 2-6 | 3 | 1 | `views/edu/school/sch_init/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增学校 | `edu_school` + `edu_school_stage` | 基线学段随学校一起建立 |
| 基线初始化 | `edu_school_stage` + `edu_campus` + 默认学科 | 幂等：已有配置时跳过并返回差异 |
| 校区增改 | `edu_campus` 单表 | 名称与编码双唯一 |
| 创建授权 | `edu_data_grant` + `edu_data_grant_scope` | 同事务；授权只读 |
| 撤销授权 | `edu_data_grant` 状态 + 缓存失效 | 历史记录保留，不做物理删除 |

## 6. 并发与幂等

- `uk_school_tenant (tenant_id)` 保证「一个租户对应一个学校」（`BR-ORG-001`）
- `uk_school_code (parent_tenant_id, school_code)`、`uk_campus_name` / `uk_campus_code` 兜底重复
- `uk_grant_no`、`uk_grant_scope (grant_id, scope_type, scope_id, resource_code, access_level)` 保证授权幂等
- 授权到期由定时任务置为 `expired`；缓存键 `grant:<school_tenant>:<resource_code>` 到期即失效

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 层级 | 运营方 → 集团 → 学校，不超过三级 |
| 一租户一学校 | 学校租户与集团租户都是租户；集团是一类租户，不是「下辖多校的容器」（`BR-ORG-001`） |
| 集团权限 | 集团不看下属学校教学数据；集团租户管理员可维护下属学校组织信息但读不到教学数据（`RV-SCH-05`） |
| 授权对象 | 仅教学资源（题库习题、试卷等）；学生、班级、年级、教师、成绩不参与共享（`BR-DATA-018`） |
| 授权级别 | 首轮仅 `read` / `export`；不开放写权限；无审批环节 |
| 授权记录 | 授权到期后历史保留，可查询已失效授权 |
| 平台运营 | 默认不可导出，需逐次授权并留痕（`IMP-Q-04`） |

## 8. 失败恢复与补偿

- 学校停用：不影响历史数据；停用后该校用户登录被拒
- 授权撤销：立即失效缓存；历史授权记录保留
- 基线初始化重复执行：幂等跳过并返回差异清单
- 集团跨校查询被拒：走 `DS-01` 或共享授权两条明确路径，不静默放行

## 9. 权限与数据范围

- 平台运营租户是超级租户，`DS-01` 全平台范围，只读并留痕
- 学校租户内按 `DS-04` ~ `DS-08` 解析
- **集团租户**：默认只看集团自有数据；要看下属学校组织信息走 `DS-10`（只读）；
  要看教学数据必须由运营方创建数据共享授权（只读）
- 跨校共享与学生数据的边界：共享授权只覆盖教学资源，不含学生 / 班级 / 教师 / 成绩

## 10. 关联图与时序

- 时序图：`diagrams/sequence/data-grant.mmd`、`diagrams/sequence/school-baseline.mmd`
- 领域模型：`diagrams/class/school-domain.mmd`

## 11. 验收要点

1. 同一租户创建第二个学校被 `uk_school_tenant` 拒绝
2. 集团租户查询下属学校学生名单返回 403（集团无教学数据默认权限）
3. 共享授权只允许 `read` / `export`，提交写权限被拒绝
4. 授权到期后历史记录仍可查，且不再放行
5. `DS-01` 运营访问在租户侧可见并留痕

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
