"""阶段 6 路由覆盖核对：45 条路由逐条对应到生产前端的 view。

核对口径（两层）：

1. 页面树 `docs/40-detailed-design/frontend-page-tree.yaml` 的 `routes`（共 45 条）逐条取
   `route` 与 `view_component`，检查 `apps/plus-ui/src/<view_component>` 是否存在。

2. 静态路由例外（GAP-087）：登录页与 401 / 403 / 404 / 500 是框架静态路由，页面树的
   `views/edu/common/*` 是生成器按 `conventions.views_root` 推导的投影，真实组件在
   `views/login.vue` 与 `views/error/*.vue`。这四条按例外表核对，并要求在 `router/index.ts`
   里能找到对应的静态路由登记。

退出码 0 表示 45 条路由全部有对应 view；非 0 时打印缺失项。
"""

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PAGE_TREE = 'docs/40-detailed-design/frontend-page-tree.yaml'
ROUTER = 'apps/plus-ui/src/router/index.ts'

# 静态路由例外表：page_id -> (实际组件相对 apps/plus-ui/src 的路径, 期望在 router 里出现的片段)
STATIC_EXCEPTIONS = {
    'PAGE-LOGIN': ('views/login.vue', "path: '/login'"),
    'PAGE-403': ('views/error/403.vue', "path: '/403'"),
    # 404 在框架里是通配路由 `/:pathMatch(.*)*`，没有 `path: '/404'`，按组件引用核对
    'PAGE-404': ('views/error/404.vue', '@/views/error/404.vue'),
    'PAGE-500': ('views/error/500.vue', "path: '/500'"),
}


def read(path: str) -> str:
    return io.open(os.path.join(REPO_ROOT, path), encoding='utf-8').read()


def parse_routes() -> list:
    lines = read(PAGE_TREE).splitlines()
    rows = []
    current = None
    for line in lines:
        m = re.match(r'- page_id: (\S+)', line)
        if m:
            current = {'page_id': m.group(1)}
            rows.append(current)
            continue
        if current is None:
            continue
        m2 = re.match(r'\s+route: (\S+)', line)
        if m2:
            current['route'] = m2.group(1)
        m3 = re.match(r'\s+view_component: (\S+)', line)
        if m3:
            current['view_component'] = m3.group(1)
    return [row for row in rows if 'route' in row]


def main() -> int:
    routes = parse_routes()
    router_text = read(ROUTER)
    problems = []
    statics = 0

    for row in routes:
        page_id = row['page_id']
        route = row['route']
        if page_id in STATIC_EXCEPTIONS:
            view, router_marker = STATIC_EXCEPTIONS[page_id]
            statics += 1
        else:
            view = row.get('view_component')
            router_marker = None
        if not view:
            problems.append('%s（%s）页面树没有 view_component' % (page_id, route))
            continue
        absolute = os.path.join(REPO_ROOT, 'apps/plus-ui/src', view)
        if not os.path.isfile(absolute):
            problems.append('%s（%s）缺少组件文件：apps/plus-ui/src/%s' % (page_id, route, view))
        if router_marker and router_marker not in router_text:
            problems.append('%s（%s）router/index.ts 里找不到静态路由登记：%s' % (page_id, route, router_marker))
        print('  %-22s %-28s → apps/plus-ui/src/%s' % (page_id, route, view))

    print('')
    print('路由总数：%d；静态路由例外：%d；缺失项：%d' % (len(routes), statics, len(problems)))
    if problems:
        for item in problems:
            print('问题：' + item)
        return 1
    print('通过：45 条路由在生产前端都有对应 view')
    return 0


if __name__ == '__main__':
    sys.exit(main())
