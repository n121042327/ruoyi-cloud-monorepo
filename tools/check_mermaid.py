#!/usr/bin/env python3
"""Mermaid 源文件结构检查（不依赖 Node）。

背景：docs/30-architecture/02-architecture.md 的架构分层图里出现过
subgraph 能力层（公共组件）——全角括号不是合法标识符字符，Mermaid 直接报
Lexical error on line N. Unrecognized text.，整个图渲染失败。

本脚本只做一件事：扫出没有用 id["标题"] 引号形式包裹、且标题里含有
Mermaid 不接受的字符的 subgraph 行。这类写法不一定报错（例如纯中文标题是合法的），
所以脚本只对含有危险字符的判失败，避免误报。

危险字符：全角/半角括号、方括号、花括号、逗号、冒号、斜杠、反斜杠、引号、井号、百分号。
判为合法的两种写法：
    1. subgraph 纯标题      —— 标题只含中英文、数字、空格与安全符号
    2. subgraph id["标题"]  —— 显式 id + 引号标题（推荐）

覆盖范围：仓库内所有 *.mmd，以及所有 Markdown 里的 mermaid 代码块。

用法：
    python tools/check_mermaid.py            # 全仓库
    python tools/check_mermaid.py docs       # 只查某个目录
退出码：0 通过，1 发现问题。

注意：这是静态检查，只能挡住已知的一类错误；权威校验仍是用真实解析器
（mermaid.parse，见 evidence 里的 Node 校验记录）跑一遍。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKIP_DIRS = {'.git', 'node_modules', '.idea', '.vscode', 'dist', 'target', '__pycache__'}

RISKY = set('（）()[]{}，,、：:/\\"\'#%')
SUBGRAPH_RE = re.compile(r'^\s*subgraph\s+(.*?)\s*$')
BRACKET_FORM_RE = re.compile(r'^\S+\s*\[\s*".*"\s*\]$')
FENCE_RE = re.compile(r'```mermaid\r?\n(.*?)```', re.S)
problems: list[str] = []


def rel(path: str) -> str:
    try:
        return os.path.relpath(path, REPO_ROOT).replace('\\', '/')
    except ValueError:
        # 目标目录不在同一盘符时（Windows）relpath 会抛错，退回绝对路径
        return path.replace('\\', '/')


def check_block(label: str, body: str) -> None:
    for lineno, line in enumerate(body.split('\n'), start=1):
        m = SUBGRAPH_RE.match(line)
        if not m:
            continue
        title = m.group(1)
        if BRACKET_FORM_RE.match(title):
            continue
        bad = sorted({ch for ch in title if ch in RISKY})
        if bad:
            problems.append(
                '%s:%d  subgraph 标题含 Mermaid 不接受的字符 %s：%s（建议改成 subgraph <ascii_id>["%s"]）'
                % (label, lineno, ''.join(bad), title, title)
            )


def walk(root: str):
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for name in filenames:
            if name.endswith('.mmd') or name.endswith('.md'):
                yield os.path.join(dirpath, name)


def main() -> int:
    target = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else REPO_ROOT
    count = 0
    for path in walk(target):
        text = io.open(path, encoding='utf-8').read()
        if path.endswith('.mmd'):
            count += 1
            check_block(rel(path), text)
        else:
            for i, block in enumerate(FENCE_RE.findall(text), start=1):
                count += 1
                check_block('%s#%d' % (rel(path), i), block)
    print('=== Mermaid 结构检查（%d 张图）===' % count)
    if problems:
        for p in problems:
            print('问题：' + p)
        print('\n共 %d 项问题' % len(problems))
        return 1
    print('通过：未发现问题')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
