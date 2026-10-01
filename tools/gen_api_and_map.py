#!/usr/bin/env python3
"""生成阶段 5 的接口契约与两张映射表（全部来自已冻结的上游产物，避免手工誊抄走样）。

输入：
  docs/30-architecture/06-api-catalog.md        （173 个 operationId，由 PRD 第 8 节生成）
  prototypes/functional/v1/pages/*.html          （每个 data-action-id + data-api + data-permission）
  prototypes/functional/v1/navigation.yaml       （页面注册表：路由 / 类型 / 模板 / 批次 / 菜单）
  prototypes/high-fidelity/v1/page-manifest.yaml （页面文件与页面编号的对应）

输出：
  docs/40-detailed-design/api/openapi.yaml          OpenAPI 3.0.3（173 个操作）
  docs/40-detailed-design/page-action-api-map.yaml  页面动作 → 权限 → operationId
  docs/40-detailed-design/frontend-page-tree.yaml   前端页面树（路由 / 组件归属 / 批次 / 文件）

用法：python tools/gen_api_and_map.py
"""

from __future__ import annotations

import io
import json
import os
import re

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CATALOG = os.path.join(REPO_ROOT, "docs", "30-architecture", "06-api-catalog.md")
PRD_DIR = os.path.join(REPO_ROOT, "docs", "10-prd", "modules")
FUNC_PAGES = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "pages")
NAV = os.path.join(REPO_ROOT, "prototypes", "functional", "v1", "navigation.yaml")
MANIFEST = os.path.join(REPO_ROOT, "prototypes", "high-fidelity", "v1", "page-manifest.yaml")
OUT_DIR = os.path.join(REPO_ROOT, "docs", "40-detailed-design")

MODULE_CN = {
    "student": "学生管理", "teacher": "教师管理", "grade": "年级管理", "class": "班级管理",
    "promotion": "升班与学籍异动", "stream": "3+1+2 选科与教学班", "subject": "学科与配置",
    "school": "学校与租户", "term": "学年学期", "import-export": "导入导出与异步任务",
    "audit": "审计与操作日志",
}

ROW_RE = re.compile(
    r"\|\s*\d+\s*\|\s*`([A-Za-z][A-Za-z0-9]+)`\s*\|\s*(GET|POST|PUT|DELETE|PATCH)\s*\|\s*`([^`]+)`\s*\|"
    r"\s*([^|]+)\|\s*`([^`]+)`\s*\|\s*([^|]+)\|\s*([^|]+)\|"
)

# 阶段 2 的 v1 原型是冻结版本，里面仍写着后来被收敛掉的历史 operationId。
# 口径以模块 PRD 第 8 节为准（再经 06-api-catalog.md 落到本文件），因此这里做一次显式别名映射，
# 避免映射表引用一个已经不存在的 operationId。新增别名必须同时写明来源与收敛依据。
API_ALIASES = {
    # CR-029 / A-077：学生调班统一复用班级模块的调班接口（DP-01）
    "transferStudentClass": "transferClass",
    # CR-035 / GAP-066：批量调班同样复用 transferClass（D-067：单条 = 调班，多条 = 批量迁移）
    "batchTransferStudent": "transferClass",
    # CR-036 / GAP-079：v2 已改名对齐 PRD，v1 原型里的历史字符串由别名兜住
    "saveTeacherEduRole": "saveTeacherRole",
    "saveTeacherLeave": "leaveTeacher",
}

# 模块 PRD 里的查询参数小节：标题固定为 `### 8.N \`<operationId>\` 查询参数`，
# 表格列固定为「参数 | 位置 | 类型 | 必填 | 说明」。阶段 4 的接口清单只有一个路径 + 说明，
# 放不下逐字段参数，因此参数以 PRD 为上游源，由本脚本写进 OpenAPI。
QUERY_SECTION_RE = re.compile(r"^#{2,4}\s+[\d.]*\s*`([A-Za-z][A-Za-z0-9]+)`\s*查询参数", re.M)
QUERY_ROW_RE = re.compile(
    r"^\|\s*([A-Za-z][A-Za-z0-9]*)\s*\|\s*(query|path|header)\s*\|\s*([^|]+?)\s*\|"
    r"\s*(是|否)\s*\|\s*([^|]+?)\s*\|\s*$",
    re.M,
)


def parse_query_params():
    """从各模块 PRD 读回查询参数表。返回 {operationId: [参数, ...]}。"""
    out = {}
    if not os.path.isdir(PRD_DIR):
        return out
    for module in sorted(os.listdir(PRD_DIR)):
        prd = os.path.join(PRD_DIR, module, "PRD.md")
        if not os.path.isfile(prd):
            continue
        text = io.open(prd, encoding="utf-8").read()
        for m in QUERY_SECTION_RE.finditer(text):
            seg = text[m.end():]
            nxt = re.search(r"\n#{2,4}\s", seg)
            if nxt:
                seg = seg[:nxt.start()]
            rows = [{
                "name": r.group(1),
                "in": r.group(2),
                "schema": {"type": r.group(3)},
                "required": r.group(4) == "是",
                "description": r.group(5),
            } for r in QUERY_ROW_RE.finditer(seg)]
            if rows:
                out[m.group(1)] = rows
    return out


def parse_catalog():
    """从 06-api-catalog.md 读回 operationId / 方法 / 路径 / 说明 / 权限资源 / 同步异步。"""
    text = io.open(CATALOG, encoding="utf-8").read()
    ops = []
    module = ""
    for line in text.split("\n"):
        m = re.match(r"^## 5\.\d+\s+(.+?)（`([\w-]+)`）", line)
        if m:
            module = m.group(2)
        row = ROW_RE.match(line.strip())
        if row:
            ops.append({
                "module": module,
                "op": row.group(1),
                "method": row.group(2),
                "path": row.group(3).strip(),
                "desc": row.group(4).strip(),
                "resource": row.group(5).strip(),
                "sync": row.group(7).strip(),
            })
    return ops


def path_params(path: str):
    return re.findall(r"\{([A-Za-z0-9_]+)\}", path)


def gen_openapi(ops):
    query_params = parse_query_params()
    tags = []
    seen = set()
    for o in ops:
        if o["module"] not in seen:
            seen.add(o["module"])
            tags.append({"name": o["module"], "description": MODULE_CN.get(o["module"], o["module"])})

    paths = {}
    for o in ops:
        params = [{
            "name": p, "in": "path", "required": True,
            "schema": {"type": "string"},
            "description": "主键一律序列化为字符串（禁止前端 Number() 转换，见 06-field-dictionary 的 id 说明）",
        } for p in path_params(o["path"])]
        if o["method"] == "GET" and o["op"].startswith("list"):
            params += [
                {"name": "pageNum", "in": "query", "required": False, "schema": {"type": "integer", "minimum": 1, "default": 1}},
                {"name": "pageSize", "in": "query", "required": False, "schema": {"type": "integer", "minimum": 1, "maximum": 200, "default": 20}},
            ]
        params += query_params.get(o["op"], [])
        entry = {
            "tags": [o["module"]],
            "operationId": o["op"],
            "summary": o["desc"],
            "description": (
                f"{o['desc']}\n\n"
                f"- 权限资源：`{o['resource']}`（动作映射规则见 06-api-catalog.md 第 2 节）\n"
                f"- 同步 / 异步：{o['sync']}\n"
                f"- 请求与响应字段：见 `docs/10-prd/06-field-dictionary.yaml` 与该模块 PRD 第 7 节\n"
                f"- 数据范围：接口层按 `DataScopeResolver` 解析后注入（见 30-architecture/09-permission-architecture.md）"
            ),
            "responses": {
                "200": {"description": "成功", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}}},
                "400": {"description": "参数或业务规则校验失败", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}}},
                "401": {"description": "未登录或令牌失效", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}}},
                "403": {"description": "功能权限或数据范围拒绝（DS-DENY-01 / 02）", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}}},
                "500": {"description": "服务异常（返回请求编号）", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}}},
            },
        }
        if params:
            entry["parameters"] = params
        if o["method"] in ("POST", "PUT", "PATCH"):
            entry["requestBody"] = {
                "required": True,
                "content": {"application/json": {"schema": {"$ref": "#/components/schemas/" + o["module"].replace("-", "_") + "_Request"}}},
            }
        if o["sync"] == "异步":
            entry["responses"]["202"] = {
                "description": "已入队（返回 task_no，进度与结果在异步任务中心查询）",
                "content": {"application/json": {"schema": {"$ref": "#/components/schemas/Result"}}},
            }
        paths.setdefault(o["path"], {})[o["method"].lower()] = entry

    schemas = {
        "Result": {
            "type": "object",
            "description": "统一响应信封。错误码见 api/error-codes.yaml；requestId 用于排障（原型里的请求编号就是这个字段）",
            "properties": {
                "code": {"type": "integer", "example": 200},
                "msg": {"type": "string"},
                "data": {},
                "requestId": {"type": "string", "description": "请求编号，异常时返回给前端展示"},
            },
            "required": ["code", "msg"],
        },
        "PageResult": {
            "type": "object",
            "description": "分页结果；列表类接口的 data 结构",
            "properties": {
                "total": {"type": "integer"},
                "rows": {"type": "array", "items": {}},
                "pageNum": {"type": "integer"},
                "pageSize": {"type": "integer"},
            },
            "required": ["total", "rows"],
        },
    }
    for m in sorted({o["module"] for o in ops}):
        schemas[m.replace("-", "_") + "_Request"] = {
            "type": "object",
            "description": (
                f"{MODULE_CN.get(m, m)}模块的请求体。字段以 `docs/10-prd/06-field-dictionary.yaml` 为准；"
                "阶段 6 生成 DTO 时逐字段落实（字段名、类型、必填、枚举）"
            ),
            "additionalProperties": True,
            "x-field-dictionary": "docs/10-prd/06-field-dictionary.yaml",
            "x-module-prd": f"docs/10-prd/modules/{m}/PRD.md",
        }

    doc = {
        "openapi": "3.0.3",
        "info": {
            "title": "K12 教育 ToB 平台 · 教育域接口",
            "version": "1.0.0",
            "description": (
                "由 `tools/gen_api_and_map.py` 从 `docs/30-architecture/06-api-catalog.md` 生成，"
                "接口行不可手工编辑；新增接口先改模块 PRD 第 8 节。\n\n"
                "- 网关前缀：`/api`（servers 已写入）\n"
                "- 鉴权：Sa-Token Bearer；功能权限见 `docs/10-prd/05-permission-matrix.yaml`\n"
                "- 数据范围：见 `docs/30-architecture/09-permission-architecture.md`\n"
                "- 主键：统一序列化为字符串\n"
            ),
        },
        "servers": [{"url": "/api", "description": "网关前缀"}],
        "tags": tags,
        "security": [{"bearerAuth": []}],
        "paths": paths,
        "components": {
            "securitySchemes": {
                "bearerAuth": {"type": "http", "scheme": "bearer", "bearerFormat": "Sa-Token"}
            },
            "schemas": schemas,
        },
    }
    return doc


def gen_page_action_map():
    pages = []
    for name in sorted(os.listdir(FUNC_PAGES)):
        if not name.endswith(".html"):
            continue
        body = io.open(os.path.join(FUNC_PAGES, name), encoding="utf-8").read()
        page_ids = sorted(set(re.findall(r'data-page="([A-Z][A-Z0-9-]+)"', body)))
        actions = []
        for m in re.finditer(r"<[^>]*data-action-id=\"(ACT-[A-Z0-9-]+)\"[^>]*>", body):
            tag = m.group(0)
            def attr(key):
                mm = re.search(key + r'="([^"]*)"', tag)
                return mm.group(1) if mm else None
            actions.append({
                "action_id": m.group(1),
                "element": re.sub(r"<[^>]+>", "", tag.split(">")[0])[-1:] or "",
                "permission": attr("data-permission") or "-",
                "api": API_ALIASES.get(attr("data-api"), attr("data-api")) or "-",
                "nav": attr("data-nav") or "-",
                "overlay": attr("data-overlay") or "-",
            })
        # 元素文字用更稳的方式再扫一遍（上面的 element 只取标签尾部没有意义）
        for a in actions:
            mm = re.search(r'data-action-id="' + a["action_id"] + r'"[^>]*>([^<]{0,40})', body)
            a["element"] = (mm.group(1).strip() if mm and mm.group(1).strip() else "-")
        pages.append({"file": name, "page_ids": page_ids, "actions": actions})
    return pages


def gen_frontend_page_tree(ops):
    nav = yaml.safe_load(io.open(NAV, encoding="utf-8"))
    manifest = yaml.safe_load(io.open(MANIFEST, encoding="utf-8"))
    file_of = {}
    for entry in manifest.get("pages", []):
        for pid in entry.get("page_ids") or []:
            file_of.setdefault(pid, entry["file"])
    api_of_action = {}
    for p in gen_page_action_map():
        for a in p["actions"]:
            if a["api"] and a["api"] != "-":
                api_of_action.setdefault(a["action_id"], a["api"])

    views = []
    for page in nav.get("pages", []):
        pid = page.get("id")
        module = page.get("module")
        pt = page.get("type")
        route = page.get("route")
        if pt in ("page", "wizard", "standalone") and route:
            name = pid.replace("PAGE-", "").lower().replace("-", "_")
            views.append({
                "page_id": pid,
                "name_cn": page.get("name"),
                "module": module,
                "carrier": pt,
                "route": route,
                "prototype_file": file_of.get(pid, "—"),
                "high_fidelity_file": file_of.get(pid, "—"),
                "view_component": f"views/edu/{module}/{name}/index.vue",
                "composables": f"views/edu/{module}/{name}/composables/*.ts",
                "api_layer": f"api/edu/{module}/index.ts",
                "batch": page.get("batch"),
            })
    fragments = [{
        "page_id": p.get("id"),
        "name_cn": p.get("name"),
        "module": p.get("module"),
        "carrier": p.get("type"),
        "size": p.get("size"),
        "parent": p.get("parent"),
        "batch": p.get("batch"),
    } for p in nav.get("pages", []) if p.get("type") in ("dialog", "detail", "block")]

    return {
        "schema_version": "1.0",
        "generated_by": "tools/gen_api_and_map.py",
        "source": ["prototypes/functional/v1/navigation.yaml", "prototypes/high-fidelity/v1/page-manifest.yaml"],
        "conventions": {
            "views_root": "apps/plus-ui/src/views/edu/<module>/<page>/index.vue",
            "api_root": "apps/plus-ui/src/api/edu/<module>/index.ts",
            "types_root": "apps/plus-ui/src/types/edu/<module>.ts",
            "router": "教育模块走菜单动态路由（后端菜单表），不写死 router/edu.ts；仅登录页与异常页是静态路由",
            "carrier_rule": "表单类浮层 = el-dialog；详情类 = el-drawer；含表格或分页 = 独立路由页（D-059 / CR-015）",
        },
        "totals": {
            "routes": len(views),
            "fragments": len(fragments),
            "operation_ids": len(ops),
        },
        "routes": views,
        "fragments": fragments,
    }


def main() -> int:
    ops = parse_catalog()
    if not ops:
        print("未能从 06-api-catalog.md 解析出接口，先运行 tools/extract_api_catalog.py")
        return 2
    os.makedirs(os.path.join(OUT_DIR, "api"), exist_ok=True)

    doc = gen_openapi(ops)
    with io.open(os.path.join(OUT_DIR, "api", "openapi.yaml"), "w", encoding="utf-8", newline="") as fh:
        yaml.safe_dump(doc, fh, allow_unicode=True, sort_keys=False, width=200)

    tree = gen_frontend_page_tree(ops)
    with io.open(os.path.join(OUT_DIR, "frontend-page-tree.yaml"), "w", encoding="utf-8", newline="") as fh:
        yaml.safe_dump(tree, fh, allow_unicode=True, sort_keys=False, width=200)

    pages = gen_page_action_map()
    total_actions = sum(len(p["actions"]) for p in pages)
    mapped = sum(1 for p in pages for a in p["actions"] if a["api"] not in ("-", None))
    doc_map = {
        "schema_version": "1.0",
        "generated_by": "tools/gen_api_and_map.py",
        "purpose": (
            "页面动作 → 功能权限 → 后端 operationId 的映射。阶段 6 的每个按钮/行内动作都按 action_id 落实："
            "data-api 非 '-' 的必须调用对应 operationId；为 '-' 的是纯前端动作（打开弹窗、切换筛选、跳转）。"
        ),
        "totals": {"pages": len(pages), "actions": total_actions, "mapped_to_api": mapped, "frontend_only": total_actions - mapped},
        "conventions": {
            "action_id": "与 prototypes/functional/v1/page-actions.yaml 登记一致（每个可点元素必有一个）",
            "permission": "对应 docs/10-prd/05-permission-matrix.yaml 的 <resource>:<action>；'-' 表示不校验功能权限",
            "api": "对应 docs/40-detailed-design/api/openapi.yaml 的 operationId；'-' 表示不调接口",
        },
        "pages": pages,
    }
    with io.open(os.path.join(OUT_DIR, "page-action-api-map.yaml"), "w", encoding="utf-8", newline="") as fh:
        yaml.safe_dump(doc_map, fh, allow_unicode=True, sort_keys=False, width=200)

    print(f"OpenAPI：{len(ops)} 个 operationId，{len(doc['paths'])} 条路径")
    print(f"前端页面树：{tree['totals']['routes']} 个路由 + {tree['totals']['fragments']} 个浮层片段")
    print(f"页面动作映射：{len(pages)} 页 / {total_actions} 个动作（其中 {mapped} 个调接口，{total_actions - mapped} 个纯前端动作）")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
