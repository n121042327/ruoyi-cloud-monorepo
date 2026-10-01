#!/usr/bin/env python3
"""从 docs/40-detailed-design/database/schema.yaml 生成阶段 5 的全部数据库产物。

生成物（阶段 5 门禁：er-diagram 与 domain-table-map 与建表脚本三者一致）：
  docs/40-detailed-design/database/physical-schema.md      逐表字段说明
  docs/40-detailed-design/database/domain-table-map.csv    领域对象 → 表
  docs/40-detailed-design/database/er-diagram.mmd          ER 图（仅物理外键）
  docs/40-detailed-design/database/keys-and-indexes.md     唯一键 / 外键 / 索引清单与理由
  docs/40-detailed-design/database/check-sql.sql           一致性与不变式检查
  docs/40-detailed-design/migrations/V1__edu_student_teacher.sql
  docs/40-detailed-design/migrations/V2__edu_org_config.sql
  docs/40-detailed-design/migrations/V3__edu_grade_class.sql
  docs/40-detailed-design/migrations/V4__edu_promotion_stream_support.sql
  docs/40-detailed-design/migrations/V5__edu_foreign_keys.sql

脚本顺序按**依赖**排（组织配置先于班级），模块批次（5-0 / 5-1 / 5-2）在文档里保留，映射关系写在 migration-plan.md。
用法：python tools/gen_schema_artifacts.py
"""

from __future__ import annotations

import csv
import io
import os
import sys

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCHEMA = os.path.join(REPO_ROOT, "docs", "40-detailed-design", "database", "schema.yaml")
DD_DIR = os.path.join(REPO_ROOT, "docs", "40-detailed-design")
DB_DIR = os.path.join(DD_DIR, "database")
MIG_DIR = os.path.join(DD_DIR, "migrations")

# 依赖顺序（脚本） ←→ 模块批次（文档）
SCRIPT_GROUPS = [
    ("V1", "edu_student_teacher", ["student", "teacher"],
     "学生与教师（含平台级实体：学生主体 / 监护人主体）"),
    ("V2", "edu_org_config", ["school", "term", "subject"],
     "组织与配置（学校 / 校区 / 学段 / 学年学期 / 学科）：班级与选科都要引用，必须在它们之前建"),
    ("V3", "edu_grade_class", ["grade", "class"],
     "年级与班级（含教学班与两套成员关系）"),
    ("V4", "edu_promotion_stream_support", ["promotion", "stream", "importexport", "audit", "datascope"],
     "升班 / 选科 / 导入导出与异步任务 / 审计 / 数据权限 + 归档表 + 两个派生视图"),
]

MODULE_CN = {
    "student": "学生与监护人",
    "teacher": "教师与任教",
    "grade": "年级",
    "class": "班级与教学班",
    "promotion": "升班与学籍异动",
    "stream": "3+1+2 选科",
    "subject": "学科与配置",
    "school": "学校与租户",
    "term": "学年学期",
    "importexport": "导入导出与异步任务",
    "audit": "审计与操作日志",
    "datascope": "数据权限（横切）",
}


def load():
    with io.open(SCHEMA, encoding="utf-8") as fh:
        return yaml.safe_load(fh)


def resolved_columns(table: dict) -> list[dict]:
    """按 scope / with_audit / soft_delete 注入公共列，返回完整列清单。"""
    cols: list[dict] = [{
        "name": "id", "type": "bigint unsigned", "nullable": False, "auto_increment": True,
        "comment": "主键",
    }]
    scope = table.get("scope", "school")
    if scope in ("tenant", "school"):
        cols.append({"name": "tenant_id", "type": "varchar(20)", "nullable": False,
                     "comment": "租户隔离键（NFR-SEC-01）"})
    if scope == "school":
        cols.append({"name": "school_id", "type": "bigint unsigned", "nullable": False,
                     "comment": "学校归属"})
    for c in table.get("columns", []):
        col = dict(c)
        col.setdefault("nullable", True)
        cols.append(col)
    if table.get("with_audit"):
        cols += [
            {"name": "create_by", "type": "bigint unsigned", "nullable": True, "comment": "创建人"},
            {"name": "create_time", "type": "datetime", "nullable": False,
             "default": "CURRENT_TIMESTAMP", "comment": "创建时间"},
            {"name": "update_by", "type": "bigint unsigned", "nullable": True, "comment": "更新人"},
            {"name": "update_time", "type": "datetime", "nullable": True,
             "default": "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "comment": "更新时间"},
        ]
    if table.get("soft_delete"):
        cols.append({"name": "del_flag", "type": "char(1)", "nullable": False, "default": "'0'",
                     "comment": "逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除"})
    # 生成列必须排在它引用的列之后，因此统一在公共列之后追加
    for c in table.get("generated_columns") or []:
        col = dict(c)
        col["is_generated"] = True
        col.setdefault("nullable", True)
        col.setdefault("stored", True)
        cols.append(col)
    return cols


def col_ddl(c: dict) -> str:
    if c.get("generated"):
        parts = [
            f"`{c['name']}`", c["type"], "GENERATED ALWAYS AS (" + c["generated"] + ")",
            "STORED" if c.get("stored", True) else "VIRTUAL",
            "NULL" if c.get("nullable", True) else "NOT NULL",
            "COMMENT " + "'" + str(c.get("comment", "")).replace("'", "''") + "'",
        ]
        return " ".join(parts)
    parts = [f"`{c['name']}`", c["type"]]
    parts.append("NULL" if c.get("nullable", True) else "NOT NULL")
    if c.get("auto_increment"):
        parts.append("AUTO_INCREMENT")
    if c.get("default") is not None:
        parts.append(f"DEFAULT {c['default']}")
    parts.append("COMMENT " + "'" + str(c.get("comment", "")).replace("'", "''") + "'")
    return " ".join(parts)


def table_ddl(table: dict) -> str:
    lines = [f"DROP TABLE IF EXISTS `{table['name']}`;",
             f"CREATE TABLE `{table['name']}` ("]
    body = ["  " + col_ddl(c) for c in resolved_columns(table)]
    body.append("  PRIMARY KEY (`id`)")
    for uk in table.get("unique_keys") or []:
        cols = ", ".join(f"`{c}`" for c in uk["columns"])
        body.append(f"  UNIQUE KEY `{uk['name']}` ({cols})")
    for idx in table.get("indexes") or []:
        cols = ", ".join(f"`{c}`" for c in idx["columns"])
        body.append(f"  KEY `{idx['name']}` ({cols})")
    lines.append(",\n".join(body))
    cn = table.get("cn", "")
    lines.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci "
                 f"COMMENT='{cn}'" + ";")
    return "\n".join(lines)


def fk_ddl(table: dict) -> list[str]:
    out = []
    for fk in table.get("foreign_keys") or []:
        cols = ", ".join(f"`{c}`" for c in fk["columns"])
        ref = ", ".join(f"`{c}`" for c in fk["ref_columns"])
        out.append(
            f"ALTER TABLE `{table['name']}` ADD CONSTRAINT `{fk['name']}` "
            f"FOREIGN KEY ({cols}) REFERENCES `{fk['ref_table']}` ({ref}) "
            f"ON DELETE {fk.get('on_delete', 'RESTRICT')} ON UPDATE {fk.get('on_update', 'RESTRICT')};"
        )
    return out


def write(path: str, text: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="") as fh:
        fh.write(text)


def gen_migrations(data: dict) -> list[str]:
    tables = data["tables"]
    written = []
    for ver, name, modules, desc in SCRIPT_GROUPS:
        group = [t for t in tables if t["module"] in modules]
        body = [
            f"-- {ver}__{name}.sql",
            f"-- {desc}",
            f"-- 共 {len(group)} 张表；生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml",
            "-- 外键统一在 V5__edu_foreign_keys.sql 里添加，避免脚本顺序耦合",
            "",
            "SET NAMES utf8mb4;",
            "SET FOREIGN_KEY_CHECKS = 0;",
            "",
        ]
        for t in group:
            body += [f"-- ---------- {t['name']}（{t.get('cn','')}） ----------", table_ddl(t), ""]
        if ver == "V4":
            for dt in data.get("derived_tables", []):
                body += [f"-- ---------- {dt['name']}（{dt.get('note','')[:40]}） ----------",
                         f"DROP TABLE IF EXISTS `{dt['name']}`;",
                         dt["ddl"], ""]
            for v in data.get("views", []):
                # 列名允许写成表达式（如 "detail AS purpose"），含空格的按原样输出，不加反引号
                cols = ", ".join(c if " " in c else f"`{c}`" for c in v["columns"])
                body += [f"-- ---------- {v['name']}（{v.get('cn','')}） ----------",
                         f"DROP VIEW IF EXISTS `{v['name']}`;",
                         f"CREATE VIEW `{v['name']}` AS SELECT {cols} FROM `{v['definition_from']}` "
                         f"WHERE {v['filter']};", ""]
        body.append("SET FOREIGN_KEY_CHECKS = 1;")
        path = os.path.join(MIG_DIR, f"{ver}__{name}.sql")
        write(path, "\n".join(body) + "\n")
        written.append(f"{ver}__{name}.sql")

    # V5：外键
    body = [
        "-- V5__edu_foreign_keys.sql",
        "-- 全部外键约束（同服务内核心关系使用物理外键，禁止级联删除；跨服务关系用逻辑引用）",
        "-- 生成工具 tools/gen_schema_artifacts.py",
        "",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "",
    ]
    count = 0
    for t in tables:
        for stmt in fk_ddl(t):
            body.append(stmt)
            count += 1
    body += ["", "SET FOREIGN_KEY_CHECKS = 1;"]
    write(os.path.join(MIG_DIR, "V5__edu_foreign_keys.sql"), "\n".join(body) + "\n")
    written.append("V5__edu_foreign_keys.sql")
    print(f"  外键语句 {count} 条")
    return written


def gen_physical_schema(data: dict) -> None:
    tables = data["tables"]
    lines = [
        "# 物理表结构说明（逐表）",
        "",
        "> 本文件由 `tools/gen_schema_artifacts.py` 从 `database/schema.yaml` 生成，**不要手工编辑**；",
        "> 改表结构先改 `schema.yaml`，再重跑生成器（这样 ER 图、领域对象映射表与建表脚本必然一致）。",
        "",
        "## 0. 约定",
        "",
        f"- 方言：{data.get('dialect', 'MySQL 8')}；引擎 InnoDB；字符集 utf8mb4 / utf8mb4_general_ci",
        f"- 主键：{data['conventions']['id']}",
        f"- 公共列：{data['conventions']['audit_columns']}",
        f"- 逻辑删除：{data['conventions']['soft_delete']}",
        f"- 外键：{data['conventions']['foreign_keys']}",
        "- 平台级实体（scope=platform）不带 `tenant_id`，读取走两段式（`DS-DENY-09`）",
        "",
        "## 1. 表总览",
        "",
        "| # | 表 | 中文名 | 模块 | 范围 | 批次 | 列数 | 唯一键 | 外键 |",
        "|---|---|---|---|---|---|---|---|---|",
    ]
    for i, t in enumerate(tables, 1):
        lines.append(
            f"| {i} | `{t['name']}` | {t.get('cn','')} | {MODULE_CN.get(t['module'], t['module'])} | "
            f"{t.get('scope','school')} | {t['batch']} | {len(resolved_columns(t))} | "
            f"{len(t.get('unique_keys') or [])} | {len(t.get('foreign_keys') or [])} |"
        )
    derived = data.get("derived_tables", [])
    if derived:
        lines += ["", "### 派生表（与主表同构）", "", "| 表 | 依据 | 说明 |", "|---|---|---|"]
        for d in derived:
            lines.append(f"| `{d['name']}` | `{d['based_on']}` | {d.get('note','').strip().splitlines()[0]} |")
    views = data.get("views", [])
    if views:
        lines += ["", "### 派生视图", "", "| 视图 | 来源 | 过滤条件 | 说明 |", "|---|---|---|---|"]
        for v in views:
            lines.append(f"| `{v['name']}` | `{v['definition_from']}` | `{v['filter']}` | {v.get('note','').strip().splitlines()[0]} |")

    for t in tables:
        lines += [
            "",
            f"## {t['name']} · {t.get('cn','')}",
            "",
            f"- 模块：{MODULE_CN.get(t['module'], t['module'])}（`{t['module']}`）",
            f"- 范围：`{t.get('scope','school')}`｜批次：`{t['batch']}`",
            f"- 说明：{t.get('note','').strip()}",
            "",
            "| 列 | 类型 | 可空 | 默认值 | 说明 |",
            "|---|---|---|---|---|",
        ]
        for c in resolved_columns(t):
            if c.get("generated"):
                default = f"生成列（{c['generated']}，{'STORED' if c.get('stored', True) else 'VIRTUAL'}）"
            else:
                default = c.get("default") or "—"
            note = c.get("comment", "")
            extra = []
            if c.get("auto_increment"):
                extra.append("自增")
            if c.get("is_generated"):
                extra.append("生成列")
            if c.get("enum_ref"):
                extra.append(f"枚举 `{c['enum_ref']}`")
            if c.get("sensitive"):
                extra.append("**敏感字段**（掩码 + 访问留痕）")
            if extra:
                note = f"{note}（{'；'.join(extra)}）"
            lines.append(f"| `{c['name']}` | {c['type']} | {'是' if c.get('nullable', True) else '否'} | {default} | {note} |")
        if t.get("unique_keys"):
            lines += ["", "**唯一键**", "", "| 名称 | 列 | 说明 |", "|---|---|---|"]
            for uk in t["unique_keys"]:
                lines.append(f"| `{uk['name']}` | {', '.join('`'+c+'`' for c in uk['columns'])} | {uk.get('note','')} |")
        if t.get("indexes"):
            lines += ["", "**索引**", "", "| 名称 | 列 | 说明 |", "|---|---|---|"]
            for idx in t["indexes"]:
                lines.append(f"| `{idx['name']}` | {', '.join('`'+c+'`' for c in idx['columns'])} | {idx.get('note','')} |")
        if t.get("foreign_keys"):
            lines += ["", "**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）", "",
                      "| 名称 | 列 → 目标 | 级联 |", "|---|---|---|"]
            for fk in t["foreign_keys"]:
                lines.append(f"| `{fk['name']}` | {', '.join('`'+c+'`' for c in fk['columns'])} → "
                             f"`{fk['ref_table']}`({', '.join('`'+c+'`' for c in fk['ref_columns'])}) | "
                             f"{fk.get('on_delete','RESTRICT')} / {fk.get('on_update','RESTRICT')} |")
    write(os.path.join(DB_DIR, "physical-schema.md"), "\n".join(lines) + "\n")


def gen_domain_table_map(data: dict) -> None:
    path = os.path.join(DB_DIR, "domain-table-map.csv")
    with io.open(path, "w", encoding="utf-8", newline="") as fh:
        w = csv.writer(fh, lineterminator="\n")
        w.writerow(["领域对象", "表名", "模块", "范围", "批次", "说明"])
        for t in data["tables"]:
            first = t.get("note", "").strip().splitlines()[0] if t.get("note") else ""
            w.writerow([t.get("cn", ""), t["name"], t["module"], t.get("scope", "school"), t["batch"], first])
        for d in data.get("derived_tables", []):
            w.writerow([d.get("note", "").strip().splitlines()[0][:40], d["name"], d["module"], "—", d["batch"],
                        f"派生表，依据 {d['based_on']}"])
        for v in data.get("views", []):
            w.writerow([v.get("cn", ""), v["name"], v["module"], "—", v["batch"],
                        f"派生视图，来源 {v['definition_from']}，过滤 {v['filter']}"])


def gen_er_diagram(data: dict) -> None:
    tables = {t["name"]: t for t in data["tables"]}
    lines = ["%% ER 图（仅物理外键，与 V5__edu_foreign_keys.sql 一致）",
             "%% 生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml",
             "erDiagram"]
    used = set()
    for t in data["tables"]:
        for fk in t.get("foreign_keys") or []:
            used.add(t["name"])
            used.add(fk["ref_table"])
    for name in sorted(used):
        t = tables.get(name)
        if not t:
            continue
        lines.append(f"  {name} {{")
        for c in resolved_columns(t)[:12]:
            ctype = c["type"].replace(" ", "_").replace("(", "").replace(")", "")
            lines.append(f"    {ctype} {c['name']}")
        lines.append("  }")
    for t in data["tables"]:
        for fk in t.get("foreign_keys") or []:
            lines.append(f'  {fk["ref_table"]} ||--o{{ {t["name"]} : "{fk["name"]}"')
    write(os.path.join(DB_DIR, "er-diagram.mmd"), "\n".join(lines) + "\n")


def gen_keys_and_indexes(data: dict) -> None:
    lines = [
        "# 唯一键、外键与索引清单（含理由）",
        "",
        "> 本文件由 `tools/gen_schema_artifacts.py` 从 `database/schema.yaml` 生成。",
        "> 索引的取舍原则：先满足**数据范围解析**与**唯一性约束**，再满足列表筛选与排序；不做「以防万一」的索引。",
        "",
        "## 1. 唯一键（业务不变式的最后一道防线）",
        "",
        "| 表 | 唯一键 | 列 | 理由 |",
        "|---|---|---|---|",
    ]
    for t in data["tables"]:
        for uk in t.get("unique_keys") or []:
            lines.append(f"| `{t['name']}` | `{uk['name']}` | {', '.join('`'+c+'`' for c in uk['columns'])} | {uk.get('note','')} |")
    lines += [
        "",
        "### 无法用唯一索引表达的约束（由业务层保证 + 索引兜底）",
        "",
        "| 约束 | 表 | 做法 |",
        "|---|---|---|",
        "| 同一学生同一学期同时只允许一条**待审批**的选科变更 | `edu_stream_change_request` | 业务层在提交前按 `(term_id, student_id, request_status='pending')` 计数；`idx_stream_request_student` 用于这次检查 |",
        "| 同一学生同一字段同时只允许一条**待审核**的资料变更 | `edu_student_field_change` | 业务层按 `(student_id, field_name, status='pending')` 计数；`idx_sfc_pending` 兜底 |",
        "| 同一学生同一时刻只允许一个**未使用**的激活码 | `edu_activation_code` | 业务层按 `(student_id, status='unused')` 计数；重置时先把旧码置为已作废 |",
        "| 同一学校同一学期只允许一个教学班同一组合 | `edu_teaching_class` | `uk_teaching_class` 已覆盖（同一学期同一组合同一名称唯一） |",
        "| 一名班主任同时只负责一个班级的「在任」关系 | `edu_class` | 班主任是 `edu_class.head_teacher_id` 字段，一名教师可同时是多个班的班主任（现实中常见），因此不加唯一键 |",
        "",
        "## 2. 外键（同服务内核心关系，禁止级联删除）",
        "",
        "| 表 | 外键 | 列 → 目标 | 级联 |",
        "|---|---|---|---|",
    ]
    for t in data["tables"]:
        for fk in t.get("foreign_keys") or []:
            lines.append(f"| `{t['name']}` | `{fk['name']}` | {', '.join('`'+c+'`' for c in fk['columns'])} → "
                         f"`{fk['ref_table']}` | {fk.get('on_delete','RESTRICT')} / {fk.get('on_update','RESTRICT')} |")
    lines += [
        "",
        "> 跨服务关系（例如未来题库引用学科）用逻辑引用 + 一致性检查，不建物理外键。",
        "> 全部外键在 `V5__edu_foreign_keys.sql` 中统一添加：建表脚本按依赖顺序执行，外键最后加可以避免脚本顺序耦合。",
        "",
        "## 3. 索引",
        "",
        "| 表 | 索引 | 列 | 用途 |",
        "|---|---|---|---|",
    ]
    for t in data["tables"]:
        for idx in t.get("indexes") or []:
            lines.append(f"| `{t['name']}` | `{idx['name']}` | {', '.join('`'+c+'`' for c in idx['columns'])} | {idx.get('note','')} |")
    lines += [
        "",
        "## 4. 数据范围解析依赖的索引（性能关键路径）",
        "",
        "| 范围 | 依赖索引 |",
        "|---|---|",
        "| `DS-04` 本校 | `edu_user_role.idx_user_role_user`、各表的 `(tenant_id, school_id, …)` 前缀 |",
        "| `DS-05` 本年级 | `edu_grade_leader.idx_grade_leader_user_term` |",
        "| `DS-06` 本班 | `edu_class.idx_class_head_teacher` |",
        "| `DS-07` 任教班级 | `edu_teaching_assignment.idx_assignment_teacher_term` |",
        "| `DS-01` / 共享授权 | `edu_data_grant.idx_grant_grantee`、`edu_data_grant_scope.idx_grant_scope_resource` |",
        "",
        "## 5. 统计",
        "",
        f"- 表：{len(data['tables'])}（另有 {len(data.get('derived_tables', []))} 张派生表、{len(data.get('views', []))} 个派生视图）",
        f"- 唯一键：{sum(len(t.get('unique_keys') or []) for t in data['tables'])}",
        f"- 外键：{sum(len(t.get('foreign_keys') or []) for t in data['tables'])}",
        f"- 索引：{sum(len(t.get('indexes') or []) for t in data['tables'])}",
    ]
    write(os.path.join(DB_DIR, "keys-and-indexes.md"), "\n".join(lines) + "\n")


def gen_check_sql(data: dict) -> None:
    lines = [
        "-- check-sql.sql —— 结构与数据一致性检查",
        "-- 生成工具 tools/gen_schema_artifacts.py；每条检查在「库为空」时返回 0 行，返回非 0 行表示不一致。",
        "-- 用法：mysql -h<host> -u<user> -p<password> <db> < check-sql.sql",
        "",
        "SET NAMES utf8mb4;",
        "",
        "-- 1. 表是否齐全",
        "SELECT '缺表' AS check_name, expect.table_name AS detail FROM (",
    ]
    rows = " UNION ALL ".join(
        f"SELECT '{t['name']}' AS table_name" for t in data["tables"]
    )
    lines += [
        f"  {rows}",
        ") AS expect",
        "LEFT JOIN information_schema.tables AS actual",
        "  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name",
        "WHERE actual.table_name IS NULL;",
        "",
        "-- 2. 唯一键是否齐全",
        "SELECT '缺唯一键' AS check_name, expect.table_name, expect.index_name FROM (",
    ]
    uk_rows = []
    for t in data["tables"]:
        for uk in t.get("unique_keys") or []:
            uk_rows.append(f"SELECT '{t['name']}' AS table_name, '{uk['name']}' AS index_name")
    lines += [
        " UNION ALL ".join(uk_rows) if uk_rows else "SELECT NULL AS table_name, NULL AS index_name",
        ") AS expect",
        "LEFT JOIN information_schema.statistics AS actual",
        "  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name",
        " AND actual.index_name = expect.index_name",
        "WHERE actual.index_name IS NULL;",
        "",
        "-- 3. 外键是否齐全",
        "SELECT '缺外键' AS check_name, expect.table_name, expect.constraint_name FROM (",
    ]
    fk_rows = []
    for t in data["tables"]:
        for fk in t.get("foreign_keys") or []:
            fk_rows.append(f"SELECT '{t['name']}' AS table_name, '{fk['name']}' AS constraint_name")
    lines += [
        " UNION ALL ".join(fk_rows) if fk_rows else "SELECT NULL AS table_name, NULL AS constraint_name",
        ") AS expect",
        "LEFT JOIN information_schema.table_constraints AS actual",
        "  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name",
        " AND actual.constraint_name = expect.constraint_name",
        "WHERE actual.constraint_name IS NULL;",
        "",
        "-- 4. 平台级实体不得出现 tenant_id（DS-DENY-09）",
        "SELECT '平台级实体带了 tenant_id' AS check_name, table_name, column_name",
        "FROM information_schema.columns",
        "WHERE table_schema = DATABASE() AND table_name IN ('edu_student', 'edu_guardian')",
        "  AND column_name = 'tenant_id';",
        "",
        "-- 5. 学生必须有一条在校记录（否则学校侧看不到该学生）",
        "SELECT '学生无在校记录' AS check_name, s.student_no, s.student_name",
        "FROM edu_student s",
        "LEFT JOIN edu_student_enrollment e ON e.student_id = s.id AND e.del_flag = '0'",
        "WHERE s.del_flag = '0' AND e.id IS NULL;",
        "",
        "-- 6. 一名学生同一学期只能有一个行政班关系（uk 已保证；此处防历史脏数据）",
        "SELECT '行政班关系重复' AS check_name, m.term_id, m.student_id, COUNT(*) AS cnt",
        "FROM edu_class_member m",
        "WHERE m.class_type = 'administrative' AND m.status = '1'",
        "GROUP BY m.term_id, m.student_id HAVING cnt > 1;",
        "",
        "-- 7. 班主任必须是本校在职教师",
        "SELECT '班主任不是本校在职教师' AS check_name, c.id, c.class_name, c.head_teacher_id",
        "FROM edu_class c",
        "LEFT JOIN edu_teacher t ON t.id = c.head_teacher_id",
        "WHERE c.head_teacher_id IS NOT NULL AND c.del_flag = '0'",
        "  AND (t.id IS NULL OR t.employment_status <> 'active');",
        "",
        "-- 8. 教学班不得设班主任（REQ-CLS-039）",
        "SELECT '教学班设了班主任' AS check_name, id, class_name, head_teacher_id",
        "FROM edu_class WHERE class_type = 'teaching' AND head_teacher_id IS NOT NULL;",
        "",
        "-- 9. 选科的学科必须存在且参与 3+1+2",
        "SELECT '选科引用未启用学科' AS check_name, ss.id, ss.primary_subject_code",
        "FROM edu_student_stream ss",
        "LEFT JOIN edu_subject sub ON sub.subject_code = ss.primary_subject_code AND sub.del_flag = '0'",
        "WHERE sub.id IS NULL OR sub.stream_enabled <> '1';",
        "",
        "-- 10. 再选科目必须恰好 2 门（BR-STREAM-002）",
        "SELECT '再选科目数量不是 2' AS check_name, id, secondary_subject_codes",
        "FROM edu_student_stream",
        "WHERE (LENGTH(secondary_subject_codes) - LENGTH(REPLACE(secondary_subject_codes, ',', '')) + 1) <> 2;",
        "",
        "-- 11. 变更申请通过后必须已生效（不允许出现「已通过但没有选科记录」）",
        "SELECT '审批通过的申请没有生效记录' AS check_name, r.request_no, r.student_id",
        "FROM edu_stream_change_request r",
        "LEFT JOIN edu_student_stream s ON s.student_id = r.student_id AND s.term_id = r.term_id",
        "WHERE r.request_status = 'approved' AND s.id IS NULL;",
        "",
        "-- 12. 异步任务状态与时间字段自洽（running 必须有 start_time，终态必须有 finish_time）",
        "SELECT '任务时间字段不自洽' AS check_name, task_no, task_status",
        "FROM edu_async_task",
        "WHERE (task_status = 'running' AND start_time IS NULL)",
        "   OR (task_status IN ('succeeded','partial_failed','failed','cancelled') AND finish_time IS NULL);",
        "",
        "-- 13. 死信记录必须来自失败任务（超过重试上限）",
        "SELECT '死信记录与任务状态不一致' AS check_name, d.task_no, t.task_status, d.retry_count",
        "FROM edu_dead_letter_task d JOIN edu_async_task t ON t.task_no = d.task_no",
        "WHERE t.task_status NOT IN ('failed','dead') AND d.replay_status = 'replayable';",
        "",
        "-- 14. 共享授权不得包含业务数据资源（BR-DATA-018）",
        "SELECT '共享授权出现业务数据资源' AS check_name, grant_no, resource_types",
        "FROM edu_data_grant",
        "WHERE resource_types REGEXP 'person\\.|org\\.(class|grade|school)|enrollment\\.|stream\\.';",
        "",
        "-- 15. 共享授权不得开放写权限（BR-DATA-015）",
        "SELECT '共享授权出现写权限' AS check_name, g.grant_no, s.access_level",
        "FROM edu_data_grant g JOIN edu_data_grant_scope s ON s.grant_id = g.id",
        "WHERE s.access_level NOT IN ('read','export');",
        "",
        "-- 16. 审计日志不得出现明文证件号（BR-AUDIT-007 / REQ-AUD-010）",
        "SELECT '审计日志疑似明文证件号' AS check_name, c.id, c.log_id, c.field_name",
        "FROM edu_audit_change c",
        "WHERE c.before_value REGEXP '^[0-9]{17}[0-9Xx]$' OR c.after_value REGEXP '^[0-9]{17}[0-9Xx]$'",
        "LIMIT 50;",
        "",
        "-- 17. 在职教师工号在租户内唯一（uk 已保证；此处防数据迁移遗留）",
        "SELECT '工号重复' AS check_name, tenant_id, teacher_no, COUNT(*) AS cnt",
        "FROM edu_teacher WHERE del_flag = '0' GROUP BY tenant_id, teacher_no HAVING cnt > 1;",
        "",
        "-- 18. 学年日期必须连续不重叠（RV-TERM-08）",
        "SELECT '学年日期重叠' AS check_name, a.academic_year_code, b.academic_year_code",
        "FROM edu_academic_year a JOIN edu_academic_year b",
        "  ON a.school_id = b.school_id AND a.id < b.id",
        "WHERE a.start_date <= b.end_date AND b.start_date <= a.end_date;",
        "",
        "-- 19. 同一学校同一学年只能有一个当前学期（BR-TERM-002）",
        "SELECT '当前学期不唯一' AS check_name, t.school_id, t.academic_year_id, COUNT(*) AS cnt",
        "FROM edu_term t WHERE t.is_current = '1'",
        "GROUP BY t.school_id, t.academic_year_id HAVING cnt > 1;",
        "",
        "-- 20. 教学班成员必须能对应到有效的教学班与学生在校记录",
        "SELECT '教学班成员无在校记录' AS check_name, m.teaching_class_id, m.student_id",
        "FROM edu_teaching_class_member m",
        "LEFT JOIN edu_student_enrollment e ON e.student_id = m.student_id AND e.school_id = m.school_id",
        "WHERE m.status = '1' AND e.id IS NULL;",
        "",
        "-- 21. 视图是否齐全",
        "SELECT '缺视图' AS check_name, expect.view_name FROM (",
    ]
    vw = [f"SELECT '{v['name']}' AS view_name" for v in data.get("views", [])]
    lines += [
        " UNION ALL ".join(vw) if vw else "SELECT NULL AS view_name",
        ") AS expect",
        "LEFT JOIN information_schema.views AS actual",
        "  ON actual.table_schema = DATABASE() AND actual.table_name = expect.view_name",
        "WHERE actual.table_name IS NULL;",
        "",
        "-- 22. 归档表是否已建",
    ]
    for d in data.get("derived_tables", []):
        lines += [
            "SELECT '缺派生表' AS check_name, expect.table_name FROM (",
            f"  SELECT '{d['name']}' AS table_name",
            ") AS expect",
            "LEFT JOIN information_schema.tables AS actual",
            "  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name",
            "WHERE actual.table_name IS NULL;",
            "",
        ]
    write(os.path.join(DB_DIR, "check-sql.sql"), "\n".join(lines))


def main() -> int:
    if not os.path.exists(SCHEMA):
        print(f"缺少事实源：{SCHEMA}", file=sys.stderr)
        return 2
    data = load()
    files = gen_migrations(data)
    gen_physical_schema(data)
    gen_domain_table_map(data)
    gen_er_diagram(data)
    gen_keys_and_indexes(data)
    gen_check_sql(data)
    print(f"schema.yaml → {len(data['tables'])} 张表 / {len(data.get('derived_tables', []))} 张派生表 / {len(data.get('views', []))} 个视图")
    print("  生成迁移脚本：" + "、".join(files))
    print("  生成文档：physical-schema.md、domain-table-map.csv、er-diagram.mmd、keys-and-indexes.md、check-sql.sql")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
