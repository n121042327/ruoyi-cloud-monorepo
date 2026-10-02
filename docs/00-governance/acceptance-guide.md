# 人工验收指南（阶段 2 / 3 原型返工）

这份文件回答两件事：**在哪验收**、**怎么验收**。

## 一、验收在哪

验收对象就是原型页面本身——它们是可点开的 HTML，不需要跑服务、不需要装依赖。
两种打开方式任选：

1. **本地文件**：直接用 Chrome 打开 `prototypes/functional/v2/pages/<页面>.html`
   （高保真版把 `functional` 换成 `high-fidelity`）。
2. **本地服务**：如果已经起了静态服务（例如 127.0.0.1:8785 指向仓库根），用
   `http://127.0.0.1:8785/prototypes/functional/v2/pages/<页面>.html#role=<角色>`。

地址栏 hash 支持这些参数，验收时很有用：

| 参数 | 作用 | 例子 |
|---|---|---|
| `role` | 切角色（决定按钮显隐、字段只读、数据范围） | `#role=academic_director` |
| `panel` | 直接打开某个浮层，省去点击 | `&panel=PAGE-STU-STATUS` |
| `school` | 直接切到某校（只有平台运营 / 超级管理员能切） | `&school=202` |
| `state` | 切页面状态：加载中 / 空数据 / 查询失败 / 无权限 | `&state=empty` |
| `sample` | 切详情样本（部分页面） | `&sample=B` |
| `status=all` | 教师列表把「在职状态」切到全部，便于看非在职行 | `&status=all` |

常用角色：`academic_director`（教务主任）、`grade_leader`（年级主任）、`homeroom`（班主任）、
`platform_ops`（平台运营，可切学校）、`super_admin`（超级管理员，看全部入口）。

## 二、验收怎么做

三步：

1. **打开对应页面**（下表给了路径）。
2. **按「要看的点」逐条核对**，每条都写明了期望结果；想看细节就点开该行「浮层」列里的对话框。
3. **回一句话**：某批通过就写「CR-0xx 通过」，有问题就写「CR-0xx：某某不对」。
   我会把结果写进 `docs/00-governance/decisions.md` 与 `gap-register.yaml` 的状态字段；
   **在你回话之前，所有批次都保持「待人工验收」，我不会替你记成已验收。**

不想逐条看也可以：挑你关心的页面看，其余批注明「先不验收 / 暂缓」即可。

## 三、待验收清单（按页面归并）

### 1. 学生列表 `prototypes/functional/v2/pages/student-list.html`

涉及批次：CR-020（筛选级联与追踪样板）、CR-027（表格列序与表单分区）、CR-028（三个浮层分组）、
CR-029（调班对齐）、CR-033（详情抽屉分组）、CR-035（批量调班收敛）。

| 要看的点 | 期望结果 |
|---|---|
| 表格列序 | 学号、姓名、性别 → 入学年份、学段、年级、班级、学籍状态 → 联系电话 → 更新时间；操作列最右 |
| 筛选区顺序 | 学校 → 学年学期 → 年级 → 班级 → 学籍状态 → 关键字 → 性别 → 入学年份 → 证件号后四位 |
| 切学校（`#role=platform_ops&school=202`） | 年级 / 班级下拉只剩该校选项 |
| 点「新增学生」 | 弹窗分步，第 1 步有「基础信息 / 教育信息 / 补充信息」分区标题 |
| 行内「异动」/「调班」 | 弹窗分「异动信息 / 复学·报到安排 / 异动说明」「班级关系（目标班级 → 生效日期）/ 调班说明」 |
| 调班弹窗 | 目标班级与生效日期都必填，两个都空时提交被拦截 |
| 点数据行 | 详情抽屉里「学生信息」卡片分 基础信息 / 教育信息 / 证件信息 / 联系方式 四组 |
| 「批量调班」按钮 | 走班级模块的调班接口（演示提示即可，无独立浮层） |

### 2. 班级列表 `prototypes/functional/v2/pages/class-list.html`

涉及批次：CR-022、CR-025。

| 要看的点 | 期望结果 |
|---|---|
| 筛选区顺序 | 学校 → 校区 → 学年学期 → 年级 → 班级类型 → 班主任 → 关键字 |
| 切学校 | 年级 / 班级选项收窄（参考实现，CR-022 起就是这个形态） |
| 班级表列序 | 校区、年级、班级名称、班级类型、班主任、教室、容量、在读人数、状态 |
| 「新建班级」 | 表单分「教育信息 / 管理信息」两组 |

### 3. 教师列表 `prototypes/functional/v2/pages/teacher-list.html`

涉及批次：CR-023、CR-025、CR-026、CR-030、CR-034。

| 要看的点 | 期望结果 |
|---|---|
| 筛选区分组 | 教育信息（学校 / 任教年级 / 任教班级 / 任教学科 / 教育角色）→ 职业信息（在职状态）→ 检索信息（关键字） |
| 切学校（`#role=platform_ops&school=202`） | 任教年级 / 任教班级只剩该校选项；任教学科不变 |
| 行内「编辑」 | 打开弹窗后，字段来自**该行**教师；样例没提供的字段清空并标注 |
| 非在职行（`&status=all`） | 离职、调离行只剩「撤销离职登记」；在职行保持 编辑 / 角色 / 任教 / 离职 |
| 「撤销离职登记」 | 二次确认弹窗说明「账号自动重新启用、教育角色不自动恢复」 |

### 4. 年级列表 `prototypes/functional/v2/pages/grade-list.html`

涉及批次：CR-023、CR-027、CR-031、CR-034。

| 要看的点 | 期望结果 |
|---|---|
| 筛选区 | 学校 → 学段 → 入学年份 → 年级主任 |
| 切学校（`&school=202`） | 学段只剩初中；入学年份只剩 2026；年级主任只剩「未指定」 |
| 年级表列序 | 学段、入学年份、年级名称、序号、年级主任 → 班级数、在读学生数、状态 |
| 点数据行 | 详情抽屉「年级信息」卡片分 教育信息（学校 → 学段 → 入学年份 → 序号 → 年级名称）/ 管理信息（状态） |
| 行内「指定年级主任」 | 弹窗分「任职信息（学年学期 → 当前任职清单）/ 新增任职（教师 / 主要负责人 / 生效日期）」 |
| 行内「归档」「删除」 | 都分「影响 / 说明」两组，原因必填 |

### 5. 升班任务列表与异动历史

`prototypes/functional/v2/pages/promotion-list.html`、`promotion-history.html`（CR-024）

| 要看的点 | 期望结果 |
|---|---|
| 升班任务筛选区 | 教育信息（学校 → 源学年学期 → 目标学年学期）→ 任务信息（状态 / 创建人 / 任务号） |
| 异动历史 | 学年学期下拉**禁用**并说明「历史学期归属未补齐」；生效日期为单日精确筛选 |

### 6. 文档与架构

| 要看的点 | 期望结果 | 文件 |
|---|---|---|
| 架构分层图能正常渲染 | 不再报 `Lexical error`，五层分组正常显示 | `docs/30-architecture/02-architecture.md` |
| 所有 Mermaid 图 | 用支持 Mermaid 的预览器打开 `docs/diagrams.md` 能看到图 | `docs/diagrams.md` |
| 字段布局约束 | 规则本身是否认可 | `docs/00-governance/page-field-layout.md` |
| 接口清单与原型一致 | 184 个 operationId、原型 112 处 `data-api` 零孤儿 | `docs/30-architecture/06-api-catalog.md` |
| 追踪矩阵与组件映射 | 626 条需求记录（接口链 580 / 表链 537，178 个 operationId 全部可追溯到需求）+ 45 页 1361 条元素映射 | `docs/00-governance/traceability.yaml`、`prototypes/high-fidelity/v2/component-mapping-all-pages.yaml` |
| 缺口已关账 | 学生激活码 4 个接口、教师启用账号 / 复制任教关系 2 个接口都写进 PRD 第 8 节（CR-043） | `docs/10-prd/modules/student/PRD.md`、`docs/10-prd/modules/teacher/PRD.md` |

## 四、想自己复跑验证怎么做

不需要——每条验收点我都在 `evidence/` 下留了可复跑的验证器和日志。真要复跑：

```bash
python tools/check_docs.py
python tools/check_mermaid.py
python tools/check_api_contract.py

chrome --headless=new --disable-gpu --allow-file-access-from-files \
       --user-data-dir=<临时目录> --virtual-time-budget=30000 --dump-dom \
       "file:///<仓库路径>/evidence/stage2-prototype-v2/<验证器>.html?layer=functional&version=v2"
```

各批次的通过与失败计数写在 `evidence/stage2-prototype-v2/*.log` 里，逐条明细在对应的
`2026-1*_<验证器>-*.html` 结果文件里（打开就能看到每条断言）。**冻结 v1 是基线**：
把 URL 里的 `version=v2` 改成 `v1`，看到的就是返工前的形态。
