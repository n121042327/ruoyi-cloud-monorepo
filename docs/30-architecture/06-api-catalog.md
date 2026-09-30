# 接口清单（模块 × 资源 × 操作 × operationId）

> 本文件由 `tools/extract_api_catalog.py` 从各模块 PRD 第 8 节生成，**不手工编辑接口行**；
> 新增 / 修改接口先改模块 PRD 第 8 节，再重跑工具。
> 阶段 5 的 `api/openapi.yaml` 与 `page-action-api-map.yaml` 必须以本清单的 operationId 为准。

## 1. 服务归属

| 归属 | 说明 | 覆盖的模块 |
|---|---|---|
| `ruoyi-edu`（教育域主服务） | 首轮全部教育业务接口都在该服务内，避免早期拆服务带来的分布式事务成本 | 学生 / 教师 / 年级 / 班级 / 升班 / 选科 / 学科 / 学校 / 学年学期 |
| `ruoyi-edu` + 消息（RabbitMQ） | 导入执行、升班执行、教学班生成、日志归档等耗时操作走异步任务，接口只入队 | 导入导出与异步任务 |
| `ruoyi-edu` + Elasticsearch（后续） | 题库检索不在首轮；审计日志的归档检索首轮用数据库分区实现 | 审计与操作日志（检索部分） |

## 2. 权限点推断规则

| 路径前缀 | 权限资源（`05-permission-matrix.yaml`） |
|---|---|
| `/edu/student` | `person.student` |
| `/edu/guardian` | `person.student_guardian` |
| `/edu/teacher` | `person.teacher` |
| `/edu/teaching-assignment` | `person.teaching_assignment` |
| `/edu/grade` | `org.grade` |
| `/edu/class` | `org.class` |
| `/edu/teaching-class` | `org.teaching_class` |
| `/edu/promotion` | `promotion.batch` |
| `/edu/enrollment` | `enrollment.status` |
| `/edu/transfer` | `enrollment.transfer` |
| `/edu/stream` | `stream.*` |
| `/edu/subject` | `org.subject` |
| `/edu/school` | `org.school` |
| `/edu/campus` | `org.school` |
| `/edu/term` | `org.term` |
| `/edu/import` | `data.import` |
| `/edu/export` | `data.export` |
| `/edu/async-task` | `data.async_task` |
| `/edu/audit` | `audit.log` |

动作名映射：`list*` / `get*` → `read`；`add*` → `create`；`update*` / `save*` / `assign*` / `change*` → `update`；
`remove*` / `disable*` → `delete`（逻辑删除）；`export*` → `export`；`import*` / `validate*` → `import`；
`approve*` → `approve`；`submit*` / `execute*` / `cancel*` / `retry*` / `replay*` → 资源已有的写动作（见 CR-012 的口径）。

## 3. 同步 / 异步边界

**同步**：单条增删改查、列表分页、详情、统计（≤ 5000 行）、审批、状态流转。

**异步（入队后立即返回任务号）**：导入执行、导出（> 2000 行）、升班预览与执行、教学班生成（> 1 万人）、
归档区间检索（> 30 秒）、任务重试与死信重放。异步接口统一返回 `task_no`，进度与结果在异步任务中心查询。

## 4. 接口总览（共 173 个 operationId）

| 模块 | 接口数 |
|---|---|
| 审计与操作日志（`audit`） | 10 |
| 班级管理（`class`） | 22 |
| 年级管理（`grade`） | 11 |
| 导入导出与异步任务（`import-export`） | 14 |
| 升班与学籍异动（`promotion`） | 20 |
| 学校与租户（`school`） | 16 |
| 3+1+2 选科与教学班（`stream`） | 17 |
| 学生管理（`student`） | 19 |
| 学科与配置（`subject`） | 12 |
| 教师管理（`teacher`） | 20 |
| 学年学期（`term`） | 12 |
| **合计** | **173** |

## 5.1 审计与操作日志（`audit`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listOperationLog` | GET | `/edu/audit/log/list` | 日志分页查询 | `audit.log` | 查询 | 同步 |
| 2 | `getOperationLog` | GET | `/edu/audit/log/{id}` | 日志详情（含变更明细） | `audit.log` | 查询 | 同步 |
| 3 | `listObjectChangeLog` | GET | `/edu/audit/object/{objectType}/{objectId}/timeline` | 对象变更时间线 | `audit.log` | 查询 | 同步 |
| 4 | `exportOperationLog` | POST | `/edu/audit/log/export` | 日志导出 | `audit.log` | 写入 / 触发 | 异步 |
| 5 | `listOperatorAccess` | GET | `/edu/audit/operator-access/list` | 运营访问记录（租户侧） | `audit.log` | 查询 | 同步 |
| 6 | `exportOperatorAccess` | POST | `/edu/audit/operator-access/export` | 运营访问记录导出 | `audit.log` | 写入 / 触发 | 异步 |
| 7 | `listSensitiveAccess` | GET | `/edu/audit/sensitive-access/list` | 敏感数据访问记录 | `audit.log` | 查询 | 同步 |
| 8 | `listSecurityEvent` | GET | `/edu/audit/security-event/list` | 登录与安全事件 | `audit.log` | 查询 | 同步 |
| 9 | `listArchiveBatch` | GET | `/edu/audit/archive/list` | 归档批次列表 | `audit.log` | 查询 | 同步 |
| 10 | `searchArchivedLog` | POST | `/edu/audit/archive/search` | 归档区间检索 | `audit.log` | 写入 / 触发 | 异步 |

## 5.2 班级管理（`class`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listClass` | GET | `/edu/class/list` | 分页查询 | `org.class` | 查询 | 同步 |
| 2 | `getClass` | GET | `/edu/class/{id}` | 详情 | `org.class` | 查询 | 同步 |
| 3 | `addClass` | POST | `/edu/class` | 新建 | `org.class` | 写入 / 触发 | 同步 |
| 4 | `batchAddClass` | POST | `/edu/class/batch` | 批量生成 | `org.class` | 写入 / 触发 | 异步 |
| 5 | `updateClass` | PUT | `/edu/class` | 编辑 | `org.class` | 更新 | 同步 |
| 6 | `removeClass` | DELETE | `/edu/class/{id}` | 逻辑删除 | `org.class` | 逻辑删除 | 同步 |
| 7 | `disableClass` | POST | `/edu/class/{id}/disable` | 停用 | `org.class` | 写入 / 触发 | 同步 |
| 8 | `mergeClass` | POST | `/edu/class/merge` | 合并 | `org.class` | 写入 / 触发 | 同步 |
| 9 | `listClassRoster` | GET | `/edu/class/{id}/roster` | 花名册 | `org.class` | 查询 | 同步 |
| 10 | `addClassRoster` | POST | `/edu/class/{id}/roster` | 添加学生 | `org.class` | 写入 / 触发 | 同步 |
| 11 | `removeClassRoster` | DELETE | `/edu/class/{id}/roster/{studentId}` | 移出学生 | `org.class` | 逻辑删除 | 同步 |
| 12 | `transferClass` | POST | `/edu/class/roster/transfer` | 调班 | `org.class` | 写入 / 触发 | 同步 |
| 13 | `assignHeadTeacher` | POST | `/edu/class/{id}/head-teacher` | 指定班主任 | `org.class` | 写入 / 触发 | 同步 |
| 14 | `listClassTeachingAssignment` | GET | `/edu/class/{id}/teaching-assignment` | 任课教师（只读） | `org.class` | 查询 | 同步 |
| 15 | `importRosterValidate` | POST | `/edu/class/roster/import/validate` | 编班校验 | `org.class` | 写入 / 触发 | 同步 |
| 16 | `importRosterExecute` | POST | `/edu/class/roster/import/execute` | 编班执行（异步） | `org.class` | 写入 / 触发 | 异步 |
| 17 | `exportClassRoster` | POST | `/edu/class/{id}/roster/export` | 花名册导出 | `org.class` | 写入 / 触发 | 异步 |
| 18 | `listTeachingClass` | GET | `/edu/teaching-class/list` | 教学班列表 | `org.teaching_class` | 查询 | 同步 |
| 19 | `addTeachingClass` | POST | `/edu/teaching-class` | 新建教学班 | `org.teaching_class` | 写入 / 触发 | 同步 |
| 20 | `getTeachingClass` | GET | `/edu/teaching-class/{id}` | 教学班详情（`CR-017` 补登记：详情抽屉的数据来源） | `org.teaching_class` | 查询 | 同步 |
| 21 | `disableTeachingClass` | POST | `/edu/teaching-class/{id}/disable` | 停用教学班（`CR-017` 补登记：原因必填、写审计、历史成员保留） | `org.teaching_class` | 写入 / 触发 | 同步 |
| 22 | `listTeachingClassRoster` | GET | `/edu/teaching-class/{id}/roster` | 教学班成员清单（`CR-017` 补登记：只读，成员写入仍由生成流程触发） | `org.teaching_class` | 查询 | 同步 |

## 5.3 年级管理（`grade`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listGrade` | GET | `/edu/grade/list` | 分页查询 | `org.grade` | 查询 | 同步 |
| 2 | `getGrade` | GET | `/edu/grade/{id}` | 详情 | `org.grade` | 查询 | 同步 |
| 3 | `addGrade` | POST | `/edu/grade` | 新建 | `org.grade` | 写入 / 触发 | 同步 |
| 4 | `batchAddGrade` | POST | `/edu/grade/batch` | 按学段批量生成 | `org.grade` | 写入 / 触发 | 同步 |
| 5 | `updateGrade` | PUT | `/edu/grade` | 编辑 | `org.grade` | 更新 | 同步 |
| 6 | `removeGrade` | DELETE | `/edu/grade/{id}` | 逻辑删除 | `org.grade` | 逻辑删除 | 同步 |
| 7 | `archiveGrade` | POST | `/edu/grade/{id}/archive` | 归档 | `org.grade` | 写入 / 触发 | 同步 |
| 8 | `listGradeLeader` | GET | `/edu/grade/{id}/leader` | 年级主任列表 | `org.grade` | 查询 | 同步 |
| 9 | `saveGradeLeader` | POST | `/edu/grade/{id}/leader` | 指定年级主任 | `org.grade` | 写入 / 触发 | 同步 |
| 10 | `removeGradeLeader` | DELETE | `/edu/grade/{id}/leader/{leaderId}` | 解除任职 | `org.grade` | 逻辑删除 | 同步 |
| 11 | `getGradePromotionView` | GET | `/edu/grade/promotion-view` | 学年升级只读视图 | `org.grade` | 查询 | 同步 |

## 5.4 导入导出与异步任务（`import-export`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listImportTemplate` | GET | `/edu/import/template` | 模板清单与当前版本 | `data.import` | 查询 | 同步 |
| 2 | `downloadImportTemplate` | GET | `/edu/import/template/{module}` | 模板下载 | `data.import` | 查询 | 同步 |
| 3 | `validateImportFile` | POST | `/edu/import/validate` | 上传并同步校验 | `data.import` | 写入 / 触发 | 同步 |
| 4 | `executeImport` | POST | `/edu/import/execute` | 确认执行（异步） | `data.import` | 写入 / 触发 | 异步 |
| 5 | `downloadImportFailedRows` | GET | `/edu/import/{batchNo}/failed-rows` | 失败行下载 | `data.import` | 查询 | 同步 |
| 6 | `downloadImportResult` | GET | `/edu/import/{batchNo}/result` | 结果摘要与对照表 | `data.import` | 查询 | 同步 |
| 7 | `exportData` | POST | `/edu/export` | 导出（同步或异步） | `data.export` | 写入 / 触发 | 异步 |
| 8 | `listAsyncTask` | GET | `/edu/async-task/list` | 任务列表 | `data.async_task` | 查询 | 同步 |
| 9 | `getAsyncTask` | GET | `/edu/async-task/{taskNo}` | 任务详情 | `data.async_task` | 查询 | 同步 |
| 10 | `cancelAsyncTask` | POST | `/edu/async-task/{taskNo}/cancel` | 取消排队中的任务 | `data.async_task` | 写入 / 触发 | 同步 |
| 11 | `retryAsyncTask` | POST | `/edu/async-task/{taskNo}/retry` | 重试 | `data.async_task` | 写入 / 触发 | 同步 |
| 12 | `downloadTaskResult` | GET | `/edu/async-task/{taskNo}/file/{fileId}` | 结果文件下载（短时签名） | `data.async_task` | 查询 | 同步 |
| 13 | `listDeadLetterTask` | GET | `/edu/async-task/dead-letter` | 死信任务列表 | `data.async_task` | 查询 | 同步 |
| 14 | `replayDeadLetterTask` | POST | `/edu/async-task/dead-letter/{taskNo}/replay` | 死信重放 | `data.async_task` | 写入 / 触发 | 同步 |

## 5.5 升班与学籍异动（`promotion`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listPromotionTask` | GET | `/edu/promotion/task/list` | 任务列表 | `promotion.batch` | 查询 | 同步 |
| 2 | `getPromotionTask` | GET | `/edu/promotion/task/{id}` | 任务详情 | `promotion.batch` | 查询 | 同步 |
| 3 | `addPromotionTask` | POST | `/edu/promotion/task` | 创建任务 | `promotion.batch` | 写入 / 触发 | 同步 |
| 4 | `previewPromotionTask` | POST | `/edu/promotion/task/{id}/preview` | 生成预览 | `promotion.batch` | 写入 / 触发 | 同步 |
| 5 | `updatePromotionItem` | PUT | `/edu/promotion/task/{id}/item` | 调整单条去向 | `promotion.batch` | 更新 | 同步 |
| 6 | `batchUpdatePromotionItem` | PUT | `/edu/promotion/task/{id}/item/batch` | 批量调整 | `promotion.batch` | 更新 | 同步 |
| 7 | `validatePromotionTask` | POST | `/edu/promotion/task/{id}/validate` | 校验 | `promotion.batch` | 写入 / 触发 | 同步 |
| 8 | `executePromotionTask` | POST | `/edu/promotion/task/{id}/execute` | 执行（异步） | `promotion.batch` | 写入 / 触发 | 异步 |
| 9 | `retryPromotionTask` | POST | `/edu/promotion/task/{id}/retry` | 只重试失败项 | `promotion.batch` | 写入 / 触发 | 同步 |
| 10 | `cancelPromotionTask` | POST | `/edu/promotion/task/{id}/cancel` | 取消 | `promotion.batch` | 写入 / 触发 | 同步 |
| 11 | `exportPromotionPreview` | POST | `/edu/promotion/task/{id}/preview/export` | 预览导出 | `promotion.batch` | 写入 / 触发 | 同步 |
| 12 | `exportPromotionResult` | POST | `/edu/promotion/task/{id}/result/export` | 结果报告导出 | `promotion.batch` | 写入 / 触发 | 同步 |
| 13 | `listEnrollmentChange` | GET | `/edu/enrollment/change/list` | 异动记录 | `enrollment.status` | 查询 | 同步 |
| 14 | `addEnrollmentChange` | POST | `/edu/enrollment/change` | 发起异动 | `enrollment.status` | 写入 / 触发 | 同步 |
| 15 | `approveEnrollmentChange` | POST | `/edu/enrollment/change/{id}/approve` | 审批异动 | `enrollment.status` | 写入 / 触发 | 同步 |
| 16 | `listTransfer` | GET | `/edu/enrollment/transfer/list` | 转学单列表 | `enrollment.status` | 查询 | 同步 |
| 17 | `addTransfer` | POST | `/edu/enrollment/transfer` | 发起转出 | `enrollment.status` | 写入 / 触发 | 同步 |
| 18 | `acceptTransfer` | POST | `/edu/enrollment/transfer/{id}/accept` | 转入校接收 | `enrollment.status` | 写入 / 触发 | 同步 |
| 19 | `checkInTransfer` | POST | `/edu/enrollment/transfer/{id}/check-in` | 报到 | `enrollment.status` | 写入 / 触发 | 同步 |
| 20 | `cancelTransfer` | POST | `/edu/enrollment/transfer/{id}/cancel` | 撤销接收 | `enrollment.status` | 写入 / 触发 | 同步 |

## 5.6 学校与租户（`school`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listSchool` | GET | `/edu/school/list` | 分页查询 | `org.school` | 查询 | 同步 |
| 2 | `getSchool` | GET | `/edu/school/{id}` | 详情 | `org.school` | 查询 | 同步 |
| 3 | `getCurrentSchool` | GET | `/edu/school/current` | 学校侧角色读取本校信息 | `org.school` | 查询 | 同步 |
| 4 | `addSchool` | POST | `/edu/school` | 新建 | `org.school` | 写入 / 触发 | 同步 |
| 5 | `updateSchool` | PUT | `/edu/school` | 编辑 | `org.school` | 更新 | 同步 |
| 6 | `updateSchoolCode` | PUT | `/edu/school/{id}/school-code` | 修改编码 | `org.school` | 更新 | 同步 |
| 7 | `disableSchool` | POST | `/edu/school/{id}/disable` | 停用 | `org.school` | 写入 / 触发 | 同步 |
| 8 | `enableSchool` | POST | `/edu/school/{id}/enable` | 启用 | `org.school` | 写入 / 触发 | 同步 |
| 9 | `listCampus` | GET | `/edu/school/{id}/campus` | 校区列表 | `org.school` | 查询 | 同步 |
| 10 | `saveCampus` | POST | `/edu/school/{id}/campus` | 新增 / 编辑校区 | `org.school` | 写入 / 触发 | 同步 |
| 11 | `removeCampus` | DELETE | `/edu/school/campus/{id}` | 删除校区（校验引用） | `org.school` | 逻辑删除 | 同步 |
| 12 | `listSchoolStage` | GET | `/edu/school/{id}/stage` | 开设学段 | `org.school` | 查询 | 同步 |
| 13 | `saveSchoolStage` | POST | `/edu/school/{id}/stage` | 保存学段配置 | `org.school` | 写入 / 触发 | 同步 |
| 14 | `getSchoolSummary` | GET | `/edu/school/{id}/summary` | 统计摘要 | `org.school` | 查询 | 同步 |
| 15 | `initSchoolBaseline` | POST | `/edu/school/{id}/init` | 开通初始化 | `org.school` | 写入 / 触发 | 异步 |
| 16 | `exportSchool` | POST | `/edu/school/export` | 导出 | `org.school` | 写入 / 触发 | 同步 |

## 5.7 3+1+2 选科与教学班（`stream`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `getStreamConfig` | GET | `/edu/stream/config` | 查询选科配置 | `stream.*` | 查询 | 同步 |
| 2 | `saveStreamConfig` | POST | `/edu/stream/config` | 保存选科配置 | `stream.*` | 写入 / 触发 | 同步 |
| 3 | `getStreamOption` | GET | `/edu/stream/option` | 可选科目与规则 | `stream.*` | 查询 | 同步 |
| 4 | `getMyStream` | GET | `/edu/stream/my` | 学生查询本人选科 | `stream.*` | 查询 | 同步 |
| 5 | `submitMyStream` | POST | `/edu/stream/my` | 学生提交选科 | `stream.*` | 写入 / 触发 | 同步 |
| 6 | `updateMyStream` | PUT | `/edu/stream/my` | 截止前自助修改 | `stream.*` | 更新 | 同步 |
| 7 | `listStreamSelection` | GET | `/edu/stream/selection/list` | 选科清单 | `stream.*` | 查询 | 同步 |
| 8 | `getStreamStat` | GET | `/edu/stream/stat` | 组合分布统计 | `stream.*` | 查询 | 同步 |
| 9 | `listUnselectedStudent` | GET | `/edu/stream/unselected` | 未选科学生清单 | `stream.*` | 查询 | 同步 |
| 10 | `listStreamChangeRequest` | GET | `/edu/stream/change/list` | 变更申请列表 | `stream.*` | 查询 | 同步 |
| 11 | `addStreamChangeRequest` | POST | `/edu/stream/change` | 发起变更申请 | `stream.*` | 写入 / 触发 | 同步 |
| 12 | `cancelStreamChangeRequest` | POST | `/edu/stream/change/{id}/cancel` | 撤回申请 | `stream.*` | 写入 / 触发 | 同步 |
| 13 | `approveStreamChangeRequest` | POST | `/edu/stream/change/{id}/approve` | 审批 | `stream.*` | 写入 / 触发 | 同步 |
| 14 | `listStreamHistory` | GET | `/edu/stream/history` | 选科历史 | `stream.*` | 查询 | 同步 |
| 15 | `exportStreamSelection` | POST | `/edu/stream/export` | 导出选科结果 | `stream.*` | 写入 / 触发 | 异步 |
| 16 | `previewTeachingClassGenerate` | POST | `/edu/stream/teaching-class/preview` | 教学班生成预览 | `stream.*` | 写入 / 触发 | 异步 |
| 17 | `executeTeachingClassGenerate` | POST | `/edu/stream/teaching-class/generate` | 触发生成（写入由班级模块执行） | `stream.*` | 写入 / 触发 | 异步 |

## 5.8 学生管理（`student`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listStudent` | GET | `/edu/student/list` | 分页查询 | `person.student` | 查询 | 同步 |
| 2 | `getStudent` | GET | `/edu/student/{id}` | 详情 | `person.student` | 查询 | 同步 |
| 3 | `addStudent` | POST | `/edu/student` | 新增 | `person.student` | 写入 / 触发 | 同步 |
| 4 | `updateStudent` | PUT | `/edu/student` | 编辑 | `person.student` | 更新 | 同步 |
| 5 | `updateStudentNo` | PUT | `/edu/student/{id}/student-no` | 修改学号（高级操作） | `person.student` | 更新 | 同步 |
| 6 | `removeStudent` | DELETE | `/edu/student/{id}` | 逻辑删除 | `person.student` | 逻辑删除 | 同步 |
| 7 | `listStudentChangeLog` | GET | `/edu/student/{id}/change-log` | 变更记录 | `person.student` | 查询 | 同步 |
| 8 | `listEnrollmentStatusOption` | GET | `/edu/student/{id}/status-options` | 当前状态可执行的异动 | `person.student` | 查询 | 同步 |
| 9 | `changeEnrollmentStatus` | POST | `/edu/student/{id}/enrollment-change` | 学籍异动 | `person.student` | 写入 / 触发 | 同步 |
| 10 | `transferStudentClass` | POST | `/edu/student/{id}/class-transfer` | 调班 | `person.student` | 写入 / 触发 | 同步 |
| 11 | `crossSchoolTransfer` | POST | `/edu/student/cross-school-transfer` | 跨校转学 | `person.student` | 写入 / 触发 | 同步 |
| 12 | `importStudentValidate` | POST | `/edu/student/import/validate` | 导入校验 | `person.student` | 写入 / 触发 | 同步 |
| 13 | `importStudentExecute` | POST | `/edu/student/import/execute` | 导入执行（异步） | `person.student` | 写入 / 触发 | 异步 |
| 14 | `downloadStudentImportTemplate` | GET | `/edu/student/import/template` | 模板下载 | `person.student` | 查询 | 同步 |
| 15 | `exportStudent` | POST | `/edu/student/export` | 导出 | `person.student` | 写入 / 触发 | 异步 |
| 16 | `resetStudentPassword` | POST | `/edu/student/{id}/reset-password` | 重置密码 | `person.student` | 写入 / 触发 | 同步 |
| 17 | `listStudentGuardian` | GET | `/edu/student/{id}/guardian` | 监护人列表 | `person.student` | 查询 | 同步 |
| 18 | `saveStudentGuardian` | POST | `/edu/student/{id}/guardian` | 新增 / 修改监护人 | `person.student` | 写入 / 触发 | 同步 |
| 19 | `unbindStudentGuardian` | POST | `/edu/student/{id}/guardian/{guardianId}/unbind` | 解绑（需审核） | `person.student` | 写入 / 触发 | 同步 |

## 5.9 学科与配置（`subject`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listSubject` | GET | `/edu/subject/list` | 学科列表 | `org.subject` | 查询 | 同步 |
| 2 | `getSubject` | GET | `/edu/subject/{id}` | 详情 | `org.subject` | 查询 | 同步 |
| 3 | `addSubject` | POST | `/edu/subject` | 新建 | `org.subject` | 写入 / 触发 | 同步 |
| 4 | `updateSubject` | PUT | `/edu/subject` | 编辑 | `org.subject` | 更新 | 同步 |
| 5 | `batchInitSubject` | POST | `/edu/subject/batch-init` | 按学段批量初始化 | `org.subject` | 写入 / 触发 | 同步 |
| 6 | `saveSubjectStreamRole` | POST | `/edu/subject/{id}/stream-role` | 配置选科角色 | `org.subject` | 写入 / 触发 | 同步 |
| 7 | `saveSubjectStage` | POST | `/edu/subject/{id}/stage` | 配置学段启用 | `org.subject` | 写入 / 触发 | 同步 |
| 8 | `disableSubject` | POST | `/edu/subject/{id}/disable` | 停用 | `org.subject` | 写入 / 触发 | 同步 |
| 9 | `enableSubject` | POST | `/edu/subject/{id}/enable` | 启用 | `org.subject` | 写入 / 触发 | 同步 |
| 10 | `removeSubject` | DELETE | `/edu/subject/{id}` | 逻辑删除（校验引用） | `org.subject` | 逻辑删除 | 同步 |
| 11 | `checkSubjectReference` | GET | `/edu/subject/{id}/reference` | 引用检查 | `org.subject` | 查询 | 同步 |
| 12 | `listSubjectOption` | GET | `/edu/subject/option` | 供各模块使用的下拉清单（按学段过滤） | `org.subject` | 查询 | 同步 |

## 5.10 教师管理（`teacher`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listTeacher` | GET | `/edu/teacher/list` | 分页查询 | `person.teacher` | 查询 | 同步 |
| 2 | `getTeacher` | GET | `/edu/teacher/{id}` | 详情 | `person.teacher` | 查询 | 同步 |
| 3 | `addTeacher` | POST | `/edu/teacher` | 新增 | `person.teacher` | 写入 / 触发 | 同步 |
| 4 | `updateTeacher` | PUT | `/edu/teacher` | 编辑 | `person.teacher` | 更新 | 同步 |
| 5 | `updateTeacherNo` | PUT | `/edu/teacher/{id}/teacher-no` | 修改工号 | `person.teacher` | 更新 | 同步 |
| 6 | `listTeacherRole` | GET | `/edu/teacher/{id}/role` | 教育角色列表 | `person.teacher` | 查询 | 同步 |
| 7 | `saveTeacherRole` | POST | `/edu/teacher/{id}/role` | 分配角色 | `person.teacher` | 写入 / 触发 | 同步 |
| 8 | `removeTeacherRole` | DELETE | `/edu/teacher/{id}/role/{roleId}` | 解除角色 | `person.teacher` | 逻辑删除 | 同步 |
| 9 | `listTeachingAssignment` | GET | `/edu/teacher/assignment/list` | 任教关系查询 | `person.teacher` | 查询 | 同步 |
| 10 | `saveTeachingAssignment` | POST | `/edu/teacher/assignment` | 新增任教关系 | `person.teacher` | 写入 / 触发 | 同步 |
| 11 | `batchSaveTeachingAssignment` | POST | `/edu/teacher/assignment/batch` | 批量设置 | `person.teacher` | 写入 / 触发 | 同步 |
| 12 | `removeTeachingAssignment` | DELETE | `/edu/teacher/assignment/{id}` | 失效任教关系 | `person.teacher` | 逻辑删除 | 同步 |
| 13 | `leaveTeacher` | POST | `/edu/teacher/{id}/leave` | 离职 / 调离登记 | `person.teacher` | 写入 / 触发 | 同步 |
| 14 | `revokeTeacherLeave` | POST | `/edu/teacher/{id}/leave/revoke` | 撤销离职登记 | `person.teacher` | 写入 / 触发 | 同步 |
| 15 | `resetTeacherPassword` | POST | `/edu/teacher/{id}/reset-password` | 重置密码 | `person.teacher` | 写入 / 触发 | 同步 |
| 16 | `disableTeacherAccount` | POST | `/edu/teacher/{id}/account/disable` | 停用账号 | `person.teacher` | 写入 / 触发 | 同步 |
| 17 | `importTeacherValidate` | POST | `/edu/teacher/import/validate` | 导入校验 | `person.teacher` | 写入 / 触发 | 同步 |
| 18 | `importTeacherExecute` | POST | `/edu/teacher/import/execute` | 导入执行（异步） | `person.teacher` | 写入 / 触发 | 异步 |
| 19 | `downloadTeacherImportTemplate` | GET | `/edu/teacher/import/template` | 模板下载 | `person.teacher` | 查询 | 同步 |
| 20 | `exportTeacher` | POST | `/edu/teacher/export` | 导出 | `person.teacher` | 写入 / 触发 | 同步 |

## 5.11 学年学期（`term`）

| # | operationId | 方法 | 路径 | 说明 | 权限资源 | 动作 | 同步/异步 |
|---|---|---|---|---|---|---|---|
| 1 | `listAcademicYear` | GET | `/edu/term/year/list` | 学年列表 | `org.term` | 查询 | 同步 |
| 2 | `getAcademicYear` | GET | `/edu/term/year/{id}` | 学年详情 | `org.term` | 查询 | 同步 |
| 3 | `addAcademicYear` | POST | `/edu/term/year` | 新建学年 | `org.term` | 写入 / 触发 | 同步 |
| 4 | `updateAcademicYear` | PUT | `/edu/term/year` | 编辑学年 | `org.term` | 更新 | 同步 |
| 5 | `listTerm` | GET | `/edu/term/list` | 学期列表 | `org.term` | 查询 | 同步 |
| 6 | `saveTerm` | POST | `/edu/term` | 新增 / 编辑学期 | `org.term` | 写入 / 触发 | 同步 |
| 7 | `removeTerm` | DELETE | `/edu/term/{id}` | 删除学期（校验引用） | `org.term` | 逻辑删除 | 同步 |
| 8 | `getCurrentTerm` | GET | `/edu/term/current` | 当前学年学期 | `org.term` | 查询 | 同步 |
| 9 | `setCurrentTerm` | POST | `/edu/term/{id}/set-current` | 设为当前 | `org.term` | 写入 / 触发 | 同步 |
| 10 | `checkTermReference` | GET | `/edu/term/{id}/reference` | 引用检查 | `org.term` | 查询 | 同步 |
| 11 | `archiveAcademicYear` | POST | `/edu/term/year/{id}/archive` | 归档学年 | `org.term` | 写入 / 触发 | 同步 |
| 12 | `revokeArchiveAcademicYear` | POST | `/edu/term/year/{id}/archive/revoke` | 撤销归档 | `org.term` | 写入 / 触发 | 同步 |

## 6. 与原型动作的对应关系

原型里每个 `data-action-id` 都通过 `data-api` 指向本清单的 operationId；
对应关系的机器可读版本在阶段 5 的 `page-action-api-map.yaml`（覆盖 403 个动作编号）。
不调接口的动作（打开弹窗、切换筛选项、跳转页面）在映射表里记 `-`，并在 `page-actions.yaml` 里同样登记。
