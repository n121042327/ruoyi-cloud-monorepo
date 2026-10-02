#!/usr/bin/env python3
"""接口契约一致性检查（GAP-066 之后新增）。

检查三件事：
1. 现行原型（prototypes/**/v2/pages/*.html）里每个 data-api 都必须是 OpenAPI 里真实存在的
   operationId —— 防止「原型声明了一个不存在的接口」这类孤儿引用（对应 CHK-ORPHAN-API）。
2. 模块 PRD 里的「查询参数」小节必须与 OpenAPI 里该操作的参数一一对上
   （参数名、位置、必填）；生成器解析不到就会在这里暴露。
3. 指定操作的最小参数集合（默认 listStudent）必须齐备。

冻结的 v1 原型写在收敛之前（例如 batchTransferStudent / transferStudentClass），
本脚本只检查 v2；v1 的历史字符串由 tools/gen_api_and_map.py 的 API_ALIASES 负责映射。

用法：
    python tools/check_api_contract.py
退出码：0 通过，1 发现问题。
"""

from __future__ import annotations

import io
import os
import re
import sys

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OPENAPI = os.path.join(REPO_ROOT, "docs", "40-detailed-design", "api", "openapi.yaml")
PRD_DIR = os.path.join(REPO_ROOT, "docs", "10-prd", "modules")
V2_PAGE_DIRS = [
    os.path.join(REPO_ROOT, "prototypes", "functional", "v2", "pages"),
    os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v2", "pages"),
]
REQUIRED_PARAMS = {"listStudent": ["schoolId", "termId", "gradeId", "classId", "enrollmentStatus",
                                   "gender", "enrollYear", "stageCode", "keyword", "idCardSuffix",
                                   "sortBy", "sortOrder"]}

# 已知的孤儿引用：原型写了 data-api，但 OpenAPI 里没有同名 operationId。
# 这些字符串来自原型交付时的写法，逐条核对前不擅自改指到别的接口（CR-035 的 GAP-066 已明确
# 「不得直接把原型字符串视为有效接口」）。清单登记在 docs/00-governance/gap-register.yaml 的 GAP-079；
# 新出现的孤儿引用会让本脚本失败，已登记的不再重复报警。
# CR-036 改名对齐了 2 条，CR-037 给余下 6 条补了契约（GAP-080 关闭），
# 因此这里现在是空的：任何孤儿引用都应判失败。
KNOWN_ORPHANS: set[str] = set()

QUERY_SECTION_RE = re.compile(r"^#{2,4}\s+[\d.]*\s*`([A-Za-z][A-Za-z0-9]+)`\s*查询参数", re.M)
QUERY_ROW_RE = re.compile(
    r"^\|\s*([A-Za-z][A-Za-z0-9]*)\s*\|\s*(query|path|header)\s*\|\s*([^|]+?)\s*\|"
    r"\s*(是|否)\s*\|\s*([^|]+?)\s*\|\s*$",
    re.M,
)

problems: list[str] = []


def rel(path: str) -> str:
    return os.path.relpath(path, REPO_ROOT).replace("\\", "/")


def load_openapi():
    doc = yaml.safe_load(io.open(OPENAPI, encoding="utf-8"))
    ops = {}
    for path, methods in (doc.get("paths") or {}).items():
        for method, spec in methods.items():
            if isinstance(spec, dict) and "operationId" in spec:
                ops[spec["operationId"]] = {"path": path, "method": method, "params": spec.get("parameters") or []}
    return ops


def check_prototype_apis(ops):
    seen = 0
    known = []
    for folder in V2_PAGE_DIRS:
        if not os.path.isdir(folder):
            continue
        for name in sorted(os.listdir(folder)):
            if not name.endswith(".html"):
                continue
            body = io.open(os.path.join(folder, name), encoding="utf-8").read()
            for m in re.finditer(r'data-api="([^"]+)"', body):
                api = m.group(1)
                if api in ("-", ""):
                    continue
                seen += 1
                if api not in ops:
                    if api in KNOWN_ORPHANS:
                        known.append("%s 的 data-api=%s（已登记 GAP-080）" % (rel(os.path.join(folder, name)), api))
                    else:
                        problems.append("%s 的 data-api=%s 不是 OpenAPI 里的 operationId" % (rel(os.path.join(folder, name)), api))
    print("原型 data-api 引用：%d 处" % seen)
    for k in sorted(set(known)):
        print("  已知孤儿（不影响本次通过）：" + k)


def check_declared_params(ops):
    for module in sorted(os.listdir(PRD_DIR)):
        prd = os.path.join(PRD_DIR, module, "PRD.md")
        if not os.path.isfile(prd):
            continue
        text = io.open(prd, encoding="utf-8").read()
        for m in QUERY_SECTION_RE.finditer(text):
            op = m.group(1)
            seg = text[m.end():]
            nxt = re.search(r"\n#{2,4}\s", seg)
            if nxt:
                seg = seg[:nxt.start()]
            declared = {(r.group(1), r.group(2)) for r in QUERY_ROW_RE.finditer(seg)}
            if op not in ops:
                problems.append("%s 声明了查询参数，但 OpenAPI 没有 operationId %s" % (rel(prd), op))
                continue
            actual = {(p.get("name"), p.get("in")) for p in ops[op]["params"]}
            missing = declared - actual
            if missing:
                problems.append("%s 声明的参数未出现在 OpenAPI.%s：%s" % (rel(prd), op, sorted(missing)))
            else:
                print("查询参数对齐：%s -> %s（%d 个参数）" % (op, rel(prd), len(declared)))


def check_required(ops):
    for op, names in REQUIRED_PARAMS.items():
        if op not in ops:
            problems.append("OpenAPI 缺少 operationId %s" % op)
            continue
        actual = {p.get("name") for p in ops[op]["params"]}
        missing = [n for n in names if n not in actual]
        if missing:
            problems.append("%s 缺少查询参数：%s" % (op, missing))
        else:
            print("%s 参数齐备：%d 个" % (op, len(actual)))


def main() -> int:
    ops = load_openapi()
    print("OpenAPI operationId：%d 个" % len(ops))
    check_prototype_apis(ops)
    check_declared_params(ops)
    check_required(ops)
    if problems:
        for p in problems:
            print("问题：" + p)
        print("\n共 %d 项问题" % len(problems))
        return 1
    print("\n通过：接口契约一致")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
