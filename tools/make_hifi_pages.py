#!/usr/bin/env python3
"""生成阶段 3 高保真原型页面（由阶段 2 的功能原型派生）。

派生规则（只做两件事，不改信息架构）：
1. 视觉层替换：把阶段 2 的 prototype-shell.css / wizard.css 作为**组件基类**保留（相对路径改为跨目录），
   并在其后追加高保真覆盖层 assets/hifi.css —— 基类提供全部组件类，覆盖层换掉视觉 token；
2. 外壳替换：把 prototype-shell.js 换成 hifi-shell.js（8 角色 + 8 状态 + 高保真外壳）。

为什么派生而不是逐页手抄：阶段 3 的门禁要求「覆盖业务原型全部页面与状态，无功能删减」，
派生方式让列 / 字段 / 动作 / 状态片段与阶段 2 逐字节一致，天然满足该门禁；
视觉差异完全由 hifi.css 的覆盖层表达。需要超出覆盖层的手工精修时，该页从派生清单里排除并单独手写
（样板页 pages/student-list.html 就是手写的参考实现）。

用法：python tools/make_hifi_pages.py
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "pages")
DST_DIR = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "pages")

# 手工精修的页面：不参与派生（由人工维护）。
# 注意：手工精修的学生列表是**视觉基准**，放在 reference/student-list-visual-reference.html；
# pages/ 下的 45 个交付页全部由阶段 2 派生，以保证「覆盖全部页面与状态、无功能删减」这条门禁可被脚本证明。
HANDCRAFTED: set[str] = set()

BASE_CSS = '<link rel="stylesheet" href="../../functional/v1/assets/prototype-shell.css" />'
WIZARD_CSS = '<link rel="stylesheet" href="../../functional/v1/assets/wizard.css" />'
HIFI_CSS = '<link rel="stylesheet" href="../assets/hifi.css" />'
HIFI_JS = '<script src="../assets/hifi-shell.js"></script>'

SRC_BASE_CSS_TAG = '<link rel="stylesheet" href="../assets/prototype-shell.css" />'
SRC_WIZARD_CSS_TAG = '<link rel="stylesheet" href="../assets/wizard.css" />'
SRC_SHELL_JS_TAG = '<script src="../assets/prototype-shell.js"></script>'

BANNER = (
    "<!-- 阶段 3 高保真页面：由 prototypes/functional/v1/pages/{name} 派生（工具：tools/make_hifi_pages.py）。\n"
    "     只替换视觉层与外壳，信息架构 / 字段 / 动作 / 状态片段与阶段 2 一致；视觉差异见 assets/hifi.css 与 design-tokens.json。 -->\n"
)


def derive(name: str, body: str) -> str:
    if SRC_BASE_CSS_TAG not in body:
        raise ValueError(f"{name}: 未找到阶段 2 的 prototype-shell.css 引用")

    body = body.replace(SRC_BASE_CSS_TAG, BASE_CSS)
    body = body.replace(SRC_WIZARD_CSS_TAG, WIZARD_CSS)
    # 覆盖层必须排在所有基类之后
    body = body.replace("</head>", f"  {HIFI_CSS}\n</head>", 1)
    # 独立页（登录 / 403 / 404 / 500）不套管理外壳，只换视觉层，不注入高保真外壳脚本
    if SRC_SHELL_JS_TAG in body:
        body = body.replace(SRC_SHELL_JS_TAG, HIFI_JS)

    # 标题与主题色标记，便于人工与脚本识别高保真页面
    body = re.sub(r"<title>(.*?)</title>", lambda m: f"<title>{m.group(1)} · 高保真</title>", body, count=1)
    body = body.replace("<head>", "<head>\n" + BANNER.format(name=name), 1)
    return body


def main() -> int:
    if not os.path.isdir(SRC_DIR):
        print(f"缺少阶段 2 页面目录：{SRC_DIR}", file=sys.stderr)
        return 2
    os.makedirs(DST_DIR, exist_ok=True)

    written, skipped = [], []
    for name in sorted(os.listdir(SRC_DIR)):
        if not name.endswith(".html"):
            continue
        if name in HANDCRAFTED:
            skipped.append(name)
            continue
        with io.open(os.path.join(SRC_DIR, name), encoding="utf-8") as fh:
            body = fh.read()
        out = derive(name, body)
        with io.open(os.path.join(DST_DIR, name), "w", encoding="utf-8", newline="") as fh:
            fh.write(out)
        written.append(name)

    print(f"派生高保真页面 {len(written)} 个；手工维护跳过 {len(skipped)} 个（{', '.join(skipped) or '无'}）")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
