#!/usr/bin/env python3
"""用 Chrome DevTools Protocol 做真实点击验证：外壳菜单与跨页入口是否真的跳转。

背景：`--virtual-time-budget` 会把 setTimeout 全部快进，多步异步跳转没法在
`--dump-dom` 模式下验证；所以这里改用 CDP 控制一个真实的 headless Chrome，
点击后读 `location.href`，直接证明「点了有反应」。

用法：python tools/verify_nav_links.py
日志：evidence/stage3-highfidelity/nav-click-verify.log
"""

from __future__ import annotations

import asyncio
import io
import json
import os
import subprocess
import sys
import tempfile
import time
import urllib.parse
import urllib.request

import websockets

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PORT = 9333
LOG_PATH = os.path.join(REPO_ROOT, "evidence", "stage3-highfidelity", "nav-click-verify.log")
CHROME_CANDIDATES = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
]

HF = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/high-fidelity/v1/pages/"
FUNC = "file:///D:/work/person_work/ruoyi-cloud-monorepo/prototypes/functional/v1/pages/"

CASES = [
    ("HF-01", "高保真侧边菜单「异步任务」", HF + "grade-list.html",
     '[data-page-nav="PAGE-IMP-TASK-LIST"]', "async-task-list.html"),
    ("HF-02", "高保真侧边菜单「学科与配置」", HF + "grade-list.html",
     '[data-page-nav="PAGE-SUB-LIST"]', "subject-list.html"),
    ("HF-03", "高保真侧边菜单「审计日志」", HF + "grade-list.html",
     '[data-page-nav="PAGE-AUDIT-LOG-LIST"]', "audit-log-list.html"),
    ("HF-04", "高保真「查看任务中心」按钮", HF + "grade-list.html",
     '[data-nav="PAGE-IMP-TASK-LIST"]', "async-task-list.html"),
    ("FN-01", "业务原型侧边菜单「审计日志」", FUNC + "subject-list.html",
     '[data-page-nav="PAGE-AUDIT-LOG-LIST"]', "audit-log-list.html"),
    ("FN-02", "业务原型侧边菜单「学年学期」", FUNC + "subject-list.html",
     '[data-page-nav="PAGE-TERM-LIST"]', "term-list.html"),
    ("FN-03", "业务原型「查看任务中心」按钮", FUNC + "grade-list.html",
     '[data-nav="PAGE-IMP-TASK-LIST"]', "async-task-list.html"),
]


def find_chrome() -> str:
    for p in CHROME_CANDIDATES:
        if os.path.exists(p):
            return p
    raise SystemExit("找不到 Chrome")


class CDP:
    def __init__(self, ws):
        self.ws = ws
        self._id = 0

    async def send(self, method: str, params: dict | None = None):
        self._id += 1
        cur = self._id
        await self.ws.send(json.dumps({"id": cur, "method": method, "params": params or {}}))
        while True:
            raw = await asyncio.wait_for(self.ws.recv(), timeout=30)
            msg = json.loads(raw)
            if msg.get("id") == cur:
                return msg.get("result", {})

    async def eval(self, expr: str):
        r = await self.send("Runtime.evaluate", {"expression": expr, "returnByValue": True})
        return r.get("result", {}).get("value")


async def run_case(cdp: CDP, case):
    cid, label, page_url, selector, expect = case
    await cdp.send("Page.navigate", {"url": page_url})
    await asyncio.sleep(1.5)
    sel = json.dumps(selector)
    exists = await cdp.eval(f"!!document.querySelector({sel})")
    if not exists:
        return cid, label, False, "页面上找不到该元素"
    await cdp.eval(f"document.querySelector({sel}).click()")
    await asyncio.sleep(1.5)
    href = await cdp.eval("location.href.split('/').pop().split('?')[0]")
    return cid, label, href == expect, f"跳到 {href}（期望 {expect}）"


async def main() -> int:
    chrome = find_chrome()
    profile = os.path.join(tempfile.gettempdir(), "codex-cdp-nav")
    proc = subprocess.Popen(
        [chrome, "--headless=new", "--disable-gpu", "--no-first-run",
         "--allow-file-access-from-files", f"--remote-debugging-port={PORT}",
         f"--user-data-dir={profile}", "about:blank"],
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
    )
    try:
        for _ in range(60):
            try:
                urllib.request.urlopen(f"http://127.0.0.1:{PORT}/json/version", timeout=1).read()
                break
            except Exception:
                time.sleep(0.25)
        else:
            raise SystemExit("Chrome CDP 未就绪")

        req = urllib.request.Request(
            f"http://127.0.0.1:{PORT}/json/new?{urllib.parse.quote(FUNC + 'grade-list.html')}",
            method="PUT",
        )
        target = json.loads(urllib.request.urlopen(req, timeout=5).read())
        ws_url = target["webSocketDebuggerUrl"]

        lines = []
        passed = 0
        async with websockets.connect(ws_url, max_size=8 * 1024 * 1024) as ws:
            cdp = CDP(ws)
            await cdp.send("Page.enable")
            await cdp.send("Runtime.enable")
            for case in CASES:
                cid, label, ok, detail = await run_case(cdp, case)
                if ok:
                    passed += 1
                mark = "PASS" if ok else "FAIL"
                line = f"[{mark}] {cid} {label} —— {detail}"
                lines.append(line)
                print(line, flush=True)

        summary = f"真实点击验证：{passed} / {len(CASES)} 通过"
        print(summary, flush=True)
        lines.append(summary)
        os.makedirs(os.path.dirname(LOG_PATH), exist_ok=True)
        with io.open(LOG_PATH, "w", encoding="utf-8", newline="\n") as fh:
            fh.write("\n".join(lines) + "\n")
        return 0 if passed == len(CASES) else 1
    finally:
        proc.terminate()


if __name__ == "__main__":
    raise SystemExit(asyncio.run(main()))
