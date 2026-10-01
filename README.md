# K12 教育 ToB 平台（monorepo）

面向学校、基教集团与平台运营方的 K12 教育管理平台，PC 端 B/S 架构。
首轮交付范围：**年级管理、班级管理、教师管理、学生管理**，以及支撑它们的学校、学年学期、学科配置、
教育角色、任教关系、租户接入、升班与学籍异动、3+1+2 选科、教学班、导入导出、异步任务与审计。

治理规则以 [`AGENTS.md`](AGENTS.md) 为准，本文件只做导航与上手说明。

## 流水线与当前进度

```
阶段 0 项目核查与工程约束 → 阶段 1 PRD → 阶段 2 业务原型 → 阶段 3 独立高保真原型
→ 阶段 4 概要设计 → 阶段 5 详细设计及建表 → 阶段 6 生产前端 → 阶段 7 生产后端 → 阶段 8 联调验收
```

- 阶段 0—5 已完成并合并回 `main`，阶段边界有 annotated tag：`stage2-prototype-end`、`stage3-highfidelity-end`、
  `stage4-architecture-end`、`stage5-detailed-design-end`。
- 阶段 2 / 3 的原型仍在按批返工：**`prototypes/**/v1` 是冻结版本，不再改动**；所有调整都落在 `v2`，
  每批 2—3 页，交付后暂停等人工验收。当前分支 `codex/stage2-student-cascade`。
- 阶段 6 及以后尚未开始。阻塞项与待用户拍板事项见 [`docs/00-governance/gap-register.yaml`](docs/00-governance/gap-register.yaml)。

## 目录

| 路径 | 内容 |
|---|---|
| `apps/plus-ui/` | Vue 3 生产前端（Vue 3.5 + TS 5.9 + Vite 7 + Element Plus 2.13） |
| `services/RuoYi-Cloud-Plus/` | Java 后端（JDK 17 + Spring Boot 3.5 + RuoYi-Cloud-Plus 2.6.2；`ruoyi-modules/ruoyi-edu` 待建） |
| `docs/00-governance/` | 工程约束、变更单、缺项登记、决策、文件清单、追踪矩阵 |
| `docs/10-prd/` | 各模块 PRD、字段字典、权限矩阵、数据范围模型 |
| `docs/30-architecture/` | 概要设计：架构、模块划分、接口清单、数据归属、缓存与异步边界 |
| `docs/40-detailed-design/` | 详细设计：OpenAPI、错误码、页面动作映射、表结构、迁移脚本、模块设计 |
| `prototypes/functional/{v1,v2}/` | 业务原型（v1 冻结，v2 现行） |
| `prototypes/high-fidelity/{v1,v2}/` | 高保真原型与组件映射（v1 冻结，v2 现行） |
| `evidence/` | 验收证据：验证器、命令输出、截图、日志 |
| `tools/` | 生成器与检查脚本 |

## 常用命令

前端（在仓库根执行，pnpm workspace + Turbo 编排）：

```bash
pnpm install
pnpm dev                      # 启动 apps/plus-ui
pnpm build                    # = turbo run build:prod
pnpm lint
pnpm typecheck
pnpm --filter @edu/plus-ui preview
```

后端（`services/**` 不在 pnpm workspace 内，走 Maven）：

```bash
cd services/RuoYi-Cloud-Plus
# 根 pom 默认 skipTests=true，必须显式打开才能作为测试证据
mvn -q -DskipTests=false -pl ruoyi-modules/ruoyi-system -am test
```

## 文档与原型自检

```bash
python tools/check_docs.py        # 文档存在性、YAML/JSON 合法性、交叉引用
python tools/check_mermaid.py     # Mermaid subgraph 标题写法（全仓库 *.mmd 与 md 代码块）
python tools/verify_nav_links.py  # 原型导航真实点击（需要 Chrome）
```

原型页面本身就是可点开的 HTML：用 Chrome 打开 `prototypes/functional/v2/pages/*.html`，
地址栏 hash 支持 `#role=academic_director&state=empty&panel=PAGE-STU-STATUS` 这类深链接
（`role` 角色、`state` 页面状态、`panel` 直接打开某个浮层、`sample` 切换样本）。

交互验证器在 `evidence/` 下，用 headless Chrome 跑：

```bash
chrome --headless=new --disable-gpu --allow-file-access-from-files \
       --user-data-dir=<临时目录> --virtual-time-budget=30000 --dump-dom \
       "file:///<仓库路径>/evidence/stage2-prototype-v2/<验证器>.html?layer=functional&version=v2"
```

`layer` 取 `functional` / `hifi`，`version` 取 `v1`（冻结基线）/ `v2`（现行）。
输出里的「合计 N 条，通过 M 条」就是证据，日志汇总在各目录的 `*.log` 与 `README.md`。

## 文档生成器（改上游后重跑）

```bash
python tools/extract_api_catalog.py   # 各模块 PRD 第 8 节 → docs/30-architecture/06-api-catalog.md
python tools/gen_api_and_map.py       # 接口清单 → openapi.yaml / page-action-api-map.yaml / frontend-page-tree.yaml
python tools/gen_stage5_docs.py       # 接口清单 → 各模块详细设计、错误码、时序图、状态机、领域模型
python tools/make_diagrams_doc.py     # 所有 *.mmd → docs/diagrams.md（可直接预览）
```

这些文件都是生成物，不要手改；先改 PRD 或原型，再重跑对应脚本。

## 协作约定（摘要）

- 每批只做 2—3 页或 1 个服务模块，交付后暂停等人工验收；样板先做一件，通过后再批量复制。
- 冻结的 v1 不就地覆盖；需要回头改上游时先说明影响，再出 **新版本** 与变更单（`docs/00-governance/change-requests/`）。
- 发现「前置文件缺失、仓库里没有依据的业务规则、两份上游文档矛盾、技术选型冲突」四类情况，
  按「记录 → 说明影响 → 建议 → 等用户确认 → 补全验证」走 `gap-register.yaml`，不自行补造。
- 功能状态只有 `已设计 / 已实现 / 已启用 / 已验证`；检查结果只有 `通过 / 失败 / 未执行 / 不适用 / 已失效`。
  「编译通过」不等于「测试通过」，任何通过结论都要附实际命令输出。
- 不自动 commit、不自动 push；`main` 是唯一长期分支，阶段用短命分支 `codex/stage<N>-<name>`，完成后 `--no-ff` 合并。
- 所有文件 UTF-8 无 BOM、LF 换行；文档与注释用中文，代码标识符与路径用英文。
