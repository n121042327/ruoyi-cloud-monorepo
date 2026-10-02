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
    }
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
    for m in re.finditer(r"<th([^>]*)>(.*?)</th>", html, re.S):
        attrs, inner = m.group(1), m.group(2)
        if 'data-role="column"' not in attrs and 'data-layout-group="操作"' not in attrs:
            continue
        field = re.search(r'data-field="([^"]+)"', attrs)
        group = re.search(r'data-layout-group="([^"]+)"', attrs)
        label = re.sub(r"<[^>]+>", "", inner).strip()
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
    problems.append(
        "%s 查询区字段顺序与原型不一致\n      原型：%s\n      生产：%s"
        % (check["page_id"], " → ".join(expected), " → ".join(actual))
    )


def check_columns(check: dict, prototype_html: str, vue: str) -> None:
    expected = parse_prototype_columns(prototype_html)
    deferred = check.get("deferred_groups") or {}
    expected_kept = []
    for col in expected:
        if col["group"] in deferred:
            print("  原型列 %s（分组「%s」）本批不做：%s" % (col["label"], col["group"], deferred[col["group"]]))
            continue
        expected_kept.append(col)

    actual = parse_vue_columns(vue)
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
        if exp["group"] != act["group"]:
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
