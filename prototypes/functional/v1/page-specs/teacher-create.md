# 新增教师

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-CREATE` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | drawer（`md` = 640px，右侧滑出，内部三步） |
| 骨架模板 | `TPL-OVERLAY` + `el-steps` |
| 所属批次 | 2-2b-1 |
| 上游需求 | `REQ-TCH-014` ~ `REQ-TCH-020`；另被 `REQ-TCH-016`、`REQ-TCH-017`、`REQ-TCH-018`、`REQ-TCH-019`、`REQ-TCH-034` 引用 |
| 上游规则 | `BR-TEACHER-007`、`BR-TEACHER-001`、`BR-ACCOUNT-001`、`BR-AUDIT-001`、`DS-DENY-02` |
| 权限资源 | `person.teacher` 的 `create`（新增按钮取此权限） |
| 数据范围 | 所属学校必须落在操作人的数据范围内（`DS-DENY-02`）；租户管理员 `DS-02`，其他角色锁定本校 |
| 原型文件 | `pages/teacher-list.html` 的 `data-demo-panel="PAGE-TCH-CREATE"` |

## 1. 页面目的

教务主任、租户管理员把一名新教师录入系统。三步走：基本信息 → 联系方式 → 任职信息。
保存成功后系统创建教师主体、按学校规则创建登录账号并生成一次性初始凭据，同时写审计（`REQ-TCH-018` / `REQ-TCH-020`）。
保存成功提示条固定给三个后续动作：继续新增、查看详情、立即设置任教（`REQ-TCH-019`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 抽屉头 | — | `el-drawer` 的 `#header` | 标题、说明标签、关闭按钮 |
| 2 | 步骤条 | — | `el-steps` | 基本信息 → 联系方式 → 任职信息，横向，不跳页 |
| 3 | 校验汇总 | — | `el-alert` | 默认隐藏；校验失败时出现"请修正下方标红的字段" |
| 4 | 成功结果条 | — | `el-alert.success` | 默认隐藏；保存成功后出现，含登录名与三个后续动作 |
| 5 | 步骤 1 基本信息 | `form` | `el-form` + `form-grid` | 工号、姓名、性别、所属学校、在职状态 |
| 6 | 步骤 2 联系方式 | `form` | `el-form` + `form-grid` | 手机号、邮箱 |
| 7 | 步骤 3 任职信息 | `form` | `el-form` + `form-grid` | 入职日期、教育角色、备注 + "保存后会发生什么"提示 |
| 8 | 底部操作条 | — | 固定 footer，高 64px | 取消（打开放弃确认）/ 上一步 / 下一步 / 保存 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `teacher_no` | 工号 | `el-input` | 是 | 教务主任、租户管理员 | 空 | 必填；长度 2–32 位字母/数字/连字符；租户内唯一 | 唯一性由服务端判定，重复时按"工号已存在"提示 |
| `teacher_name` | 姓名 | `el-input` | 是 | 教务主任、租户管理员 | 空 | 必填；长度 2–50 | 不含首尾空格 |
| `gender` | 性别 | `el-select` | 是 | 教务主任、租户管理员 | 空 | 必填；取值来自 `gender` 枚举 | |
| `school_id` | 所属学校 | `el-select` | 是 | 租户管理员、平台运营 | 本校 | 必填；必须在数据范围内 | 非租户管理员锁定只读（`data-role-enabled`） |
| `employment_status` | 在职状态 | `el-select` | 否 | 不可编辑 | 在职 | 新增仅允许在职 | 枚举 `employment_status`；离职与调离走 `ACT-TCH-009` |
| `teacher_phone` | 手机号 | `el-input` | 否 | 教务主任、租户管理员 | 空 | 非空时为 11 位手机号 | 敏感字段，列表与详情默认掩码 |
| `email` | 邮箱 | `el-input` | 否 | 教务主任、租户管理员 | 空 | 非空时为邮箱格式 | 账号找回渠道 |
| `hire_date` | 入职日期 | `el-date-picker` | 否 | 租户管理员、教务主任 | 空 | 格式 YYYY-MM-DD | |
| `edu_role` | 教育角色 | 多选（`el-select multiple` 形态） | 否 | 教务主任、租户管理员 | 空 | 只可选 `edu_role` 枚举值 | 校领导与教务主任为学校级角色；年级主任需补学年学期与年级 |
| `remark` | 备注 | `el-input` | 否 | 教务主任、租户管理员 | 空 | 长度 ≤ 200 | |

> `school_id`、`teacher_phone`、`email`、`hire_date`、`employment_status` 已由 `CR-004` 补登记进 `06-field-dictionary.yaml`（`GAP-033` 已关闭）。

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-TCH-001` | 新增（列表页） | 点击 | `person.teacher:create` | 打开本抽屉并重置为默认值 | — |
| `ACT-TCH-019` | 下一步 | 点击 | 当前步骤校验通过 | 进入下一步；失败则停留并标红 | — |
| `ACT-TCH-020` | 上一步 | 点击 | 非第一步 | 返回上一步，内容保留 | — |
| `ACT-TCH-021` | 保存 | 点击 | 校验全部通过 | 创建教师与账号，结果条出现并给三个后续动作 | `addTeacher` |
| `ACT-TCH-022` | 取消 / 关闭 | 点击 | 始终可用 | 打开放弃确认弹窗 | — |
| `ACT-TCH-024` | 继续新增 | 点击结果条 | `person.teacher:create` | 重置为空白表单 | — |
| `ACT-TCH-025` | 查看详情 | 点击结果条 | `person.teacher:read` | 关闭抽屉并打开 `PAGE-TCH-DETAIL` | `getTeacher` |
| `ACT-TCH-026` | 立即设置任教 | 点击结果条 | `person.teaching_assignment:create` | 进入 `PAGE-TCH-ASSIGN`（批次 2-2b-2） | — |
| `ACT-TCH-027` / `ACT-TCH-028` | 放弃并关闭 / 继续填写 | 点击 | 放弃确认弹窗已打开 | 关闭两个浮层 / 关回表单 | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 不适用（新增无既有数据）；保存时按钮置 loading 并禁用 | — |
| 空数据 | `empty` | 表单本身就是空态，不给插画；教育角色不选即为空 | — |
| 查询失败 | `error` | 不适用（无查询）；保存失败按 `feedback_templates` 给出原因与请求编号 | 重试 |
| 无权限 | `forbidden` | 无 `person.teacher:create` 时不渲染"新增"入口；绕过前端直接调接口由后端拒绝 | — |
| 提交中 | `submitting` | 保存按钮 loading + 禁用，禁止重复提交；失败时字段级红字 + 顶部汇总 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 列表"新增" | `PAGE-TCH-CREATE` | 抽屉 | 关闭后回到列表，保留筛选与页码 |
| 取消 / 关闭 | `DIALOG-TCH-DISCARD` | 弹窗 | 确认放弃则同时关闭两个浮层；继续填写则关回抽屉 |
| 保存成功"查看详情" | `PAGE-TCH-DETAIL` | 抽屉 | 关闭后回到列表 |
| 保存成功"立即设置任教" | `PAGE-TCH-ASSIGN` | 独立页 | 返回后回到列表 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（`academic_director`） | 整个三步表单 | 新增、下一步、上一步、保存、取消 | 所属学校锁定本校（`data-role-editable` 不含该角色） |
| 租户管理员（`tenant_admin`） | 整个三步表单 | 上列全部 + 所属学校可切换 | 无 |
| 年级主任（`grade_leader`） | 不渲染入口 | — | — |
| 班主任 / 任课教师 | 不渲染入口 | — | — |
| 校领导（`school_leader`） | 不渲染入口（`05-permission-matrix.yaml` 中无 create） | — | `CR-004` 已统一：PRD 4.4 字段级矩阵与权限矩阵一致，校领导对教师主体只读 |
| 平台运营（`platform_ops`） | 不渲染入口（只读角色） | — | — |

## 8. 样例数据

| 节点 | 用途 | 为什么用它 |
|---|---|---|
| `content-samples.json` → `teacher_create_sample` | 保存成功后的登录名与工号展示 | 新增记录不存在于既有数据集，必须由样例节点统一口径，禁止在页面里临时编造工号 |
| `content-samples.json` → `org` | 所属学校下拉的三个选项 | 3 所学校与租户层级一致，验证数据范围约束 |
| `content-samples.json` → `dictionaries` 的 `gender` / `edu_role` | 性别与教育角色选项 | 枚举唯一来源是 `06-field-dictionary.yaml`，此处只做渲染副本 |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-OVERLAY` + `el-steps`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-001`、`ACT-TCH-019` ~ `028`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`CR-004`，`GAP-033` 已关闭）
- [x] 五类状态按本页实际情况给出形态（见第 5 节）
- [x] 1440×900 与 1366×768 下未出现横向滚动条与元素重叠
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
