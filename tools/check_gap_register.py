#!/usr/bin/env python3
"""缺项登记表的重复键检查。

YAML 解析器遇到重复键时默认是「后者覆盖前者」，不报错。缺项表里每条缺项是一段
`- id: GAP-xxx` 块，如果同一块内出现两个 `status` 或两个 `answer`，登记就会出现
「看着改成 closed、实际还是 open」这种静默覆盖。本脚本扫描每个块的顶层键并报重复。

用法：python tools/check_gap_register.py
退出码：0 通过，1 有问题。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REGISTER = os.path.join(REPO_ROOT, "docs", "00-governance", "gap-register.yaml")
GAP_RE = re.compile(r"^  - id: (GAP-\d+)\s*$", re.M)
KEY_RE = re.compile(r"^    ([A-Za-z_][A-Za-z0-9_]*):")


def main() -> int:
    text = io.open(REGISTER, encoding="utf-8").read()
    marks = list(GAP_RE.finditer(text))
    problems = []
    for i, m in enumerate(marks):
        end = marks[i + 1].start() if i + 1 < len(marks) else len(text)
        block = text[m.end():end]
        keys = KEY_RE.findall(block)
        dup = sorted({k for k in keys if keys.count(k) > 1})
        if dup:
            problems.append("%s 的顶层键重复：%s" % (m.group(1), ", ".join(dup)))
    print("=== 缺项登记表重复键检查（%d 条）===" % len(marks))
    if problems:
        for p in problems:
            print("问题：" + p)
        print("\n共 %d 项问题" % len(problems))
        return 1
    print("通过：未发现重复键")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
