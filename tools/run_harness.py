#!/usr/bin/env python3
"""批量运行 evidence/stage2-prototype 下的断言 harness（verify-*.html）。

每个 harness 在页面里通过 iframe 载入目标原型，跑完断言后把结果写进 <p class="sum">，
文本格式为：合计 N / EXPECTED 条，通过 M 条，不通过 K 条 —— 全部通过。

本脚本用 headless Chrome 的 --dump-dom 抓取该行，逐个人工核对口径相同：
  - RESULTS 条数必须等于 EXPECTED（防止 harness 自身漏跑断言）
  - 不通过条数必须为 0

用法：
  python tools/run_harness.py                 # 全部 harness
  python tools/run_harness.py verify-task     # 只跑名字匹配的
日志：evidence/stage2-prototype/harness-regression.log
"""

from __future__ import annotations

import glob
import io
import os
import pathlib
import re
import subprocess
import sys
import tempfile

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HARNESS_DIR = os.path.join(REPO_ROOT, "evidence", "stage2-prototype")
LOG_PATH = os.path.join(HARNESS_DIR, "harness-regression.log")

CHROME_CANDIDATES = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
]

# 两种格式都要支持：
#   合计 36 / 36 条，通过 36 条，不通过 0 条
#   合计 12 条，通过 12 条，不通过 0 条     （早期批次只打印实际条数）
SUM_RE = re.compile(
    r"合计\s*(\d+)\s*(?:/\s*(\d+)\s*)?条，通过\s*(\d+)\s*条，不通过\s*(\d+)\s*条"
)


def find_chrome() -> str:
    for path in CHROME_CANDIDATES:
        if os.path.exists(path):
            return path
    raise SystemExit("找不到 Chrome，请检查安装路径")


def run_one(chrome: str, harness: str, profile: str):
    url = pathlib.Path(harness).as_uri()
    cmd = [
        chrome,
        "--headless=new",
        "--disable-gpu",
        "--no-first-run",
        "--allow-file-access-from-files",
        f"--user-data-dir={profile}",
        "--virtual-time-budget=60000",
        "--dump-dom",
        url,
    ]
    proc = subprocess.run(cmd, capture_output=True, timeout=300)
    dom = proc.stdout.decode("utf-8", "replace")
    m = SUM_RE.search(dom)
    if not m:
        return {"ok": False, "detail": "未找到结果行（harness 可能未执行完成）"}
    total = int(m.group(1))
    expected = int(m.group(2)) if m.group(2) else total
    passed = int(m.group(3))
    failed = int(m.group(4))
    ok = failed == 0 and total == expected
    detail = f"合计 {total}/{expected} 条，通过 {passed} 条，不通过 {failed} 条"
    if not ok:
        detail += f"（期望 {expected} 条全通过）"
    return {"ok": ok, "detail": detail}


def main() -> int:
    pattern = sys.argv[1] if len(sys.argv) > 1 else ""
    chrome = find_chrome()
    profile = os.path.join(tempfile.gettempdir(), "codex-harness-run")

    files = sorted(glob.glob(os.path.join(HARNESS_DIR, "verify-*.html")))
    if pattern:
        files = [f for f in files if pattern in os.path.basename(f)]
    if not files:
        raise SystemExit("没有匹配的 harness 文件")

    lines = []
    failed_names = []
    total_assertions = 0
    for f in files:
        name = os.path.basename(f)
        try:
            r = run_one(chrome, f, profile)
        except subprocess.TimeoutExpired:
            r = {"ok": False, "detail": "超时（>300s）"}
        mark = "PASS" if r["ok"] else "FAIL"
        lines.append(f"[{mark}] {name} —— {r['detail']}")
        print(lines[-1], flush=True)
        m = SUM_RE.search(r["detail"])
        if m:
            total_assertions += int(m.group(1))
        if not r["ok"]:
            failed_names.append(name)

    summary = (
        f"harness 回归：{len(files) - len(failed_names)} / {len(files)} 通过"
        f"；累计断言 {total_assertions} 条"
        + ("" if not failed_names else "；失败：" + ", ".join(failed_names))
    )
    print(summary, flush=True)
    with io.open(LOG_PATH, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n" + summary + "\n")
    return 1 if failed_names else 0


if __name__ == "__main__":
    raise SystemExit(main())
