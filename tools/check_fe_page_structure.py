#!/usr/bin/env python3
"""生产前端页面结构对照检查（阶段 6）。

把已交付的生产页面与它对应的高保真 / 业务原型逐项对照，防止「写代码时悄悄改了分组或列序」：

  查询区字段顺序与语义分组   ←→  原型 data-role="filter" 段内的 label 顺序
  表格列顺序、列名与分组     ←→  原型 <th data-role="column"> 的 data-field / data-layout-group / 文本
  操作列独立分组并固定右侧   ←→  原型 data-layout-group="操作" 的列 + 生产页 fixed="right"

依据：docs/00-governance/page-field-layout.md（语义分组、组内顺序、表格列序、操作列独立）。
首轮只登记已交付的页面；每交付一页在此加一条 CHECK。

用法：python tools/check_fe_page_structure.py
退出码：0 通过，1 发现问题。
"""

from __future__ import annotations

import io
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 已交付页面的对照清单：原型页面 ←→ 生产页面
CHECKS = [
    {
        "page_id": "PAGE-STU-LIST",
        "name": "学生管理列表",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/index.vue",
        # 原型有、生产页本批不做：写清原因，不算失败
        "deferred_groups": {
            "选择": "批量操作（批量导出 / 批量调班）在阶段 6 后续批次交付，见 CR-044 的边界说明"
        },
    },
    {
        "page_id": "PAGE-CLS-LIST",
        "name": "班级管理列表",
        "prototype": "prototypes/functional/v2/pages/class-list.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-GRD-LIST",
        "name": "年级管理列表",
        "prototype": "prototypes/functional/v2/pages/grade-list.html",
        "vue": "apps/plus-ui/src/views/edu/grade/grd_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-TCH-LIST",
        "name": "教师管理列表",
        "prototype": "prototypes/functional/v2/pages/teacher-list.html",
        "vue": "apps/plus-ui/src/views/edu/teacher/tch_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-SUB-LIST",
        "name": "学科与配置列表",
        "prototype": "prototypes/high-fidelity/v1/pages/subject-list.html",
        "vue": "apps/plus-ui/src/views/edu/subject/sub_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-TERM-LIST",
        "name": "学年学期列表",
        "prototype": "prototypes/functional/v1/pages/term-list.html",
        "vue": "apps/plus-ui/src/views/edu/term/term_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-SCH-LIST",
        "name": "学校管理列表",
        "prototype": "prototypes/functional/v1/pages/school-list.html",
        "vue": "apps/plus-ui/src/views/edu/school/sch_list/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-AUDIT-LOG-LIST",
        "name": "操作日志列表",
        "prototype": "prototypes/functional/v1/pages/audit-log-list.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_log_list/index.vue",
        "deferred_groups": {},
        "filter_note": "审计 PRD 4.4 要求按时间范围 / 操作人 / 对象 / 操作类型筛选，v1 原型未给查询区标记",
    },
    {
        "page_id": "PAGE-AUDIT-OPS-ACCESS",
        "name": "运营访问记录",
        "prototype": "prototypes/functional/v1/pages/audit-ops-access.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_ops_access/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-AUDIT-SENSITIVE-ACCESS",
        "name": "敏感数据访问记录",
        "prototype": "prototypes/functional/v1/pages/audit-sensitive-access.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_sensitive_access/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-AUDIT-SECURITY-EVENT",
        "name": "登录与安全事件",
        "prototype": "prototypes/functional/v1/pages/audit-security-event.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_security_event/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-AUDIT-ARCHIVE",
        "name": "归档管理",
        "prototype": "prototypes/functional/v1/pages/audit-archive.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_archive/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-IMP-TASK-LIST",
        "name": "异步任务列表",
        "prototype": "prototypes/functional/v1/pages/async-task-list.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_task_list/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-IMP-DEADLETTER",
        "name": "死信任务",
        "prototype": "prototypes/functional/v1/pages/dead-letter-task.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_deadletter/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-PRM-HISTORY",
        "name": "异动历史",
        "prototype": "prototypes/functional/v2/pages/promotion-history.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_history/index.vue",
        "deferred_groups": {},
        
        
    },
    {
        "page_id": "PAGE-SCH-CAMPUS",
        "name": "校区管理",
        "prototype": "prototypes/functional/v1/pages/school-campus.html",
        "vue": "apps/plus-ui/src/views/edu/school/sch_campus/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-SCH-INIT",
        "name": "开通初始化向导",
        "prototype": "prototypes/functional/v1/pages/school-init.html",
        "vue": "apps/plus-ui/src/views/edu/school/sch_init/index.vue",
        "steps": [
            ("学校基本信息", ["学校基本信息"]),
            ("学段与年级", ["学段与年级"]),
            ("学年学期", ["学年学期"]),
            ("执行与结果", ["执行与结果"]),
        ],
    },
    {
        "page_id": "PAGE-TERM-TERMS",
        "name": "学期管理",
        "prototype": "prototypes/functional/v1/pages/term-terms.html",
        "vue": "apps/plus-ui/src/views/edu/term/term_terms/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-PRM-LIST",
        "name": "升班任务列表",
        "prototype": "prototypes/functional/v2/pages/promotion-list.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_list/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-PRM-CREATE",
        "name": "新建升班任务",
        "prototype": "prototypes/functional/v1/pages/promotion-create.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_create/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-PRM-PREVIEW",
        "name": "升班预览与调整",
        "prototype": "prototypes/functional/v1/pages/promotion-preview.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_preview/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-PRM-VALIDATE",
        "name": "升班校验",
        "prototype": "prototypes/functional/v1/pages/promotion-validate.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_validate/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-PRM-EXECUTE",
        "name": "执行与进度",
        "prototype": "prototypes/functional/v1/pages/promotion-execute.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_execute/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-PRM-RESULT",
        "name": "执行结果与重试",
        "prototype": "prototypes/functional/v1/pages/promotion-result.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_result/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-CLS-ROSTER-ADD",
        "name": "添加学生",
        "prototype": "prototypes/functional/v1/pages/class-roster-add.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_roster_add/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-CLS-MOVE",
        "name": "批量迁学生",
        "prototype": "prototypes/functional/v1/pages/class-move-students.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_move/index.vue",
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-CLS-ROSTER-IMPORT",
        "name": "编班表导入向导",
        "prototype": "prototypes/functional/v1/pages/class-import-roster.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_roster_import/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-CLS-TEACHING",
        "name": "教学班管理",
        "prototype": "prototypes/functional/v1/pages/teaching-class-list.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_teaching/index.vue",
        "deferred_groups": {},
        
        "filter_note": "v1 原型的查询区由 3 个裸 select（data-role=\"filter\"）组成，没有 <label> 文本，因此查询项无法由原型提取；生产页按 PRD 6.1 补学年学期 / 年级 / 组合 · 学科 / 状态四项",
    },
    {
        "page_id": "PAGE-STR-CONFIG",
        "name": "选科配置",
        "prototype": "prototypes/functional/v1/pages/stream-config.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_config/index.vue",
        "deferred_groups": {},
        "filter_note": "原型是「固定规则卡 + 开放期表单」的单页配置，整页没有查询区与表格；生产页按原型的分组（固定规则 / 开放期与审批）实现，不比对查询项",
    },
    {
        "page_id": "PAGE-STR-STUDENT",
        "name": "学生选科",
        "prototype": "prototypes/functional/v1/pages/stream-selection.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_student/index.vue",
        "deferred_groups": {},
        "filter_note": "原型是「首选科目 + 再选科目 + 当前结果」的单页表单，整页没有查询区与表格；生产页按 BR-STREAM-001 / 002 固定集合实现，不比对查询项",
    },
    {
        "page_id": "PAGE-STR-LIST",
        "name": "选科清单",
        "prototype": "prototypes/functional/v1/pages/stream-list.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_list/index.vue",
        "deferred_groups": {},
        
        "filter_note": "v1 原型整页没有查询区标记，生产页按 PRD 6.1 补学年学期 / 年级 / 首选 / 状态 / 关键词五项",
    },
    {
        "page_id": "PAGE-STR-STAT",
        "name": "组合分布统计",
        "prototype": "prototypes/functional/v1/pages/stream-stat.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_stat/index.vue",
        "deferred_groups": {},
        
        "filter_note": "v1 原型整页没有查询区标记，生产页按当前学年学期上下文取数（getStreamStat），不额外增加查询项",
    },
    {
        "page_id": "PAGE-STR-APPROVE",
        "name": "选科变更审批",
        "prototype": "prototypes/functional/v1/pages/stream-approve.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_approve/index.vue",
        "deferred_groups": {},
        
        "filter_note": "v1 原型的查询区由裸 select / input（data-role=\"filter\"）组成，没有 <label> 文本，查询项无法由原型提取；生产页按 PRD 6.1 补状态 / 年级 / 关键词三项",
    },
    {
        "page_id": "PAGE-STR-GEN-CLASS",
        "name": "按组合生成教学班",
        "prototype": "prototypes/functional/v1/pages/stream-generate-class.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_gen_class/index.vue",
        "deferred_groups": {},
        
        "filter_note": "v1 原型是「选择方式与范围 → 生成预览 → 执行与进度 → 核对结果」的四步向导，整页没有查询区；生产页按原型步骤实现，不比对查询项",
    },
    {
        "page_id": "PAGE-TCH-ASSIGN",
        "name": "教师任教关系",
        "prototype": "prototypes/functional/v1/pages/teacher-assign.html",
        "vue": "apps/plus-ui/src/views/edu/teacher/tch_assign/index.vue",
        "deferred_groups": {},
        "filter_note": "原型的第一个 data-role=\"table\" 落在左侧班级选择面板（div 包裹的内层 table），检查器切片只覆盖到该面板，主表列（学科 / 任教教师 / 班级 / 班级类型 / 周课时 / 状态 / 操作）无法被提取，因此 expected=0；生产页按原型的查询区（视角 / 学年学期 / 关键字）实现，主表列放在 components/AssignmentTable.vue 并在 CR-078 里逐列登记",
    },
    {
        "page_id": "PAGE-TCH-IMPORT",
        "name": "教师导入向导",
        "prototype": "prototypes/functional/v1/pages/teacher-import.html",
        "vue": "apps/plus-ui/src/views/edu/teacher/tch_import/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-IMP-WIZARD",
        "name": "导入向导",
        "prototype": "prototypes/functional/v1/pages/import-wizard.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_wizard/index.vue",
        "deferred_groups": {},
        
    },
    {
        "page_id": "PAGE-403",
        "name": "无权限（403）",
        "prototype": "prototypes/functional/v1/pages/403.html",
        "vue": "apps/plus-ui/src/views/error/403.vue",
        "deferred_groups": {},
        "filter_note": "原型是静态异常页（只有错误码 / 标题 / 说明 / 两个动作），整页没有查询区与表格；生产页按框架静态路由组件实现，组件路径按 GAP-087 裁决落在 views/error/403.vue",
    },
    {
        "page_id": "PAGE-500",
        "name": "服务异常（500）",
        "prototype": "prototypes/functional/v1/pages/500.html",
        "vue": "apps/plus-ui/src/views/error/500.vue",
        "deferred_groups": {},
        "filter_note": "原型是静态异常页（错误码 / 标题 / 说明 / 三个动作 / 请求编号与错误码元信息），整页没有查询区与表格；生产页按框架静态路由组件实现，组件路径按 GAP-087 裁决落在 views/error/500.vue",
    },
    {
        "page_id": "PAGE-CLS-DETAIL",
        "name": "班级详情（花名册）",
        "prototype": "prototypes/high-fidelity/v1/pages/class-detail.html",
        "vue": "apps/plus-ui/src/views/edu/class/cls_detail/index.vue",
        # 原型第 2 / 3 张表（任课教师 / 变更记录）没有 data-role="column" 标记，无法逐列对照；
        # 这两张表已按先例拆成 components/TeachingAssignmentTable.vue 与 ClassChangeLogTable.vue，
        # 待原型出新版本补标记后再各自加一条 CHECKS（见 CR-135 / GAP-112）。
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-IMP-TASK-ROWS",
        "name": "异步任务行明细（抽屉）",
        "prototype": "prototypes/functional/v1/pages/student-import.html",
        "vue": "apps/plus-ui/src/views/edu/import-export/imp_task_list/components/TaskRowsDialog.vue",
        # 原型「校验结果」表只列失败行（行号 / 对象 / 失败原因）；任务中心的行明细同时含成功行，
        # 因此多两列：结果（成功 / 跳过 / 失败）与原始数据（排障用），见 CR-149。
        "table_index": 1,
        "deferred_groups": {},
        "extra_columns": {
            "结果": "任务中心行明细同时含成功行，需要结果列区分（原型只给失败行）",
            "原始数据": "排障用：保留导入行的原始单元格，便于定位错列 / 错值",
        },
    },
    {
        "page_id": "PAGE-AUDIT-LOG-DETAIL",
        "name": "审计日志字段级变更（抽屉）",
        "prototype": "prototypes/functional/v1/pages/audit-log-list.html",
        "vue": "apps/plus-ui/src/views/edu/audit/audit_log_list/components/LogDetailDrawer.vue",
        "table_index": 2,
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-STR-GEN-CLASS-RESULT",
        "name": "教学班生成核对结果",
        "prototype": "prototypes/functional/v1/pages/stream-generate-class.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_gen_class/components/CheckResultTable.vue",
        "table_index": 2,
        "deferred_groups": {},
    },
    {
        "page_id": "PAGE-STR-STAT-SUBJECT",
        "name": "选科统计 · 学科分布",
        "prototype": "prototypes/functional/v1/pages/stream-stat.html",
        "vue": "apps/plus-ui/src/views/edu/stream/str_stat/components/SubjectStatTable.vue",
        "table_index": 2,
        "deferred_groups": {},
    },
]

# 已交付浮层的对照清单：分组顺序来自原型（`data-layout-group` 与卡片标题）
OVERLAY_CHECKS = [
    {
        "page_id": "PAGE-TCH-DETAIL",
        "name": "教师详情抽屉",
        "prototype": "prototypes/high-fidelity/v1/pages/teacher-list.html",
        "vue": "apps/plus-ui/src/views/edu/teacher/tch_list/components/TeacherDetailDrawer.vue",
        "groups": ["基础信息", "任职信息", "任教清单"],
    },
    {
        "page_id": "PAGE-GRD-DETAIL",
        "name": "年级详情抽屉",
        "prototype": "prototypes/high-fidelity/v1/pages/grade-list.html",
        "vue": "apps/plus-ui/src/views/edu/grade/grd_list/components/GradeDetailDrawer.vue",
        "groups": ["基础信息", "下辖班级"],
    },
    {
        "page_id": "PAGE-STU-DETAIL",
        "name": "学生详情抽屉",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentDetailDrawer.vue",
        "groups": ["基础信息", "教育信息", "证件信息", "联系方式", "监护人", "变更记录"],
    },
    {
        "page_id": "PAGE-STU-CREATE",
        "name": "新增 / 编辑三步向导",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentFormDialog.vue",
        "steps": [
            ("学籍信息", ["基础信息", "教育信息", "补充信息"]),
            ("证件与联系", ["证件信息", "联系方式"]),
            ("监护人", ["监护人"]),
        ],
    },
    {
        "page_id": "PAGE-STU-STATUS",
        "name": "学籍异动弹窗（同文件按 mode=promotion 复用为 PAGE-PRM-CHANGE 异动登记）",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentStatusDialog.vue",
        "groups": ["异动信息", "复学 / 报到安排", "异动说明"],
    },
    {
        "page_id": "PAGE-STU-TRANSFER",
        "name": "调班弹窗",
        "prototype": "prototypes/functional/v2/pages/student-list.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_list/components/StudentTransferDialog.vue",
        "groups": ["班级关系", "调班说明"],
    },
    {
        "page_id": "PAGE-STU-CROSS-TRANSFER",
        "name": "跨校转学（转出校）向导",
        "prototype": "prototypes/functional/v1/pages/student-cross-transfer.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_cross_transfer/index.vue",
        "steps": [
            ("选择学生", ["选择学生"]),
            ("选择转入校与目标班级", ["选择转入校与目标班级"]),
            ("确认与提交", ["确认与提交"]),
            ("结果", ["结果"]),
        ],
    },
    {
        "page_id": "PAGE-PRM-TRANSFER",
        "name": "跨校转学（转入校）向导",
        "prototype": "prototypes/functional/v1/pages/promotion-transfer.html",
        "vue": "apps/plus-ui/src/views/edu/promotion/prm_transfer/index.vue",
        "steps": [
            ("待接收转学单", ["待接收转学单"]),
            ("核对信息与接收", ["核对信息与接收"]),
            ("接收确认", ["接收确认"]),
            ("报到", ["报到"]),
        ],
    },
    {
        "page_id": "PAGE-STU-IMPORT",
        "name": "学生批量导入向导",
        "prototype": "prototypes/functional/v1/pages/student-import.html",
        "vue": "apps/plus-ui/src/views/edu/student/stu_import/index.vue",
        "steps": [
            ("下载模板", ["下载模板"]),
            ("上传与校验", ["上传与校验"]),
            ("校验结果", ["校验结果"]),
            ("执行与进度", ["执行与进度"]),
        ],
    },
    {
        "page_id": "PAGE-TERM-CREATE",
        "name": "新建 / 编辑学年弹窗",
        "prototype": "prototypes/functional/v1/pages/term-list.html",
        "vue": "apps/plus-ui/src/views/edu/term/term_list/components/AcademicYearFormDialog.vue",
        "groups": ["学年信息"],
    },
    {
        "page_id": "PAGE-TERM-ARCHIVE",
        "name": "学年归档弹窗",
        "prototype": "prototypes/functional/v1/pages/term-list.html",
        "vue": "apps/plus-ui/src/views/edu/term/term_list/components/AcademicYearArchiveDialog.vue",
        "groups": ["归档信息"],
    },
    {
        # 页面树未提供原型（prototype_file 为「—」，batch: deferred，GAP-088 取推荐方案实现），
        # 因此只核对页面文件里的分组顺序，不做列比对。
        "page_id": "PAGE-CLS-MERGE",
        "name": "班级合并",
        "prototype": None,
        "vue": "apps/plus-ui/src/views/edu/class/cls_merge/index.vue",
        "groups": ["合并范围"],
    },
]

FILTER_SECTION_START = 'data-role="filter"'
FILTER_SECTION_END = 'data-role="table"'

problems: list[str] = []


def rel(path: str) -> str:
    return os.path.relpath(path, REPO_ROOT).replace("\\", "/")


def read(path: str) -> str:
    return io.open(os.path.join(REPO_ROOT, path), encoding="utf-8").read()


def parse_prototype_filter(html: str) -> list[str]:
    start = html.find(FILTER_SECTION_START)
    end = html.find(FILTER_SECTION_END)
    if start < 0 or end < 0:
        return []
    seg = html[start:end]
    labels = []
    for m in re.finditer(r"<label[^>]*>(.*?)</label>", seg, re.S):
        text = re.sub(r"<[^>]+>", "", m.group(1)).strip()
        text = text.replace("*", "").strip()
        if text:
            labels.append(text)
    return labels


def parse_prototype_columns(html: str, table_index: int | None = None) -> list[dict]:
    columns = []
    if table_index:
        # 组件内的表用 table_index 指定原型里的第 N 张表（如弹窗 / 抽屉里的表）
        tables = list(re.finditer(r"<table\b[^>]*>.*?</table>", html, re.S))
        if len(tables) < table_index:
            return columns
        html = tables[table_index - 1].group(0)
    else:
        # 只解析主表：页面里可能还有展开行 / 嵌套的小表，限定到 data-role="table" 所在的这张表
        marker = html.find('data-role="table"')
        if marker >= 0:
            end = html.find("</table>", marker)
            if end > 0:
                html = html[marker:end]
    for m in re.finditer(r"<th\b([^>]*)>(.*?)</th>", html, re.S):
        attrs, inner = m.group(1), m.group(2)
        label_text = re.sub(r"<[^>]+>", "", inner).strip()
        # 数据列靠 data-role="column" 识别；操作列在部分原型里没有 data-* 标记，按列名兜住
        if 'data-role="column"' not in attrs and 'data-layout-group="操作"' not in attrs and label_text != "操作":
            continue
        field = re.search(r'data-field="([^"]+)"', attrs)
        group = re.search(r'data-layout-group="([^"]+)"', attrs)
        label = label_text
        columns.append(
            {
                "field": field.group(1) if field else "",
                "group": group.group(1) if group else "",
                "label": label,
            }
        )
    return columns


def parse_vue_filter(vue: str) -> list[str]:
    labels = []
    for m in re.finditer(r"<el-form-item\b([^>]*)>", vue, re.S):
        attrs = m.group(1)
        if 'data-layout-group="' not in attrs:
            continue
        label = re.search(r'\blabel="([^"]+)"', attrs)
        if label:
            labels.append(label.group(1).strip())
    return labels


def parse_vue_columns(vue: str) -> list[dict]:
    columns = []
    for m in re.finditer(r"<el-table-column\b([^>]*)>", vue, re.S):
        attrs = m.group(1)
        label = re.search(r'\blabel="([^"]+)"', attrs)
        group = re.search(r'data-layout-group="([^"]+)"', attrs)
        if not label or not group:
            continue
        prop = re.search(r'\bprop="([^"]+)"', attrs)
        columns.append(
            {
                "prop": prop.group(1) if prop else "",
                "group": group.group(1),
                "label": label.group(1).strip(),
                "fixed_right": 'fixed="right"' in attrs,
            }
        )
    return columns


def check_filter(check: dict, prototype_html: str, vue: str) -> None:
    expected = parse_prototype_filter(prototype_html)
    actual = parse_vue_filter(vue)
    if expected == actual:
        print("  查询区字段顺序一致：%d 项（%s）" % (len(actual), " → ".join(actual)))
        return
    # 冻结 v1 原型可能整页没有查询区标记；此时只核对生产页的查询项，不判失败
    if not expected and check.get("filter_note"):
        print("  原型没有查询区标记，生产页查询项（%d 项）：%s —— %s" % (len(actual), " → ".join(actual), check["filter_note"]))
        return
    problems.append(
        "%s 查询区字段顺序与原型不一致\n      原型：%s\n      生产：%s"
        % (check["page_id"], " → ".join(expected), " → ".join(actual))
    )


def check_columns(check: dict, prototype_html: str, vue: str) -> None:
    expected = parse_prototype_columns(prototype_html, check.get("table_index"))
    deferred = check.get("deferred_groups") or {}
    expected_kept = []
    deferred_labels = check.get("deferred_labels") or {}
    for col in expected:
        if col["group"] in deferred:
            print("  原型列 %s（分组「%s」）本批不做：%s" % (col["label"], col["group"], deferred[col["group"]]))
            continue
        if col["label"] in deferred_labels:
            print("  原型列 %s 本批不做：%s" % (col["label"], deferred_labels[col["label"]]))
            continue
        expected_kept.append(col)

    actual = parse_vue_columns(vue)
    # 生产页可以保留原型未标记的列（v1 原型部分 th 没有 data-role），登记后不参与逐列比对
    extra_columns = check.get("extra_columns") or {}
    if extra_columns:
        kept_actual = []
        for col in actual:
            if col["label"] in extra_columns:
                print("  生产页保留列 %s：%s" % (col["label"], extra_columns[col["label"]]))
                continue
            kept_actual.append(col)
        actual = kept_actual
    ok = True
    if len(expected_kept) != len(actual):
        problems.append(
            "%s 表格列数与原型不一致：原型 %d 列（已扣除延后项），生产 %d 列"
            % (check["page_id"], len(expected_kept), len(actual))
        )
        ok = False

    for index, (exp, act) in enumerate(zip(expected_kept, actual), 1):
        if exp["label"] != act["label"]:
            problems.append("%s 第 %d 列列名不一致：原型「%s」，生产「%s」" % (check["page_id"], index, exp["label"], act["label"]))
            ok = False
        # 冻结的 v1 原型没有 data-layout-group，这类页面只对照列名与列序
        if exp["group"] and exp["group"] != act["group"]:
            problems.append("%s 第 %d 列分组不一致：原型「%s」，生产「%s」" % (check["page_id"], index, exp["group"], act["group"]))
            ok = False
        if exp["group"] == "操作" and not act["fixed_right"]:
            problems.append("%s 操作列必须 fixed=\"right\"（page-field-layout 第 4 节）" % check["page_id"])
            ok = False

    if ok:
        order = " → ".join("%s(%s)" % (c["label"], c["group"]) for c in actual)
        print("  表格列顺序与分组一致：%d 列（%s）" % (len(actual), order))


def main() -> int:
    for check in CHECKS:
        proto_path = os.path.join(REPO_ROOT, check["prototype"])
        vue_path = os.path.join(REPO_ROOT, check["vue"])
        for path in (proto_path, vue_path):
            if not os.path.isfile(path):
                problems.append("缺少文件：%s" % rel(path))
        if problems:
            break
        print("=== %s %s ===" % (check["page_id"], check["name"]))
        prototype_html = read(check["prototype"])
        vue = read(check["vue"])
        check_filter(check, prototype_html, vue)
        check_columns(check, prototype_html, vue)

    for check in OVERLAY_CHECKS:
        vue_path = os.path.join(REPO_ROOT, check["vue"])
        if not os.path.isfile(vue_path):
            problems.append("缺少文件：%s" % rel(vue_path))
            continue
        print("=== %s %s ===" % (check["page_id"], check["name"]))
        overlay = read(check["vue"])
        # 操作列是独立分组，且已在页面级检查里单独校验，不参与字段分组顺序
        groups = [g for g in re.findall(r'data-layout-group="([^"]+)"', overlay) if g != "操作"]
        expected_groups = list(check.get("groups") or [])
        for _, step_groups in check.get("steps") or []:
            expected_groups.extend(step_groups)
        if expected_groups and groups != expected_groups:
            problems.append(
                "%s 分组顺序与原型不一致\n      原型：%s\n      生产：%s"
                % (check["page_id"], " → ".join(expected_groups), " → ".join(groups))
            )
        else:
            print("  分组顺序一致：%s" % " → ".join(groups))
        if check.get("steps"):
            titles = re.findall(r'<el-step\s+title="([^"]+)"', overlay)
            expected_titles = [title for title, _ in check["steps"]]
            if titles != expected_titles:
                problems.append(
                    "%s 向导步骤与原型不一致：原型 %s，生产 %s"
                    % (check["page_id"], " / ".join(expected_titles), " / ".join(titles))
                )
            else:
                print("  向导步骤一致：%s" % " → ".join(titles))

    if problems:
        print("")
        for item in problems:
            print("问题：" + item)
        print("\n共 %d 项问题" % len(problems))
        return 1
    print("\n通过：生产页面结构与应用原型一致")
    return 0


if __name__ == "__main__":
    sys.exit(main())
