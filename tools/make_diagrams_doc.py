#!/usr/bin/env python3
"""把所有 Mermaid 源文件（*.mmd）汇总成一份可直接预览的 Markdown。

.mmd 是纯文本的 Mermaid 图定义，本身不是图片；单独打开看不到图。
本脚本把它们嵌进 `docs/diagrams.md` 的 ```mermaid 代码块，
用支持 Mermaid 的 Markdown 预览器（VS Code 扩展 / Typora / GitHub 等）即可直接看图。

用法：python tools/make_diagrams_doc.py
输出：docs/diagrams.md
"""

from __future__ import annotations

import io
import os

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(REPO_ROOT, "docs", "diagrams.md")

GROUPS = [
    ("概要设计 · 架构与流程", os.path.join(REPO_ROOT, "docs", "30-architecture", "diagrams")),
    ("详细设计 · ER 图", os.path.join(REPO_ROOT, "docs", "40-detailed-design", "database")),
    ("详细设计 · 时序图", os.path.join(REPO_ROOT, "docs", "40-detailed-design", "diagrams", "sequence")),
    ("详细设计 · 状态机", os.path.join(REPO_ROOT, "docs", "40-detailed-design", "diagrams", "state")),
    ("详细设计 · 领域模型", os.path.join(REPO_ROOT, "docs", "40-detailed-design", "diagrams", "class")),
]


def collect(folder: str) -> list:
    if not os.path.isdir(folder):
        return []
    out = []
    for name in sorted(os.listdir(folder)):
        if name.endswith(".mmd"):
            out.append(os.path.join(folder, name))
    return out


def main() -> int:
    lines = [
        "# Mermaid 图汇总（可直接预览）",
        "",
        "> 本文件由 `tools/make_diagrams_doc.py` 生成，把仓库里所有 `*.mmd` 源文件嵌成 Mermaid 代码块。",
        "> `.mmd` 是**纯文本**的图定义，不是图片，单独打开看不到图；",
        "> 用支持 Mermaid 的 Markdown 预览器打开本文件即可看到渲染结果：",
        "> VS Code（装 Markdown Preview Mermaid Support / Mermaid Preview 扩展）、Typora、Obsidian、",
        "> 或把代码块内容粘到 <https://mermaid.live>。",
        "",
    ]
    total = 0
    for title, folder in GROUPS:
        files = collect(folder)
        if not files:
            continue
        lines += [f"## {title}", ""]
        if folder.endswith("database"):
            files = [f for f in files if os.path.basename(f) == "er-diagram.mmd"]
        for f in files:
            rel = os.path.relpath(f, REPO_ROOT).replace("\\", "/")
            body = io.open(f, encoding="utf-8").read().strip()
            lines += [f"### `{os.path.basename(f)}`", "", f"源文件：`{rel}`", "", "```mermaid", body, "```", ""]
            total += 1
    lines += [f"---", "", f"共 {total} 张图。", ""]
    io.open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    print(f"已生成 {os.path.relpath(OUT, REPO_ROOT)}，共 {total} 张图")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
