#!/usr/bin/env python3
"""从各模块 PRD 第 8 节抽取接口清单，生成阶段 4 的 06-api-catalog.md。

作者手工维护的只有：文件头说明、服务归属规则、权限点推断规则、同步/异步判定规则。
接口行本身全部来自 PRD，保证「接口清单覆盖原型中每一个动作」这条门禁可被追溯：
    PRD 第 8 节 operationId  ←→  stage 4 的 06-api-catalog.md
    ←→  stage 5 的 openapi.yaml 与 page-action-api-map.yaml

用法：python tools/extract_api_catalog.py
"""

from __future__ import annotations

import io
import os
import re

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PRD_DIR = os.path.join(REPO_ROOT, "docs", "10-prd", "modules")
OUT = os.path.join(REPO_ROOT, "docs", "30-architecture", "06-api-catalog.md")

MODULE_CN = {
    "student": "学生管理",
    "teacher": "教师管理",
    "grade": "年级管理",
    "class": "班级管理",
    "promotion": "升班与学籍异动",
    "stream": "3+1+2 选科与教学班",
    "subject": "学科与配置",
    "school": "学校与租户",
    "term": "学年学期",
    "import-export": "导入导出与异步任务",
    "audit": "审计与操作日志",
}

# 资源前缀 → 权限资源（05-permission-matrix.yaml 的 code）
RESOURCE_RULES = [
    ("/edu/student", "person.student"),
    ("/edu/guardian", "person.student_guardian"),
    ("/edu/teacher", "person.teacher"),
    ("/edu/teaching-assignment", "person.teaching_assignment"),
    ("/edu/grade", "org.grade"),
    ("/edu/class", "org.class"),
    ("/edu/teaching-class", "org.teaching_class"),
    ("/edu/promotion", "promotion.batch"),
    ("/edu/enrollment", "enrollment.status"),
    ("/edu/transfer", "enrollment.transfer"),
    ("/edu/stream", "stream.*"),
    ("/edu/subject", "org.subject"),
    ("/edu/school", "org.school"),
    ("/edu/campus", "org.school"),
    ("/edu/term", "org.term"),
    ("/edu/import", "data.import"),
    ("/edu/export", "data.export"),
    ("/edu/async-task", "data.async_task"),
    ("/edu/audit", "audit.log"),
]

# 明显异步的接口（PRD 里写「异步」「转异步」「任务」的）
ASYNC_HINTS = ("executeImport", "exportData", "exportStudent", "exportClassRoster",
               "exportStreamSelection", "exportOperationLog", "exportOperatorAccess",
               "executeTeachingClassGenerate", "previewTeachingClassGenerate",
               "searchArchivedLog", "batchAddClass", "initSchoolBaseline", "executePromotion",
               "applyPromotion", "importRosterExecute")


def resource_of(path: str) -> str:
    for prefix, resource in RESOURCE_RULES:
        if path.startswith(prefix):
            return resource
    return "—"


def method_kind(method: str) -> str:
    return {"GET": "查询", "POST": "写入 / 触发", "PUT": "更新", "DELETE": "逻辑删除", "PATCH": "更新"}.get(method, method)


def extract(module: str):
    path = os.path.join(PRD_DIR, module, "PRD.md")
    with io.open(path, encoding="utf-8") as fh:
        body = fh.read()
    start = body.find("## 8.")
    if start < 0:
        return []
    end = body.find("\n## ", start + 5)
    seg = body[start:end if end > 0 else len(body)]
    rows = []
    for m in re.finditer(
        r"\|\s*`([A-Za-z][A-Za-z0-9]+)`\s*\|\s*(GET|POST|PUT|DELETE|PATCH)\s*\|\s*`([^`]+)`\s*\|\s*([^|]+)\|",
        seg,
    ):
        op, method, path_, desc = m.group(1), m.group(2), m.group(3).strip(), m.group(4).strip()
        rows.append({"module": module, "op": op, "method": method, "path": path_, "desc": desc})
    return rows


def main() -> int:
    modules = sorted(d for d in os.listdir(PRD_DIR) if os.path.isdir(os.path.join(PRD_DIR, d)))
    all_rows = []
    per_module = {}
    for m in modules:
        rows = extract(m)
        per_module[m] = rows
        all_rows.extend(rows)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    lines = [
        "# 接口清单（模块 × 资源 × 操作 × operationId）",
        "",
        "> 本文件由 `tools/extract_api_catalog.py` 从各模块 PRD 第 8 节生成，**不手工编辑接口行**；",
        "> 新增 / 修改接口先改模块 PRD 第 8 节，再重跑工具。",
        "> 阶段 5 的 `api/openapi.yaml` 与 `page-action-api-map.yaml` 必须以本清单的 operationId 为准。",
        "",
        "## 1. 服务归属",
        "",
        "| 归属 | 说明 | 覆盖的模块 |",
        "|---|---|---|",
        "| `ruoyi-edu`（教育域主服务） | 首轮全部教育业务接口都在该服务内，避免早期拆服务带来的分布式事务成本 | 学生 / 教师 / 年级 / 班级 / 升班 / 选科 / 学科 / 学校 / 学年学期 |",
        "| `ruoyi-edu` + 消息（RabbitMQ） | 导入执行、升班执行、教学班生成、日志归档等耗时操作走异步任务，接口只入队 | 导入导出与异步任务 |",
        "| `ruoyi-edu` + Elasticsearch（后续） | 题库检索不在首轮；审计日志的归档检索首轮用数据库分区实现 | 审计与操作日志（检索部分） |",
        "",
        "## 2. 权限点推断规则",
        "",
        "| 路径前缀 | 权限资源（`05-permission-matrix.yaml`） |",
        "|---|---|",
    ]
    for prefix, resource in RESOURCE_RULES:
        lines.append(f"| `{prefix}` | `{resource}` |")
    lines += [
        "",
        "动作名映射：`list*` / `get*` → `read`；`add*` → `create`；`update*` / `save*` / `assign*` / `change*` → `update`；",
        "`remove*` / `disable*` → `delete`（逻辑删除）；`export*` → `export`；`import*` / `validate*` → `import`；",
        "`approve*` → `approve`；`submit*` / `execute*` / `cancel*` / `retry*` / `replay*` → 资源已有的写动作（见 CR-012 的口径）。",
        "",
        "## 3. 同步 / 异步边界",
        "",
        "**同步**：单条增删改查、列表分页、详情、统计（≤ 5000 行）、审批、状态流转。",
        "",
        "**异步（入队后立即返回任务号）**：导入执行、导出（> 2000 行）、升班预览与执行、教学班生成（> 1 万人）、",
        "归档区间检索（> 30 秒）、任务重试与死信重放。异步接口统一返回 `task_no`，进度与结果在异步任务中心查询。",
        "",
        f"## 4. 接口总览（共 {len(all_rows)} 个 operationId）",
        "",
        "| 模块 | 接口数 |",
        "|---|---|",
    ]
    for m in sorted(per_module):
        lines.append(f"| {MODULE_CN.get(m, m)}（`{m}`） | {len(per_module[m])} |")
    lines += ["| **合计** | **" + str(len(all_rows)) + "** |", ""]

    for m in sorted(per_module):
        rows = per_module[m]
        if not rows:
            continue
        lines += [
            f"## 5.{list(sorted(per_module)).index(m) + 1} {MODULE_CN.get(m, m)}（`{m}`）",
            "",
            "| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |",
            "|---|---|---|---|---|---|---|---|",
        ]
        for idx, r in enumerate(rows, 1):
            async_flag = "异步" if r["op"] in ASYNC_HINTS or "异步" in r["desc"] else "同步"
            lines.append(
                f"| {idx} | `{r['op']}` | {r['method']} | `{r['path']}` | {r['desc']} | "
                f"`{resource_of(r['path'])}` | {method_kind(r['method'])} | {async_flag} |"
            )
        lines.append("")

    lines += [
        "## 6. 与原型动作的对应关系",
        "",
        "原型里每个 `data-action-id` 都通过 `data-api` 指向本清单的 operationId；",
        "对应关系的机器可读版本在阶段 5 的 `page-action-api-map.yaml`（覆盖 403 个动作编号）。",
        "不调接口的动作（打开弹窗、切换筛选项、跳转页面）在映射表里记 `-`，并在 `page-actions.yaml` 里同样登记。",
        "",
    ]
    with io.open(OUT, "w", encoding="utf-8", newline="") as fh:
        fh.write("\n".join(lines))

    print(f"生成 {os.path.relpath(OUT, REPO_ROOT)}：{len(per_module)} 个模块 / {len(all_rows)} 个 operationId")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
