# 数据权限表结构与关系

本文档把 `05-permission-matrix.yaml`（能不能做）与 `08-data-scope-model.md`（能看哪些数据）
翻译成可建表的结构。字段类型、索引与迁移脚本在阶段 5 定稿。

## 1. 设计原则

| 编号 | 原则 |
|---|---|
| DP-01 | 每一类数据范围只有一个权威来源，同一事实不在两张表里重复存 |
| DP-02 | 数据范围从**业务关系**推导，不靠管理员手工配置 |
| DP-03 | 跨校共享是**显式授权**，不是默认行为 |
| DP-04 | 授权可撤销、有有效期、**不设审批**（运营人员创建即生效）、每次访问留痕 |
| DP-05 | 教育数据权限独立实现，不继承系统租户管理员的放行逻辑 |
| DP-06 | 权限解析结果可缓存，但任一来源变更时必须立即失效 |

## 2. 数据范围的六个来源

| 范围 | 权威来源 | 表 |
|---|---|---|
| 全平台 | 运营方租户身份 | `sys_tenant`（`tenant_type = operator`） |
| 本租户组织 | 学校租户管理员角色 | `edu_user_role` |
| 本校 | 校领导 / 教务主任角色 | `edu_user_role` |
| 本年级 | 年级主任任职 | `edu_grade_leader` |
| 本班 | 班主任任职 | `edu_class.head_teacher_id` |
| 任教班级 | 任教关系 | `edu_teaching_assignment` |
| 额外共享 | 运营方授权 | `edu_data_grant` + `edu_data_grant_scope` |

注意最后一行是**叠加项**，不是角色。它把额外的学校租户集合加进用户已有的范围。

## 3. 表结构草案

### 3.1 `edu_user_role` 用户教育角色

只承载与具体班级、年级无关的**学校级角色**。

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `tenant_id` | varchar(20) | 学校租户 ID |
| `school_id` | bigint | 学校 |
| `user_id` | bigint | 系统用户 |
| `edu_role` | varchar(30) | `school_leader` / `academic_director` |
| `status` | char(1) | 启用 / 停用 |
| `start_date` / `end_date` | date | 任职有效期，可为空 |
| `create_by` / `create_time` / `update_by` / `update_time` | | 审计列 |
| `del_flag` | char(1) | 逻辑删除 |

唯一键：`uk_user_role (tenant_id, school_id, user_id, edu_role)`
索引：`idx_user (user_id, status)`

> 年级主任与班主任**不写入本表**，它们的权威来源分别是 `edu_grade_leader` 与 `edu_class.head_teacher_id`。

### 3.2 `edu_grade_leader` 年级主任任职

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `tenant_id` | varchar(20) | 学校租户 |
| `school_id` | bigint | 学校 |
| `grade_id` | bigint | 年级 |
| `term_id` | bigint | 学年学期 |
| `user_id` | bigint | 年级主任 |
| `is_primary` | char(1) | 是否主要负责人 |
| `status` | char(1) | 在职 / 离任 |
| `create_by` / `create_time` / `update_by` / `update_time` | | 审计列 |

唯一键：`uk_grade_leader (term_id, grade_id, user_id)`
索引：`idx_user_term (user_id, term_id, status)`
外键：`grade_id → edu_grade.id`（禁止级联删除）

### 3.3 `edu_class.head_teacher_id` 班主任（字段，不新建表）

班主任的权威来源是班级表上的一个字段，一名班主任负责的班级集合即为他的数据范围。
预留 `edu_class.head_teacher_id` 与 `head_teacher_start_date` / `head_teacher_end_date`，
以支持学年切换时保留历史任职。

### 3.4 `edu_teaching_assignment` 任教关系

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `tenant_id` | varchar(20) | 学校租户 |
| `school_id` | bigint | 学校 |
| `term_id` | bigint | 学年学期 |
| `teacher_id` | bigint | 教师 |
| `subject_id` | bigint | 学科 |
| `class_type` | varchar(20) | `administrative` / `teaching` |
| `class_id` | bigint | 行政班或教学班 |
| `status` | char(1) | 有效 / 失效 |
| `create_by` / `create_time` / `update_by` / `update_time` | | 审计列 |

唯一键：`uk_assignment (term_id, teacher_id, subject_id, class_type, class_id)`
索引：`idx_teacher_term (teacher_id, term_id, status)`

任课教师看到的是"任教班级的必要基本资料 + 本人所授学科数据"，
其中"本人所授学科"由本表的 `subject_id` 决定。

### 3.5 `edu_data_grant` 数据共享授权

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `grant_no` | varchar(32) | 业务编号，幂等键 |
| `grantor_user_id` | bigint | 授权人（运营方用户） |
| `grantee_type` | varchar(20) | `tenant` / `user` / `role` |
| `grantee_id` | varchar(64) | 被授权对象 ID |
| `title` | varchar(200) | 事由标题 |
| `reason` | varchar(500) | 共享原因 |
| `effective_start` | datetime | 生效时间 |
| `effective_end` | datetime | 失效时间，为空表示长期有效 |
| `status` | varchar(20) | 草稿 / 生效 / 已撤销 / 已过期 |
| `revoke_by` / `revoke_time` / `revoke_reason` | | 撤销信息 |
| `create_by` / `create_time` / `update_by` / `update_time` | | 审计列 |

唯一键：`uk_grant_no (grant_no)`
索引：`idx_grantee (grantee_type, grantee_id, status)`、`idx_effective (status, effective_end)`

状态机：`草稿 → 生效`；`生效 → 已撤销`；到期由定时任务置为 `已过期`

**本表不设审批环节。** 运营人员创建后直接生效，创建人与时间即审计依据（BR-DATA-017）。
授权是否真正可用，由三个条件共同判定：

```
status = '生效' AND now >= effective_start AND (effective_end IS NULL OR now < effective_end)
```

`已过期` 状态只用于报表与清理，查询判定不依赖它，避免定时任务延迟导致越权窗口。

### 3.6 `edu_data_grant_scope` 授权范围明细

一条授权可以覆盖多个学校与多种资源。

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `grant_id` | bigint | 所属授权 |
| `scope_type` | varchar(20) | `school_tenant` / `grade` / `class` |
| `scope_id` | varchar(64) | 范围对象 ID |
| `resource_code` | varchar(64) | 资源编码，取自权限矩阵，如 `person.student` |
| `access_level` | varchar(20) | 首轮仅 `read` / `export`，不开放 `write`（BR-DATA-015） |
| `create_time` | datetime | 时间戳 |

唯一键：`uk_grant_scope (grant_id, scope_type, scope_id, resource_code, access_level)`
外键：`grant_id → edu_data_grant.id`（禁止级联删除）

## 4. 范围解析流程

```
输入：当前用户、目标资源类型、目标操作

1. 取用户的租户与租户类型
   租户类型 = operator        → 返回 platform 范围，结束

2. 收集范围片段
   edu_user_role             → 本校范围（school_leader / academic_director）
   edu_grade_leader          → 本年级范围
   edu_class.head_teacher_id → 本班范围
   edu_teaching_assignment   → 任教班级范围（并限定 subject_id）

3. 叠加共享授权
   查 status = 生效 且当前时间落在 [effective_start, effective_end) 内的授权
   按 resource_code 与 access_level 匹配本次操作
   把 scope_type = school_tenant 的范围展开为额外学校租户 ID 集合
   注意：即使匹配到授权，第 2 步的功能权限仍必须单独通过，授权只加数据范围，不加操作权限

4. 合并
   同一维度取并集；不同维度之间是 OR 关系
   结果为空集 → 返回空列表，不做全量降级（DS-DENY-03）

5. 生成查询条件
   把范围转成 where 条件，与业务筛选条件 AND
```

关键点：第 3 步是唯一的"额外授权"入口。第 2 步完全由业务关系决定，管理员改不了，
因此不存在"手工配置出一个越权范围"的可能。

## 5. 缓存与失效

权限解析在每次请求都要跑，直接查四张表代价过高。方案：

```
缓存键：edu:scope:{userId}:{ver}
版本键：edu:scope:ver:{userId}

解析前：读版本键 → 拼缓存键 → 命中则直接用
变更时：任一来源发生变化 → 对相关用户的版本键 INCR
```

必须触发版本递增的变更：

| 变更 | 影响 |
|---|---|
| `edu_user_role` 增删改 | 该用户 |
| `edu_grade_leader` 增删改 | 该用户 |
| `edu_class.head_teacher_id` 变更 | 原班主任 + 新班主任 |
| `edu_teaching_assignment` 增删改 | 该教师 |
| `edu_data_grant` 生效 / 撤销 / 过期 | 该授权的被授权对象 |
| `edu_data_grant_scope` 增删改 | 所属授权的被授权对象 |

版本键本身不设过期时间，缓存键设短 TTL（建议 5 分钟）作为兜底。
授权被撤销时除了递增版本，还要主动删除缓存键，确保"立即失效"（BR-DATA-013）。

## 6. 审计

| 事件 | 记录内容 |
|---|---|
| 授权创建 / 撤销 / 到期 | 授权人、被授权对象、范围、原因、时间 |
| 跨租户访问 | 用户、被访问的学校租户、资源、时间、授权来源 |
| 范围判定拒绝 | 用户、目标资源、拒绝原因，用于排查越权尝试 |

## 7. 与现有基线的边界

| 项 | 处理方式 |
|---|---|
| `ruoyi-common-tenant` 的 `tenant_id` 过滤 | 保留。学校租户的数据仍按 `tenant_id` 隔离 |
| 平台级实体 | `edu_student`（学生主体）与 `edu_guardian`（监护人主体）**加入租户拦截器的忽略表**，不设 `tenant_id`。学校侧的租户隔离由 `edu_student_school_record`（在校记录）与 `edu_student_class`（行政班关系）承载，读学生一律两段式取数（见 `D-037`、`NFR-SEC-10`） |
| 租户管理员的放行逻辑 | **不继承**。教育服务独立实现数据范围判定 |
| 多租户上下文 | 学校租户请求用请求级上下文；共享授权作为叠加范围，不改变上下文的租户 |
| 缓存前缀 | 复用现有 Redis 前缀规则，权限键额外加 `edu:` 命名空间 |

## 8. 首轮落地范围

| 项 | 首轮 |
|---|---|
| 六张表（`edu_user_role` / `edu_grade_leader` / `edu_teaching_assignment` / `edu_data_grant` / `edu_data_grant_scope`，以及 `edu_class` 的班主任字段） | 建表，进入首轮迁移脚本 |
| 范围解析与拦截 | 实现 |
| 共享授权界面 | 不实现界面，仅提供表与后端解析能力 |
| 授权审批 | **不设审批**，运营人员创建后直接生效（BR-DATA-017） |
| 授权权限级别 | 仅 `read` 与 `export`，不开放 `write`（BR-DATA-015） |

## 9. 已确认事项（2026-09-29）

| 编号 | 事项 | 结论 |
|---|---|---|
| DP-07 | 共享授权的审批人是谁 | **不设审批**。运营人员操作，创建即生效 |
| DP-08 | 是否开放写权限 | **不开放**。首轮只给 `read` 与 `export`；被授权方需要修改共享内容时，复制到自有空间后再改 |
| DP-09 | 授权到期后历史记录是否保留 | **保留**。仅效力失效，记录可查 |
