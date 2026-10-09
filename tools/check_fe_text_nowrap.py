#!/usr/bin/env python3
"""生产前端排版检查：文字不换行 / 组件对齐（阶段 6）。

依据用户 2026-10-09 截图反馈：查询区 label（「证件号后四位」）与表格操作列按钮
（「校区管理 / 学段配置」）在窄列下折行。

检查两条：

1. 全局排版保护在位 —— `src/assets/styles/index.scss` 必须包含
   `.el-form--inline .el-form-item__label { white-space: nowrap }` 与
   `.el-table .cell > .el-button { white-space: nowrap }`；
2. 操作列宽度足够 —— 按「同行最多同时出现的按钮」估算总宽：
   无条件的按钮全部计入；带 v-if / v-else 的按钮按条件字符串分组，同组相加、组间取最大值；
   `<template v-if>` 包裹的整块算一个条件组。
   估算口径：中文 14px、其它字符 8px、按钮间距 12px、单元格左右内边距 26px。

用法：python tools/check_fe_text_nowrap.py
退出码：0 通过，1 发现问题。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STYLE_FILE = "apps/plus-ui/src/assets/styles/index.scss"
VIEWS_ROOT = "apps/plus-ui/src/views/edu"

STYLE_RULES = [
    (".el-form--inline .el-form-item__label", "查询区 label 不换行"),
    (".el-table .cell > .el-button", "表格行内按钮不折行"),
]

CHAR_CN = 14
CHAR_OTHER = 8
GAP = 12
CELL_PADDING = 26


def text_width(text: str) -> int:
    total = 0
    for ch in text:
        total += CHAR_CN if ord(ch) > 0x2E80 else CHAR_OTHER
    return total


def button_text(inner: str) -> str:
    s = re.sub(r"<[^>]+>", "", inner)
    s = re.sub(r"\{\{[^}]*\}\}", "xx", s)
    return s.strip()


def estimate_column_need(inner: str):
    """返回 (估算宽度, 同时出现的按钮文本列表)。"""
    groups = {}
    common = []

    # 1) <template v-if|v-else-if|v-else ...> ... </template> 整块视作一个条件组
    for m in re.finditer(r"<template\s+(v-(?:if|else-if|else)(?:=\"[^\"]*\")?)[^>]*>(.*?)</template>", inner, re.S):
        cond, body = m.group(1), m.group(2)
        texts = [button_text(b.group(1)) for b in re.finditer(r"<el-button[^>]*>([\s\S]*?)</el-button\s*>", body, re.S)]
        texts = [t for t in texts if t]
        if texts:
            key = ("template", cond)
            groups.setdefault(key, []).extend(texts)

    # 2) 其余按钮：按自身 v-if / v-else 分组
    stripped = re.sub(r"<template\s+v-(?:if|else-if|else)(?:=\"[^\"]*\")?[^>]*>.*?</template>", "", inner, flags=re.S)
    for m in re.finditer(r"<el-button([^>]*)>([\s\S]*?)</el-button\s*>", stripped, re.S):
        attrs, body = m.group(1), m.group(2)
        txt = button_text(body)
        if not txt:
            continue
        cond = re.search(r'v-if="([^"]*)"', attrs)
        if cond:
            groups.setdefault(("cond", cond.group(1)), []).append(txt)
        elif re.search(r"\bv-else\b", attrs):
            groups.setdefault(("cond", "__else__"), []).append(txt)
        else:
            common.append(txt)

    common_w = sum(text_width(t) for t in common)
    best_group, best_w = [], 0
    for texts in groups.values():
        w = sum(text_width(t) for t in texts)
        if w > best_w:
            best_w, best_group = w, texts

    count = len(common) + len(best_group)
    need = common_w + best_w + GAP * max(0, count - 1) + CELL_PADDING
    inline = common + best_group
    if count > 1 and inline:
        need = max(need, max(text_width(t) for t in inline) + CELL_PADDING)
    return need, inline


def main() -> int:
    problems = []
    print("=== 生产前端排版检查（文字不换行 / 组件对齐）===")

    style_path = os.path.join(REPO_ROOT, STYLE_FILE)
    if not os.path.exists(style_path):
        problems.append("找不到全局样式文件：%s" % STYLE_FILE)
        style_text = ""
    else:
        style_text = io.open(style_path, encoding="utf-8").read()
    for selector, desc in STYLE_RULES:
        if selector in style_text and "nowrap" in style_text.split(selector, 1)[1][:200]:
            print("  全局样式：%s 保护在位（%s）" % (desc, selector))
        else:
            problems.append("全局样式缺少「%s」保护：%s 需要 white-space: nowrap" % (desc, selector))

    views_root = os.path.join(REPO_ROOT, VIEWS_ROOT)
    columns = 0
    for dp, _dn, fn in os.walk(views_root):
        for f in sorted(fn):
            if not f.endswith(".vue"):
                continue
            path = os.path.join(dp, f)
            rel = os.path.relpath(path, REPO_ROOT).replace(os.sep, "/")
            text = io.open(path, encoding="utf-8").read()
            for m in re.finditer(r'<el-table-column([^>]*label="操作"[^>]*)>', text):
                attrs = m.group(1)
                width_m = re.search(r'width="(\d+)"', attrs)
                width = int(width_m.group(1)) if width_m else 0
                rest = text[m.end():]
                end = rest.find("</el-table-column>")
                inner = rest[:end] if end > 0 else rest[:4000]
                need, texts = estimate_column_need(inner)
                if not texts:
                    continue
                columns += 1
                line = text[: m.start()].count("\n") + 1
                if width and width < need:
                    problems.append(
                        "%s:%d 操作列宽度不足：width=%d，估算需要 %d（%s）"
                        % (rel, line, width, need, " / ".join(texts))
                    )
                elif not width:
                    problems.append("%s:%d 操作列未设置 width，按钮会随内容折行（估算需要 %d）" % (rel, line, need))

    print("  操作列：检查 %d 个" % columns)

    # ---- 组件对齐 ----
    align_root = os.path.join(REPO_ROOT, VIEWS_ROOT)
    aligned = 0
    for dp, _dn, fn in os.walk(align_root):
        for f in sorted(fn):
            if not f.endswith(".vue"):
                continue
            path = os.path.join(dp, f)
            rel = os.path.relpath(path, REPO_ROOT).replace(os.sep, "/")
            text = io.open(path, encoding="utf-8").read()
            body = text.split("<script", 1)[0]

            # (1) 页面标题区必须在页面根容器的第一个位置
            rootm = re.search(r'<div class="p-2"[^>]*>\s*\n\s*(.*)', body)
            if rootm and "page-head" not in rootm.group(1)[:200] and "page-head" in body:
                problems.append("%s 页面的 page-head 不在根容器第一个位置，会与查询区错位" % rel)

            # (2) 同一 el-form 内不能混用 label-width（会造成标签与控件基线不一致）
            for fm in re.finditer(r"<el-form([^>]*)>(.*?)</el-form>", body, re.S):
                attrs, inner = fm.group(1), fm.group(2)
                own = re.search(r'label-width="([^"]+)"', attrs)
                item_widths = re.findall(r'<el-form-item[^>]*label-width="([^"]+)"', inner)
                if not own and len(set(item_widths)) > 1:
                    problems.append("%s 同一 el-form 内混用多个 label-width（%s），字段会错位"
                                    % (rel, " / ".join(sorted(set(item_widths)))))

            # (3) 操作列必须固定在右侧（page-field-layout 第 4 节 / CR-015）
            for cm in re.finditer(r'<el-table-column([^>]*label="操作"[^>]*)>', body):
                if 'fixed="right"' not in cm.group(1):
                    line = body[: cm.start()].count("\n") + 1
                    problems.append("%s:%d 操作列未固定右侧，横向滚动时会与数据列错位" % (rel, line))
            aligned += 1
    print("  组件对齐：检查 %d 个页面" % aligned)

    if problems:
        print("")
        for item in problems:
            print("问题：" + item)
        print("\n共 %d 项问题" % len(problems))
        return 1
    print("\n通过：查询区 label 与操作列按钮均不折行")
    return 0


if __name__ == "__main__":
    sys.exit(main())
