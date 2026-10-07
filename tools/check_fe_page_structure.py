#!/usr/bin/env python3
"""生产前端页面结构对照检查（阶段 6）。

把已交付的生产页面与它对应的高保真 / 业务原型逐项对照，防止「写代码时悄悄改了分组或列序」：

  查询区字段顺序与语义分组   ←→  原型 data-role="filter" 段内的 label 顺序
  表格列顺序、列名与分组     ←→  原型 <th data-role="column"> 的 data-field / data-layout-group / 文本
  操作列独立分组并固定右侧   ←→  原型 data-layout-group="操作" 的列 + 生产页 fixed="right"

依据：docs/00-governance/page-field-layout.md（语义分组、组内顺序、表格列序、操作列独立）。
首轮只登记已交付的页面；每交付一页在此加一条 CHECK。

用法：python tools/check_fe_page_structure.py
退出码：0 通过，1 发现问题。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 已交付页面的对照清单：原型页面 ←→ 生产页面
CHECKS = [
    {
        "page_id": "PAGE-STU-LIST",
        "name": "学生管理列表",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/index.vue",
        # 原型有、生产页本批不做：写清原因，不算失败
        "deferred_groups": {
            "选择": "批量操作（批量导出 / 批量调班）在阶段 6 后续批次交付，见 CR-044 的边界说明"
        },
    },
    {
        "page_id": "PAGE-CLS-LIST",
        "name": "班级管理列表",
        "prototype": "prototypes/functional/v2/pages/class-list.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-GRD-LIST",
        "name": "年级管理列表",
        "prototype": "prototypes/functional/v2/pages/grade-list.html",
        "vue": "apps/plus-ui/src/views/edu/grade/grd_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-TCH-LIST",
        "name": "教师管理列表",
        "prototype": "prototypes/functional/v2/pages/teacher-list.html",
        "vue": "apps/plus-ui/src/views/edu/teacher/tch_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-SUB-LIST",
        "name": "学科与配置列表",
        "prototype": "prototypes/functional/v1/pages/subject-list.html",
        "vue": "apps/plus-ui/src/views/edu/subject/sub_list/index.vue",
        "deferred_groups": {},
        "deferred_labels": {"操作": "行内动作（编辑 / 配置学段 / 启停用 / 引用检查）与对应弹窗在下一批交付"},
    },
    {
        "page_id": "PAGE-TERM-LIST",
        "name": "学年学期列表",
        "prototype": "prototypes/functional/v1/pages/term-list.html",
        "vue": "apps/plus-ui/src/views/edu/term/term_list/index.vue",
        "deferred_groups": {},
        "extra_columns": {"学年": "v1 原型的「学年」列没有 data-role 标记，生产页保留学年编码列"},
    },
    {
        "page_id": "PAGE-SCH-LIST",
        "name": "学校管理列表",
        "prototype": "prototypes/functional/v1/pages/school-list.html",
        "vue": "apps/plus-ui/src/views/edu/school/sch_list/index.vue",
        "deferred_groups": {},
        "extra_columns": {"学校名称": "v1 原型的「学校名称」列没有 data-role 标记，生产页保留学校名称列"},
    },
    {
        "page_id": "PAGE-AUDIT-LOG-LIST",
        "name": "操作日志列表",
        "prototype": "prototypes/functional/v1/pages/audit-log-list.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_log_list/index.vue",
        "deferred_groups": {},
        "extra_columns": {"时间": "v1 原型的时间列没有 data-role 标记，生产页保留时间列"},
        "filter_note": "审计 PRD 4.4 要求按时间范围 / 操作人 / 对象 / 操作类型筛选，v1 原型未给查询区标记",
    },
    {
        "page_id": "PAGE-AUDIT-OPS-ACCESS",
        "name": "运营访问记录",
        "prototype": "prototypes/functional/v1/pages/audit-ops-access.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_ops_access/index.vue",
        "deferred_groups": {},
        "extra_columns": {"访问时间": "v1 原型的访问时间列没有 data-role 标记，生产页保留访问时间列"},
    },
    {
        "page_id": "PAGE-AUDIT-SENSITIVE-ACCESS",
        "name": "敏感数据访问记录",
        "prototype": "prototypes/functional/v1/pages/audit-sensitive-access.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_sensitive_access/index.vue",
        "deferred_groups": {},
        "extra_columns": {"访问时间": "v1 原型的访问时间列没有 data-role 标记，生产页保留访问时间列"},
    },
    {
        "page_id": "PAGE-AUDIT-SECURITY-EVENT",
        "name": "登录与安全事件",
        "prototype": "prototypes/functional/v1/pages/audit-security-event.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_security_event/index.vue",
        "deferred_groups": {},
        "extra_columns": {"时间": "v1 原型的时间列没有 data-role 标记，生产页保留时间列"},
    },
    {
        "page_id": "PAGE-AUDIT-ARCHIVE",
        "name": "归档管理",
        "prototype": "prototypes/functional/v1/pages/audit-archive.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_archive/index.vue",
        "deferred_groups": {},
        "deferred_labels": {"操作": "归档区间检索（searchArchivedLog）在后续轮次交付"},
        "extra_columns": {"归档批次": "v1 原型的归档批次列没有 data-role 标记，生产页保留归档批次列"},
    },
    {
        "page_id": "PAGE-IMP-TASK-LIST",
        "name": "异步任务列表",
        "prototype": "prototypes/functional/v1/pages/async-task-list.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_task_list/index.vue",
        "deferred_groups": {},
        "extra_columns": {"任务编号": "v1 原型的任务编号列没有 data-role 标记，生产页保留任务编号列"},
    },
    {
        "page_id": "PAGE-IMP-DEADLETTER",
        "name": "死信任务",
        "prototype": "prototypes/functional/v1/pages/dead-letter-task.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_deadletter/index.vue",
        "deferred_groups": {},
        "extra_columns": {"任务编号": "v1 原型的任务编号列没有 data-role 标记，生产页保留任务编号列"},
    },
]

# 已交付浮层的对照清单：分组顺序来自原型（`data-layout-group` 与卡片标题）
OVERLAY_CHECKS = [
    {
        "page_id": "PAGE-STU-DETAIL",
        "name": "学生详情抽屉",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentDetailDrawer.vue",
        "groups": ["基础信息", "教育信息", "证件信息", "联系方式", "监护人", "变更记录"],
    },
    {
        "page_id": "PAGE-STU-CREATE",
        "name": "新增 / 编辑三步向导",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentFormDialog.vue",
        "steps": [
            ("学籍信息", ["基础信息", "教育信息", "补充信息"]),
            ("证件与联系", ["证件信息", "联系方式"]),
            ("监护人", ["监护人"]),
        ],
    },
    {
        "page_id": "PAGE-STU-STATUS",
        "name": "学籍异动弹窗（同文件按 mode=promotion 复用为 PAGE-PRM-CHANGE 异动登记）",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentStatusDialog.vue",
        "groups": ["异动信息", "复学 / 报到安排", "异动说明"],
    },
    {
        "page_id": "PAGE-STU-TRANSFER",
        "name": "调班弹窗",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentTransferDialog.vue",
        "groups": ["班级关系", "调班说明"],
    },
    {
        "page_id": "PAGE-STU-CROSS-TRANSFER",
        "name": "跨校转学（转出校）向导",
        "prototype": "prototypes/functional/v1/pages/student-cross-transfer.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_cross_transfer/index.vue",
        "steps": [
            ("选择学生", ["选择学生"]),
            ("选择转入校与目标班级", ["选择转入校与目标班级"]),
            ("确认与提交", ["确认与提交"]),
            ("结果", ["结果"]),
        ],
    },
    {
        "page_id": "PAGE-PRM-TRANSFER",
        "name": "跨校转学（转入校）向导",
        "prototype": "prototypes/functional/v1/pages/promotion-transfer.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_transfer/index.vue",
        "steps": [
            ("待接收转学单", ["待接收转学单"]),
            ("核对信息与接收", ["核对信息与接收"]),
            ("接收确认", ["接收确认"]),
            ("报到", ["报到"]),
        ],
    },
    {
        "page_id": "PAGE-STU-IMPORT",
        "name": "学生批量导入向导",
        "prototype": "prototypes/functional/v1/pages/student-import.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_import/index.vue",
        "steps": [
            ("下载模板", ["下载模板"]),
            ("上传与校验", ["上传与校验"]),
            ("校验结果", ["校验结果"]),
            ("执行与进度", ["执行与进度"]),
        ],
    },
]

FILTER_SECTION_START = 'data-role="filter"'
FILTER_SECTION_END = 'data-role="table"'

problems: list[str] = []


def rel(path: str) -> str:
    return os.path.relpath(path, REPO_ROOT).replace("\\", "/")


def read(path: str) -> str:
    return io.open(os.path.join(REPO_ROOT, path), encoding="utf-8").read()


def parse_prototype_filter(html: str) -> list[str]:
    start = html.find(FILTER_SECTION_START)
    end = html.find(FILTER_SECTION_END)
    if start < 0 or end < 0:
        return []
    seg = html[start:end]
    labels = []
    for m in re.finditer(r"<label[^>]*>(.*?)</label>", seg, re.S):
        text = re.sub(r"<[^>]+>", "", m.group(1)).strip()
        text = text.replace("*", "").strip()
        if text:
            labels.append(text)
    return labels


def parse_prototype_columns(html: str) -> list[dict]:
    columns = []
    # 只解析主表：页面里可能还有展开行 / 嵌套的小表，限定到 data-role="table" 所在的这张表
    marker = html.find('data-role="table"')
    if marker >= 0:
        end = html.find("</table>", marker)
        if end > 0:
            html = html[marker:end]
    for m in re.finditer(r"<th([^>]*)>(.*?)</th>", html, re.S):
        attrs, inner = m.group(1), m.group(2)
        label_text = re.sub(r"<[^>]+>", "", inner).strip()
        # 数据列靠 data-role="column" 识别；操作列在部分原型里没有 data-* 标记，按列名兜住
        if 'data-role="column"' not in attrs and 'data-layout-group="操作"' not in attrs and label_text != "操作":
            continue
        field = re.search(r'data-field="([^"]+)"', attrs)
        group = re.search(r'data-layout-group="([^"]+)"', attrs)
        label = label_text
        columns.append(
            {
                "field": field.group(1) if field else "",
                "group": group.group(1) if group else "",
                "label": label,
            }
        )
    return columns


def parse_vue_filter(vue: str) -> list[str]:
    labels = []
    for m in re.finditer(r"<el-form-item\b([^>]*)>", vue, re.S):
        attrs = m.group(1)
        if 'data-layout-group="' not in attrs:
            continue
        label = re.search(r'\blabel="([^"]+)"', attrs)
        if label:
            labels.append(label.group(1).strip())
    return labels


def parse_vue_columns(vue: str) -> list[dict]:
    columns = []
    for m in re.finditer(r"<el-table-column\b([^>]*)>", vue, re.S):
        attrs = m.group(1)
        label = re.search(r'\blabel="([^"]+)"', attrs)
        group = re.search(r'data-layout-group="([^"]+)"', attrs)
        if not label or not group:
            continue
        prop = re.search(r'\bprop="([^"]+)"', attrs)
        columns.append(
            {
                "prop": prop.group(1) if prop else "",
                "group": group.group(1),
                "label": label.group(1).strip(),
                "fixed_right": 'fixed="right"' in attrs,
            }
        )
    return columns


def check_filter(check: dict, prototype_html: str, vue: str) -> None:
    expected = parse_prototype_filter(prototype_html)
    actual = parse_vue_filter(vue)
    if expected == actual:
        print("  查询区字段顺序一致：%d 项（%s）" % (len(actual), " → ".join(actual)))
        return
    # 冻结 v1 原型可能整页没有查询区标记；此时只核对生产页的查询项，不判失败
    if not expected and check.get("filter_note"):
        print("  原型没有查询区标记，生产页查询项（%d 项）：%s —— %s" % (len(actual), " → ".join(actual), check["filter_note"]))
        return
    problems.append(
        "%s 查询区字段顺序与原型不一致\n      原型：%s\n      生产：%s"
        % (check["page_id"], " → ".join(expected), " → ".join(actual))
    )


def check_columns(check: dict, prototype_html: str, vue: str) -> None:
    expected = parse_prototype_columns(prototype_html)
    deferred = check.get("deferred_groups") or {}
    expected_kept = []
    deferred_labels = check.get("deferred_labels") or {}
    for col in expected:
        if col["group"] in deferred:
            print("  原型列 %s（分组「%s」）本批不做：%s" % (col["label"], col["group"], deferred[col["group"]]))
            continue
        if col["label"] in deferred_labels:
            print("  原型列 %s 本批不做：%s" % (col["label"], deferred_labels[col["label"]]))
            continue
        expected_kept.append(col)

    actual = parse_vue_columns(vue)
    # 生产页可以保留原型未标记的列（v1 原型部分 th 没有 data-role），登记后不参与逐列比对
    extra_columns = check.get("extra_columns") or {}
    if extra_columns:
        kept_actual = []
        for col in actual:
            if col["label"] in extra_columns:
                print("  生产页保留列 %s：%s" % (col["label"], extra_columns[col["label"]]))
                continue
            kept_actual.append(col)
        actual = kept_actual
    ok = True
    if len(expected_kept) != len(actual):
        problems.append(
            "%s 表格列数与原型不一致：原型 %d 列（已扣除延后项），生产 %d 列"
            % (check["page_id"], len(expected_kept), len(actual))
        )
        ok = False

    for index, (exp, act) in enumerate(zip(expected_kept, actual), 1):
        if exp["label"] != act["label"]:
            problems.append("%s 第 %d 列列名不一致：原型「%s」，生产「%s」" % (check["page_id"], index, exp["label"], act["label"]))
            ok = False
        # 冻结的 v1 原型没有 data-layout-group，这类页面只对照列名与列序
        if exp["group"] and exp["group"] != act["group"]:
            problems.append("%s 第 %d 列分组不一致：原型「%s」，生产「%s」" % (check["page_id"], index, exp["group"], act["group"]))
            ok = False
        if exp["group"] == "操作" and not act["fixed_right"]:
            problems.append("%s 操作列必须 fixed=\"right\"（page-field-layout 第 4 节）" % check["page_id"])
            ok = False

    if ok:
        order = " → ".join("%s(%s)" % (c["label"], c["group"]) for c in actual)
        print("  表格列顺序与分组一致：%d 列（%s）" % (len(actual), order))


def main() -> int:
    for check in CHECKS:
        proto_path = os.path.join(REPO_ROOT, check["prototype"])
        vue_path = os.path.join(REPO_ROOT, check["vue"])
        for path in (proto_path, vue_path):
            if not os.path.isfile(path):
                problems.append("缺少文件：%s" % rel(path))
        if problems:
            break
        print("=== %s %s ===" % (check["page_id"], check["name"]))
        prototype_html = read(check["prototype"])
        vue = read(check["vue"])
        check_filter(check, prototype_html, vue)
        check_columns(check, prototype_html, vue)

    for check in OVERLAY_CHECKS:
        vue_path = os.path.join(REPO_ROOT, check["vue"])
        if not os.path.isfile(vue_path):
            problems.append("缺少文件：%s" % rel(vue_path))
            continue
        print("=== %s %s ===" % (check["page_id"], check["name"]))
        overlay = read(check["vue"])
        # 操作列是独立分组，且已在页面级检查里单独校验，不参与字段分组顺序
        groups = [g for g in re.findall(r'data-layout-group="([^"]+)"', overlay) if g != "操作"]
        expected_groups = list(check.get("groups") or [])
        for _, step_groups in check.get("steps") or []:
            expected_groups.extend(step_groups)
        if expected_groups and groups != expected_groups:
            problems.append(
                "%s 分组顺序与原型不一致\n      原型：%s\n      生产：%s"
                % (check["page_id"], " → ".join(expected_groups), " → ".join(groups))
            )
        else:
            print("  分组顺序一致：%s" % " → ".join(groups))
        if check.get("steps"):
            titles = re.findall(r'<el-step\s+title="([^"]+)"', overlay)
            expected_titles = [title for title, _ in check["steps"]]
            if titles != expected_titles:
                problems.append(
                    "%s 向导步骤与原型不一致：原型 %s，生产 %s"
                    % (check["page_id"], " / ".join(expected_titles), " / ".join(titles))
                )
            else:
                print("  向导步骤一致：%s" % " → ".join(titles))

    if problems:
        print("")
        for item in problems:
            print("问题：" + item)
        print("\n共 %d 项问题" % len(problems))
        return 1
    print("\n通过：生产页面结构与应用原型一致")
    return 0


if __name__ == "__main__":
    sys.exit(main())
