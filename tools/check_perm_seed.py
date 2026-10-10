#!/usr/bin/env python3
"""权限点种子完整性检查（阶段 6/7）。

背景：前端 `v-hasPermi` / `checkPermi` 与后端 `@SaCheckPermission` 用到的权限点，必须在
`script/sql/**` 的菜单种子里存在（`sys_menu.perms`）—— 否则除 super_admin 外任何角色都拿不到该权限，
按钮会永远不显示，而且不报错（CR-148 发现 `org.class:remove` 与 `person.student_guardian:update` 就是这样漏掉的）。

用法：python tools/check_perm_seed.py
退出码：0 通过，1 有缺失。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FRONTEND_EDU = os.path.join(REPO_ROOT, "apps", "plus-ui", "src", "views", "edu")
BACKEND_EDU = os.path.join(REPO_ROOT, "services", "RuoYi-Cloud-Plus", "ruoyi-modules", "ruoyi-edu", "src", "main", "java")
SEED_DIR = os.path.join(REPO_ROOT, "services", "RuoYi-Cloud-Plus", "script", "sql")

PERM_RE = re.compile(r"'((?:person|org|data|audit|system|stream|promotion|enrollment|resource)\.[a-z_]+:[a-z_]+)'")


def collect_frontend() -> set[str]:
    used: set[str] = set()
    for dirpath, _, filenames in os.walk(FRONTEND_EDU):
        for name in filenames:
            if not name.endswith(".vue"):
                continue
            text = io.open(os.path.join(dirpath, name), encoding="utf-8").read()
            for block in re.findall(r"v-hasPermi=\"\[([^\]]+)\]\"", text):
                used.update(re.findall(r"'([^']+)'", block))
            for block in re.findall(r"checkPermi\(\[([^\]]+)\]\)", text):
                used.update(re.findall(r"'([^']+)'", block))
    return used


def collect_backend() -> set[str]:
    used: set[str] = set()
    for dirpath, _, filenames in os.walk(BACKEND_EDU):
        for name in filenames:
            if not name.endswith(".java"):
                continue
            text = io.open(os.path.join(dirpath, name), encoding="utf-8").read()
            used.update(re.findall(r"@SaCheckPermission\(\"([^\"]+)\"\)", text))
    return used


def collect_seed() -> set[str]:
    seed: set[str] = set()
    for dirpath, _, filenames in os.walk(SEED_DIR):
        for name in filenames:
            if not name.endswith(".sql"):
                continue
            text = io.open(os.path.join(dirpath, name), encoding="utf-8").read()
            seed.update(PERM_RE.findall(text))
    return seed


def main() -> int:
    used = collect_frontend() | collect_backend()
    seed = collect_seed()
    missing = sorted(used - seed)
    sys.stdout.buffer.write(("前端/后端权限点 %d 个，种子权限点 %d 个\n" % (len(used), len(seed))).encode("utf-8"))
    if missing:
        for item in missing:
            sys.stdout.buffer.write(("缺失：%s\n" % item).encode("utf-8"))
        sys.stdout.buffer.write(("\n共 %d 个权限点未出现在菜单种子里\n" % len(missing)).encode("utf-8"))
        return 1
    sys.stdout.buffer.write("通过：所有权限点都在菜单种子里\n".encode("utf-8"))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
