# 数据库迁移计划

> 本文件由 `tools/gen_stage5_docs.py` 生成；表结构事实源是 `database/schema.yaml`。
> 迁移脚本由 `tools/gen_schema_artifacts.py` 生成，不要手工编辑脚本内容。

## 1. 迁移脚本清单与顺序

| 顺序 | 脚本 | 内容 | 表数 | 必须先于 |
|---|---|---|---|---|
| 1 | `migrations/V1__edu_student_teacher.sql` | 学生（6 表）+ 教师（4 表） | 10 | V3（班级引用教师） |
| 2 | `migrations/V2__edu_org_config.sql` | 学校（3）+ 学年学期（2）+ 学科（2） | 7 | **必须先于 V3**（班级引用学期与学科） |
| 3 | `migrations/V3__edu_grade_class.sql` | 年级（1）+ 班级（4） | 5 | V4 |
| 4 | `migrations/V4__edu_promotion_stream_support.sql` | 升班 / 选科 / 导入导出 / 审计 / 授权（20 表 + 1 派生表 + 2 视图） | 20 | V5 |
| 5 | `migrations/V5__edu_foreign_keys.sql` | 32 条外键统一添加 | — | — |

对象规模：主表 42 张 / 业务列 373 个 / 唯一键 48 个 / 索引 82 个 / 外键 32 条；派生表 1 张 / 视图 2 个。

### 1.1 为什么外键单独放到 V5

建表脚本按模块分批生成，模块之间存在跨文件引用（如班级引用教师、班级引用学期）。
若在 V1 中就加外键，V1 必须依赖尚未执行的 V2 / V3，脚本顺序被迫耦合。
统一放到 V5：V1 ~ V4 只建表与索引，V5 一次性补外键。代价是多一次 `ALTER TABLE`，
换来的是每个脚本可以单独重放、单独理解。

### 1.2 为什么 V2 必须先于 V3

`edu_class` 同时引用 `edu_term`（`fk_class_term`）与 `edu_subject` 相关的学科配置。
V2 建立学校、学年学期、学科；V3 才能建立班级。V1 与 V2 之间没有依赖，可并行。

## 2. 各批次表清单

### 批次 5-0

| 表 |
|---|
| `edu_activation_code` |
| `edu_grade_leader` |
| `edu_guardian` |
| `edu_student` |
| `edu_student_enrollment` |
| `edu_student_field_change` |
| `edu_student_guardian` |
| `edu_teacher` |
| `edu_teaching_assignment` |
| `edu_user_role` |

### 批次 5-1

| 表 |
|---|
| `edu_class` |
| `edu_class_member` |
| `edu_grade` |
| `edu_teaching_class` |
| `edu_teaching_class_member` |

### 批次 5-2

| 表 |
|---|
| `edu_academic_year` |
| `edu_async_task` |
| `edu_async_task_retry` |
| `edu_audit_archive_batch` |
| `edu_audit_change` |
| `edu_audit_log` |
| `edu_campus` |
| `edu_data_grant` |
| `edu_data_grant_scope` |
| `edu_dead_letter_task` |
| `edu_enrollment_change` |
| `edu_file_ref` |
| `edu_import_batch` |
| `edu_import_error` |
| `edu_import_template` |
| `edu_promotion_item` |
| `edu_promotion_task` |
| `edu_school` |
| `edu_school_stage` |
| `edu_stream_change_request` |
| `edu_stream_config` |
| `edu_stream_history` |
| `edu_student_stream` |
| `edu_subject` |
| `edu_subject_stage` |
| `edu_term` |
| `edu_transfer_order` |

## 3. 回滚方案

| 场景 | 回滚方式 | 说明 |
|---|---|---|
| 单表结构错误 | 数据库快照恢复 | 首轮上线前用 Docker 数据卷快照，不写反向 DDL |
| 批次上线失败 | 回滚应用版本 + 保留表 | 教育域表为新增，应用回滚不要求删表 |
| 外键导致写入阻塞 | 先 `ALTER TABLE ... DROP FOREIGN KEY`，修数据后重加 | 只在事故处置时执行，需运维审批 |
| 逻辑删除误删 | 应用层恢复 `del_flag` | 历史与学籍类表禁止物理删除（`conventions.soft_delete`） |

**不提供 `DROP TABLE` 反向脚本**：生产上删表属红线操作，需要人工审批与备份，
生成器不产出这类脚本，避免误用。

## 4. 存量升级路径

1. 首轮为**新建教育域**，不涉及存量业务表改造（`ruoyi-edu` 为新模块）
2. 基线 RuoYi 表（`sys_user` 等）不动；教育角色通过 `edu_user_role` 关联
3. 升级顺序：V1 → V2 → V3 → V4 → V5，可整体重放；脚本使用 `DROP TABLE IF EXISTS` + `CREATE TABLE`，
   对已存在结构是**重建语义**，因此只适用于新建与演练环境
4. 生产增量变更不得直接重放上述脚本，必须另立增量迁移文件（编号 `V6+`），并按 `change-control.md` 走变更

## 5. 验证证据

| 证据 | 内容 |
|---|---|
| `evidence/stage5-detailed-design/2026-10-01_mysql8-empty-install.log` | MySQL 8.4.11 空库安装、对象计数、`check-sql.sql` 结构检查 |
| `evidence/stage5-detailed-design/2026-10-01_mysql8-upgrade-path.log` | 5 个脚本可重复执行、对象计数不变 |
| `evidence/stage5-detailed-design/2026-10-01_generated-column-guard.log` | 生成列唯一约束的语义验证（重复待审被拒、不同字段可通过、状态流转后可重新提交） |

## 6. 执行方式

```bash
# 演练 / 验证环境（Docker）
docker run -d --name edu-mysql -e MYSQL_ROOT_PASSWORD=*** -p 3306:3306 mysql:8
docker cp docs/40-detailed-design/migrations/. edu-mysql:/sql/
for f in V1__edu_student_teacher V2__edu_org_config V3__edu_grade_class \
         V4__edu_promotion_stream_support V5__edu_foreign_keys; do
  docker exec edu-mysql sh -c "mysql -uroot -p*** edu < /sql/$f.sql"
done
```

生产环境使用 Flyway（或项目既有迁移工具）按文件名顺序执行，禁止跳号。

## 7. 已知限制

- 脚本是**重建语义**，不适合对已有数据的表做结构变更；增量变更另立 V6+
- 生成列 `pending_guard` / `active_guard` 依赖 MySQL 8；MariaDB 或 MySQL 5.7 不受支持
- 归档表与视图与主表同构，由 V4 一起创建；归档数据的搬迁由应用侧任务执行
