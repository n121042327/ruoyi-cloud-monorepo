# 教师管理 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-TCH-*`（见 `docs/10-prd/modules/teacher/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：教师主体（`edu_teacher`，学校租户级）、教育角色（`edu_user_role`）、年级主任任职
（`edu_grade_leader`）、任教关系（`edu_teaching_assignment`）、账号启停与密码重置、教师导入导出、离职。

不负责：

- 班主任 —— 唯一写入入口是班级管理（`edu_class.head_teacher_id`），本模块只读并跳转（`DP-01`）
- 登录账号本体 —— 复用 RuoYi 基线 `sys_user`，本模块只做教育角色的映射
- 学生名单导出 —— 任课教师禁止导出任教班级名单（已确认口径）

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_grade_leader` | 年级主任任职 | `school` | 5-0 | 6 | 1 | 1 |
| `edu_teacher` | 教师主体 | `school` | 5-0 | 10 | 1 | 0 |
| `edu_teaching_assignment` | 任教关系 | `school` | 5-0 | 6 | 1 | 2 |
| `edu_user_role` | 用户教育角色（学校级） | `school` | 5-0 | 6 | 1 | 0 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listTeacher` | GET | `/edu/teacher/list` | 分页查询 | `person.teacher` | 同步 |
| `getTeacher` | GET | `/edu/teacher/{id}` | 详情 | `person.teacher` | 同步 |
| `addTeacher` | POST | `/edu/teacher` | 新增 | `person.teacher` | 同步 |
| `updateTeacher` | PUT | `/edu/teacher` | 编辑 | `person.teacher` | 同步 |
| `updateTeacherNo` | PUT | `/edu/teacher/{id}/teacher-no` | 修改工号 | `person.teacher` | 同步 |
| `listTeacherRole` | GET | `/edu/teacher/{id}/role` | 教育角色列表 | `person.teacher` | 同步 |
| `saveTeacherRole` | POST | `/edu/teacher/{id}/role` | 分配角色 | `person.teacher` | 同步 |
| `removeTeacherRole` | DELETE | `/edu/teacher/{id}/role/{roleId}` | 解除角色 | `person.teacher` | 同步 |
| `listTeachingAssignment` | GET | `/edu/teacher/assignment/list` | 任教关系查询 | `person.teacher` | 同步 |
| `saveTeachingAssignment` | POST | `/edu/teacher/assignment` | 新增任教关系 | `person.teacher` | 同步 |
| `batchSaveTeachingAssignment` | POST | `/edu/teacher/assignment/batch` | 批量设置 | `person.teacher` | 同步 |
| `removeTeachingAssignment` | DELETE | `/edu/teacher/assignment/{id}` | 失效任教关系 | `person.teacher` | 同步 |
| `leaveTeacher` | POST | `/edu/teacher/{id}/leave` | 离职 / 调离登记 | `person.teacher` | 同步 |
| `revokeTeacherLeave` | POST | `/edu/teacher/{id}/leave/revoke` | 撤销离职登记 | `person.teacher` | 同步 |
| `resetTeacherPassword` | POST | `/edu/teacher/{id}/reset-password` | 重置密码 | `person.teacher` | 同步 |
| `disableTeacherAccount` | POST | `/edu/teacher/{id}/account/disable` | 停用账号 | `person.teacher` | 同步 |
| `importTeacherValidate` | POST | `/edu/teacher/import/validate` | 导入校验 | `person.teacher` | 同步 |
| `importTeacherExecute` | POST | `/edu/teacher/import/execute` | 导入执行（异步） | `person.teacher` | 异步 |
| `downloadTeacherImportTemplate` | GET | `/edu/teacher/import/template` | 模板下载 | `person.teacher` | 同步 |
| `exportTeacher` | POST | `/edu/teacher/export` | 导出 | `person.teacher` | 同步 |
| `enableTeacherAccount` | POST | `/edu/teacher/{id}/account/enable` | 启用账号（`CR-043` 补登记：与停用对称，写审计） | `person.teacher` | 同步 |
| `copyTeachingAssignment` | POST | `/edu/teacher/assignment/copy` | 复制上一学年任教关系（`CR-043` 补登记：预览 + 冲突清单 + 异步执行） | `person.teacher` | 异步 |

共 22 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 教师管理列表 | `PAGE-TCH-LIST` | `/edu/teacher/list` | 2-2 | 48 | 9 | `views/edu/teacher/tch_list/index.vue` |
| 任教关系设置 | `PAGE-TCH-ASSIGN` | `/edu/teacher/assignment` | 2-2 | 33 | 5 | `views/edu/teacher/tch_assign/index.vue` |
| 教师导入向导 | `PAGE-TCH-IMPORT` | `/edu/teacher/import` | 2-4 | 12 | 6 | `views/edu/teacher/tch_import/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改教师 | `edu_teacher`（+ 可选 `edu_user_role`） | 同事务；分配角色与建主体一起提交 |
| 教育角色变更 | `edu_user_role` 单表 | 变更后立即失效该用户的 `scope:user:*` 缓存 |
| 年级主任任职 | `edu_grade_leader` 单表 | 按 `term_id` 维度；变更后失效 `scope:grade-leader:*` |
| 任教关系批量保存 | `edu_teaching_assignment` 删除 + 插入 | 同一事务；变更后失效 `scope:teaching:*` |
| 离职 | `edu_teacher` 状态 + 账号停用 | 存在在任教关系或班主任任职时拒绝，提示先解绑 |

## 6. 并发与幂等

- 工号：`uk_teacher_no (tenant_id, teacher_no)` 租户内唯一；改工号走 `updateTeacherNo`，不允许与历史冲突
- 任教关系：`uk_assignment (term_id, teacher_id, subject_id, class_type, class_id)` 幂等，重复保存不产生重复行
- 年级主任：`uk_grade_leader (term_id, grade_id, user_id)`；同一教师同一学期可负责多个年级，同一年级可有多名主任
- 班主任唯一性由班级模块的 `edu_class` 保证，本模块不重复校验
- 账号停用：直接失效 Sa-Token 会话，不等缓存过期

## 7. 校验规则

| 规则 | 校验 |
|---|---|
| 工号 | 租户内唯一、必填；修改需走独立接口并留痕 |
| 跨校任教 | `edu_teacher.school_id` 为主校；`edu_teaching_assignment.school_id` 记录任教学校，跨校由集团 / 运营授权开启 |
| 年级主任 | 必须是本校在职教师，且 `term_id` 必须存在 |
| 任教关系 | 学科必须是本校启用学科；`class_id` 必须属于同一学期 |
| 离职 | 有在任教关系、班主任任职、未结束的年级主任任期时拒绝 |
| 角色 | `edu_user_role.edu_role` 只写教育角色；年级主任不写此表 |

## 8. 失败恢复与补偿

- 角色 / 任职变更后缓存失效失败：以 `DataScope` 版本号兜底，视为失效（`C-04`），不依赖删除成功
- 任教关系批量保存失败：整体回滚，保留原有关系，不出现「删了旧的没插上新的」
- 导入失败：错误行可下载重提，幂等键 `batch_no`
- 账号停用后重复登录：Sa-Token 拒绝，并记安全事件

## 9. 权限与数据范围

- 校领导 `DS-04`；年级主任 `DS-05`；班主任 `DS-06`；任课教师 `DS-08`（只看本人任教班级必要资料）
- 教师本人可读自己的任教关系与班级，但不可读其他教师的教学数据
- 集团身份不自动获得教师教学数据读取权；平台运营 `DS-01` 只读并留痕

## 10. 关联图与时序

- 时序图：`diagrams/sequence/teacher-assignment.mmd`
- 状态机：`diagrams/state/teacher-account-status.mmd`
- 领域模型：`diagrams/class/teacher-domain.mmd`

## 11. 验收要点

1. 同一租户内重复工号被 `uk_teacher_no` 拒绝
2. 重复保存同一任教关系不产生重复行
3. 变更班主任后，原班主任与新班主任的 `scope:class-head:*` 缓存都被清除
4. 有在任教关系的教师执行离职被拒绝，并明确指出阻塞项
5. 任课教师读取其他班级名单返回空 / 403，不泄露存在性

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
