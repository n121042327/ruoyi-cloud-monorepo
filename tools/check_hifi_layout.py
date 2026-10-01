#!/usr/bin/env python3
"""用 CDP 实测高保真页面的布局问题（搜索区是否逐行堆叠、表格是否横向溢出）。

判定口径：
  - 搜索区：统计 .form-item 的 top 去重数 = 实际占用行数；行数 == 字段数 视为「一行一个」（异常）
  - 表格：table.offsetWidth 与父容器 clientWidth 比较，判断是否溢出；并核对列宽之和 vs min-width
  - 操作列：td.actions 的 computedStyle.display 是否为 flex（会破坏表格布局）

用法：
  python tools/check_hifi_layout.py               # 全部 45 页
  python tools/check_hifi_layout.py student-list  # 只查名字匹配的
"""

from __future__ import annotations

import asyncio
import io
import json
import os
import pathlib
import subprocess
import sys
import tempfile
import time
import urllib.parse
import urllib.request

import websockets

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PAGES_DIR = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "pages")
LOG_PATH = os.path.join(REPO_ROOT, "evidence", "stage3-highfidelity", "layout-check.log")
PORT = 9334
CHROME_CANDIDATES = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
]

MEASURE_JS = r"""
(() => {
  const out = { file: location.pathname.split('/').pop() };

  // 搜索区
  const inline = document.querySelector('.form-inline');
  const items = [...document.querySelectorAll('.form-inline .form-item')];
  if (inline) {
    const tops = [...new Set(items.map(el => Math.round(el.getBoundingClientRect().top)))];
    out.filter = {
      containerW: inline.offsetWidth,
      itemCount: items.length,
      rows: tops.length,
      itemWidths: items.map(el => el.offsetWidth),
      display: getComputedStyle(inline).display,
      flexWrap: getComputedStyle(inline).flexWrap,
      itemDisplay: items.length ? getComputedStyle(items[0]).display : null,
    };
  }

  // 表格
  out.tables = [...document.querySelectorAll('table.el-table')].map(t => {
    const wrap = t.parentElement;
    const ths = [...t.querySelectorAll('thead th')];
    const colSum = ths.reduce((s, th) => s + th.offsetWidth, 0);
    const cs = getComputedStyle(t);
    return {
      id: t.id || '(no-id)',
      tableW: t.offsetWidth,
      wrapW: wrap ? wrap.clientWidth : null,
      wrapScrollW: wrap ? wrap.scrollWidth : null,
      minWidth: cs.minWidth,
      layout: cs.tableLayout,
      colSum: colSum,
      cols: ths.length,
      overflow: wrap ? Math.max(0, t.offsetWidth - wrap.clientWidth) : null,
    };
  });

  // 操作列 display（flex 会破坏表格列对齐）
  const act = document.querySelector('table.el-table td.actions');
  out.actionsDisplay = act ? getComputedStyle(act).display : null;

  // 卡片直接子元素：只有「既没有内边距也没有上边框」的才算贴边
  // （部分页面用内联 <style> 给 .tab-bar 之类的块定义了 padding，不应误报）
  out.nakedCards = [...document.querySelectorAll('section.card')].map(c =>
    [...c.children].filter(k => !k.classList.contains('card-header') && !k.classList.contains('card-body'))
      .filter(k => {
        const cs = getComputedStyle(k);
        return cs.paddingTop === '0px' && cs.paddingLeft === '0px' && cs.borderTopWidth === '0px';
      })
      .map(k => k.className)
  ).flat().slice(0, 3);
  return JSON.stringify(out);
})()
"""


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

    async def measure(self, url: str):
        await self.send("Page.navigate", {"url": url})
        await asyncio.sleep(0.9)
        r = await self.send("Runtime.evaluate",
                            {"expression": MEASURE_JS, "returnByValue": True, "awaitPromise": True})
        val = r.get("result", {}).get("value")
        return json.loads(val) if val else None


async def main() -> int:
    pattern = sys.argv[1] if len(sys.argv) > 1 else ""
    pages_dir = os.path.abspath(os.environ.get("LAYOUT_DIR", PAGES_DIR))
    files = sorted(f for f in os.listdir(pages_dir) if f.endswith(".html"))
    if pattern:
        files = [f for f in files if pattern in f]
    if not files:
        raise SystemExit("没有匹配的页面")

    chrome = find_chrome()
    profile = os.path.join(tempfile.gettempdir(), "codex-layout-check")
    proc = subprocess.Popen(
        [chrome, "--headless=new", "--disable-gpu", "--no-first-run",
         "--allow-file-access-from-files", f"--remote-debugging-port={PORT}",
         f"--user-data-dir={profile}", "--window-size=1366,900", "about:blank"],
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
    )
    lines = []
    problems = []
    try:
        for _ in range(60):
            try:
                urllib.request.urlopen(f"http://127.0.0.1:{PORT}/json/version", timeout=1).read()
                break
            except Exception:
                time.sleep(0.25)
        else:
            raise SystemExit("Chrome CDP 未就绪")

        req = urllib.request.Request(f"http://127.0.0.1:{PORT}/json/new?about:blank", method="PUT")
        target = json.loads(urllib.request.urlopen(req, timeout=5).read())
        async with websockets.connect(target["webSocketDebuggerUrl"], max_size=8 * 1024 * 1024) as ws:
            cdp = CDP(ws)
            await cdp.send("Page.enable")
            await cdp.send("Runtime.enable")
            await cdp.send("Network.enable")
            # file:// 下 CSS 会被缓存，改完样式后不复测会拿到旧布局
            await cdp.send("Network.setCacheDisabled", {"cacheDisabled": True})
            for f in files:
                url = pathlib.Path(os.path.join(pages_dir, f)).as_uri()
                m = await cdp.measure(url)
                if not m:
                    lines.append(f"[SKIP] {f} 无测量结果")
                    continue
                notes = []
                flt = m.get("filter")
                if flt and flt["itemCount"] >= 3 and flt["rows"] == flt["itemCount"]:
                    notes.append(
                        f"搜索区 {flt['itemCount']} 个字段占 {flt['rows']} 行（逐行堆叠，容器 {flt['containerW']}px，"
                        f"字段宽 {flt['itemWidths']}，.form-inline display={flt.get('display')} "
                        f"wrap={flt.get('flexWrap')}，.form-item display={flt.get('itemDisplay')}）"
                    )
                for t in m.get("tables", []):
                    if t["wrapW"] and t["tableW"] > t["wrapW"] + 1:
                        notes.append(f"表格 {t['id']} 溢出 {t['overflow']}px（表 {t['tableW']} / 容器 {t['wrapW']}）")
                    if t["colSum"] and abs(t["colSum"] - t["tableW"]) > 2:
                        notes.append(f"表格 {t['id']} 列宽和 {t['colSum']} ≠ 表宽 {t['tableW']}")
                if m.get("actionsDisplay") == "flex":
                    notes.append("td.actions 为 display:flex（破坏表格列对齐）")
                for nk in m.get("nakedCards", []):
                    notes.append(f"卡片子元素没有 card-body 包裹：{nk}")
                if notes:
                    problems.append(f)
                    for n in notes:
                        lines.append(f"[问题] {f} —— {n}")
                else:
                    lines.append(f"[正常] {f}")
    finally:
        proc.terminate()

    summary = f"布局检查：{len(files)} 页，其中 {len(problems)} 页有问题"
    print(summary)
    lines.append(summary)
    os.makedirs(os.path.dirname(LOG_PATH), exist_ok=True)
    with io.open(LOG_PATH, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n")
    for line in lines:
        if line.startswith("[问题]"):
            print(line)
    return 0


if __name__ == "__main__":
    raise SystemExit(asyncio.run(main()))
