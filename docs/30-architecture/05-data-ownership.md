# 数据归属

## 1. 规则

1. **一个表只有一个归属模块**（= 只有它能写），与 `03-module-division.md` 第 3 节一致。
2. 表的读写分离：其他模块可以只读查询，但**不允许**直接写。
3. 租户隔离列（`tenant_id` / `school_id`）按 `06-field-dictionary.yaml` 的 `common_columns` 规则携带；
   平台级实体（`edu_student` / `edu_guardian`）例外，见 `D-037` / `D-030`。
4. 表名以 `edu_` 前缀标识教育域；平台侧（租户、用户、角色、字典、文件、任务调度）沿用 RuoYi 的 `sys_*` / `ry_*`。

## 2. 归属总表

| 模块 | 表 | 归属说明 | 阶段 5 的建表批次 |
|---|---|---|---|
| 平台基础（RuoYi 基线，**不改结构**） | `sys_tenant`、`sys_user`、`sys_role`、`sys_menu`、`sys_dict_*`、`sys_oss_file`、`sys_job*` | 租户类型（`tenant_type`）承载运营方 / 集团 / 学校三类 | 不在教育域迁移脚本内 |
| 学校与租户 | `edu_school`、`edu_campus`、`edu_school_stage` | 学校、校区、学校开设学段 | 5-1 |
| 学年学期 | `edu_academic_year`、`edu_term` | 学年连年、学期；日期连续不重叠（`RV-TERM-08`） | 5-1 |
| 学科与配置 | `edu_subject`、`edu_subject_stage` | 学科主体 + 学段启用表；`stream_role` 决定首选 / 再选（`BR-SUBJECT-002`） | 5-2 |
| 年级 | `edu_grade` | 年级主体；学段内序号固定映射（`RV-GRD-03`） | 5-1 |
| 班级与教学班 | `edu_class`、`edu_class_member`、`edu_teaching_class`、`edu_teaching_class_member` | 行政班 / 教学班 / 成员关系；含 `head_teacher_id` 班主任字段 | 5-1 |
| 学生与监护人 | `edu_student`（平台级）、`edu_student_enrollment`、`edu_guardian`（平台级）、`edu_student_guardian`、`edu_activation_code` | 学生主体与在校记录分离（`D-030`）；一次性激活码（`D-039`） | 5-0 |
| 教师与任教 | `edu_teacher`、`edu_user_role`、`edu_grade_leader`、`edu_teaching_assignment` | 教师主体、学校级教育角色、年级主任任职、任教关系（数据权限的三个权威来源） | 5-0 |
| 升班与学籍异动 | `edu_promotion_task`、`edu_promotion_item`、`edu_enrollment_change` | 升班任务与明细；学籍异动追加式记录 | 5-2 |
| 3+1+2 选科 | `edu_stream_config`、`edu_student_stream`、`edu_stream_change_request`、`edu_stream_history` | 选科配置 / 学生选科 / 变更申请（含审批轨迹）/ 选科历史 | 5-2 |
| 导入导出与异步 | `edu_import_batch`、`edu_import_error`、`edu_async_task`、`edu_file_ref`、`edu_dead_letter_task` | 批次、失败行、任务、文件引用、死信 | 5-2 |
| 审计 | `edu_audit_log`、`edu_audit_change`、`edu_audit_sensitive_access`、`edu_audit_operator_access`、`edu_audit_security_event`、`edu_audit_archive_batch` | 操作日志 + 变更明细 + 三类专项留痕 + 归档批次 | 5-2 |
| 数据权限（横切，归属 `datascope`） | `edu_data_grant`、`edu_data_grant_scope` | 教学资源共享授权（只读） | 5-2 |

**合计 33 张教育域表**（5-0：9 张；5-1：8 张；5-2：16 张）。

## 3. 关键归属裁决

| 问题 | 裁决 | 依据 |
|---|---|---|
| 学生主体与在校记录是否一张表 | **两张**：`edu_student`（平台级，不设 `tenant_id`）+ `edu_student_enrollment`（学校租户级） | `D-030` / `D-037`；学号跨校唯一、允许跨校转学 |
| 监护人是学生字段还是独立表 | **独立表**：`edu_guardian`（平台级，一个家长可对应多个孩子）+ `edu_student_guardian` 关系表 | `GAP-015` 的家长账号模型（跨租户 + 多对多 + 学校侧审核） |
| 班主任存哪 | `edu_class.head_teacher_id`（+ 起止日期），**不新建任职表** | `10-data-permission-schema.md` 3.3；一个班同时只有一名在任班主任 |
| 年级主任存哪 | `edu_grade_leader`（专门表，带 `term_id`） | 同上 3.2；一个年级可有多名主任，且按学年学期生效 |
| 学校级教育角色存哪 | `edu_user_role`（只存 `school_leader` / `academic_director`） | 同上 3.1；年级主任与班主任不写本表 |
| 任教关系存哪 | `edu_teaching_assignment`（含 `class_type` 区分行政班 / 教学班） | `BR-TEACHER-003`；任课教师的字段裁剪依据 |
| 教学班成员由谁写 | `edu_teaching_class_member` 归 `clazz` 模块；`stream` 只触发 | `REQ-STR-056` / `DP-01`；避免两套写入路径 |
| 学籍状态由谁写 | 只有 `promotion` 模块的异动流程写 | `DP-01`；学生编辑表单里的学籍状态是只读 |
| 选科组合是否落库 | **不落库**：由 `primary_subject_code` + `secondary_subject_codes` 派生展示值 | `subject_combination` 在字段字典里标注为"派生、不单独存储"（`CR-016`） |
| 审计日志是否与业务表同库 | 同库不同表，按时间分区；关键写操作同事务 | `REQ-AUD-035` / `A-07` |
| 文件二进制是否入库 | 不入库，只存引用（`edu_file_ref`） | `REQ-IMP-041` / `NFR-DATA-03` |

## 4. 平台级实体与租户隔离的关系

| 表 | 是否带 `tenant_id` | 读取方式 | 依据 |
|---|---|---|---|
| `edu_student` | 否（平台级） | 学校侧一律经 `edu_student_enrollment` / `edu_class_member` **两段式**取数 | `D-037` / `DS-DENY-07` |
| `edu_guardian` | 否（平台级） | 经 `edu_student_guardian` 关系取数；家长本人经 `DS-09` | `D-030` / `GAP-015` |
| 其余 `edu_*` | 是 | 查询必须带租户条件（`NFR-SEC-01`） | `06-field-dictionary.yaml` 的 `common_columns` |

## 5. 与阶段 5 的接口

阶段 5 的产物必须与本文件一致，且三者互相对得上：

| 阶段 5 产物 | 与本文件的关系 |
|---|---|
| `database/physical-schema.md` | 逐表覆盖本文件第 2 节的全部 33 张表 |
| `database/domain-table-map.csv` | 领域对象 → 表的映射，领域对象名取自各模块 PRD 的"逻辑实体"小节 |
| `database/migrations/V1__edu_*.sql` | 按本文件第 2 节的"阶段 5 的建表批次"拆分脚本 |
| `database/er-diagram.mmd` | 覆盖本文件第 3 节的 12 条关键归属裁决涉及的表与关系 |

## 6. 结论

- 33 张教育域表全部有唯一归属模块，且与 `03-module-division.md` 的写入边界一致。
- 12 条关键归属裁决都能追溯到已冻结的 PRD 条目或已关闭的缺口（`D-*` / `GAP-*`），没有"先建表再补规则"。
- 平台级实体（学生 / 监护人）不参与学校租户隔离，读取走两段式，与 `DS-DENY-07` 一致。
