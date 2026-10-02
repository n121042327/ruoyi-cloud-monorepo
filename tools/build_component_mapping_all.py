#!/usr/bin/env python3
"""为全部高保真页面生成逐元素组件映射（GAP-064 全量扩展）。

输入：prototypes/high-fidelity/v1/pages/*.html（45 页，含同页浮层片段）
      prototypes/functional/v1/pages/*.html（页面编号与动作编号）
      prototypes/high-fidelity/v1/page-manifest.yaml（页数与元素计数口径）
输出：prototypes/high-fidelity/v2/component-mapping-all-pages.yaml

规则：
1. 逐页登记**可交互元素**（button / input / textarea / select / el-table / 分页 / 浮层片段），
   与 page-manifest.yaml 的 buttons + inputs + selects + tables 计数对齐；
2. 组件名优先取原型的 data-component 声明；没有声明时按 v1 通用规则推断，并在 basis 里写明；
3. 选择器优先用 id，其次 data-action-id / data-field / data-demo-panel；同类重复时标 selector_unique=false
   并给出同类序号，不假装唯一；
4. 学生列表的 v2 逐元素样板（207 条，含权限 / 接口 / 角色字段）保持独立文件，本脚本不覆盖它。

用法：python tools/build_component_mapping_all.py
"""

from __future__ import annotations

import io
import os
import re
from html.parser import HTMLParser

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HIFI_DIR = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "pages")
FUNC_DIR = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "pages")
MANIFEST = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "page-manifest.yaml")
OUT = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v2", "component-mapping-all-pages.yaml")

PAGE_ID_RE = re.compile(r'data-page="([A-Z][A-Z0-9-]+)"')
PANEL_RE = re.compile(r'data-demo-panel="([A-Za-z0-9-]+)"')
ACTIONS_RE = re.compile(r'data-action-id="(ACT-[A-Z0-9-]+)"')
FIELD_RE = re.compile(r'data-field="([A-Za-z0-9_]+)"')
PANEL_TITLE_RE = re.compile(r'<h2>(.*?)</h2>', re.S)


class Scanner(HTMLParser):
    """收集需要登记的元素：(tag, attrs, 是否浮层根)。"""

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.items = []
        self.depth_overlay = 0

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        kind = None
        classes = (attrs.get("class") or "").split()
        if attrs.get("data-demo-panel"):
            kind = "panel"
        elif tag == "button":
            kind = "button"
        elif tag == "select":
            kind = "select"
        elif tag == "textarea":
            kind = "textarea"
        elif tag == "input":
            kind = "input"
        elif tag == "table" and "el-table" in classes:
            kind = "table"
        elif "pagination" in classes:
            kind = "pagination"
        elif attrs.get("data-component"):
            kind = "declared"
        if kind:
            self.items.append({"tag": tag, "attrs": attrs, "kind": kind})


def selector_of(item, seen):
    attrs = item["attrs"]
    if attrs.get("id"):
        base = "#" + attrs["id"]
        needle = 'id="%s"' % attrs["id"]
    elif attrs.get("data-action-id"):
        base = '[data-action-id="%s"]' % attrs["data-action-id"]
        needle = 'data-action-id="%s"' % attrs["data-action-id"]
    elif attrs.get("data-demo-panel"):
        base = '[data-demo-panel="%s"]' % attrs["data-demo-panel"]
        needle = 'data-demo-panel="%s"' % attrs["data-demo-panel"]
    elif attrs.get("data-field"):
        base = '%s[data-field="%s"]' % (item["tag"], attrs["data-field"])
        needle = 'data-field="%s"' % attrs["data-field"]
    elif attrs.get("data-component"):
        base = '%s[data-component="%s"]' % (item["tag"], attrs["data-component"])
        needle = 'data-component="%s"' % attrs["data-component"]
    else:
        classes = ".".join((attrs.get("class") or "").split())
        base = "%s.%s" % (item["tag"], classes) if classes else item["tag"]
        needle = 'class="%s"' % (attrs.get("class") or "")
    seen[base] = seen.get(base, 0) + 1
    return base, seen[base], needle


def component_of(item):
    attrs, tag = item["attrs"], item["tag"]
    raw = attrs.get("data-component")
    if raw:
        head, _, tail = raw.partition(".")
        props = {}
        parts = [p for p in tail.split(".") if p]
        if "textarea" in parts:
            props["type"] = "textarea"
        for value in ("primary", "danger", "success", "warning", "info"):
            if value in parts:
                props["type"] = value
        for value in ("plain", "link"):
            if value in parts:
                props[value] = True
        if "multiple" in parts:
            props["multiple"] = True
        if raw.endswith("daterange"):
            props["type"] = "daterange"
        return head or raw, props, "原型明确声明"
    if item["kind"] == "panel":
        kind = attrs.get("data-overlay-kind") or "dialog"
        size = attrs.get("data-size") or ""
        return ("el-drawer" if kind == "drawer" else "el-dialog"), ({"size": size} if size else {}), "已声明浮层载体"
    if tag == "button":
        classes = (attrs.get("class") or "").split()
        props = {}
        for value in ("primary", "danger", "success", "warning", "info"):
            if value in classes:
                props["type"] = value
        for value in ("plain", "link"):
            if value in classes:
                props[value] = True
        return "el-button", props, "按 v1 通用按钮规则"
    if tag == "select":
        return "el-select", {}, "原生下拉"
    if tag == "textarea":
        return "el-input", {"type": "textarea"}, "多行输入"
    if tag == "input":
        itype = (attrs.get("type") or "text").lower()
        if itype == "checkbox":
            return "el-checkbox", {}, "复选框"
        if itype == "radio":
            return "el-radio", {}, "单选框"
        if itype == "date":
            return "el-date-picker", {}, "日期输入"
        if "num" in (attrs.get("class") or ""):
            return "el-input-number", {}, "数字输入"
        return "el-input", {}, "文本输入"
    if item["kind"] == "table":
        return "el-table", {}, "表格"
    if item["kind"] == "pagination":
        return "el-pagination", {}, "分页控件"
    return "el-" + tag, {}, "按 v1 通用规则"


def main() -> int:
    manifest = yaml.safe_load(io.open(MANIFEST, encoding="utf-8"))
    pages, totals = [], {}
    for entry in manifest.get("pages", []):
        name = entry["file"]
        path = os.path.join(HIFI_DIR, name)
        if not os.path.isfile(path):
            continue
        html = io.open(path, encoding="utf-8").read()
        scanner = Scanner()
        scanner.feed(html)
        seen, elements = {}, []
        prefix = re.sub(r"[^A-Z0-9]+", "-", name.replace(".html", "").upper())[:18]
        for n, item in enumerate(scanner.items, start=1):
            component, props, basis = component_of(item)
            selector, index, needle = selector_of(item, seen)
            attrs = item["attrs"]
            elements.append({
                "id": "EL-%s-%03d" % (prefix, n),
                "selector": selector,
                "selector_unique": index == 1 and needle != 'class=""' and html.count(needle) == 1,
                "kind": item["kind"],
                "tag": item["tag"],
                "element_plus": component,
                "props": props,
                "basis": basis,
                "page_id": attrs.get("data-page"),
                "panel_id": attrs.get("data-demo-panel"),
                "action_id": attrs.get("data-action-id"),
                "field": attrs.get("data-field") or (FIELD_RE.search(str(attrs)) and None),
            })
        func = os.path.join(FUNC_DIR, name)
        func_html = io.open(func, encoding="utf-8").read() if os.path.isfile(func) else ""
        pages.append({
            "file": name,
            "origin": entry.get("origin", "derived"),
            "title": (entry.get("title") or "").replace(" · 高保真", ""),
            "page_ids": sorted(set(PAGE_ID_RE.findall(func_html or html))),
            "panel_ids": sorted(set(PANEL_RE.findall(html))),
            "action_ids_count": len(set(ACTIONS_RE.findall(func_html or html))),
            "manifest_counts": entry.get("elements") or {},
            "elements": elements,
        })
        for e in elements:
            totals[e["kind"]] = totals.get(e["kind"], 0) + 1

    doc = {
        "schema_version": "1.0",
        "generated_by": "tools/build_component_mapping_all.py",
        "status": "已设计",
        "review_status": "待人工验收",
        "purpose": "全部 45 个高保真页面的逐元素组件映射；学生列表的 v2 精修样板另见 component-mapping.yaml。",
        "source": "prototypes/high-fidelity/v1/pages + prototypes/functional/v1/pages",
        "baseline": "prototypes/high-fidelity/v1/page-manifest.yaml",
        "coverage": {
            "html_files": len(pages),
            "elements": sum(len(p["elements"]) for p in pages),
            "by_kind": totals,
            "note": "本文件的元素口径比 page-manifest.yaml 更宽：manifest 只统计 class 带 btn / input / select / el-table 的元素，"
                    "本文件登记页内所有 button / input / select / textarea / el-table / pagination 与带 data-component 的元素，"
                    "因此条数更大；每页的 manifest_counts 与本文件的元素条数并列，便于逐页比对。"
                    "panel 是同页浮层片段，单独成类，不与页内元素重复计数。",
        },
        "conventions": {
            "scope": "每个可交互元素一条记录；只读展示元素（标签、卡片、字段值）不在本文件范围。",
            "component_rule": "优先取原型 data-component 声明；未声明时按标签与 class 推断，并在 basis 写明。",
            "identity_rule": "每个元素有页内唯一的 id（EL-<页面>-<序号>）；selector 是定位辅助，selector_unique=false 表示该选择器在页内重复，需结合 page_id / panel_id / action_id 区分。",
        },
        "pages": pages,
    }
    with io.open(OUT, "w", encoding="utf-8", newline="\n") as fh:
        yaml.safe_dump(doc, fh, allow_unicode=True, sort_keys=False, width=200)
    print("已生成 %s：%d 页 / %d 条元素 %s"
          % (os.path.relpath(OUT, REPO_ROOT).replace("\\", "/"), len(pages),
             doc["coverage"]["elements"], totals))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
