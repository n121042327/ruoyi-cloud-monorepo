#!/usr/bin/env python3
"""生成需求追踪矩阵骨架（GAP-063）。

矩阵的权威来源是各模块 PRD 与验收用例文件，本脚本只做机械连接，不判断业务：

  需求行          docs/10-prd/modules/<模块>/PRD.md        （| `REQ-X-n` | 标题 | 业务规则 | 权限 |）
  验收用例        docs/10-prd/modules/<模块>/acceptance.md  （| `AC-X-n` | `REQ-X-n` | 步骤 | 期望 | 证据 |）
  模块接口        docs/30-architecture/06-api-catalog.md    （第 5.N 节标题里的模块代码）
  原型页面        prototypes/functional/v1/navigation.yaml + prototypes/high-fidelity/v1/page-manifest.yaml
  前端计划路径    docs/40-detailed-design/frontend-page-tree.yaml

脚本**保留** traceability.yaml 里已有人工判断的字段（api_operation_ids / tables / tests / status / notes），
只补齐机械可得的字段；需求到接口与表的逐条对应关系需要人工核对，脚本不猜（GAP-081）。

用法：python tools/gen_traceability.py
"""

from __future__ import annotations

import io
import os
import re

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PRD_DIR = os.path.join(REPO_ROOT, "docs", "10-prd", "modules")
OUT = os.path.join(REPO_ROOT, "docs", "00-governance", "traceability.yaml")
CATALOG = os.path.join(REPO_ROOT, "docs", "30-architecture", "06-api-catalog.md")
NAV = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "navigation.yaml")
MANIFEST = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "page-manifest.yaml")
TREE = os.path.join(REPO_ROOT, "docs", "40-detailed-design", "frontend-page-tree.yaml")
COMPONENT_MAPPING = "prototypes/high-fidelity/v2/component-mapping.yaml"
LINKS = os.path.join(REPO_ROOT, "docs", "00-governance", "requirement-links.yaml")

REQ_ROW_RE = re.compile(r"^\|\s*`(REQ-[A-Z]+-\d+)`\s*\|(.*)$", re.M)
AC_ROW_RE = re.compile(r"^\|\s*`(AC-[A-Z]+-[0-9]+)`\s*\|\s*`(REQ-[A-Z]+-\d+)`\s*\|", re.M)
CATALOG_SECTION_RE = re.compile(r"^## 5\.\d+\s+(.+?)（`([\w-]+)`）", re.M)
CATALOG_ROW_RE = re.compile(r"^\|\s*\d+\s*\|\s*`([A-Za-z][A-Za-z0-9]+)`\s*\|", re.M)


def load_yaml(path):
    with io.open(path, encoding="utf-8") as fh:
        return yaml.safe_load(fh)


def cell(value: str) -> str:
    return value.strip().strip("`").strip()


def parse_requirements():
    """返回 {REQ: {title, business_rules, permission, module, source, acceptance:[...]}}。"""
    out = {}
    ac_by_req = {}
    for module in sorted(os.listdir(PRD_DIR)):
        prd = os.path.join(PRD_DIR, module, "PRD.md")
        if not os.path.isfile(prd):
            continue
        text = io.open(prd, encoding="utf-8").read()
        for m in REQ_ROW_RE.finditer(text):
            req, rest = m.group(1), m.group(2)
            if req in out:
                continue
            cols = [cell(c) for c in rest.split("|")]
            if len(cols) < 2 or not cols[0]:
                continue
            line = text[:m.start()].count("\n") + 1
            out[req] = {
                "title": cols[0],
                "business_rules": [c for c in [cols[1]] if c and c != "—"],
                "permission": cols[2] if len(cols) > 2 else "—",
                "module": module,
                "source": "docs/10-prd/modules/%s/PRD.md:%d" % (module, line),
                "acceptance": [],
            }
        acc = os.path.join(PRD_DIR, module, "acceptance.md")
        if os.path.isfile(acc):
            for m in AC_ROW_RE.finditer(io.open(acc, encoding="utf-8").read()):
                ac_by_req.setdefault(m.group(2), []).append(m.group(1))
    for req, acs in ac_by_req.items():
        if req in out:
            out[req]["acceptance"] = sorted(set(acs))
    return out


def parse_module_ops():
    """返回 {模块: [operationId, ...]}。"""
    text = io.open(CATALOG, encoding="utf-8").read()
    out = {}
    marks = list(CATALOG_SECTION_RE.finditer(text))
    for i, m in enumerate(marks):
        end = marks[i + 1].start() if i + 1 < len(marks) else len(text)
        out[m.group(2)] = CATALOG_ROW_RE.findall(text[m.end():end])
    return out


def parse_pages():
    """返回 {模块: [页面信息]}；prototype_* 指向当前版本（有 v2 用 v2），另附冻结 v1 路径。"""
    nav = load_yaml(NAV)
    manifest = load_yaml(MANIFEST)
    file_of = {}
    for entry in manifest.get("pages", []):
        for pid in entry.get("page_ids") or []:
            file_of.setdefault(pid, entry["file"])
    out = {}
    for page in nav.get("pages", []):
        pid, module = page.get("id"), page.get("module")
        if not pid or not module:
            continue
        fname = file_of.get(pid)
        if not fname:
            continue
        out.setdefault(module, []).append({
            "page_id": pid,
            "name": page.get("name"),
            "carrier": page.get("type"),
            "frozen_functional": "prototypes/functional/v1/pages/%s" % fname,
            "frozen_hifi": "prototypes/high-fidelity/v1/pages/%s" % fname,
            "current_functional": "prototypes/functional/v2/pages/%s" % fname
            if os.path.isfile(os.path.join(REPO_ROOT, "prototypes", "functional", "v2", "pages", fname))
            else "prototypes/functional/v1/pages/%s" % fname,
            "current_hifi": "prototypes/high-fidelity/v2/pages/%s" % fname
            if os.path.isfile(os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v2", "pages", fname))
            else "prototypes/high-fidelity/v1/pages/%s" % fname,
        })
    return out


def parse_frontend():
    tree = load_yaml(TREE)
    out = {}
    for view in tree.get("routes", []):
        out[view["page_id"]] = view.get("view_component")
    return out


def parse_links():
    """读回人工维护的「需求 → 接口 / 表」对应表（GAP-081）。"""
    if not os.path.isfile(LINKS):
        return {}
    doc = load_yaml(LINKS) or {}
    out = {}
    for module, block in (doc.get("modules") or {}).items():
        for req, link in (block.get("links") or {}).items():
            out[req] = link or {}
    return out


def main() -> int:
    existing = load_yaml(OUT) or {}
    old = {e["requirement_id"]: e for e in existing.get("traceability") or []}
    reqs = parse_requirements()
    ops = parse_module_ops()
    pages = parse_pages()
    frontend = parse_frontend()
    links = parse_links()

    entries, stats = [], {}
    for req in sorted(reqs, key=lambda r: (reqs[r]["module"], int(r.rsplit("-", 1)[1]))):
        info = reqs[req]
        module = info["module"]
        page = (pages.get(module) or [{}])[0]
        item = {
            "requirement_id": req,
            "title": info["title"],
            "source": info["source"],
            "module": module,
            "business_rules": info["business_rules"],
            "permission": info["permission"],
            "acceptance_cases": info["acceptance"],
            "acceptance_source": "docs/10-prd/modules/%s/acceptance.md" % module,
            "prototype_functional": page.get("current_functional"),
            "prototype_hifi": page.get("current_hifi"),
            "prototype_frozen_v1": page.get("frozen_functional"),
            "component_mapping": COMPONENT_MAPPING,
            "frontend": {
                "planned_page": frontend.get(page.get("page_id")),
                "plan_source": "docs/40-detailed-design/frontend-page-tree.yaml",
            },
            "api_operation_ids": [],
            "tables": [],
            "tests": {"unit": [], "integration": [], "permission": [], "prototype": []},
            "status": "未开始",
            "notes": ["需求到接口与表的逐条对应关系待人工核对（GAP-081）；生产代码与测试尚未产出。"],
        }
        if req in old:  # 保留人工判断过的字段
            keep = old[req]
            for key in ("api_operation_ids", "tables", "tests", "status", "notes", "data_scope"):
                if keep.get(key):
                    item[key] = keep[key]
            for key in ("component_mapping", "prototype_functional", "prototype_hifi", "prototype_frozen_v1"):
                if keep.get(key):
                    item[key] = keep[key]
        if req in links:  # 人工维护的对应表优先于历史值
            link = links[req]
            item["api_operation_ids"] = list(link.get("api") or [])
            item["tables"] = list(link.get("tables") or [])
            item["status"] = "部分覆盖"
            note = link.get("note")
            item["notes"] = [note] if note else []
            item["link_source"] = os.path.relpath(LINKS, REPO_ROOT).replace("\\", "/")
        entries.append(item)
        s = stats.setdefault(module, {"requirements": 0, "with_api": 0, "with_tables": 0, "with_acceptance": 0})
        s["requirements"] += 1
        s["with_api"] += 1 if item["api_operation_ids"] else 0
        s["with_tables"] += 1 if item["tables"] else 0
        s["with_acceptance"] += 1 if item["acceptance_cases"] else 0

    module_index = [{
        "module": m,
        "operations": ops.get(m, []),
        "requirements": stats[m]["requirements"],
        "note": "模块级接口索引：满足 CHK-ORPHAN-API 的粗粒度追溯；逐条需求到接口的对应关系见 GAP-081。",
    } for m in sorted(stats)]

    doc = {
        "schema_version": "1.1",
        "generated_by": "tools/gen_traceability.py",
        "purpose": existing.get("purpose"),
        "status_values": existing.get("status_values"),
        "coverage_summary": {
            "requirements": len(entries),
            "by_module": stats,
            "linked_to_api": sum(1 for e in entries if e["api_operation_ids"]),
            "linked_to_tables": sum(1 for e in entries if e["tables"]),
            "with_acceptance_cases": sum(1 for e in entries if e["acceptance_cases"]),
            "note": "本文件由脚本生成骨架并保留人工判断字段；api_operation_ids / tables 为空表示尚未逐条核对，不代表没有对应接口。",
        },
        "module_api_index": module_index,
        "traceability": entries,
        "coverage_checks": existing.get("coverage_checks"),
        "template_note": existing.get("template_note"),
        "template": existing.get("template"),
    }
    with io.open(OUT, "w", encoding="utf-8", newline="\n") as fh:
        yaml.safe_dump(doc, fh, allow_unicode=True, sort_keys=False, width=200)
    print("已生成 %s：%d 条需求 / %d 个模块 / 已有接口链 %d 条 / 有验收用例 %d 条"
          % (os.path.relpath(OUT, REPO_ROOT), len(entries), len(stats),
             doc["coverage_summary"]["linked_to_api"], doc["coverage_summary"]["with_acceptance_cases"]))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
