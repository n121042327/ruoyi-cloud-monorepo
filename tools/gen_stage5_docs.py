#!/usr/bin/env python3
"""生成阶段 5（详细设计与建表）的文档类产物。

事实源（全部来自已冻结的上游产物，禁止在生成器里另造业务规则）：
  docs/40-detailed-design/database/schema.yaml   表结构事实源
  docs/30-architecture/06-api-catalog.md         173 个 operationId
  docs/40-detailed-design/api/openapi.yaml       接口契约（本阶段生成）
  docs/40-detailed-design/page-action-api-map.yaml  页面动作映射（本阶段生成）
  docs/10-prd/modules/<module>/PRD.md            需求编号来源

输出：
  docs/40-detailed-design/00-index.md
  docs/40-detailed-design/modules/<module>/design.md        （11 个）
  docs/40-detailed-design/api/error-codes.yaml
  docs/40-detailed-design/diagrams/sequence/*.mmd
  docs/40-detailed-design/diagrams/state/*.mmd
  docs/40-detailed-design/diagrams/class/*.mmd
  docs/40-detailed-design/database/migration-plan.md
  docs/40-detailed-design/runtime-design.md

设计叙述（事务边界、并发、校验、失败恢复等）在本文件内手写；表与接口清单从上游提取，
避免誊抄走样。用法：python tools/gen_stage5_docs.py
"""

from __future__ import annotations

import io
import os
import re

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
S40 = os.path.join(REPO_ROOT, "docs", "40-detailed-design")
SCHEMA = os.path.join(S40, "database", "schema.yaml")
CATALOG = os.path.join(REPO_ROOT, "docs", "30-architecture", "06-api-catalog.md")
ACTION_MAP = os.path.join(S40, "page-action-api-map.yaml")
PAGE_TREE = os.path.join(S40, "frontend-page-tree.yaml")

MODULES = [
    "audit", "class", "grade", "import-export", "promotion", "school",
    "stream", "student", "subject", "teacher", "term",
]

MODULE_CN = {
    "audit": "审计与操作日志",
    "class": "班级管理",
    "grade": "年级管理",
    "import-export": "导入导出与异步任务",
    "promotion": "升班与学籍异动",
    "school": "学校与租户",
    "stream": "3+1+2 选科与教学班",
    "student": "学生管理",
    "subject": "学科与配置",
    "teacher": "教师管理",
    "term": "学年学期",
}

REQ_PREFIX = {
    "audit": "REQ-AUD",
    "class": "REQ-CLS",
    "grade": "REQ-GRD",
    "import-export": "REQ-IMP",
    "promotion": "REQ-PRM",
    "school": "REQ-SCH",
    "stream": "REQ-STR",
    "student": "REQ-STU",
    "subject": "REQ-SUB",
    "teacher": "REQ-TCH",
    "term": "REQ-TERM",
}


def load_schema():
    data = yaml.safe_load(io.open(SCHEMA, encoding="utf-8"))
    tables = {t["name"]: t for t in data["tables"]}
    derived = {t["name"]: t for t in (data.get("derived_tables") or [])}
    views = {v["name"]: v for v in (data.get("views") or [])}
    return data, tables, derived, views


def module_tables(tables, derived, module):
    """表按 schema.yaml 的 module 字段归属；datascope 表归到 school 模块。"""
    keys = {module}
    if module == "school":
        keys.add("datascope")
    if module == "import-export":
        keys.add("importexport")
    picked = [t for t in tables.values() if t.get("module") in keys]
    picked.sort(key=lambda t: (t.get("batch", ""), t["name"]))
    extra = [t for t in derived.values() if t.get("module") in keys]
    return picked + extra


ROW_RE = re.compile(
    r"\|\s*\d+\s*\|\s*`([A-Za-z][A-Za-z0-9]+)`\s*\|\s*(GET|POST|PUT|DELETE|PATCH)\s*\|\s*`([^`]+)`\s*\|"
    r"\s*([^|]+)\|\s*`([^`]+)`\s*\|\s*([^|]+)\|\s*([^|]+)\|"
)


def load_ops():
    """解析 06-api-catalog.md，按模块返回 operationId 清单。"""
    text = io.open(CATALOG, encoding="utf-8").read()
    ops = {}
    module = ""
    for line in text.split("\n"):
        m = re.match(r"^## 5\.\d+\s+.+?（`([\w-]+)`）", line)
        if m:
            module = m.group(1)
        row = ROW_RE.match(line.strip())
        if row and module:
            ops.setdefault(module, []).append({
                "op": row.group(1),
                "method": row.group(2),
                "path": row.group(3).strip(),
                "desc": row.group(4).strip(),
                "resource": row.group(5).strip(),
                "sync": row.group(7).strip(),
            })
    return ops


def load_action_map():
    return yaml.safe_load(io.open(ACTION_MAP, encoding="utf-8"))


def load_page_tree():
    return yaml.safe_load(io.open(PAGE_TREE, encoding="utf-8"))


def pages_of_module(action_map, page_tree, module):
    """按 frontend-page-tree 的 module 字段取页面，再过 action_map 补动作统计。"""
    by_file = {p.get("file"): p for p in (action_map.get("pages") or [])}
    hit = []
    for r in page_tree.get("routes") or []:
        if r.get("module") != module:
            continue
        acts = (by_file.get(r.get("prototype_file")) or {}).get("actions") or []
        hit.append({
            "page_id": r.get("page_id"),
            "name": r.get("name_cn"),
            "route": r.get("route"),
            "component": r.get("view_component"),
            "batch": r.get("batch"),
            "actions": acts,
            "api_actions": [a for a in acts if (a.get("api") or "-") not in ("-", "")],
        })
    return hit


def rel(path: str) -> str:
    return path.replace("\\", "/")


def write(path: str, text: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    print("written:", rel(os.path.relpath(path, REPO_ROOT)))


def render_module_design(module: str, tables, ops, pages):
    cn = MODULE_CN[module]
    d = MODULE_DESIGN[module]
    prefix = REQ_PREFIX[module]

    lines = [
        f"# {cn} · 详细设计",
        "",
        "> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从",
        "> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、",
        "> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。",
        f"> 需求编号前缀：`{prefix}-*`（见 `docs/10-prd/modules/{module}/PRD.md`）。",
        "> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。",
        "",
        "## 1. 模块边界",
        "",
        d["boundary"].strip(),
        "",
        "## 2. 数据归属",
        "",
    ]
    if tables:
        lines += ["| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |", "|---|---|---|---|---|---|---|"]
        for t in tables:
            cols = len(t.get("columns") or [])
            lines.append(
                f"| `{t['name']}` | {t.get('cn','')} | `{t.get('scope','school')}` | {t.get('batch','')} | "
                f"{cols} | {len(t.get('unique_keys') or [])} | {len(t.get('foreign_keys') or [])} |"
            )
        lines += ["", "字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。"]
    else:
        lines.append("本模块不持有独立物理表（复用其他模块的表或组件）。")

    lines += ["", "## 3. 接口清单（operationId）", ""]
    if ops:
        lines += ["| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |", "|---|---|---|---|---|---|"]
        for o in ops:
            lines.append(
                f"| `{o['op']}` | {o['method']} | `{o['path']}` | {o['desc']} | `{o['resource']}` | {o['sync']} |"
            )
        lines += ["", f"共 {len(ops)} 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，"
                      "错误码见 `docs/40-detailed-design/api/error-codes.yaml`。"]
    else:
        lines.append("本模块不对外暴露接口。")

    if pages:
        lines += ["", "## 4. 页面与动作落点", "",
                  "| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |",
                  "|---|---|---|---|---|---|---|"]
        for p in pages:
            lines.append(
                f"| {p.get('name')} | `{p.get('page_id')}` | `{p.get('route','')}` | {p.get('batch','')} | "
                f"{len(p.get('actions') or [])} | {len(p.get('api_actions') or [])} | `{p.get('component','')}` |"
            )
        lines += ["", "完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。"]

    lines += [
        "",
        "## 5. 事务边界",
        "",
        d["tx"].strip(),
        "",
        "## 6. 并发与幂等",
        "",
        d["concurrency"].strip(),
        "",
        "## 7. 校验规则",
        "",
        d["validation"].strip(),
        "",
        "## 8. 失败恢复与补偿",
        "",
        d["recovery"].strip(),
        "",
        "## 9. 权限与数据范围",
        "",
        d["scope"].strip(),
        "",
        "## 10. 关联图与时序",
        "",
        d["refs"].strip(),
        "",
        "## 11. 验收要点",
        "",
        d["acceptance"].strip(),
        "",
        "## 12. 状态口径",
        "",
        "| 口径 | 当前值 | 说明 |",
        "|---|---|---|",
        "| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |",
        "| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |",
        "| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |",
        "",
    ]
    return "\n".join(lines)


MODULE_DESIGN = {
    "student": {
        "boundary": """
负责：学生主体（`edu_student`，**平台级实体，不带 `tenant_id`**）、在校记录（`edu_student_enrollment`）、
监护人主体（`edu_guardian`，平台级）与监护人关系（`edu_student_guardian`）、学生资料变更申请
（`edu_student_field_change`）、一次性激活凭据（`edu_activation_code`）、学籍状态变更、
学生导入导出。

不负责：

- 班级归属写入 —— 唯一写入入口是班级管理（`edu_class_member`），本模块只读展示（`DP-01`）
- 升班执行 —— 唯一执行入口是升班模块；本模块只提供学生维度的历史查询
- 选科结果写入 —— 属选科模块（`edu_student_stream`）
- 家长登录账号本身 —— 属认证与账号域；本模块只维护监护人主体与绑定关系
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增学生 | `edu_student` + `edu_student_enrollment` +（可选）`edu_student_guardian` | 一个事务提交；任一失败整体回滚，不留下孤儿主体 |
| 修改学生 | `edu_student`（+ 触发 `edu_student_field_change` 时） | 敏感字段改动走申请，不在本事务内直接改 |
| 学籍状态变更 | `edu_enrollment_change` + `edu_student_enrollment.enrollment_status` | 同事务；跨校转学另起转学单事务（见升班模块） |
| 监护人绑定 / 解绑审核 | `edu_student_guardian` 单表 | 审核通过后写 `bind_status`，同时写审计 |
| 激活码签发 / 重置 | `edu_activation_code` 单表 | 签发走批量插入，重置走状态更新 |
| 导入执行 | 每 500 行一个事务 | 批次状态与错误行独立提交，保证「部分成功可续跑」 |

禁止跨模块写表：本模块不得写 `edu_class_member`、`edu_student_stream`、`edu_promotion_item`。
""",
        "concurrency": """
- 学号：`uk_student_no` 兜底，平台唯一且永不回收；并发插入冲突转 `EDU-STU-4001`
- 证件号：`uk_id_card_no` 兜底（非空时唯一，`GAP-020`）
- 激活码：使用采取条件更新 `UPDATE ... SET status='used' WHERE code=? AND status='unused'`，
  影响行数为 0 即判定已被使用；`uk_activation_active` 保证同一学生同时只有一个未使用激活码
- 资料变更申请：`uk_sfc_pending`（生成列 `pending_guard`）保证同一学生同一字段同时只有一条待审核
- 导入：同一用户同时 1 个任务、同一学校 3 个任务，超出进入排队（`REQ-IMP-047` / `REQ-IMP-048`）
- 导出：以任务创建时刻的范围快照执行；下载时重新解析范围（`DS-DENY-04`）
""",
        "validation": """
| 字段 / 规则 | 校验 | 依据 |
|---|---|---|
| 学号 | 平台唯一、必填、不可修改后复用 | `GAP-020` |
| 证件号 | 可空；填写时 18 位格式校验 + 平台唯一 | `GAP-020` |
| 登录名 | `s` + 学号，平台唯一 | 已确认口径 |
| 姓名 / 性别 / 出生日期 | 必填，出生日期不得晚于今天 | PRD 第 7 节 |
| 入学年份 / 学段 / 年级 | 必须与所在班级一致 | `BR-STU-*` |
| 监护人手机号 | 平台唯一（一个家长一个账号），绑定上限 3 | `GAP-015` |
| 解绑 | 需班主任确认；驳回可重提，同字段同时只允许一条待审 | `GAP-018` |
| 字段级可编辑性 | 班主任可改监护人信息；年级主任只读；任课教师只读 | 已确认的字段级矩阵 |
| 任课教师导出名单 | **禁止**（导出接口不授予任课教师） | 已确认口径 |
""",
        "recovery": """
- 单条写失败：事务整体回滚，返回错误码，无副作用
- 导入失败：错误行落 `edu_import_error`，可下载失败行后重提；批次复用 `batch_no`
- 任务重试耗尽：进 `edu_dead_letter_task`，运维重放复用原幂等键，重放写审计
- 激活码丢失：班主任重置，旧码置为作废，新码签发；全过程留痕
- 消息消费重复：以 `batch_no` / `task_no` 先查后写，唯一索引兜底
""",
        "scope": """
- 学生主体与监护人主体是平台级实体：**不带 `tenant_id`**，学校侧读取走两段式
  （先按 `edu_student_enrollment.school_id` 过滤，再取 `edu_student`），见 `DS-DENY-09`
- 校领导看本校全部（`DS-04`）；年级主任看负责年级（`DS-05`）；班主任看本班（`DS-06`）；
  任课教师看本人任教班级的必要资料（`DS-08`）
- 缺少租户 / 学校上下文一律拒绝（`DS-DENY-01` / `DS-DENY-02`）；范围为空返回空列表而非全量（`DS-DENY-03`）
- 按 ID 查详情先注入范围条件，越权 ID 表现为「查不到」（`DS-DENY-07`）
""",
        "refs": """
- 时序图：`diagrams/sequence/student-enroll.mmd`、`diagrams/sequence/student-import.mmd`
- 状态机：`diagrams/state/student-enrollment-status.mmd`、`diagrams/state/guardian-bind.mmd`
- 领域模型：`diagrams/class/student-domain.mmd`
""",
        "acceptance": """
1. 同一学号 / 同一证件号第二次写入被数据库唯一键拒绝，接口返回对应错误码
2. 同一学生同一字段提交两条待审，第二条被 `uk_sfc_pending` 拒绝
3. 同一学生签发第二个未使用激活码被 `uk_activation_active` 拒绝
4. 监护人绑定第 4 条被拒绝；解绑后可由班主任重新确认
5. 平台级实体在缺少学校上下文时读取返回 403，而不是全量
6. 任课教师调用导出接口返回 403
""",
    },
    "teacher": {
        "boundary": """
负责：教师主体（`edu_teacher`，学校租户级）、教育角色（`edu_user_role`）、年级主任任职
（`edu_grade_leader`）、任教关系（`edu_teaching_assignment`）、账号启停与密码重置、教师导入导出、离职。

不负责：

- 班主任 —— 唯一写入入口是班级管理（`edu_class.head_teacher_id`），本模块只读并跳转（`DP-01`）
- 登录账号本体 —— 复用 RuoYi 基线 `sys_user`，本模块只做教育角色的映射
- 学生名单导出 —— 任课教师禁止导出任教班级名单（已确认口径）
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改教师 | `edu_teacher`（+ 可选 `edu_user_role`） | 同事务；分配角色与建主体一起提交 |
| 教育角色变更 | `edu_user_role` 单表 | 变更后立即失效该用户的 `scope:user:*` 缓存 |
| 年级主任任职 | `edu_grade_leader` 单表 | 按 `term_id` 维度；变更后失效 `scope:grade-leader:*` |
| 任教关系批量保存 | `edu_teaching_assignment` 删除 + 插入 | 同一事务；变更后失效 `scope:teaching:*` |
| 离职 | `edu_teacher` 状态 + 账号停用 | 存在在任教关系或班主任任职时拒绝，提示先解绑 |
""",
        "concurrency": """
- 工号：`uk_teacher_no (tenant_id, teacher_no)` 租户内唯一；改工号走 `updateTeacherNo`，不允许与历史冲突
- 任教关系：`uk_assignment (term_id, teacher_id, subject_id, class_type, class_id)` 幂等，重复保存不产生重复行
- 年级主任：`uk_grade_leader (term_id, grade_id, user_id)`；同一教师同一学期可负责多个年级，同一年级可有多名主任
- 班主任唯一性由班级模块的 `edu_class` 保证，本模块不重复校验
- 账号停用：直接失效 Sa-Token 会话，不等缓存过期
""",
        "validation": """
| 规则 | 校验 |
|---|---|
| 工号 | 租户内唯一、必填；修改需走独立接口并留痕 |
| 跨校任教 | `edu_teacher.school_id` 为主校；`edu_teaching_assignment.school_id` 记录任教学校，跨校由集团 / 运营授权开启 |
| 年级主任 | 必须是本校在职教师，且 `term_id` 必须存在 |
| 任教关系 | 学科必须是本校启用学科；`class_id` 必须属于同一学期 |
| 离职 | 有在任教关系、班主任任职、未结束的年级主任任期时拒绝 |
| 角色 | `edu_user_role.edu_role` 只写教育角色；年级主任不写此表 |
""",
        "recovery": """
- 角色 / 任职变更后缓存失效失败：以 `DataScope` 版本号兜底，视为失效（`C-04`），不依赖删除成功
- 任教关系批量保存失败：整体回滚，保留原有关系，不出现「删了旧的没插上新的」
- 导入失败：错误行可下载重提，幂等键 `batch_no`
- 账号停用后重复登录：Sa-Token 拒绝，并记安全事件
""",
        "scope": """
- 校领导 `DS-04`；年级主任 `DS-05`；班主任 `DS-06`；任课教师 `DS-08`（只看本人任教班级必要资料）
- 教师本人可读自己的任教关系与班级，但不可读其他教师的教学数据
- 集团身份不自动获得教师教学数据读取权；平台运营 `DS-01` 只读并留痕
""",
        "refs": """
- 时序图：`diagrams/sequence/teacher-assignment.mmd`
- 状态机：`diagrams/state/teacher-account-status.mmd`
- 领域模型：`diagrams/class/teacher-domain.mmd`
""",
        "acceptance": """
1. 同一租户内重复工号被 `uk_teacher_no` 拒绝
2. 重复保存同一任教关系不产生重复行
3. 变更班主任后，原班主任与新班主任的 `scope:class-head:*` 缓存都被清除
4. 有在任教关系的教师执行离职被拒绝，并明确指出阻塞项
5. 任课教师读取其他班级名单返回空 / 403，不泄露存在性
""",
    },
    "grade": {
        "boundary": """
负责：年级主体（`edu_grade`）、年级主任任职（`edu_grade_leader`）、升班的只读视图
（`getGradePromotionView`）。

不负责：升班执行（升班模块唯一入口）；班级与学生的实际归属（班级管理）；年级下的班级数量统计
由班级模块提供数据。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改年级 | `edu_grade` 单表 | 校验学段序号映射 |
| 批量新增 | 每 100 条一个事务 | 部分失败返回逐行结果 |
| 指定 / 变更年级主任 | `edu_grade_leader` | 变更后失效 `scope:grade-leader:*` |
| 归档 / 删除年级 | `edu_grade`（+ 引用检查） | 有班级或学生时拒绝删除，只允许归档 |
""",
        "concurrency": """
- `uk_grade_seq (school_id, stage_code, enroll_year, grade_level)` 与
  `uk_grade_name (school_id, stage_code, grade_name)` 兜底并发建同名 / 同序号年级
- 删除与新增并发：删除先做引用检查再提交；新增班级时校验年级存在且未归档
- 年级主任变更与升班执行并发：升班读取年级时使用同一 `term_id` 视图，不缓存跨学期结果
""",
        "validation": """
| 规则 | 取值 |
|---|---|
| 学段序号固定映射 | 小学 1–6、初中 1–3、高中 1–3（`RV-GRD-03`），不可自由配置 |
| 年级与学段对应 | 固定，年级不得跨学段（`RV-GRD-03`） |
| 入学年份 | 必填，用于升班与「3+1+2」推算 |
| 年级名称 | 同一学校同一学段内唯一 |
| 年级主任 | 必须是本校在职教师；同一学期同一教师可负责多个年级 |
| 删除权限 | 教务主任与租户管理员可删空年级（已确认） |
""",
        "recovery": """
- 删除失败：整体回滚，返回引用清单（班级数 / 学生数），前端给出跳转入口
- 归档后误操作：支持解除归档（`archiveGrade` 反向接口），留痕
- 年级主任变更失败：保留原任职，不出现空档
""",
        "scope": """
- 年级主任 `DS-05` 看负责年级；校领导 `DS-04` 看本校全部年级
- 年级列表按 `school_id` + 数据范围过滤；平台运营 `DS-01` 只读并留痕
- 升班只读视图与年级列表使用同一套范围解析，避免「列表看不到、视图能看到」
""",
        "refs": """
- 状态机：`diagrams/state/promotion-task.mmd`（年级在升班中的只读角色）
- 领域模型：`diagrams/class/grade-domain.mmd`
""",
        "acceptance": """
1. 建立「小学 7 年级」被拒绝（学段序号映射固定）
2. 同一学校同一学段重复年级名被唯一键拒绝
3. 有班级的年级执行删除被拒绝，并能看到阻塞它的班级
4. 年级主任在升班只读视图中只能看到自己负责的年级
""",
    },
    "class": {
        "boundary": """
负责：行政班与教学班容器（`edu_class`）、行政班成员（`edu_class_member`）、教学班主体
（`edu_teaching_class`）与成员（`edu_teaching_class_member`）、班主任指派、编班 / 调班 / 移出、
班级合并与停用、编班表导入。

不负责：

- 学生主体与在校记录（学生管理）
- 升班执行（升班模块）；本模块承接升班结果写入的班级关系
- 选科决策（选科模块）；教学班生成由选科模块**触发**，成员写入由本模块执行（`DP-01`）
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改班级 | `edu_class` 单表 | 校验班主任与班级类型组合 |
| 编班 / 调班 | `edu_class_member` 单表（调班为一次 UPDATE） | `term_id + student_id` 唯一，保证一个学生一个行政班 |
| 批量编班 | 每 200 条一个事务 | 部分失败返回逐行结果 |
| 合并班级 | 源班成员移入目标班 + 源班停用 | 同一事务；目标班容量只提示不拦截 |
| 指定 / 变更班主任 | `edu_class.head_teacher_id` | 变更后失效 `scope:class-head:*` 与 `scope:user:*` |
| 教学班成员写入 | `edu_teaching_class_member` 批量插入 | 由选科模块触发的异步任务调用，幂等键为 `generate_task_no` |
""",
        "concurrency": """
- `uk_class_member_admin (term_id, student_id)` 是「一个学生一个行政班」的数据库级保证，
  并发调班时后提交者被拒绝，不会出现双班归属
- `uk_class_name (school_id, term_id, stage_code, class_name)` 与
  `uk_class_teaching (school_id, term_id, subject_combination, class_type)` 保证教学班幂等生成
- `uk_tclass_member (teaching_class_id, student_id)` 保证教学班成员幂等
- 班主任变更与权限缓存失效在同一事务提交后触发，失败由范围版本号兜底（`C-04`）
- 容量只提示不拦截（`BR-CLASS-005`），因此不存在「容量锁」
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 班级名称 | 同一学校同一学期同一学段内唯一（`BR-CLASS-003`） |
| 学生归属 | 同一学年学期一名学生只能属于一个行政班（`BR-STU-003`） |
| 班主任 | 必须是本校在职教师；同一行政班同一学期只有一个在任班主任；教学班不设班主任 |
| 班主任权限入口 | 唯一写入入口是本模块（`DP-01`）；教师管理只读 |
| 教学班 | 不参与 `DS-06` 解析（`BR-CLASS-007` / `REQ-CLS-039`） |
| 删除 | 有在读学生时不允许删除，只允许停用（`BR-CLASS-006`） |
| 学生班级归属 | 唯一写入入口是本模块（编班 / 调班 / 移出），学生管理只读 |
""",
        "recovery": """
- 批量编班部分失败：已提交批次保留，失败行可下载后重提；批次号幂等
- 调班失败：事务回滚，学生留在原班
- 教学班生成失败：异步任务重试（复用 `generate_task_no`），不产生半成品教学班
- 合并班级中途失败：整体回滚，源班成员与状态不变
""",
        "scope": """
- 班主任 `DS-06` 看本班；年级主任 `DS-05` 看本年级全部班级；校领导 `DS-04` 看本校
- 任课教师 `DS-08` 只看本人任教班级的必要资料，且**不能导出名单**
- 教学班不参与班级维度的数据范围继承，成员可见性由行政班与任教关系共同决定
""",
        "refs": """
- 时序图：`diagrams/sequence/class-roster-import.mmd`、`diagrams/sequence/class-transfer.mmd`
- 状态机：`diagrams/state/class-status.mmd`
- 领域模型：`diagrams/class/class-domain.mmd`
""",
        "acceptance": """
1. 同一学生同一学期第二次编入行政班被 `uk_class_member_admin` 拒绝
2. 同一学期同一组合重复生成教学班不产生重复行
3. 教学班不触发 `DS-06`：班主任身份不影响教学班可见性
4. 有在读学生的班级执行删除被拒绝，停用成功
5. 变更班主任后原班主任立即失去该班数据范围（缓存失效验证）
""",
    },
    "promotion": {
        "boundary": """
负责：升班任务（`edu_promotion_task` / `edu_promotion_item`，预览 / 执行 / 重试 / 取消）、
学籍异动（`edu_enrollment_change`）、跨校转学单（`edu_transfer_order`）。

不负责：

- 年级定义与学段序号（年级管理）
- 班级与成员关系的表结构（班级管理）；升班写入的是班级模块的表，写入动作由本模块的
  升班任务执行器发起（唯一执行入口，`DP-01`）
- 学生主体字段维护（学生管理）
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 创建升班任务 + 预览 | `edu_promotion_task` + `edu_promotion_item` | 预览只写 item 的预演结果，不改班级关系 |
| 执行升班 | 每 200 名学生一个事务 | 目标学期班级关系、`edu_promotion_item.item_status` 与任务进度一起提交 |
| 学籍异动 | `edu_enrollment_change` + `edu_student_enrollment` | 同事务；跨校同时创建 `edu_transfer_order` |
| 转学单审批 / 报到 | `edu_transfer_order` 状态流转 | 报到成功后写目标校在校记录 |
| 重试 / 取消 | `edu_promotion_task` 单表 | 取消仅允许 `queued`；重试复用 `task_no` |
""",
        "concurrency": """
- `uk_promotion_task_no (task_no)`、`uk_promotion_item (task_id, student_id)` 保证幂等
- 同一学校同一源 / 目标学期同时只允许一个 `running` 任务：执行前对任务表加行锁并检查状态，
  避免双执行造成重复写班级关系
- 升班按学年**追加**、不覆盖历史（`BR-*`），因此同一学生在新学期产生新记录而非改写旧记录
- 转学单 `uk_transfer_no` 幂等；重复报到以状态机拒绝
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 目标学期 | 必须存在、未归档，且与源学期连续 |
| 学段上限 | 小学 6 年级、初中 3 年级、高中 3 年级；到顶后转「毕业」而非升班 |
| 升班范围 | 只处理在籍在读学生；休学、转出等状态按学籍状态机跳过并记原因 |
| 学籍状态 | 在读 / 休学 / 转入未报到 / 转出 / 休学 / 复学 / 毕业 / 结业 / 肄业 / 出国 / 失踪 / 退学 / 开除 / 死亡 |
| 3+1+2 变更 | 首次选科与变更分别受截止时间约束，逾期需校级管理员审批（已确认） |
| 审批 | 退学 / 开除等终态异动要求审批人；审批通过后状态不可回退，需新建异动单 |
""",
        "recovery": """
- 升班执行失败：任务置 `failed`，已提交批次不回滚（按学年追加语义可续跑），重试只处理未完成项
- 部分成功：任务置 `partial_failed`，`edu_promotion_item` 保留逐条状态，可导出失败清单
- 取消：仅 `queued` 可取消；`running` 需先停止消费者再置 `cancelled`
- 转学单超期：可取消并新建，历史保留
- 任务重试耗尽：进死信，运维重放复用 `task_no`，写审计
""",
        "scope": """
- 执行权限：校级教务主任与租户管理员；年级主任只读预览视图
- 学籍异动涉及跨校时，目标学校必须已存在且启用；跨校数据不因转学自动共享历史教学数据
- 平台运营 `DS-01` 只读并可导出（逐次授权 + 留痕）
""",
        "refs": """
- 时序图：`diagrams/sequence/promotion-execute.mmd`
- 状态机：`diagrams/state/promotion-task.mmd`、`diagrams/state/student-enrollment-status.mmd`
- 领域模型：`diagrams/class/promotion-domain.mmd`
""",
        "acceptance": """
1. 同一学校同一源 / 目标学期并发发起两个执行任务，第二个被拒绝
2. 升班后上一学年班级关系完整保留，历史可查
3. 到顶年级（小学 6 年级）执行升班被引导为「毕业」流程
4. 执行失败后重试只处理未完成学生，不重复写已完成项
5. 休学学生被跳过并在结果中标注原因
""",
    },
    "stream": {
        "boundary": """
负责：选科配置（`edu_stream_config`）、学生选科结果（`edu_student_stream`）、选科变更申请
（`edu_stream_change_request`）、变更历史（`edu_stream_history`）、教学班生成触发与统计。

不负责：

- 教学班与成员的物理写入 —— 由班级模块执行（`edu_teaching_class` / `edu_teaching_class_member`），
  本模块只创建生成任务并校验结果（`REQ-STR-056` / `DP-01`）
- 学科主体与选科角色定义（学科与配置）
- 行政班归属（班级管理）；选科组合不等于行政班
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 保存选科配置 | `edu_stream_config` 单表 | 保存后立即失效 `stream:config:<term>` 缓存 |
| 学生提交选科 | `edu_student_stream` + `edu_stream_history` | 同事务；`term_id + student_id` 唯一 |
| 变更申请 | `edu_stream_change_request` 单表 | 状态机驱动的审批流 |
| 审批通过 | 申请状态 + `edu_student_stream` + 历史 | 同事务；失效选科统计缓存 |
| 生成教学班 | 预览同步；生成为异步任务 | 任务写入由班级模块执行，本模块校验幂等键与结果 |
""",
        "concurrency": """
- `uk_student_stream (term_id, student_id)` 保证同一学期一名学生只有一份选科结果
- `uk_stream_request_no (request_no)` 保证申请幂等；同一学生同一学期同时只允许一条待审申请
  （由业务层加锁 + 申请状态索引保证，见 `idx_stream_request_student`）
- 教学班生成幂等：`uk_class_teaching` 与 `uk_tclass_member` 在班级模块兜底
- 统计缓存按「范围摘要哈希」分键，范围变化自然落到新键（`C-04`）
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 首选学科 | 固定物理 / 历史（`RV-SUB-03`） |
| 再选学科 | 固定化学 / 生物 / 思想政治 / 地理，不可自由配置（`RV-SUB-03`） |
| 学段 | 仅高中适用；非高中学生不可提交 |
| 截止时间 | 学校级可配置；逾期需校级管理员审批（已确认） |
| 变更次数 | 按配置限制；每次变更写历史 |
| 未选科 | `listUnselectedStudent` 可查应选未选学生；统计与明细口径一致（`REQ-STR-050`） |
| 组合校验 | 首选 1 门 + 再选 2 门，共 3 门；不得重复 |
""",
        "recovery": """
- 生成教学班失败：异步任务重试，幂等键 `generate_task_no`，不产生半成品
- 审批并发：乐观锁（`update_time` 或状态条件更新）拒绝重复审批
- 缓存失效失败：统计结果带范围摘要，范围变化即落新键
- 学生撤单：仅待审状态可撤，历史保留
""",
        "scope": """
- 学生本人：`getMyStream` / `submitMyStream` / `updateMyStream` 只看自己
- 年级主任 `DS-05`、班主任 `DS-06`、校领导 `DS-04` 按范围看统计与名单
- 统计接口与明细接口使用同一 `DataScopeResolver` 结果（`DS-DENY-08`）
""",
        "refs": """
- 时序图：`diagrams/sequence/stream-submit.mmd`、`diagrams/sequence/teaching-class-generate.mmd`
- 状态机：`diagrams/state/stream-change-request.mmd`
- 领域模型：`diagrams/class/stream-domain.mmd`
""",
        "acceptance": """
1. 首选非物理 / 历史被拒绝；再选超出固定集合被拒绝
2. 同一学生同一学期第二次提交选科被 `uk_student_stream` 拒绝（走变更申请）
3. 逾期提交未走审批时被拒绝，走审批后成功
4. 重复生成教学班不产生重复行
5. 统计数与明细数一致（同一用户同一范围）
""",
    },
    "subject": {
        "boundary": """
负责：学科主体（`edu_subject`）、学段启用（`edu_subject_stage`）、选科角色配置（`stream_enabled` /
`stream_role`）、批量初始化与引用检查、启用 / 停用。

不负责：选科结果（选科模块）、任教关系中的学科校验只读取本模块数据、课程表与排课（首轮不做）。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改学科 | `edu_subject` 单表 | 编码与名称双唯一 |
| 批量初始化 | 每 50 条一个事务 | 幂等：已存在的编码跳过并计入结果 |
| 保存学段启用 | `edu_subject_stage` 删除 + 插入 | 同事务；变更后失效 `subject:roles` 缓存 |
| 保存选科角色 | `edu_subject` 单表 | 变更后失效 `subject:roles` |
| 停用 | `edu_subject` 状态 + 引用检查 | 被任教关系或选科结果引用时拒绝 |
""",
        "concurrency": """
- `uk_subject_code (tenant_id, school_id, subject_code)` 与 `uk_subject_name (school_id, subject_name)` 兜底并发重复
- `uk_subject_stage (subject_id, stage_code)` 保证学段启用幂等
- 学科采用「一条主体 + 学段启用表」结构（`RV-SUB-04`）；不得按学段拆成多条主体记录，
  否则与「编码租户内唯一」冲突
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 编码 | 租户内唯一，创建后不可修改 |
| 名称 | 同一学校内唯一 |
| 学段启用 | 至少启用一个学段；停用前检查是否仍有班级 / 选科在用 |
| 选科角色 | 仅高中学科可配置 `stream_role`；首选集合与再选集合固定（`RV-SUB-03`） |
| 停用 | 被引用时拒绝，返回引用清单（任教关系数 / 选科结果数） |
""",
        "recovery": """
- 批量初始化部分失败：返回逐行结果，已成功记录保留，重跑幂等
- 缓存失效失败：`subject:roles` 版本号兜底
- 停用被拒：返回阻塞引用，前端提供跳转
""",
        "scope": """
- 全校可见作为字典（用于筛选与录入）；配置类操作仅教务主任 / 租户管理员
- 集团不默认读取下属学校学科配置
""",
        "refs": """
- 领域模型：`diagrams/class/subject-domain.mmd`
""",
        "acceptance": """
1. 同一租户重复学科编码被拒绝
2. 按学段拆多条主体记录被结构约束阻止（编码唯一）
3. 被任教关系引用的学科停用被拒绝
4. 修改选科角色后 `subject:roles` 缓存立即失效
""",
    },
    "school": {
        "boundary": """
负责：学校租户（`edu_school`）、校区（`edu_campus`）、学段启用（`edu_school_stage`）、
学校基线初始化（`initSchoolBaseline`）、数据共享授权（`edu_data_grant` / `edu_data_grant_scope`）。

不负责：租户体系本体（RuoYi 基线 `sys_tenant`）；集团与运营方租户的账号管理；
教学数据共享之外的数据流转（不存在的功能不要预留）。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增学校 | `edu_school` + `edu_school_stage` | 基线学段随学校一起建立 |
| 基线初始化 | `edu_school_stage` + `edu_campus` + 默认学科 | 幂等：已有配置时跳过并返回差异 |
| 校区增改 | `edu_campus` 单表 | 名称与编码双唯一 |
| 创建授权 | `edu_data_grant` + `edu_data_grant_scope` | 同事务；授权只读 |
| 撤销授权 | `edu_data_grant` 状态 + 缓存失效 | 历史记录保留，不做物理删除 |
""",
        "concurrency": """
- `uk_school_tenant (tenant_id)` 保证「一个租户对应一个学校」（`BR-ORG-001`）
- `uk_school_code (parent_tenant_id, school_code)`、`uk_campus_name` / `uk_campus_code` 兜底重复
- `uk_grant_no`、`uk_grant_scope (grant_id, scope_type, scope_id, resource_code, access_level)` 保证授权幂等
- 授权到期由定时任务置为 `expired`；缓存键 `grant:<school_tenant>:<resource_code>` 到期即失效
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 层级 | 运营方 → 集团 → 学校，不超过三级 |
| 一租户一学校 | 学校租户与集团租户都是租户；集团是一类租户，不是「下辖多校的容器」（`BR-ORG-001`） |
| 集团权限 | 集团不看下属学校教学数据；集团租户管理员可维护下属学校组织信息但读不到教学数据（`RV-SCH-05`） |
| 授权对象 | 仅教学资源（题库习题、试卷等）；学生、班级、年级、教师、成绩不参与共享（`BR-DATA-018`） |
| 授权级别 | 首轮仅 `read` / `export`；不开放写权限；无审批环节 |
| 授权记录 | 授权到期后历史保留，可查询已失效授权 |
| 平台运营 | 默认不可导出，需逐次授权并留痕（`IMP-Q-04`） |
""",
        "recovery": """
- 学校停用：不影响历史数据；停用后该校用户登录被拒
- 授权撤销：立即失效缓存；历史授权记录保留
- 基线初始化重复执行：幂等跳过并返回差异清单
- 集团跨校查询被拒：走 `DS-01` 或共享授权两条明确路径，不静默放行
""",
        "scope": """
- 平台运营租户是超级租户，`DS-01` 全平台范围，只读并留痕
- 学校租户内按 `DS-04` ~ `DS-08` 解析
- **集团租户**：默认只看集团自有数据；要看下属学校组织信息走 `DS-10`（只读）；
  要看教学数据必须由运营方创建数据共享授权（只读）
- 跨校共享与学生数据的边界：共享授权只覆盖教学资源，不含学生 / 班级 / 教师 / 成绩
""",
        "refs": """
- 时序图：`diagrams/sequence/data-grant.mmd`、`diagrams/sequence/school-baseline.mmd`
- 领域模型：`diagrams/class/school-domain.mmd`
""",
        "acceptance": """
1. 同一租户创建第二个学校被 `uk_school_tenant` 拒绝
2. 集团租户查询下属学校学生名单返回 403（集团无教学数据默认权限）
3. 共享授权只允许 `read` / `export`，提交写权限被拒绝
4. 授权到期后历史记录仍可查，且不再放行
5. `DS-01` 运营访问在租户侧可见并留痕
""",
    },
    "term": {
        "boundary": """
负责：学年（`edu_academic_year`）、学期（`edu_term`）、当前学期设置、学年归档与解除归档、引用检查。

不负责：节假日与作息（首轮不做）、课程表（首轮不做）、升班（引用学期但不执行）。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改学年 | `edu_academic_year` 单表 | 校验日期连续性 |
| 新增 / 修改学期 | `edu_term` 单表 | 校验属于同一学年且日期不重叠 |
| 设为当前学期 | `edu_term` 批量更新 | 同一事务内先清空再设置，保证唯一 |
| 归档学年 | `edu_academic_year` 状态 | 归档前引用检查 |
| 解除归档 | 同上 | 留痕 |
""",
        "concurrency": """
- `uk_academic_year_code (tenant_id, school_id, academic_year_code)`、`uk_term_code (academic_year_id, term_code)` 兜底重复
- 「当前学期唯一」：同一学校同一时刻只有一条 `is_current=1`，由事务 + 应用层唯一性检查保证；
  `idx_term_school_current (school_id, is_current)` 提供查询支持
- 并发「设为当前」：对学校维度加锁（或对 `edu_term` 做条件更新），后者覆盖前者而不是并存
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 学年日期连续 | 必须连续不重叠：前一年结束日 = 后一年开始日 − 1 天（`RV-TERM-08`） |
| 学期归属 | 学期必须属于同一学年，且日期落在学年范围内 |
| 当前学期 | 同一学校同一时刻只有一条 |
| 归档 | 有未结束的升班任务或未归档班级时，需提示影响范围（不强制阻断，记录确认） |
| 引用检查 | `checkTermReference` 返回班级 / 学生 / 任务数量 |
""",
        "recovery": """
- 归档失败：回滚，保持未归档
- 解除归档：恢复可编辑，留痕
- 「设为当前」失败：原当前学期不变
""",
        "scope": """
- 全校可见；配置操作仅租户管理员 / 教务主任
- 当前学期缓存键 `edu:<tenant>:term:current`，设为当前后立即删除
""",
        "refs": """
- 状态机：`diagrams/state/term-archive.mmd`
- 领域模型：`diagrams/class/term-domain.mmd`
""",
        "acceptance": """
1. 学年日期出现重叠或断档被拒绝
2. 并发设置当前学期后只保留一条 `is_current=1`
3. 已归档学年不能新增学期
4. 解除归档后恢复可编辑并留痕
""",
    },
    "import-export": {
        "boundary": """
负责：导入模板（`edu_import_template`）、导入批次与错误行（`edu_import_batch` / `edu_import_error`）、
异步任务与重试（`edu_async_task` / `edu_async_task_retry`）、死信（`edu_dead_letter_task`）、
文件引用（`edu_file_ref`）、通用导出。

不负责：各业务模块的业务校验（由对应模块在导入执行时提供校验器）；文件存储本体（对象存储）。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 上传 + 结构校验 | `edu_import_batch` + `edu_async_task` | 同事务创建批次与任务；校验结果写批次 |
| 导入执行 | 每 500 行一个事务 | 错误行独立提交，保证部分成功可续跑 |
| 导出 | `edu_async_task` + `edu_file_ref` | 任务完成时写文件引用 |
| 任务重试 | `edu_async_task_retry` + 任务状态 | 同事务记录重试序号 |
| 死信重放 | `edu_dead_letter_task` 状态 + 原任务 | 复用原幂等键，不新建任务 |
""",
        "concurrency": """
- `uk_import_batch_no`、`uk_async_task_no`、`uk_task_retry (task_no, retry_no)`、`uk_dead_letter_task` 保证幂等
- 消费幂等：先按 `task_no` / `batch_no` 查状态，已成功的消息直接 ack；唯一索引兜底
- 并发配额：同一用户 1 个、同一学校 3 个，超出排队（`REQ-IMP-047` / `REQ-IMP-048`）
- 任务状态更新使用条件更新（`WHERE task_status = 'running'`），避免重复消费覆盖结果
""",
        "validation": """
| 规则 | 取值 |
|---|---|
| 同步导入上限 | 5000 行 / 10 MB / 30 秒校验（`IMP-Q-01`） |
| 同步导出上限 | 2000 行，超出转异步 |
| 学生导入模板 | 14 列（已确认） |
| 模板过期 | 过期后仍可下载但强提示（`IMP-Q-05`） |
| 文件有效期 | 导出与结果文件 7 天，任务元数据 90 天（`IMP-Q-02`） |
| 重试 | 最大 3 次，退避 30s / 2m / 8m（`REQ-IMP-039`） |
| 平台运营导出 | 默认不可导出，需逐次授权并留痕（`IMP-Q-04`） |
""",
        "recovery": """
- 超过重试上限：进 `edu.task.dead`，落 `edu_dead_letter_task`，运维在死信页重放
- 消息丢失：定时巡检把长时间 `queued` / `running` 的任务标记异常并可重试（`NFR-MQ-01`）
- 文件过期：任务元数据保留 90 天，文件失效后提示重新生成
- 导出失败：任务置 `failed`，重试复用 `task_no`
""",
        "scope": """
- 任务列表默认只看本人任务；学校管理员可看本校任务（按 `DS-04`）
- 下载结果文件时重新解析数据范围（`DS-DENY-04`），文件行数可能少于列表
- 平台运营下载需逐次授权，授权记录对租户可见（`AUD-Q-03`）
""",
        "refs": """
- 时序图：`diagrams/sequence/import-execute.mmd`、`diagrams/sequence/export-generate.mmd`
- 状态机：`diagrams/state/async-task.mmd`
- 领域模型：`diagrams/class/task-domain.mmd`
""",
        "acceptance": """
1. 同一 `batch_no` 重复执行不产生重复数据
2. 重试 3 次后进死信，重放复用原幂等键
3. 超出并发配额的任务进入排队而不是失败
4. 导出下载时范围变化，文件行数随之减少并给出说明
5. 过期模板仍可下载但出现强提示
""",
    },
    "audit": {
        "boundary": """
负责：操作日志（`edu_audit_log`）、变更明细（`edu_audit_change`）、归档批次
（`edu_audit_archive_batch`）、派生归档表与视图、运营访问记录、安全事件、归档检索。

不负责：业务对象的变更本身；日志的业务语义解释由各模块在写入时提供；
登录认证日志的主体在基线系统，本模块只做教育域扩展。
""",
        "tx": """
| 操作 | 事务范围 | 说明 |
|---|---|---|
| 业务写 + 日志 | 业务事务内写 `edu_audit_log`（+ `edu_audit_change`） | 与业务同事务，保证「业务成功则日志必在」 |
| 异步场景日志 | 消费者处理成功后写日志 | 通过 `edu.audit.exchange` 补偿 |
| 归档批次创建 | `edu_audit_archive_batch` + 数据搬迁 | 同构归档表，按批次搬迁后记录区间 |
| 归档检索 | 只读 | 命中归档区间时路由到归档表 |
""",
        "concurrency": """
- `uk_audit_idempotent (request_id, object_id, action_type)` 保证重试不产生重复日志
- `uk_audit_change (log_id, field_name)` 保证同一日志同一字段只有一条变更明细
- 归档与在线查询并发：归档只搬迁已关闭批次区间，查询按「在线 + 归档」并集去重
- 不使用 MySQL 原生分区：分区要求所有唯一键含分区列，会破坏幂等语义；
  改用同构归档表 `edu_audit_log_archive` + 归档批次留痕（已在 `schema.yaml` 记录）
""",
        "validation": """
| 规则 | 说明 |
|---|---|
| 变更明细 | 只记录发生变化的字段（`AUD-Q-02`） |
| 运营访问 | 对租户全量可见，用途说明必填（`AUD-Q-03`） |
| 保留策略 | ≥ 3 年；在线 12 个月后归档，归档仍可检索（`AUD-Q-04`） |
| 写入降级 | 日志写入持续失败时业务进入只读降级（`AUD-Q-05`） |
| 查询 / 导出上限 | 90 天 / 5 万行（`AUD-Q-06`） |
| 操作日志与审计日志 | 合并为一张表 + 视图区分（已确认 A 方案） |
""",
        "recovery": """
- 日志写入失败：重试；持续失败进入只读降级并告警，不静默丢日志
- 归档中断：批次保持 `running`，可重试；已搬迁数据不重复搬迁（按区间幂等）
- 归档查询超时：> 30 秒转异步（审计 PRD 第 9 节）
- 幂等冲突：视为重复请求，直接跳过写入
""",
        "scope": """
- 校领导 `DS-04` 看本校日志；年级主任 / 班主任按各自范围看本人相关记录
- 平台运营访问记录对租户全量可见（`AUD-Q-03`）
- 安全事件仅平台运营与租户管理员可见
- 日志只追加，不提供修改与删除接口
""",
        "refs": """
- 时序图：`diagrams/sequence/audit-write.mmd`、`diagrams/sequence/audit-archive.mmd`
- 状态机：`diagrams/state/archive-batch.mmd`
- 领域模型：`diagrams/class/audit-domain.mmd`
""",
        "acceptance": """
1. 同一 `(request_id, object_id, action_type)` 重复写入只产生一条日志
2. 变更明细只包含实际变化的字段
3. 运营访问缺用途说明被拒绝
4. 日志写入持续失败时业务进入只读降级并告警
5. 归档后在线表与归档表检索结果并集完整、无重复
""",
    },
}


# (code, module, http, level, message, trigger, handling)
ERROR_CODES = [
    # 学生管理
    ("EDU-STU-4001", "student", 409, "error", "学号已存在（平台唯一）：{student_no}",
     "新增 / 修改学生时学号命中 `uk_student_no`", "前端在学号输入框下方提示，保留其余已填内容"),
    ("EDU-STU-4002", "student", 409, "error", "证件号已存在（平台唯一）：{id_card_no}",
     "证件号命中 `uk_id_card_no`", "提示可能为重复建档，提供「按证件号检索」入口"),
    ("EDU-STU-4003", "student", 409, "error", "该学生同一字段已有待审核申请：{field_name}",
     "命中 `uk_sfc_pending`", "提示申请编号与提交时间，给出跳转到申请详情的入口"),
    ("EDU-STU-4004", "student", 400, "error", "监护人绑定数量已达上限（3 人）",
     "绑定第 4 名监护人", "提示先解绑；解绑需班主任确认"),
    ("EDU-STU-4005", "student", 400, "error", "激活码不可用或已被使用",
     "条件更新影响行数为 0", "提示联系班主任重置，不区分「不存在」与「已使用」"),
    ("EDU-STU-4006", "student", 403, "error", "任课教师不可导出班级名单",
     "任课教师调用导出接口", "功能权限直接拒绝；导出按钮对任课教师不渲染"),
    # 教师管理
    ("EDU-TCH-4001", "teacher", 409, "error", "工号已存在（学校租户内唯一）：{teacher_no}",
     "命中 `uk_teacher_no`", "提示工号占用；改工号走 `updateTeacherNo` 并留痕"),
    ("EDU-TCH-4002", "teacher", 409, "error", "该任教关系已存在",
     "命中 `uk_assignment`", "幂等场景直接视为成功；界面提示「已存在，未重复添加」"),
    ("EDU-TCH-4003", "teacher", 400, "error", "该教师仍存在未结束的任职，不能离职",
     "离职校验发现任教关系 / 班主任 / 年级主任", "返回阻塞清单与跳转入口，逐项解除后重试"),
    # 年级管理
    ("EDU-GRD-4001", "grade", 400, "error", "年级序号超出学段范围：{stage_code} 允许 1-{max_level}",
     "学段序号映射校验（RV-GRD-03）", "前端在年级序号选择器上限制可选值"),
    ("EDU-GRD-4002", "grade", 409, "error", "同一学校同一学段已存在同名年级：{grade_name}",
     "命中 `uk_grade_name`", "提示已存在年级，提供跳转"),
    ("EDU-GRD-4003", "grade", 400, "error", "该年级仍有班级或学生，不能删除",
     "删除前引用检查", "返回班级数 / 学生数，改为「归档」或先移出学生"),
    # 班级管理
    ("EDU-CLS-4001", "class", 409, "error", "同一学年学期内该学生已属于行政班：{class_name}",
     "命中 `uk_class_member_admin`", "提供「调班」入口而不是重复编班"),
    ("EDU-CLS-4002", "class", 409, "error", "同一学期同一学段已存在同名班级：{class_name}",
     "命中 `uk_class_name`", "提示占用，保留其余输入"),
    ("EDU-CLS-4003", "class", 400, "error", "该班级仍有在读学生，不能删除",
     "删除前引用检查（BR-CLASS-006）", "提示改为「停用」，并给出一键停用"),
    ("EDU-CLS-4004", "class", 409, "error", "教学班生成冲突：同一组合已存在",
     "命中 `uk_class_teaching` / `uk_tclass_member`", "按幂等处理，回显已存在教学班，不重复创建"),
    ("EDU-CLS-5004", "class", 500, "error", "教学班生成任务执行失败：{reason}",
     "教学班生成异步任务失败（原型 teaching-class-list 场景）", "任务中心可见，支持重试；重试复用幂等键"),
    # 升班与学籍异动
    ("EDU-PRM-4001", "promotion", 400, "error", "源学期与目标学期必须连续",
     "升班任务创建校验", "给出推荐目标学期"),
    ("EDU-PRM-4002", "promotion", 409, "error", "该校同一源 / 目标学期已有执行中的升班任务",
     "并发执行保护", "提示已有任务编号，跳转任务详情"),
    ("EDU-PRM-4003", "promotion", 400, "error", "到顶年级不能继续升班，请走毕业流程",
     "小学 6 年级 / 初中 3 年级 / 高中 3 年级", "引导到毕业异动入口"),
    ("EDU-PRM-5012", "promotion", 500, "error", "升班预览计算失败：{reason}",
     "预览阶段数据异常（原型 promotion-preview 场景）", "保留任务为草稿，可重新预览；错误详情写审计"),
    ("EDU-PRM-5021", "promotion", 400, "error", "升班校验未通过：目标学期缺少班级结构",
     "执行前校验（原型 promotion-validate 场景）", "返回缺失清单，跳转班级管理补齐"),
    ("EDU-PRM-5033", "promotion", 409, "error", "升班任务正在执行，不能重复提交",
     "原型 promotion-execute 场景", "展示任务进度与预计剩余时间"),
    ("EDU-PRM-5044", "promotion", 500, "error", "升班结果导出失败：{reason}",
     "原型 promotion-result 场景", "转异步导出，完成后在任务中心下载"),
    ("EDU-TRF-4001", "promotion", 400, "error", "转学单缺少双方学校或状态不允许该操作",
     "转学单状态机校验", "提示当前状态与可执行动作"),
    ("EDU-TRF-5001", "promotion", 500, "error", "转学单创建失败：{reason}",
     "原型 promotion-transfer / student-cross-transfer 场景", "保留草稿，可重试；错误写审计"),
    ("EDU-TRF-5002", "promotion", 500, "error", "转学历史查询失败：{reason}",
     "原型 promotion-history 场景", "缩小时间范围或转异步导出"),
    # 选科
    ("EDU-STR-4001", "stream", 400, "error", "选科组合不合法：首选须为物理或历史，再选须在固定集合内",
     "RV-SUB-03 固定集合校验", "前端用固定选项渲染，后端二次校验"),
    ("EDU-STR-4002", "stream", 400, "error", "选科变更已超过截止时间，需校级管理员审批",
     "截止时间可配置，逾期需审批（已确认口径）", "引导提交带审批的变更申请"),
    ("EDU-STR-4003", "stream", 400, "error", "非高中学生不参与 3+1+2 选科",
     "学段校验", "不渲染选科入口"),
    ("EDU-STR-5001", "stream", 500, "error", "选科配置保存失败：{reason}",
     "原型 stream-config 场景", "保留表单内容，可重试；失败原因写审计"),
    ("EDU-STR-5002", "stream", 500, "error", "选科提交失败：{reason}",
     "原型 stream-selection 场景", "保留已选内容，提示重试"),
    ("EDU-STR-5003", "stream", 500, "error", "选科名单查询失败：{reason}",
     "原型 stream-list 场景", "缩小范围或稍后重试"),
    ("EDU-STR-5004", "stream", 500, "error", "选科统计失败：{reason}",
     "原型 stream-stat 场景（统计与明细同口径，DS-DENY-08）", "范围过大时转异步"),
    ("EDU-STR-5005", "stream", 409, "error", "该变更申请已被处理，不能重复审批",
     "原型 stream-approve 场景的状态机保护", "刷新申请详情，展示最终状态"),
    ("EDU-STR-5006", "stream", 500, "error", "教学班生成失败：{reason}",
     "原型 stream-generate-class 场景", "任务重试复用幂等键，不产生半成品"),
    # 学科
    ("EDU-SUB-4001", "subject", 409, "error", "学科编码已存在（租户内唯一）：{subject_code}",
     "命中 `uk_subject_code`", "提示编码占用"),
    ("EDU-SUB-4002", "subject", 409, "error", "学科名称已存在：{subject_name}",
     "命中 `uk_subject_name`", "提示占用"),
    ("EDU-SUB-4003", "subject", 400, "error", "该学科已被引用，不能停用",
     "引用检查发现任教关系 / 选科结果", "返回引用清单与跳转入口"),
    ("EDU-SUB-5001", "subject", 500, "error", "学科数据加载失败：{reason}",
     "原型 subject-list 场景", "重试；持续失败进入只读降级"),
    # 学校与租户
    ("EDU-ORG-4001", "school", 409, "error", "该租户已绑定学校",
     "命中 `uk_school_tenant`（BR-ORG-001）", "一个租户对应一个学校，不允许新增第二个"),
    ("EDU-ORG-4002", "school", 409, "error", "校区编码 / 名称重复：{value}",
     "命中 `uk_campus_code` / `uk_campus_name`", "提示占用"),
    ("EDU-ORG-4003", "school", 400, "error", "组织层级超过三级（运营方 → 集团 → 学校）",
     "层级校验", "提示不允许再建下级"),
    ("EDU-ORG-4004", "school", 403, "error", "集团默认不能读取下属学校教学数据",
     "集团租户查询教学数据", "引导走数据共享授权或 DS-01 运营路径"),
    ("EDU-ORG-4005", "school", 400, "error", "共享授权只允许 read / export 级别",
     "BR-DATA-018 授权级别校验", "写权限一律拒绝"),
    ("EDU-ORG-5001", "school", 500, "error", "学校列表加载失败：{reason}",
     "原型 school-list 场景", "重试；错误写审计"),
    ("EDU-ORG-5002", "school", 500, "error", "校区保存失败：{reason}",
     "原型 school-campus 场景", "保留表单，可重试"),
    ("EDU-ORG-5010", "school", 400, "error", "基线初始化顺序错误，必须按「学校 → 学段 → 年级 → 学年 → 学期」",
     "原型 school-init 场景", "返回缺失前置项与顺序提示"),
    # 学年学期
    ("EDU-TERM-4001", "term", 400, "error", "学年日期必须连续不重叠（RV-TERM-08）",
     "学年日期校验", "前端在保存前做同校已有学年的重叠预校验"),
    ("EDU-TERM-4002", "term", 400, "error", "学期日期必须落在所属学年范围内",
     "学期日期校验", "按学年范围限制日期选择器"),
    ("EDU-TERM-5003", "term", 500, "error", "学年保存失败：{reason}",
     "原型 term-list 场景", "保留表单，可重试"),
    ("EDU-TERM-5004", "term", 500, "error", "学期保存失败：{reason}",
     "原型 term-terms 场景", "保留表单，可重试"),
    ("EDU-TERM-5031", "term", 409, "error", "目标学期不可用：已归档或不存在",
     "原型 promotion-create 场景", "返回可用学期清单"),
    # 导入导出与异步任务
    ("EDU-IMP-4001", "import-export", 400, "error", "导入文件超过同步上限（5000 行 / 10 MB）",
     "IMP-Q-01 阈值校验", "提示拆分文件或转异步"),
    ("EDU-IMP-4002", "import-export", 400, "error", "模板版本不匹配：期望 {expected}，实际 {actual}",
     "模板版本校验", "提供最新模板下载入口"),
    ("EDU-IMP-4003", "import-export", 429, "error", "任务并发配额已满（同一用户 1 个 / 同一学校 3 个）",
     "REQ-IMP-047 / REQ-IMP-048", "进入排队并展示预计等待"),
    ("EDU-IMP-4004", "import-export", 400, "error", "文件已过期（有效期 7 天）",
     "IMP-Q-02", "提示重新生成文件"),
    ("EDU-IMP-5006", "import-export", 500, "error", "异步任务查询失败：{reason}",
     "原型 async-task-list 场景", "重试；失败原因写审计"),
    ("EDU-IMP-5007", "import-export", 500, "error", "死信任务重放失败：{reason}",
     "原型 dead-letter-task 场景", "保持死信状态，提示运维排查后再次重放"),
    # 审计
    ("EDU-AUD-4001", "audit", 400, "error", "运营访问记录必须填写用途说明",
     "AUD-Q-03", "前端必填校验 + 后端二次校验"),
    ("EDU-AUD-4002", "audit", 400, "error", "查询区间不得超过 90 天或 5 万行",
     "AUD-Q-06", "提示拆分区间或转异步导出"),
    ("EDU-AUD-4003", "audit", 503, "error", "日志写入持续失败，业务进入只读降级",
     "AUD-Q-05", "页面顶部横幅提示只读，恢复后自动解除"),
    ("EDU-AUD-5001", "audit", 500, "error", "操作日志查询失败：{reason}",
     "原型 audit-log-list 场景", "缩小范围或转异步"),
    ("EDU-AUD-5002", "audit", 500, "error", "运营访问记录查询失败：{reason}",
     "原型 audit-ops-access 场景", "重试；记录访问本身留痕"),
    ("EDU-AUD-5003", "audit", 500, "error", "敏感字段访问记录查询失败：{reason}",
     "原型 audit-sensitive-access 场景", "重试；敏感查询失败也需留痕"),
    ("EDU-AUD-5004", "audit", 500, "error", "安全事件查询失败：{reason}",
     "原型 audit-security-event 场景", "重试"),
    ("EDU-AUD-5005", "audit", 500, "error", "归档批次执行失败：{reason}",
     "原型 audit-archive 场景", "批次保持 running，可重试；已搬迁区间不重复搬迁"),
    # 系统
    ("EDU-SYS-4001", "system", 400, "error", "缺少租户或学校上下文，拒绝执行",
     "DS-DENY-01 / DS-DENY-02", "返回 403 并说明缺失上下文"),
    ("EDU-SYS-4002", "system", 403, "error", "该对象超出你的数据范围",
     "DS-DENY-06 / DS-DENY-07", "按 ID 查询返回「查不到」；批量操作整体拒绝并列出越权对象"),
    ("EDU-SYS-5001", "system", 500, "error", "服务异常，请使用请求编号联系运维",
     "原型 500.html 场景（未捕获异常）", "返回请求编号，记录 trace_id；不返回堆栈"),
    ("EDU-SYS-5002", "system", 500, "error", "数据库操作失败：{reason}",
     "持久层异常", "回滚事务，返回请求编号"),
    ("EDU-SYS-5003", "system", 503, "error", "消息中间件不可用，任务已排队",
     "RabbitMQ 不可用", "任务保持 queued，恢复后继续；不丢任务"),
]


def gen_error_codes():
    lines = [
        "# 错误码表（教育域）",
        "",
        "> 本文件由 `tools/gen_stage5_docs.py` 生成。错误码在阶段 5 定义；",
        "> 原型 `prototypes/functional/v1/pages/*.html` 中已使用的码必须在此表存在，反向也要成立。",
        "",
        "## 1. 编码规则",
        "",
        "- 格式：`EDU-<MODULE>-<4 位序号>`",
        "- 号段：`4001-4099` 参数与业务规则校验；`5001-5099` 执行失败与状态冲突；",
        "  `6xxx` 预留权限细分；`9xxx` 预留系统级",
        "- `request_id` 与错误码一起返回，前端错误态必须同时展示两者（原型已如此实现）",
        "- 权限拒绝统一走 `EDU-SYS-4001` / `EDU-SYS-4002`，不按业务模块另起编号",
        "",
        "## 2. 模块码",
        "",
        "| 前缀 | 模块 |",
        "|---|---|",
    ]
    prefix_map = {
        "STU": "学生管理", "TCH": "教师管理", "GRD": "年级管理", "CLS": "班级管理",
        "PRM": "升班与学籍异动", "TRF": "跨校转学单", "STR": "3+1+2 选科",
        "SUB": "学科与配置", "ORG": "学校与租户", "TERM": "学年学期",
        "IMP": "导入导出与异步任务", "AUD": "审计与操作日志", "SYS": "通用 / 系统",
    }
    for k, v in prefix_map.items():
        lines.append(f"| `EDU-{k}` | {v} |")

    lines += ["", "## 3. 错误码清单", "", "| 错误码 | 模块 | HTTP | 级别 | 文案 | 触发条件 | 前端处理 |", "|---|---|---|---|---|---|---|"]
    for code, module, http, level, msg, trigger, handling in ERROR_CODES:
        lines.append(
            f"| `{code}` | {MODULE_CN.get(module, module)} | {http} | {level} | {msg} | {trigger} | {handling} |"
        )
    lines += ["", f"共 {len(ERROR_CODES)} 个错误码。", ""]
    write(os.path.join(S40, "api", "error-codes.yaml"), _error_codes_yaml())
    write(os.path.join(S40, "api", "error-codes.md"), "\n".join(lines))


def _error_codes_yaml():
    out = [
        'schema_version: "1.0"',
        "purpose: 教育域统一错误码表（阶段 5 定义，阶段 6 / 7 实现）",
        "conventions:",
        '  format: "EDU-<MODULE>-<4 位序号>"',
        '  ranges: "4001-4099 校验；5001-5099 执行失败；6xxx 权限细分；9xxx 系统级"',
        '  request_id: "所有错误响应必须带 request_id，前端错误态同时展示错误码与请求编号"',
        "statuses:",
        '  - "已设计"',
        "codes:",
    ]
    for code, module, http, level, msg, trigger, handling in ERROR_CODES:
        out += [
            f'  - code: "{code}"',
            f'    module: "{module}"',
            f"    http_status: {http}",
            f'    level: "{level}"',
            "    message: " + _yaml_str(msg),
            "    trigger: " + _yaml_str(trigger),
            "    handling: " + _yaml_str(handling),
            '    status: "已设计"',
        ]
    return "\n".join(out) + "\n"


def _yaml_str(s: str) -> str:
    if ": " in s or "：" in s or s.startswith(("{", "[", "*", "&", "#", "-", "?")) or '"' in s:
        return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'
    return '"' + s.replace('"', '\\"') + '"'


SEQUENCES = {
    "student-enroll.mmd": """sequenceDiagram
  autonumber
  participant T as 班主任
  participant UI as 学生管理页
  participant API as ruoyi-edu
  participant DB as MySQL
  participant AUD as 审计

  T->>UI: 填写学生基本信息 + 监护人
  UI->>API: POST addStudent
  API->>API: 校验学号 / 证件号平台唯一、学校上下文
  API->>DB: BEGIN
  API->>DB: INSERT edu_student（平台级，无 tenant_id）
  API->>DB: INSERT edu_student_enrollment（school_id + 学籍状态）
  API->>DB: INSERT edu_student_guardian（可选）
  API->>AUD: 同事务写 edu_audit_log
  API->>DB: COMMIT
  API-->>UI: 学生编号 + 登录名 s+学号
  UI->>API: POST 签发激活码（打印批次）
  API->>DB: INSERT edu_activation_code（uk_activation_active 兜底）
  API-->>T: 打印密码条
""",
    "student-import.mmd": """sequenceDiagram
  autonumber
  participant T as 教务老师
  participant UI as 导入向导
  participant API as ruoyi-edu
  participant MQ as RabbitMQ
  participant W as 导入消费者
  participant DB as MySQL

  T->>UI: 上传文件
  UI->>API: POST importStudentValidate
  API->>API: 结构校验 + 行数 / 大小 / 模板版本校验（≤5000 行 / 10MB / 30s）
  API->>DB: INSERT edu_import_batch + edu_async_task
  API-->>UI: 校验结果（通过行 / 失败行）
  T->>UI: 确认执行
  UI->>API: POST importStudentExecute
  API->>MQ: edu.import.execute（task_no 幂等键）
  MQ->>W: 投递
  loop 每 500 行一个事务
    W->>DB: 写入学生 / 在校记录
    W->>DB: 失败行写 edu_import_error
  end
  W->>DB: 更新 edu_async_task（succeeded / partial_failed）
  UI->>API: GET getAsyncTask
  API-->>T: 进度 + 失败行下载入口
""",
    "teacher-assignment.mmd": """sequenceDiagram
  autonumber
  participant A as 教务主任
  participant UI as 教师管理页
  participant API as ruoyi-edu
  participant DB as MySQL
  participant R as Redis

  A->>UI: 批量设置任教关系
  UI->>API: POST batchSaveTeachingAssignment
  API->>API: 校验学科启用、班级学期一致、教师在职
  API->>DB: BEGIN
  API->>DB: DELETE 旧关系（按 term + teacher）
  API->>DB: INSERT edu_teaching_assignment（uk_assignment 幂等）
  API->>DB: COMMIT
  API->>R: DEL edu:<tenant>:scope:teaching:<user>:<term>
  API-->>A: 成功条数 + 跳过条数
  Note over API,R: 缓存删除失败时靠范围版本号兜底（C-04）
""",
    "class-roster-import.mmd": """sequenceDiagram
  autonumber
  participant T as 班主任
  participant UI as 班级成员页
  participant API as ruoyi-edu
  participant DB as MySQL

  T->>UI: 上传编班表
  UI->>API: POST importRosterValidate
  API->>API: 校验班级 / 学生 / 学期一致
  API-->>UI: 校验结果
  T->>UI: 确认执行
  UI->>API: POST importRosterExecute
  loop 每 200 行一个事务
    API->>DB: INSERT / UPDATE edu_class_member
    Note over API,DB: uk_class_member_admin 保证一个学生一个行政班
  end
  API-->>T: 成功 / 失败行清单
""",
    "class-transfer.mmd": """sequenceDiagram
  autonumber
  participant T as 班主任
  participant UI as 班级管理页
  participant API as ruoyi-edu
  participant DB as MySQL
  participant R as Redis

  T->>UI: 选择学生 → 调班
  UI->>API: POST transferClass
  API->>API: 数据范围校验（DS-06：只能操作本班）
  API->>DB: BEGIN
  API->>DB: UPDATE edu_class_member SET class_id = 目标班 WHERE term+student
  API->>DB: 目标班容量只提示不拦截（BR-CLASS-005）
  API->>DB: COMMIT
  API->>R: DEL 相关名单缓存
  API-->>T: 调班成功
  Note over API,DB: 并发调班由 uk_class_member_admin + 行锁保证不出现双班
""",
    "promotion-execute.mmd": """sequenceDiagram
  autonumber
  participant A as 教务主任
  participant UI as 升班执行页
  participant API as ruoyi-edu
  participant MQ as RabbitMQ
  participant W as 升班消费者
  participant DB as MySQL

  A->>UI: 选择源 / 目标学期
  UI->>API: POST previewPromotionTask
  API->>DB: 写 edu_promotion_task + edu_promotion_item（预演）
  API-->>UI: 预览结果（升班 / 跳过 / 异常）
  A->>UI: 确认执行
  UI->>API: POST executePromotionTask
  API->>DB: 行锁校验：同校同源/目标学期无 running 任务
  API->>MQ: edu.promotion.execute（task_no 幂等）
  MQ->>W: 投递
  loop 每 200 名学生
    W->>DB: 目标学期班级关系（按学年追加，不覆盖历史）
    W->>DB: 更新 edu_promotion_item.item_status
  end
  W->>DB: 任务置 succeeded / partial_failed
  UI->>API: GET getPromotionTask
  API-->>A: 结果与失败清单
""",
    "stream-submit.mmd": """sequenceDiagram
  autonumber
  participant S as 学生
  participant UI as 选科页
  participant API as ruoyi-edu
  participant DB as MySQL
  participant R as Redis

  S->>UI: 选择 1 门首选 + 2 门再选
  UI->>API: POST submitMyStream
  API->>API: 校验固定集合（物理/历史 + 化学生物思想政治地理）
  API->>API: 校验是否超过截止时间；逾期需审批
  API->>DB: BEGIN
  API->>DB: INSERT edu_student_stream（uk_student_stream）
  API->>DB: INSERT edu_stream_history
  API->>DB: COMMIT
  API->>R: DEL edu:<tenant>:stream:stat:<term>:*
  API-->>S: 提交成功
""",
    "teaching-class-generate.mmd": """sequenceDiagram
  autonumber
  participant A as 教务主任
  participant UI as 教学班生成页
  participant API as ruoyi-edu
  participant MQ as RabbitMQ
  participant W as 教学班消费者
  participant CLS as 班级模块
  participant DB as MySQL

  A->>UI: 选择学期 + 组合
  UI->>API: POST previewTeachingClassGenerate
  API-->>UI: 预览（组合 → 人数 → 建议班数）
  A->>UI: 确认生成
  UI->>API: POST executeTeachingClassGenerate
  API->>MQ: edu.teaching-class.generate（generate_task_no 幂等）
  MQ->>W: 投递
  W->>CLS: 调用班级模块写入接口（唯一写入入口，DP-01）
  CLS->>DB: INSERT edu_teaching_class + edu_teaching_class_member
  Note over CLS,DB: uk_class_teaching / uk_tclass_member 保证幂等
  W->>DB: 更新任务状态
  API-->>A: 生成结果
""",
    "data-grant.mmd": """sequenceDiagram
  autonumber
  participant O as 平台运营
  participant UI as 数据共享授权页
  participant API as ruoyi-edu
  participant DB as MySQL
  participant R as Redis

  O->>UI: 选择授权对象学校 + 教学资源 + read/export
  UI->>API: POST 创建授权
  API->>API: 校验资源类型只限题库习题 / 试卷（BR-DATA-018）
  API->>DB: BEGIN
  API->>DB: INSERT edu_data_grant
  API->>DB: INSERT edu_data_grant_scope
  API->>DB: COMMIT
  API->>R: DEL edu:<tenant>:grant:<school>:<resource>
  API-->>O: 授权编号 + 有效期
  Note over API,DB: 撤销后历史保留，不做物理删除
""",
    "school-baseline.mmd": """sequenceDiagram
  autonumber
  participant O as 运营 / 租户管理员
  participant UI as 学校初始化页
  participant API as ruoyi-edu
  participant DB as MySQL

  O->>UI: 执行基线初始化
  UI->>API: POST initSchoolBaseline
  API->>API: 校验顺序：学校 → 学段 → 年级 → 学年 → 学期
  API->>DB: BEGIN
  API->>DB: upsert edu_school_stage
  API->>DB: upsert edu_campus
  API->>DB: 初始化默认学科（edu_subject + edu_subject_stage）
  API->>DB: COMMIT
  API-->>O: 初始化结果与差异清单
""",
    "import-execute.mmd": """sequenceDiagram
  autonumber
  participant U as 用户
  participant API as ruoyi-edu
  participant DB as MySQL
  participant MQ as RabbitMQ
  participant W as 消费者

  U->>API: validateImportFile
  API->>DB: INSERT edu_import_batch（batch_no 幂等）
  API-->>U: 校验结果
  U->>API: executeImport
  API->>API: 并发配额校验（用户 1 / 学校 3）
  API->>MQ: 投递 edu.import.execute
  MQ->>W: 消费
  W->>DB: 按批写业务表
  alt 成功
    W->>DB: 任务 succeeded
  else 部分失败
    W->>DB: 任务 partial_failed + edu_import_error
  else 超过重试
    W->>DB: 进 edu_dead_letter_task
  end
  U->>API: 查询任务 / 下载结果
""",
    "export-generate.mmd": """sequenceDiagram
  autonumber
  participant U as 用户
  participant API as ruoyi-edu
  participant MQ as RabbitMQ
  participant W as 导出消费者
  participant DB as MySQL

  U->>API: POST exportData
  API->>API: 行数估算；> 2000 行转异步
  API->>DB: INSERT edu_async_task（task_no 幂等）
  API->>MQ: edu.export.generate
  MQ->>W: 消费
  W->>API: 重新解析数据范围（DS-DENY-04）
  W->>DB: 生成文件 + edu_file_ref（7 天有效）
  U->>API: downloadTaskResult
  API->>API: 再次解析范围后签发下载
  API-->>U: 文件（行数可能少于列表，页面有说明）
""",
    "audit-write.mmd": """sequenceDiagram
  autonumber
  participant API as 业务服务
  participant DB as MySQL
  participant MQ as RabbitMQ
  participant AC as 审计消费者

  API->>DB: BEGIN 业务事务
  API->>DB: 写业务表
  API->>DB: INSERT edu_audit_log + edu_audit_change（同事务）
  API->>DB: COMMIT
  alt 日志写入失败
    API->>MQ: edu.audit.compensate
    MQ->>AC: 补偿写入
    alt 持续失败
      AC->>API: 触发只读降级（AUD-Q-05）
    end
  end
""",
    "audit-archive.mmd": """sequenceDiagram
  autonumber
  participant S as 定时任务
  participant API as ruoyi-edu
  participant DB as MySQL
  participant ARC as 归档消费者

  S->>API: 创建归档批次
  API->>DB: INSERT edu_audit_archive_batch（archive_no 幂等）
  API->>ARC: 投递 edu.task.archive
  loop 按区间搬迁
    ARC->>DB: INSERT INTO edu_audit_log_archive
    ARC->>DB: DELETE 在线区间
  end
  ARC->>DB: 批次置 succeeded + 记录区间
  Note over API,DB: 不使用原生分区，避免唯一键必须包含分区键
""",
}


STATES = {
    "student-enrollment-status.mmd": """stateDiagram-v2
  [*] --> 转入未报到
  转入未报到 --> 在读 : 报到
  在读 --> 休学 : 休学
  休学 --> 在读 : 复学
  在读 --> 转出 : 转学（转出单办结）
  在读 --> 毕业 : 学段到顶且合格
  在读 --> 结业 : 学段到顶未达毕业条件
  在读 --> 肄业 : 未完成学业离校
  在读 --> 出国 : 出国
  在读 --> 失踪 : 失踪
  在读 --> 退学 : 退学（需审批）
  在读 --> 开除 : 开除（需审批）
  在读 --> 死亡 : 死亡
  转出 --> [*]
  毕业 --> [*]
  结业 --> [*]
  肄业 --> [*]
  出国 --> [*]
  失踪 --> [*]
  退学 --> [*]
  开除 --> [*]
  死亡 --> [*]
""",
    "guardian-bind.mmd": """stateDiagram-v2
  [*] --> pending : 家长扫码 / 教师录入
  pending --> approved : 班主任确认
  pending --> rejected : 班主任驳回（意见必填）
  rejected --> pending : 家长修改后重提（同字段仅一条待审）
  approved --> unbinding : 发起解绑
  unbinding --> approved : 班主任驳回解绑
  unbinding --> [*] : 班主任确认解绑
  note right of approved
    绑定上限 3 人
    解绑需班主任确认
  end note
""",
    "teacher-account-status.mmd": """stateDiagram-v2
  [*] --> 在职
  在职 --> 停用 : 停用账号
  停用 --> 在职 : 启用账号
  在职 --> 离职 : 离职（无未结束任职）
  离职 --> [*]
  note right of 停用
    立即失效 Sa-Token 会话
  end note
""",
    "class-status.mmd": """stateDiagram-v2
  [*] --> active : 新建班级
  active --> disabled : 停用（有在读学生时只能停用）
  disabled --> active : 启用
  active --> [*] : 删除（仅无学生、无引用时）
  note right of disabled
    停用保留历史成员关系
  end note
""",
    "promotion-task.mmd": """stateDiagram-v2
  [*] --> draft : 创建
  draft --> previewed : 预览完成
  previewed --> queued : 提交执行
  queued --> running : 消费者取到任务
  queued --> cancelled : 取消（仅 queued）
  running --> succeeded : 全部成功
  running --> partial_failed : 有跳过 / 失败项
  running --> failed : 整体失败
  partial_failed --> running : 重试失败项（复用 task_no）
  failed --> running : 重试
  failed --> dead : 超过最大重试
  dead --> running : 运维重放
  succeeded --> [*]
  cancelled --> [*]
""",
    "stream-change-request.mmd": """stateDiagram-v2
  [*] --> pending : 学生提交变更申请
  pending --> approved : 校级管理员 / 教务主任审批通过
  pending --> rejected : 驳回（意见必填）
  pending --> cancelled : 学生撤回（仅 pending）
  rejected --> pending : 重新提交
  approved --> [*]
  cancelled --> [*]
  note right of pending
    同一学生同一学期同时只允许一条待审申请
  end note
""",
    "term-archive.mmd": """stateDiagram-v2
  [*] --> draft : 创建学年
  draft --> active : 启用（设为当前）
  active --> archived : 归档
  archived --> active : 解除归档
  active --> [*]
  note right of archived
    归档后不可新增学期
  end note
""",
    "async-task.mmd": """stateDiagram-v2
  [*] --> queued
  queued --> running : 消费者取到消息
  queued --> cancelled : 用户取消
  running --> succeeded : 全部成功
  running --> partial_failed : 部分失败
  running --> failed : 整体失败
  partial_failed --> running : 重试失败项
  failed --> running : 重试
  failed --> dead : 超过 3 次（30s / 2m / 8m）
  dead --> running : 运维重放（写审计）
  succeeded --> [*]
  cancelled --> [*]
""",
    "archive-batch.mmd": """stateDiagram-v2
  [*] --> created : 创建批次
  created --> running : 开始搬迁
  running --> succeeded : 区间搬迁完成
  running --> failed : 搬迁失败
  failed --> running : 重试（已搬迁区间不重复）
  succeeded --> [*]
""",
}


CLASS_DIAGRAMS = {
    "student-domain.mmd": """classDiagram
  class EduStudent {
    +Long id
    +String studentNo
    +String nationalStudentNo
    +String idCardNo
    +String studentName
    +String loginName
    +Date birthDate
  }
  class EduStudentEnrollment {
    +Long id
    +Long studentId
    +Long schoolId
    +String enrollmentStatus
    +Date enrollDate
  }
  class EduGuardian {
    +Long id
    +String guardianName
    +String guardianPhone
  }
  class EduStudentGuardian {
    +Long studentId
    +Long guardianId
    +String relation
    +String bindStatus
  }
  class EduStudentFieldChange {
    +Long studentId
    +String fieldName
    +String status
    +String pendingGuard
  }
  class EduActivationCode {
    +Long studentId
    +String code
    +String status
    +Long activeGuard
  }
  EduStudent "1" --> "n" EduStudentEnrollment : 在校记录
  EduStudent "1" --> "n" EduStudentGuardian : 监护人关系
  EduGuardian "1" --> "n" EduStudentGuardian : 被绑定
  EduStudent "1" --> "n" EduStudentFieldChange : 变更申请
  EduStudent "1" --> "n" EduActivationCode : 激活码
""",
    "teacher-domain.mmd": """classDiagram
  class EduTeacher {
    +Long id
    +String tenantId
    +Long schoolId
    +String teacherNo
    +String teacherName
    +Long userId
    +String teacherStatus
  }
  class EduUserRole {
    +Long userId
    +String eduRole
    +String status
  }
  class EduGradeLeader {
    +Long termId
    +Long gradeId
    +Long userId
  }
  class EduTeachingAssignment {
    +Long termId
    +Long teacherId
    +Long subjectId
    +String classType
    +Long classId
  }
  EduTeacher "1" --> "n" EduUserRole : 教育角色
  EduTeacher "1" --> "n" EduGradeLeader : 年级主任任职
  EduTeacher "1" --> "n" EduTeachingAssignment : 任教关系
""",
    "grade-domain.mmd": """classDiagram
  class EduGrade {
    +Long id
    +Long schoolId
    +String stageCode
    +String enrollYear
    +Integer gradeLevel
    +String gradeName
    +String gradeStatus
  }
  class EduGradeLeader {
    +Long termId
    +Long gradeId
    +Long userId
  }
  EduGrade "1" --> "n" EduGradeLeader : 年级主任
""",
    "class-domain.mmd": """classDiagram
  class EduClass {
    +Long id
    +Long termId
    +Long gradeId
    +String classType
    +String className
    +Long headTeacherId
    +String classStatus
  }
  class EduClassMember {
    +Long classId
    +Long studentId
    +Long termId
  }
  class EduTeachingClass {
    +Long id
    +Long termId
    +String combination
    +String className
  }
  class EduTeachingClassMember {
    +Long teachingClassId
    +Long studentId
    +String generateTaskNo
  }
  EduClass "1" --> "n" EduClassMember : 行政班成员
  EduTeachingClass "1" --> "n" EduTeachingClassMember : 教学班成员
  note for EduClass "行政班与教学班共用 edu_class，由 class_type 区分"
""",
    "promotion-domain.mmd": """classDiagram
  class EduPromotionTask {
    +Long id
    +String taskNo
    +Long sourceTermId
    +Long targetTermId
    +String taskStatus
  }
  class EduPromotionItem {
    +Long taskId
    +Long studentId
    +String itemStatus
  }
  class EduEnrollmentChange {
    +Long studentId
    +String changeType
    +String approvalStatus
  }
  class EduTransferOrder {
    +String transferNo
    +Long studentId
    +Long fromSchoolId
    +Long toSchoolId
    +String transferStatus
  }
  EduPromotionTask "1" --> "n" EduPromotionItem : 逐条结果
  EduEnrollmentChange "1" --> "0..1" EduTransferOrder : 跨校转学
""",
    "stream-domain.mmd": """classDiagram
  class EduStreamConfig {
    +Long schoolId
    +Long termId
    +DateTime deadline
    +Boolean approvalOnOverdue
  }
  class EduStudentStream {
    +Long termId
    +Long studentId
    +String primarySubjectCode
    +String secondarySubjectCodes
  }
  class EduStreamChangeRequest {
    +String requestNo
    +Long studentId
    +String requestStatus
  }
  class EduStreamHistory {
    +Long studentId
    +Long termId
    +String action
  }
  EduStreamConfig "1" --> "n" EduStudentStream : 约束
  EduStudentStream "1" --> "n" EduStreamHistory : 变更历史
  EduStreamChangeRequest "1" --> "0..1" EduStudentStream : 审批后生效
""",
    "subject-domain.mmd": """classDiagram
  class EduSubject {
    +Long id
    +String subjectCode
    +String subjectName
    +Boolean streamEnabled
    +String streamRole
  }
  class EduSubjectStage {
    +Long subjectId
    +String stageCode
  }
  EduSubject "1" --> "n" EduSubjectStage : 学段启用
  note for EduSubject "一条主体 + 学段启用表（RV-SUB-04）"
""",
    "school-domain.mmd": """classDiagram
  class EduSchool {
    +Long id
    +String tenantId
    +String schoolCode
    +String schoolName
    +String schoolStatus
  }
  class EduCampus {
    +Long schoolId
    +String campusCode
    +String campusName
  }
  class EduSchoolStage {
    +Long schoolId
    +String stageCode
  }
  class EduDataGrant {
    +String grantNo
    +String granteeType
    +DateTime effectiveEnd
    +String grantStatus
  }
  class EduDataGrantScope {
    +Long grantId
    +String resourceCode
    +String accessLevel
  }
  EduSchool "1" --> "n" EduCampus : 校区
  EduSchool "1" --> "n" EduSchoolStage : 学段
  EduDataGrant "1" --> "n" EduDataGrantScope : 授权范围
""",
    "term-domain.mmd": """classDiagram
  class EduAcademicYear {
    +Long id
    +String academicYearCode
    +Date startDate
    +Date endDate
    +String yearStatus
  }
  class EduTerm {
    +Long id
    +Long academicYearId
    +String termCode
    +Boolean isCurrent
  }
  EduAcademicYear "1" --> "n" EduTerm : 学期
""",
    "task-domain.mmd": """classDiagram
  class EduAsyncTask {
    +String taskNo
    +String taskType
    +String taskStatus
    +Long ownerId
  }
  class EduAsyncTaskRetry {
    +String taskNo
    +Integer retryNo
  }
  class EduDeadLetterTask {
    +String taskNo
    +String replayStatus
  }
  class EduFileRef {
    +String fileId
    +String bizType
    +DateTime expireTime
  }
  class EduImportBatch {
    +String batchNo
    +String importStatus
  }
  class EduImportError {
    +String batchNo
    +Integer rowNo
    +String result
  }
  EduAsyncTask "1" --> "n" EduAsyncTaskRetry : 重试记录
  EduAsyncTask "1" --> "0..1" EduDeadLetterTask : 进死信
  EduImportBatch "1" --> "n" EduImportError : 错误行
  EduImportBatch "1" --> "1" EduAsyncTask : 执行任务
""",
    "audit-domain.mmd": """classDiagram
  class EduAuditLog {
    +Long id
    +String requestId
    +String objectType
    +String objectId
    +String actionType
    +DateTime logTime
  }
  class EduAuditChange {
    +Long logId
    +String fieldName
    +String oldValue
    +String newValue
  }
  class EduAuditArchiveBatch {
    +String archiveNo
    +DateTime rangeStart
    +DateTime rangeEnd
    +String archiveStatus
  }
  EduAuditLog "1" --> "n" EduAuditChange : 字段变更明细
  EduAuditArchiveBatch "1" --> "n" EduAuditLog : 归档区间
""",
}


def gen_diagrams():
    base = os.path.join(S40, "diagrams")
    for name, text in SEQUENCES.items():
        write(os.path.join(base, "sequence", name), text)
    for name, text in STATES.items():
        write(os.path.join(base, "state", name), text)
    for name, text in CLASS_DIAGRAMS.items():
        write(os.path.join(base, "class", name), text)


def gen_migration_plan(tables, derived, views):
    batches = {}
    for t in tables:
        batches.setdefault(t.get("batch", "?"), []).append(t["name"])
    total_cols = 0
    for t in tables:
        total_cols += len(t.get("columns") or [])
    fk_count = sum(len(t.get("foreign_keys") or []) for t in tables)
    uk_count = sum(len(t.get("unique_keys") or []) for t in tables)
    idx_count = sum(len(t.get("indexes") or []) for t in tables)

    lines = [
        "# 数据库迁移计划",
        "",
        "> 本文件由 `tools/gen_stage5_docs.py` 生成；表结构事实源是 `database/schema.yaml`。",
        "> 迁移脚本由 `tools/gen_schema_artifacts.py` 生成，不要手工编辑脚本内容。",
        "",
        "## 1. 迁移脚本清单与顺序",
        "",
        "| 顺序 | 脚本 | 内容 | 表数 | 必须先于 |",
        "|---|---|---|---|---|",
        "| 1 | `migrations/V1__edu_student_teacher.sql` | 学生（6 表）+ 教师（4 表） | 10 | V3（班级引用教师） |",
        "| 2 | `migrations/V2__edu_org_config.sql` | 学校（3）+ 学年学期（2）+ 学科（2） | 7 | **必须先于 V3**（班级引用学期与学科） |",
        "| 3 | `migrations/V3__edu_grade_class.sql` | 年级（1）+ 班级（4） | 5 | V4 |",
        "| 4 | `migrations/V4__edu_promotion_stream_support.sql` | 升班 / 选科 / 导入导出 / 审计 / 授权（20 表 + 1 派生表 + 2 视图） | 20 | V5 |",
        "| 5 | `migrations/V5__edu_foreign_keys.sql` | 32 条外键统一添加 | — | — |",
        "",
        "对象规模：主表 "
        + str(len(tables))
        + f" 张 / 业务列 {total_cols} 个 / 唯一键 {uk_count} 个 / 索引 {idx_count} 个 / 外键 {fk_count} 条；"
        + f"派生表 {len(derived)} 张 / 视图 {len(views)} 个。",
        "",
        "### 1.1 为什么外键单独放到 V5",
        "",
        "建表脚本按模块分批生成，模块之间存在跨文件引用（如班级引用教师、班级引用学期）。",
        "若在 V1 中就加外键，V1 必须依赖尚未执行的 V2 / V3，脚本顺序被迫耦合。",
        "统一放到 V5：V1 ~ V4 只建表与索引，V5 一次性补外键。代价是多一次 `ALTER TABLE`，",
        "换来的是每个脚本可以单独重放、单独理解。",
        "",
        "### 1.2 为什么 V2 必须先于 V3",
        "",
        "`edu_class` 同时引用 `edu_term`（`fk_class_term`）与 `edu_subject` 相关的学科配置。",
        "V2 建立学校、学年学期、学科；V3 才能建立班级。V1 与 V2 之间没有依赖，可并行。",
        "",
        "## 2. 各批次表清单",
        "",
    ]
    for batch in sorted(batches):
        lines += [f"### 批次 {batch}", "", "| 表 |", "|---|"]
        for name in sorted(batches[batch]):
            lines.append(f"| `{name}` |")
        lines.append("")

    lines += [
        "## 3. 回滚方案",
        "",
        "| 场景 | 回滚方式 | 说明 |",
        "|---|---|---|",
        "| 单表结构错误 | 数据库快照恢复 | 首轮上线前用 Docker 数据卷快照，不写反向 DDL |",
        "| 批次上线失败 | 回滚应用版本 + 保留表 | 教育域表为新增，应用回滚不要求删表 |",
        "| 外键导致写入阻塞 | 先 `ALTER TABLE ... DROP FOREIGN KEY`，修数据后重加 | 只在事故处置时执行，需运维审批 |",
        "| 逻辑删除误删 | 应用层恢复 `del_flag` | 历史与学籍类表禁止物理删除（`conventions.soft_delete`） |",
        "",
        "**不提供 `DROP TABLE` 反向脚本**：生产上删表属红线操作，需要人工审批与备份，",
        "生成器不产出这类脚本，避免误用。",
        "",
        "## 4. 存量升级路径",
        "",
        "1. 首轮为**新建教育域**，不涉及存量业务表改造（`ruoyi-edu` 为新模块）",
        "2. 基线 RuoYi 表（`sys_user` 等）不动；教育角色通过 `edu_user_role` 关联",
        "3. 升级顺序：V1 → V2 → V3 → V4 → V5，可整体重放；脚本使用 `DROP TABLE IF EXISTS` + `CREATE TABLE`，",
        "   对已存在结构是**重建语义**，因此只适用于新建与演练环境",
        "4. 生产增量变更不得直接重放上述脚本，必须另立增量迁移文件（编号 `V6+`），并按 `change-control.md` 走变更",
        "",
        "## 5. 验证证据",
        "",
        "| 证据 | 内容 |",
        "|---|---|",
        "| `evidence/stage5-detailed-design/2026-10-01_mysql8-empty-install.log` | MySQL 8.4.11 空库安装、对象计数、"
        "`check-sql.sql` 结构检查 |",
        "| `evidence/stage5-detailed-design/2026-10-01_mysql8-upgrade-path.log` | 5 个脚本可重复执行、对象计数不变 |",
        "| `evidence/stage5-detailed-design/2026-10-01_generated-column-guard.log` | 生成列唯一约束的语义验证"
        "（重复待审被拒、不同字段可通过、状态流转后可重新提交） |",
        "",
        "## 6. 执行方式",
        "",
        "```bash",
        "# 演练 / 验证环境（Docker）",
        "docker run -d --name edu-mysql -e MYSQL_ROOT_PASSWORD=*** -p 3306:3306 mysql:8",
        "docker cp docs/40-detailed-design/migrations/. edu-mysql:/sql/",
        "for f in V1__edu_student_teacher V2__edu_org_config V3__edu_grade_class \\",
        "         V4__edu_promotion_stream_support V5__edu_foreign_keys; do",
        "  docker exec edu-mysql sh -c \"mysql -uroot -p*** edu < /sql/$f.sql\"",
        "done",
        "```",
        "",
        "生产环境使用 Flyway（或项目既有迁移工具）按文件名顺序执行，禁止跳号。",
        "",
        "## 7. 已知限制",
        "",
        "- 脚本是**重建语义**，不适合对已有数据的表做结构变更；增量变更另立 V6+",
        "- 生成列 `pending_guard` / `active_guard` 依赖 MySQL 8；MariaDB 或 MySQL 5.7 不受支持",
        "- 归档表与视图与主表同构，由 V4 一起创建；归档数据的搬迁由应用侧任务执行",
        "",
    ]
    write(os.path.join(S40, "database", "migration-plan.md"), "\n".join(lines))


def gen_runtime_design(tables):
    lines = [
        "# 运行时设计：异步任务、消息、缓存与幂等",
        "",
        "> 上游依据：`docs/30-architecture/07-sync-async-boundary.md`（同步 / 异步边界与消息）、",
        "> `docs/30-architecture/08-cache-strategy.md`（缓存策略）、`docs/10-prd/07-non-functional-requirements.md`。",
        "> 本文件不复述上游结论，只补齐详细设计阶段需要定下来的实现约束。",
        "",
        "## 1. 判断一条操作走同步还是异步",
        "",
        "| 判定 | 同步 | 异步 |",
        "|---|---|---|",
        "| 用户必须立刻看到结果 | 是 | 否 |",
        "| 数据量 | ≤ 2000 行导出 / ≤ 5000 行导入校验 | > 2000 行导出 / 执行写库 |",
        "| 需要跨请求重试 | 否 | 是 |",
        "",
        "阈值不在本文件重新发明，统一引用 `07-sync-async-boundary.md` 第 1 节。",
        "",
        "## 2. 任务与消息",
        "",
        "### 2.1 队列",
        "",
        "| 交换机 | 队列 | 用途 | 消费者 |",
        "|---|---|---|---|",
        "| `edu.task.exchange` | `edu.task.import` | 导入执行 | 导入消费者 |",
        "| `edu.task.exchange` | `edu.task.export` | 导出生成 | 导出消费者 |",
        "| `edu.task.exchange` | `edu.task.promotion` | 升班执行 | 升班消费者 |",
        "| `edu.task.exchange` | `edu.task.teaching-class` | 教学班生成 | 教学班消费者 |",
        "| `edu.task.exchange` | `edu.task.archive` | 日志归档 | 归档消费者 |",
        "| `edu.task.dlx` | `edu.task.dead` | 死信 | 死信巡视任务 |",
        "| `edu.audit.exchange` | `edu.audit.compensate` | 审计补偿 | 审计消费者 |",
        "",
        "路由键：`edu.<type>.<action>`，与 `07-sync-async-boundary.md` 第 3.1 节一致。",
        "",
        "### 2.2 统一消息信封",
        "",
        "| 字段 | 必填 | 说明 |",
        "|---|---|---|",
        "| `task_no` / `batch_no` | 是 | 幂等键，与 `edu_async_task.task_no` 一致 |",
        "| `tenant_id` / `school_id` | 是 | 消费时恢复上下文；**消费端必须重新解析范围，不得直接信任** |",
        "| `module` / `action` | 是 | 动作标识 |",
        "| `payload` | 是 | 业务参数（文件引用、筛选条件、目标学期等） |",
        "| `operator_id` | 是 | 发起人，用于「本人任务」过滤与审计 |",
        "| `retry_count` | 是 | 当前重试次数 |",
        "| `trace_id` | 是 | 贯穿日志、审计与错误响应 |",
        "",
        "### 2.3 重试与死信",
        "",
        "| 项 | 取值 | 依据 |",
        "|---|---|---|",
        "| 最大重试 | 3 | `REQ-IMP-039` |",
        "| 退避 | 30s / 2m / 8m | 同上 |",
        "| 幂等 | 以 `task_no` / `batch_no` 为唯一键先查后写；唯一索引兜底 | `REQ-IMP-037` / `NFR-MQ-02` |",
        "| 超限 | 进 `edu.task.dead`，落 `edu_dead_letter_task` | `REQ-IMP-038` |",
        "| 消费者并发 | 导入默认 3 校并发、每校串行 | `REQ-IMP-048` |",
        "| 消息丢失兜底 | 定时巡检把长时间 `queued` / `running` 标记异常 | `NFR-MQ-01` |",
        "| 重放 | 复用原幂等键，不新建任务；重放写审计 | 同上 |",
        "",
        "## 3. 幂等键清单",
        "",
        "| 幂等键 | 所在表 | 保护的操作 |",
        "|---|---|---|",
        "| `task_no` | `edu_async_task` | 所有异步任务 |",
        "| `batch_no` | `edu_import_batch` | 导入批次 |",
        "| `transfer_no` | `edu_transfer_order` | 转学单 |",
        "| `request_no` | `edu_stream_change_request` | 选科变更申请 |",
        "| `grant_no` | `edu_data_grant` | 数据共享授权 |",
        "| `archive_no` | `edu_audit_archive_batch` | 归档批次 |",
        "| `(task_no, retry_no)` | `edu_async_task_retry` | 重试记录 |",
        "| `(request_id, object_id, action_type)` | `edu_audit_log` | 审计日志写入 |",
        "",
        "## 4. 缓存",
        "",
        "### 4.1 键与 TTL",
        "",
        "| 域 | 键 | TTL | 失效时机 |",
        "|---|---|---|---|",
        "| 数据范围 | `edu:<tenant>:scope:user:<user_id>` | 10 分钟 | 角色 / 任职 / 任教 / 授权变更 |",
        "| 班主任解析 | `edu:<tenant>:scope:class-head:<user_id>` | 10 分钟 | `edu_class.head_teacher_id` 变更 |",
        "| 年级主任解析 | `edu:<tenant>:scope:grade-leader:<user_id>:<term_id>` | 10 分钟 | `edu_grade_leader` 变更 |",
        "| 任教关系 | `edu:<tenant>:scope:teaching:<user_id>:<term_id>` | 10 分钟 | `edu_teaching_assignment` 变更 |",
        "| 共享授权 | `edu:<tenant>:grant:<school_tenant_id>:<resource_code>` | 5 分钟 | 授权创建 / 撤销 / 到期 |",
        "| 当前学年学期 | `edu:<tenant>:term:current` | 30 分钟 | 「设为当前」后立即删除 |",
        "| 学科选科角色 | `edu:<tenant>:subject:roles` | 30 分钟 | 学科角色配置变更 |",
        "| 选科配置 | `edu:<tenant>:stream:config:<term_id>` | 10 分钟 | `saveStreamConfig` 后删除 |",
        "| 选科统计 | `edu:<tenant>:stream:stat:<term_id>:<range_hash>` | 5 分钟 | 提交 / 审批通过后删除 |",
        "| 字典 | `edu:<tenant>:dict:<domain>:<school_id>` | 30 分钟 | 对应实体增删改 |",
        "",
        "### 4.2 三条实现约束",
        "",
        "1. **只缓存范围片段，不缓存「允许 / 拒绝」结论。** 范围变化时旧键自然不再命中（`C-04`）",
        "2. **键必须带租户前缀**，禁止跨租户复用（`C-02`）",
        "3. **首轮不引入本地缓存**（`C-05`）：多实例下本地缓存会造成「改了一个实例、另一个还放行」",
        "",
        "### 4.3 失效失败的处理",
        "",
        "删除缓存失败不回滚业务事务，也不阻塞响应；解析结果带范围版本号，版本变化即视为失效。",
        "这样把「正确性」建立在版本号上，而不是建立在删除成功上。",
        "",
        "## 5. 并发与锁",
        "",
        "| 场景 | 手段 |",
        "|---|---|",
        "| 同一学生并发编班 / 调班 | 数据库唯一键 `uk_class_member_admin` + 行锁 |",
        "| 同一学校并发升班 | 任务表行锁 + 状态检查（同源 / 目标学期唯一 running） |",
        "| 同一学生并发提交选科 | `uk_student_stream` |",
        "| 审批并发 | 条件更新（`WHERE status='pending'`），影响行数为 0 即已被处理 |",
        "| 任务重复消费 | `task_no` 条件更新 + 唯一键 |",
        "| 缓存击穿 | 单飞（single-flight）到数据库；不做「空值缓存」以外的特殊处理 |",
        "",
        "## 6. 降级",
        "",
        "| 故障 | 降级行为 | 用户可见表现 |",
        "|---|---|---|",
        "| 审计日志写入持续失败 | 业务进入只读降级（`AUD-Q-05`） | 顶部横幅提示「当前只读」，写操作禁用 |",
        "| RabbitMQ 不可用 | 任务保持 `queued`，不丢任务 | 任务列表显示「排队中」，恢复后继续 |",
        "| Redis 不可用 | 直接走数据库解析范围（牺牲性能换正确性） | 列表变慢，功能可用 |",
        "| 导出 / 归档超时 | 转异步 | 页面上出现「已转异步」提示与任务入口 |",
        "",
        "## 7. 可观测性",
        "",
        "- 每个请求带 `request_id`，错误响应必须回传（错误码表约定）",
        "- 每个异步任务带 `task_no`，可在任务中心按编号检索",
        "- 审计日志记录 `trace_id`，用于把「页面操作 → 接口 → 消息 → 消费者 → 数据变更」串起来",
        "- 教育域新增指标：任务积压数、死信数、日志写入失败率、范围解析缓存命中率",
        "",
        "## 8. 与表结构的对应",
        "",
        f"运行时用到的表共 {len(tables)} 张，全部在 `database/schema.yaml` 有定义；",
        "其中 `edu_async_task` / `edu_async_task_retry` / `edu_dead_letter_task` / `edu_file_ref` 属导入导出模块，",
        "`edu_audit_log` / `edu_audit_change` / `edu_audit_archive_batch` 属审计模块。",
        "",
    ]
    write(os.path.join(S40, "runtime-design.md"), "\n".join(lines))


def gen_index(tables, derived, views, ops):
    total_ops = sum(len(v) for v in ops.values())
    lines = [
        "# 阶段 5 · 详细设计与建表（索引）",
        "",
        "## 1. 交付物清单",
        "",
        "| 产物 | 路径 | 说明 |",
        "|---|---|---|",
        "| 表结构事实源 | `docs/40-detailed-design/database/schema.yaml` | 42 张主表 + 1 张派生表 + 2 个视图 |",
        "| 逐表说明 | `docs/40-detailed-design/database/physical-schema.md` | 字段、类型、可空、默认值、注释、唯一键、索引、外键 |",
        "| ER 图 | `docs/40-detailed-design/database/er-diagram.mmd` | 仅物理外键（32 条） |",
        "| 领域对象映射 | `docs/40-detailed-design/database/domain-table-map.csv` | 领域对象 → 表 |",
        "| 键与索引 | `docs/40-detailed-design/database/keys-and-indexes.md` | 唯一键 / 外键 / 索引 + 理由 |",
        "| 结构检查 SQL | `docs/40-detailed-design/database/check-sql.sql` | 22 条结构与不变式检查 |",
        "| 迁移脚本 | `docs/40-detailed-design/migrations/V1` ~ `V5` | 可重复执行的建表与外键脚本 |",
        "| 迁移计划 | `docs/40-detailed-design/database/migration-plan.md` | 顺序、依赖、回滚、存量升级 |",
        "| 接口契约 | `docs/40-detailed-design/api/openapi.yaml` | OpenAPI 3.0.3，" + str(total_ops) + " 个 operationId |",
        "| 错误码 | `docs/40-detailed-design/api/error-codes.yaml` | " + str(len(ERROR_CODES)) + " 个错误码 |",
        "| 页面动作映射 | `docs/40-detailed-design/page-action-api-map.yaml` | 页面动作 → 权限 → operationId → 组件 |",
        "| 前端页面树 | `docs/40-detailed-design/frontend-page-tree.yaml` | 路由 / 组件归属 / 批次 / 文件 |",
        "| 模块详细设计 | `docs/40-detailed-design/modules/<module>/design.md` | 11 个模块（事务、并发、校验、失败恢复） |",
        "| 运行时设计 | `docs/40-detailed-design/runtime-design.md` | 异步任务、消息、缓存、幂等、降级 |",
        "| 时序图 | `docs/40-detailed-design/diagrams/sequence/*.mmd` | " + str(len(SEQUENCES)) + " 张 |",
        "| 状态机 | `docs/40-detailed-design/diagrams/state/*.mmd` | " + str(len(STATES)) + " 张 |",
        "| 领域模型图 | `docs/40-detailed-design/diagrams/class/*.mmd` | " + str(len(CLASS_DIAGRAMS)) + " 张 |",
        "",
        "## 2. 门禁对照（`docs/00-governance/stage-inputs.yaml` 阶段 5）",
        "",
        "| 门禁 | 状态 | 证据 |",
        "|---|---|---|",
        "| `physical-schema.md` 逐表覆盖字段、主键、唯一键、外键、索引 | 通过 | `database/physical-schema.md`；"
        f"主表 {len(tables)} 张、派生表 {len(derived)} 张、视图 {len(views)} 个、"
        f"业务列 {sum(len(t.get('columns') or []) for t in tables)} 个 |",
        "| ER 图与领域对象映射与建表脚本三者一致 | 通过 | 三者同源：全部由 `tools/gen_schema_artifacts.py` "
        "从 `database/schema.yaml` 生成，不存在手工维护的第二份结构 |",
        "| 迁移脚本在 MySQL 8 空库安装通过，存量升级路径验证通过 | 通过 | "
        "`evidence/stage5-detailed-design/2026-10-01_mysql8-empty-install.log`、"
        "`evidence/stage5-detailed-design/2026-10-01_mysql8-upgrade-path.log` |",
        "| OpenAPI 通过校验工具检查 | 通过 | `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log`"
        "（`openapi-spec-validator`） |",
        "",
        "## 3. 文档生成方式（可复现）",
        "",
        "```bash",
        "python tools/extract_api_catalog.py      # PRD 第 8 节 → 30-architecture/06-api-catalog.md",
        "python tools/gen_schema_artifacts.py     # schema.yaml → 物理表 / ER / 键 / 检查 SQL / 迁移脚本",
        "python tools/gen_api_and_map.py          # 接口目录 + 原型 → OpenAPI / 动作映射 / 页面树",
        "python tools/gen_stage5_docs.py          # 本目录的模块详细设计 / 图 / 运行时设计 / 错误码",
        "```",
        "",
        "生成器只读取已冻结的上游产物；发现上游缺项时登记 `docs/00-governance/gap-register.yaml`，",
        "不在生成器里补造业务规则。",
        "",
        "## 4. 与阶段 4 的对应关系",
        "",
        "| 阶段 4 产物 | 阶段 5 落点 |",
        "|---|---|",
        "| `05-data-ownership.md`（33 张表的归属） | `schema.yaml` 的 `module` 字段 + `domain-table-map.csv` |",
        "| `06-api-catalog.md`（173 个 operationId） | `api/openapi.yaml` + `modules/<m>/design.md` 第 3 节 |",
        "| `07-sync-async-boundary.md` | `runtime-design.md` 第 1、2 节 |",
        "| `08-cache-strategy.md` | `runtime-design.md` 第 4 节 |",
        "| `09-permission-architecture.md` | 各模块 design.md 第 9 节 |",
        "| `03-module-division.md` | `modules/<m>/design.md` 的模块边界 |",
        "",
        "## 5. 阶段 5 的架构级修订（实证发现）",
        "",
        "| 编号 | 发现 | 处理 |",
        "|---|---|---|",
        "| 修订 1 | `edu_audit_log` 原计划用 MySQL 原生分区，但分区要求所有唯一键包含分区列，"
        "会破坏 `(request_id, object_id, action_type)` 的幂等语义 | 改为同构归档表 `edu_audit_log_archive` "
        "+ 归档批次留痕 |",
        "| 修订 2 | `edu_class_member` 原设计用 `(term_id, student_id, class_type)` 兜底行政班唯一性，"
        "但 MySQL 唯一索引无法表达「仅 administrative 生效」 | 拆表：本表只存行政班（`class_type` 恒 `administrative`），"
        "教学班成员在 `edu_teaching_class_member` |",
        "| 修订 3 | `edu_student_field_change` 的「同一学生同一字段只允许一条待审核」原本只有普通索引，"
        "并发下可被绕过 | 增加生成列 `pending_guard` + 唯一键 `uk_sfc_pending` |",
        "| 修订 4 | `edu_activation_code` 的「同一学生同时只有一个未使用激活码」原本只靠业务层 | "
        "增加生成列 `active_guard` + 唯一键 `uk_activation_active` |",
        "",
        "## 6. 后续阶段入口",
        "",
        "- 阶段 6 生产前端：输入 `modules/*/design.md` + `page-action-api-map.yaml` + "
        "`frontend-page-tree.yaml` + `prototypes/high-fidelity/v1/**`",
        "- 阶段 7 生产后端：输入 `modules/*/design.md` + `api/openapi.yaml` + "
        "`database/physical-schema.md` + `migrations/*.sql`",
        "",
    ]
    write(os.path.join(S40, "00-index.md"), "\n".join(lines))


def main() -> int:
    data, tables, derived, views = load_schema()
    ops = load_ops()
    action_map = load_action_map()
    page_tree = load_page_tree()

    for module in MODULES:
        m_tables = module_tables(tables, derived, module)
        m_ops = ops.get(module, [])
        m_pages = pages_of_module(action_map, page_tree, module)
        text = render_module_design(module, m_tables, m_ops, m_pages)
        write(os.path.join(S40, "modules", module, "design.md"), text)

    gen_error_codes()
    gen_diagrams()
    table_list = list(tables.values())
    gen_migration_plan(table_list, derived, views)
    gen_runtime_design(table_list)
    gen_index(table_list, derived, views, ops)

    total_ops = sum(len(v) for v in ops.values())
    print(
        f"阶段 5 文档生成完成：{len(MODULES)} 个模块详细设计 / {len(ERROR_CODES)} 个错误码 / "
        f"{len(SEQUENCES)} 张时序图 / {len(STATES)} 张状态机 / {len(CLASS_DIAGRAMS)} 张领域模型 / "
        f"接口 {total_ops} 个"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
