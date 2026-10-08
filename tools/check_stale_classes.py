#!/usr/bin/env python3
"""排查「幽灵 class」：target/classes 里存在、但既没有对应源码、也不是生成物的 class。

用途（诊断工具，不作为阶段门禁）：
    编译产物残留（尤其 IDE 与 Maven 共用 target/classes、源码删过之后）会被打进 jar，
    运行时被 Spring 组件扫描到，报成
    `I/O failure while processing configuration class [...]` 或 `... .class cannot be opened`。
    这类问题 `mvn clean` 即可解决，本脚本用来先把它找出来。

判据：对每个 `**/target/classes/**/*.class`（排除内部类 `$` 与 module-info），
    依次看 `<module>/src/main/java/<同一相对路径>.java` 与
    `<module>/target/generated-sources/annotations/<同一相对路径>.java` 是否存在；
    两处都没有才算「幽灵 class」。

用法：
    python tools/check_stale_classes.py            # 扫描 services/RuoYi-Cloud-Plus
    python tools/check_stale_classes.py --root .   # 指定其它根目录

退出码：0 = 未发现；1 = 发现幽灵 class（或扫描路径不存在）。
"""

from __future__ import annotations

import argparse
import os
import sys


def scan(root: str) -> list[tuple[str, str, str]]:
    """返回 [(模块相对路径, class 相对路径, class 全路径)]。"""
    stale: list[tuple[str, str, str]] = []
    for dirpath, dirnames, _ in os.walk(root):
        if os.path.basename(dirpath) != "classes" or os.path.basename(os.path.dirname(dirpath)) != "target":
            continue
        classes_dir = dirpath
        module_dir = os.path.dirname(os.path.dirname(classes_dir))
        src_root = os.path.join(module_dir, "src", "main", "java")
        gen_root = os.path.join(module_dir, "target", "generated-sources", "annotations")
        if not os.path.isdir(src_root):
            continue
        for sub_dir, _, files in os.walk(classes_dir):
            for name in files:
                if not name.endswith(".class") or "$" in name or name == "module-info.class":
                    continue
                rel_java = os.path.relpath(os.path.join(sub_dir, name), classes_dir)[:-6] + ".java"
                if os.path.exists(os.path.join(src_root, rel_java)):
                    continue
                if os.path.exists(os.path.join(gen_root, rel_java)):
                    continue
                stale.append(
                    (
                        os.path.relpath(module_dir, root),
                        rel_java.replace(os.sep, "/"),
                        os.path.join(sub_dir, name),
                    )
                )
    return stale


def main() -> int:
    parser = argparse.ArgumentParser(description="排查 target/classes 里的幽灵 class")
    default_root = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "services",
        "RuoYi-Cloud-Plus",
    )
    parser.add_argument("--root", default=default_root, help="待扫描的目录（默认 services/RuoYi-Cloud-Plus）")
    args = parser.parse_args()

    if not os.path.isdir(args.root):
        print(f"[STALE] 扫描目录不存在：{args.root}")
        return 1

    stale = scan(args.root)
    print(f"[STALE] 扫描根目录：{args.root}")
    if not stale:
        print("[STALE] 未发现幽灵 class（通过）")
        return 0

    print(f"[STALE] 发现 {len(stale)} 个幽灵 class（源码与生成物里都没有对应 .java）：")
    for module, rel_java, class_path in sorted(stale):
        print(f"  - {module} -> {rel_java}")
        print(f"      {class_path}")
    print("[STALE] 修复：mvn -o -DskipTests -pl <模块> -am clean package（删过类/改过包名后务必 clean）")
    return 1


if __name__ == "__main__":
    sys.exit(main())
