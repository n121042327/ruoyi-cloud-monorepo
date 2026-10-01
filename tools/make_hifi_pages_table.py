#!/usr/bin/env python3
"""从 page-manifest.yaml 生成高保真入口页的「已交付页面」清单表。

背景：阶段 3 的入口 index.html 最初只列了 3-1 样板批的 3 行，3-2 ~ 3-9 派生出来的
42 页没有登记，人工验收时从入口看不到全量页面，只能靠侧边菜单。

本脚本把 index.html 里 `<!-- BEGIN pages-table -->` 到 `<!-- END pages-table -->`
之间的内容替换成按模块分组的全量清单（数据来自 page-manifest.yaml，避免手工誊抄）。

用法：python tools/make_hifi_pages_table.py
"""

from __future__ import annotations

import io
import os
import re

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HF = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1")
MANIFEST = os.path.join(HF, "page-manifest.yaml")
INDEX = os.path.join(HF, "index.html")
NAV = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "navigation.yaml")

MODULE_ORDER = [
    "student", "teacher", "grade", "class", "promotion", "stream",
    "subject", "school", "term", "import-export", "audit", "common",
]
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
    "common": "公共页面（登录 / 异常页）",
}
TYPE_CN = {
    "page": "独立页",
    "wizard": "向导页",
    "detail": "详情抽屉",
    "dialog": "弹窗",
    "block": "同页区块",
    "standalone": "独立页（不套外壳）",
}


PRIMARY_TYPES = ("page", "wizard", "detail", "standalone")


def pick_main(ids: list, nav_by_id: dict):
    """选该文件的主编号：优先取「可独立打开」的类型，其次任意存在项。

    例：teacher-assign.html 的 page_ids 第一个是 DIALOG-TCH-COPY（浮层片段），
    主编号应为 PAGE-TCH-ASSIGN。
    """
    for i in ids:
        p = nav_by_id.get(i)
        if p and p.get("type") in PRIMARY_TYPES:
            return i
    for i in ids:
        if i in nav_by_id:
            return i
    return ids[0] if ids else None


def build_rows(files: list, nav_by_id: dict) -> list:
    """把 45 个高保真文件映射成清单行（元数据取自 navigation.yaml）。"""
    rows = []
    for f in files:
        ids = f.get("page_ids") or []
        main_id = pick_main(ids, nav_by_id)
        main = nav_by_id.get(main_id) if main_id else None
        if not main:
            continue
        rows.append({
            "module": main.get("module", "common"),
            "id": main_id,
            "extra": len(ids) - 1,
            "name": main.get("name", ""),
            "type": main.get("type", "page"),
            "batch": main.get("batch", ""),
            "file": f.get("file", ""),
        })
    return rows


def build_section(rows: list) -> str:
    total = len(rows)
    out = [
        '<!-- BEGIN pages-table（由 tools/make_hifi_pages_table.py 生成，勿手改） -->',
        '  <section class="card">',
        f'    <div class="card-header"><h3>已交付页面（全量 {total} 页）</h3>'
        f'<span class="el-tag success">已产出待验收</span></div>',
        '    <div class="card-body">',
        '      <table class="el-table">',
        '        <thead>',
        '          <tr><th style="width:210px">页面编号</th><th>页面</th><th style="width:110px">类型</th>'
        '<th style="width:280px">入口</th><th style="width:90px">批次</th></tr>',
        '        </thead>',
        '        <tbody>',
    ]
    for module in MODULE_ORDER:
        items = [r for r in rows if r["module"] == module]
        if not items:
            continue
        out.append(
            f'          <tr><td colspan="5" style="background:#f5f7fa;font-weight:600">'
            f'{MODULE_CN.get(module, module)}（{len(items)} 页）</td></tr>'
        )
        for r in items:
            type_cn = TYPE_CN.get(r["type"], r["type"])
            id_text = r["id"] + (f'（+{r["extra"]} 个同页片段）' if r["extra"] > 0 else "")
            link = f'pages/{r["file"]}'
            out.append(
                '          <tr>'
                f'<td class="mono">{id_text}</td>'
                f'<td>{r["name"]}</td>'
                f'<td>{type_cn}</td>'
                f'<td><a href="{link}">{link}</a></td>'
                f'<td>{r["batch"]}</td>'
                '</tr>'
            )
    out += [
        '        </tbody>',
        '      </table>',
        '      <div class="legend" style="margin-top:12px">',
        '        <span>载体口径沿用阶段 2：<b>表单类浮层用弹窗、详情类用抽屉、含表格用独立页</b>（D-059 / CR-015）。'
        '带 <span class="mono">#panel=</span> 的入口表示该页面是同页浮层片段，进去后由演示面板自动打开。</span>',
        '      </div>',
        '    </div>',
        '  </section>',
        '  <!-- END pages-table -->',
    ]
    return "\n".join(out)


def main() -> int:
    data = yaml.safe_load(io.open(MANIFEST, encoding="utf-8"))
    nav = yaml.safe_load(io.open(NAV, encoding="utf-8"))
    nav_by_id = {p["id"]: p for p in (nav.get("pages") or [])}
    rows = build_rows(data.get("pages") or [], nav_by_id)
    html = io.open(INDEX, encoding="utf-8").read()

    section = build_section(rows)
    pattern = re.compile(
        r'<!-- BEGIN pages-table.*?<!-- END pages-table -->', re.S
    )
    if pattern.search(html):
        html = pattern.sub(lambda _: section, html)
    else:
        legacy = re.compile(
            r'<section class="card">\s*<div class="card-header"><h3>已交付页面（样板批 3-1）</h3>.*?</section>\n',
            re.S,
        )
        if not legacy.search(html):
            raise SystemExit("index.html 里找不到「已交付页面」section，无法替换")
        html = legacy.sub(section + "\n", html, count=1)

    io.open(INDEX, "w", encoding="utf-8", newline="").write(html)
    print(f"已写入 {len(rows)} 页清单 -> {os.path.relpath(INDEX, REPO_ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
