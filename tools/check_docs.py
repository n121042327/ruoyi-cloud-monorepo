#!/usr/bin/env python3
"""文档一致性核查工具。

用途：在每批任务结束前检查 docs/ 下的文档是否存在、格式是否合法、交叉引用是否断链。
用法：
    python tools/check_docs.py
退出码：0 表示无问题，1 表示发现问题。

依赖：PyYAML
"""

from __future__ import annotations

import glob
import io
import json
import os
import re
import sys

try:
    import yaml
except ImportError:  # pragma: no cover
    print("需要 PyYAML：pip install pyyaml", file=sys.stderr)
    raise SystemExit(2)

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
problems: list[tuple[str, str]] = []


def rep(kind: str, msg: str) -> None:
    problems.append((kind, msg))


def rel(path: str) -> str:
    return os.path.relpath(path, REPO_ROOT).replace("\\", "/")


def read(path: str) -> str:
    with io.open(path, encoding="utf-8") as fh:
        return fh.read()


def load_yaml(path: str):
    with io.open(path, encoding="utf-8") as fh:
        return list(yaml.safe_load_all(fh))


def require(path: str):
    """文件不存在时登记问题并返回 None，避免脚本直接崩溃。"""
    if not os.path.exists(path):
        rep("MISSING", f"必需文件不存在: {rel(path)}")
        return None
    try:
        return load_yaml(path)[0]
    except Exception as exc:  # noqa: BLE001
        rep("YAML", f"{rel(path)}: {exc}")
        return None


def glob_docs(*patterns: str) -> list[str]:
    out: list[str] = []
    for p in patterns:
        out.extend(glob.glob(os.path.join(REPO_ROOT, p), recursive=True))
    return sorted(set(out))


def check_serialization() -> None:
    for path in glob_docs("docs/**/*.yaml", "docs/**/*.yml"):
        try:
            load_yaml(path)
        except Exception as exc:  # noqa: BLE001
            rep("YAML", f"{rel(path)}: {exc}")
    for path in glob_docs("docs/**/*.json") + [
        os.path.join(REPO_ROOT, "package.json"),
        os.path.join(REPO_ROOT, "turbo.json"),
        os.path.join(REPO_ROOT, "apps", "plus-ui", "package.json"),
    ]:
        if not os.path.exists(path):
            rep("JSON", f"文件不存在: {rel(path)}")
            continue
        try:
            json.loads(read(path))
        except Exception as exc:  # noqa: BLE001
            rep("JSON", f"{rel(path)}: {exc}")


def check_document_map() -> None:
    path = os.path.join(REPO_ROOT, "docs", "00-governance", "document-map.yaml")
    dm = require(path)
    if dm is None:
        return
    node_ids = {n["id"] for n in dm.get("nodes", [])}
    if len(node_ids) != len(dm.get("nodes", [])):
        rep("DOCMAP", "nodes 中存在重复 id")
    for edge in dm.get("edges", []):
        if edge["from"] not in node_ids:
            rep("DOCMAP", f"edge.from 未定义: {edge['from']}")
        if edge["to"] not in node_ids:
            rep("DOCMAP", f"edge.to 未定义: {edge['to']}")
    for rule in dm.get("impact_rules", []):
        src = rule.get("when_changed")
        if src and src not in node_ids and src != "any_frozen":
            rep("DOCMAP", f"impact_rules.when_changed 未定义: {src}")
        for target in rule.get("must_recheck", []):
            if target not in node_ids:
                rep("DOCMAP", f"impact_rules.must_recheck 未定义: {target}")


def check_stage_inputs() -> None:
    path = os.path.join(REPO_ROOT, "docs", "00-governance", "stage-inputs.yaml")
    si = require(path)
    if si is None:
        return
    gap_path = os.path.join(REPO_ROOT, "docs", "00-governance", "gap-register.yaml")
    gap_doc = require(gap_path)
    if gap_doc is None:
        return
    gaps = {g["id"]: g for g in gap_doc["gaps"]}
    stage_ids = {s["id"] for s in si.get("stages", [])}
    for stage in si.get("stages", []):
        for gid in stage.get("blocking_gaps", []) or []:
            if gid not in gaps:
                rep("STAGE", f"{stage['id']} 引用了不存在的缺项 {gid}")
            elif gaps[gid]["status"] in ("answered", "closed", "waived"):
                rep("STAGE", f"{stage['id']} 仍把已结项 {gid}（{gaps[gid]['status']}）列为阻塞")
    # batch 中引用的 stage 是否存在
    for stage in si.get("stages", []):
        for batch in stage.get("batch_plan", []) or []:
            if not str(batch.get("batch", "")).startswith(stage["id"].replace("stage", "")):
                rep("STAGE", f"{stage['id']} 的批次编号不匹配: {batch.get('batch')}")
    return stage_ids


def check_gaps() -> None:
    path = os.path.join(REPO_ROOT, "docs", "00-governance", "gap-register.yaml")
    gr = require(path)
    if gr is None:
        return
    allowed = set(gr.get("status_values", []))
    seen = set()
    for gap in gr.get("gaps", []):
        gid = gap["id"]
        if gid in seen:
            rep("GAP", f"重复的缺项 id: {gid}")
        seen.add(gid)
        if gap["status"] not in allowed:
            rep("GAP", f"{gid} 状态非法: {gap['status']}")
        if gap["status"] in ("answered", "closed", "waived") and "resolution" not in gap:
            rep("GAP", f"{gid} 已结但缺少 resolution")
        if gap["status"] == "open" and not gap.get("owner"):
            rep("GAP", f"{gid} 未指派 owner")
        for sid in gap.get("blocks_stages", []) or []:
            if not re.fullmatch(r"stage\d", str(sid)):
                rep("GAP", f"{gid} 的 blocks_stages 含非法值: {sid}")


def check_permission_matrix() -> None:
    path = os.path.join(REPO_ROOT, "docs", "10-prd", "05-permission-matrix.yaml")
    pm = require(path)
    if pm is None:
        return
    roles = {r["code"] for r in pm["roles"]}
    resources = {r["code"] for r in pm["resources"]}
    actions = set(pm["actions"])
    if len(roles) != len(pm["roles"]):
        rep("PM", "roles 存在重复 code")
    if len(resources) != len(pm["resources"]):
        rep("PM", "resources 存在重复 code")
    for row in pm["matrix"]:
        if row["role"] not in roles:
            rep("PM", f"matrix.role 未定义: {row['role']}")
        for entry in row.get("entries", []):
            if entry["resource"] not in resources:
                rep("PM", f"{row['role']} 引用未定义资源: {entry['resource']}")
            for act in entry.get("actions", []):
                if act not in actions:
                    rep("PM", f"{entry['resource']} 引用未定义操作: {act}")
        for rule in row.get("field_rules", []):
            if rule["resource"] not in resources:
                rep("PM", f"field_rules 引用未定义资源: {rule['resource']}")
    for field in pm.get("sensitive_fields", []):
        root = field["field"].split(".")[0]
        if not any(r.startswith(root + ".") for r in resources):
            rep("PM", f"sensitive_fields 指向未知资源: {field['field']}")


def check_field_dictionary() -> None:
    path = os.path.join(REPO_ROOT, "docs", "10-prd", "06-field-dictionary.yaml")
    fd = require(path)
    if fd is None:
        return
    enums = {e["name"] for e in fd["enums"]}
    if len(enums) != len(fd["enums"]):
        rep("FD", "enums 存在重复 name")
    for enum in fd["enums"]:
        codes = [e["code"] for e in enum["entries"]]
        if len(codes) != len(set(codes)):
            rep("FD", f"枚举 {enum['name']} 存在重复 code")
    field_names = [f["name"] for f in fd["fields"]]
    if len(field_names) != len(set(field_names)):
        rep("FD", "fields 存在重复 name")
    for field in fd["fields"]:
        if "enum_ref" in field and field["enum_ref"] not in enums:
            rep("FD", f"字段 {field['name']} 的 enum_ref 未定义: {field['enum_ref']}")
        if "br" in field:
            pass


def check_business_rules() -> set[str]:
    path = os.path.join(REPO_ROOT, "docs", "10-prd", "04-business-rules.md")
    if not os.path.exists(path):
        rep("MISSING", f"必需文件不存在: {rel(path)}")
        return set()
    body = read(path)
    # 只有表格首列才算"定义"，正文中的出现属于引用
    definitions = re.findall(r"^\|\s*(BR-[A-Z]+-\d{3})\s*\|", body, flags=re.MULTILINE)
    duplicates = {r for r in definitions if definitions.count(r) > 1}
    if duplicates:
        rep("BR", f"编号重复定义: {sorted(duplicates)}")
    by_domain: dict[str, list[int]] = {}
    for rule in sorted(set(definitions)):
        domain, num = rule.rsplit("-", 1)
        by_domain.setdefault(domain, []).append(int(num))
    for domain, nums in sorted(by_domain.items()):
        nums.sort()
        if nums != list(range(1, len(nums) + 1)):
            rep("BR", f"{domain} 编号不连续: {nums}")
    return set(definitions)


def check_cross_references(rule_ids: set[str]) -> None:
    for path in glob_docs("docs/**/*.md", "docs/**/*.yaml", "docs/**/*.yml"):
        if os.path.basename(path) == "04-business-rules.md":
            continue
        body = read(path)
        for ref in set(re.findall(r"BR-[A-Z]+-\d{3}", body)):
            if ref not in rule_ids:
                rep("BR-REF", f"{rel(path)} 引用了不存在的规则 {ref}")


def check_declared_files() -> None:
    """核对 file-catalog.md 中标记为 review 的路径是否真实存在。"""
    path = os.path.join(REPO_ROOT, "docs", "00-governance", "file-catalog.md")
    body = read(path)
    for line in body.splitlines():
        if "`review`" not in line:
            continue
        for token in re.findall(r"`([^`]+)`", line):
            if token.endswith("`review`") or token == "review":
                continue
            if not re.search(r"[./]", token):
                continue
            if re.search(r"\s", token):
                continue
            if "*" in token or token.startswith("prototypes/") and not os.path.exists(
                os.path.join(REPO_ROOT, token)
            ):
                continue
            target = os.path.join(REPO_ROOT, token)
            if not os.path.exists(target):
                rep("CATALOG", f"file-catalog 标为 review 但文件不存在: {token}")


def main() -> int:
    check_serialization()
    check_document_map()
    check_stage_inputs()
    check_gaps()
    check_permission_matrix()
    check_field_dictionary()
    rule_ids = check_business_rules()
    check_cross_references(rule_ids)
    check_declared_files()

    print("=== 文档一致性核查 ===")
    if not problems:
        print("通过：未发现问题")
        return 0
    for kind, msg in problems:
        print(f"[{kind}] {msg}")
    print(f"\n共 {len(problems)} 项问题")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
