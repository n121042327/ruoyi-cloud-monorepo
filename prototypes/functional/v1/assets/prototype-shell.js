/* 业务原型演示引擎（阶段 2）
   依据：markup-contract.md 第 10 节（原型可交互约定）、layout-spec.yaml 的 system_shell
   职责：注入完整外壳、角色切换、状态切换、浮层打开/关闭、提交模拟、表单校验、轻提示。
   约束：不引第三方库、不访问后端、不依赖构建；页面级差异仍写在每个页面底部的内联 script 里。 */
(function () {
  'use strict';

  /* ---------------------------------------------------------------- 静态定义 */

  // 与 content-samples.json 的 prototype_roles 保持一致
  var ROLES = [
    { code: 'super_admin', label: '超级管理员', persona: '系统内置账号' },
    { code: 'academic_director', label: '教务主任', persona: '郑雅琴' },
    { code: 'homeroom', label: '班主任', persona: '邓丽娟' },
    { code: 'grade_leader', label: '年级主任', persona: '何文博' },
    { code: 'subject_teacher', label: '任课教师', persona: '谢明轩' },
    { code: 'school_leader', label: '校领导', persona: '陆承志' },
    { code: 'platform_ops', label: '平台运营', persona: '运维账号 A' }
  ];

  var STATES = [
    { code: 'normal', label: '正常' },
    { code: 'loading', label: '加载中' },
    { code: 'empty', label: '空数据' },
    { code: 'error', label: '查询失败' },
    { code: 'forbidden', label: '无权限' },
    { code: 'submitting', label: '提交中' },
    { code: 'partial', label: '部分失败' },
    { code: 'queued', label: '排队中' }
  ];

  // 与 navigation.yaml 的 menus / pages 保持一致；delivered 为本批已交付页面
  var MENUS = [
    {
      group: '教育管理',
      items: [
        { id: 'PAGE-STU-LIST', name: '学生管理', batch: '2-1', delivered: 'pages/student-list.html' },
        { id: 'PAGE-TCH-LIST', name: '教师管理', batch: '2-2', delivered: 'pages/teacher-list.html' },
        { id: 'PAGE-CLS-LIST', name: '班级管理', batch: '2-3', delivered: 'pages/class-list.html' },
        { id: 'PAGE-GRD-LIST', name: '年级管理', batch: '2-2', delivered: 'pages/grade-list.html' },
        { id: 'PAGE-PRM-LIST', name: '升班与学籍', batch: '2-3', delivered: 'pages/promotion-list.html' },
        { id: 'PAGE-STR-LIST', name: '选科与教学班', batch: '2-7', delivered: 'pages/stream-list.html' }
      ]
    },
    {
      group: '组织与配置',
      items: [
        { id: 'PAGE-SCH-LIST', name: '学校管理', batch: '2-6', delivered: 'pages/school-list.html' },
        { id: 'PAGE-TERM-LIST', name: '学年学期', batch: '2-6', delivered: 'pages/term-list.html' },
        { id: 'PAGE-SUB-LIST', name: '学科与配置', batch: '2-6', delivered: 'pages/subject-list.html' }
      ]
    },
    {
      group: '平台与运维',
      items: [
        { id: 'PAGE-IMP-WIZARD', name: '导入导出', batch: '2-4', delivered: 'pages/import-wizard.html' },
        { id: 'PAGE-IMP-TASK-LIST', name: '异步任务', batch: '2-9', delivered: 'pages/async-task-list.html' },
        { id: 'PAGE-AUDIT-LOG-LIST', name: '审计日志', batch: '2-8', delivered: 'pages/audit-log-list.html' }
      ]
    }
  ];

  var PAGE_BATCH = {};
  var PAGE_NAME = {};
  MENUS.forEach(function (g) {
    g.items.forEach(function (it) {
      PAGE_BATCH[it.id] = it.batch;
      PAGE_NAME[it.id] = it.name;
    });
  });

  // 不在左侧菜单里的已交付页面（详情页 / 独立业务页）。
  // menu 之外的页面无法从 MENUS 推导交付状态与名称，必须在这里显式登记，否则点击只会得到"后续批次交付"提示。
  var EXTRA_PAGES = {
    'PAGE-TCH-ASSIGN': { path: 'pages/teacher-assign.html', name: '任教关系设置', batch: '2-2' },
    'PAGE-CLS-DETAIL': { path: 'pages/class-detail.html', name: '班级详情', batch: '2-3' },
    'PAGE-CLS-ROSTER-ADD': { path: 'pages/class-roster-add.html', name: '编班（添加学生）', batch: '2-3' },
    'PAGE-CLS-MOVE': { path: 'pages/class-move-students.html', name: '批量迁学生', batch: '2-3' },
    'PAGE-PRM-CREATE': { path: 'pages/promotion-create.html', name: '新建升班任务', batch: '2-3' },
    'PAGE-PRM-PREVIEW': { path: 'pages/promotion-preview.html', name: '升班预览与调整', batch: '2-3' },
    'PAGE-PRM-VALIDATE': { path: 'pages/promotion-validate.html', name: '升班校验结果', batch: '2-3' },
    'PAGE-PRM-EXECUTE': { path: 'pages/promotion-execute.html', name: '执行与进度', batch: '2-3' },
    'PAGE-PRM-RESULT': { path: 'pages/promotion-result.html', name: '执行结果与重试', batch: '2-3' },
    'PAGE-STU-IMPORT': { path: 'pages/student-import.html', name: '学生批量导入', batch: '2-4' },
    'PAGE-TCH-IMPORT': { path: 'pages/teacher-import.html', name: '教师批量导入', batch: '2-4' },
    'PAGE-CLS-ROSTER-IMPORT': { path: 'pages/class-import-roster.html', name: '编班表导入', batch: '2-4' },
    'PAGE-LOGIN': { path: 'pages/login.html', name: '登录', batch: '2-4' },
    'PAGE-403': { path: 'pages/403.html', name: '无权限', batch: '2-4' },
    'PAGE-404': { path: 'pages/404.html', name: '页面不存在', batch: '2-4' },
    'PAGE-500': { path: 'pages/500.html', name: '服务异常', batch: '2-4' },
    'PAGE-STU-CROSS-TRANSFER': { path: 'pages/student-cross-transfer.html', name: '跨校转学（转出校）', batch: '2-5' },
    'PAGE-PRM-TRANSFER': { path: 'pages/promotion-transfer.html', name: '跨校转学（转入校）', batch: '2-5' },
    'PAGE-PRM-HISTORY': { path: 'pages/promotion-history.html', name: '异动历史', batch: '2-5' },
    'PAGE-SCH-CAMPUS': { path: 'pages/school-campus.html', name: '校区管理', batch: '2-6' },
    'PAGE-SCH-INIT': { path: 'pages/school-init.html', name: '开通初始化', batch: '2-6' },
    'PAGE-TERM-LIST': { path: 'pages/term-list.html', name: '学年学期', batch: '2-6' },
    'PAGE-TERM-TERMS': { path: 'pages/term-terms.html', name: '学期管理', batch: '2-6' },
    'PAGE-STR-CONFIG': { path: 'pages/stream-config.html', name: '选科配置', batch: '2-7' },
    'PAGE-STR-STUDENT': { path: 'pages/stream-selection.html', name: '学生选科', batch: '2-7' },
    'PAGE-STR-STAT': { path: 'pages/stream-stat.html', name: '组合分布统计', batch: '2-7' },
    'PAGE-STR-APPROVE': { path: 'pages/stream-approve.html', name: '变更审批待办', batch: '2-7' },
    'PAGE-STR-GEN-CLASS': { path: 'pages/stream-generate-class.html', name: '按组合生成教学班', batch: '2-7' },
    'PAGE-CLS-TEACHING': { path: 'pages/teaching-class-list.html', name: '教学班管理', batch: '2-7' },
    'PAGE-AUDIT-LOG-LIST': { path: 'pages/audit-log-list.html', name: '操作日志', batch: '2-8' },
    'PAGE-AUDIT-OPS-ACCESS': { path: 'pages/audit-ops-access.html', name: '运营访问记录', batch: '2-8' },
    'PAGE-AUDIT-SENSITIVE-ACCESS': { path: 'pages/audit-sensitive-access.html', name: '敏感数据访问记录', batch: '2-8' },
    'PAGE-AUDIT-SECURITY-EVENT': { path: 'pages/audit-security-event.html', name: '登录与安全事件', batch: '2-8' },
    'PAGE-AUDIT-ARCHIVE': { path: 'pages/audit-archive.html', name: '归档管理', batch: '2-8' },
    'PAGE-IMP-DEADLETTER': { path: 'pages/dead-letter-task.html', name: '死信任务', batch: '2-9' }
  };
  Object.keys(EXTRA_PAGES).forEach(function (id) {
    PAGE_NAME[id] = EXTRA_PAGES[id].name;
    PAGE_BATCH[id] = EXTRA_PAGES[id].batch;
  });

  // 已在 navigation.yaml 注册、但本批尚未交付的页面：只为把提示文案里的页面编号换成中文名，
  // 不改变交付状态判定（delivered 仍然只由 MENUS / EXTRA_PAGES 决定）。
  var PENDING_PAGES = {
    'PAGE-GRD-DETAIL': { name: '年级详情', batch: '2-2' },
    'PAGE-GRD-CREATE': { name: '新建 / 编辑年级', batch: '2-2' },
    'PAGE-GRD-BATCH': { name: '按学段批量生成', batch: '2-2' },
    'PAGE-GRD-LEADER': { name: '指定年级主任', batch: '2-2' },
    'PAGE-GRD-ARCHIVE': { name: '归档确认', batch: '2-2' },
    // 学生 / 教师模块的浮层页面：已交付的片段由外壳自动识别，这里只补中文名，
    // 让"未交付的详情仍然走提示"这条路径读起来是页面名而不是页面编号
    'PAGE-STU-CREATE': { name: '新增学生', batch: '2-1' },
    'PAGE-STU-EDIT': { name: '编辑学生', batch: '2-1' },
    'PAGE-STU-DETAIL': { name: '学生详情', batch: '2-5' },
    'PAGE-STU-STATUS': { name: '学籍异动', batch: '2-5' },
    'PAGE-STU-TRANSFER': { name: '调班', batch: '2-5' },
    'PAGE-TCH-DETAIL': { name: '教师详情', batch: '2-2' },
    'PAGE-TCH-CREATE': { name: '新增教师', batch: '2-2' },
    'PAGE-TCH-EDIT': { name: '编辑教师', batch: '2-2' },
    'PAGE-TCH-ROLE': { name: '教育角色分配', batch: '2-2' },
    // 班级与升班模块（批次 2-3，小批 2-3b ~ 2-3e 交付）
    'PAGE-CLS-CREATE': { name: '新建 / 编辑班级', batch: '2-3' },
    'PAGE-CLS-BATCH': { name: '批量生成班级', batch: '2-3' },
    'PAGE-CLS-ROSTER-ADD': { name: '添加学生', batch: '2-3' },
    'PAGE-CLS-TRANSFER': { name: '调班', batch: '2-3' },
    'PAGE-CLS-LEADER': { name: '指定 / 变更班主任', batch: '2-3' },
    'PAGE-CLS-COPY': { name: '复制班级', batch: '2-3' },
    'PAGE-CLS-MOVE': { name: '批量迁学生', batch: '2-3' },
    'PAGE-PRM-ADJUST': { name: '调整学生去向', batch: '2-3' },
    'PAGE-PRM-CHANGE': { name: '学籍异动登记', batch: '2-5' },
    'PAGE-PRM-TRANSFER': { name: '跨校转学', batch: '2-5' }
  };
  Object.keys(PENDING_PAGES).forEach(function (id) {
    if (!PAGE_NAME[id]) PAGE_NAME[id] = PENDING_PAGES[id].name;
    if (!PAGE_BATCH[id]) PAGE_BATCH[id] = PENDING_PAGES[id].batch;
  });

  var state = { role: 'academic_director', state: 'normal', panels: [] };
  var base = '.';

  /* ---------------------------------------------------------------- 工具 */

  function el(tag, className, text) {
    var node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null) node.textContent = text;
    return node;
  }

  function roleLabel(code) {
    for (var i = 0; i < ROLES.length; i++) if (ROLES[i].code === code) return ROLES[i];
    return { code: code, label: code, persona: '' };
  }

  function toast(message, type) {
    var host = document.querySelector('.toast-host');
    if (!host) return;
    var node = el('div', 'toast ' + (type || 'info'), message);
    host.appendChild(node);
    window.setTimeout(function () {
      node.style.transition = 'opacity .3s';
      node.style.opacity = '0';
      window.setTimeout(function () { node.remove(); }, 320);
    }, 3600);
  }

  /* ---------------------------------------------------------------- 外壳 */

  function buildSidebar(activePage) {
    var aside = el('aside', 'shell-sidebar');
    var logo = el('div', 'shell-logo');
    logo.appendChild(el('span', 'mark', 'K'));
    logo.appendChild(el('span', null, 'K12 教育平台'));
    aside.appendChild(logo);
    MENUS.forEach(function (group) {
      aside.appendChild(el('div', 'menu-group-title', group.group));
      group.items.forEach(function (item) {
        var node = el('div', 'menu-item' + (item.id === activePage ? ' active' : '') + (item.delivered ? '' : ' pending'));
        node.setAttribute('data-role', 'nav');
        node.setAttribute('data-page-nav', item.id);
        node.appendChild(el('span', null, item.name));
        node.appendChild(el('span', 'batch-flag', item.delivered ? '本批' : item.batch));
        node.addEventListener('click', function () {
          if (item.delivered) {
            window.location.href = base + '/' + item.delivered;
          } else {
            toast('「' + item.name + '」在批次 ' + item.batch + ' 交付（' + item.id + '），本批未提供页面。', 'warning');
          }
        });
        aside.appendChild(node);
      });
    });
    return aside;
  }

  function buildNavbar(meta) {
    var bar = el('header', 'shell-navbar');
    var collapse = el('button', 'nav-collapse', '☰');
    collapse.title = '折叠侧边菜单';
    collapse.addEventListener('click', function () {
      var sidebar = document.querySelector('.shell-sidebar');
      if (!sidebar) return;
      var collapsed = sidebar.style.display === 'none';
      sidebar.style.display = collapsed ? '' : 'none';
      toast(collapsed ? '已展开侧边菜单' : '已折叠侧边菜单', 'info');
    });
    bar.appendChild(collapse);

    var crumb = el('nav', 'breadcrumb');
    var parts = (meta.breadcrumb || '').split('/');
    parts.forEach(function (part, index) {
      if (index > 0) crumb.appendChild(el('i', null, '/'));
      var last = index === parts.length - 1;
      crumb.appendChild(el(last ? 'b' : 'span', null, part.trim()));
    });
    bar.appendChild(crumb);

    var right = el('div', 'nav-right');
    var tenant = el('span', 'nav-chip');
    tenant.appendChild(el('span', 'dot'));
    tenant.appendChild(el('span', null, meta.tenant || '云溪实验学校'));
    tenant.appendChild(el('span', null, '▾'));
    tenant.title = '租户 / 学校切换（仅多校权限角色可用）';
    right.appendChild(tenant);

    var todo = el('span', 'nav-chip');
    todo.appendChild(el('span', null, '待办'));
    var badge = el('span', 'el-tag warning', '2');
    todo.appendChild(badge);
    todo.title = '选科逾期变更 1 条、导入校验待执行 1 条';
    right.appendChild(todo);

    var user = el('span', 'nav-user');
    user.appendChild(el('span', 'avatar', '·'));
    var userName = el('span', null, '');
    userName.setAttribute('data-role', 'nav-user-name');
    user.appendChild(userName);
    right.appendChild(user);
    bar.appendChild(right);
    return bar;
  }

  function buildTabs(meta) {
    var wrap = el('div', 'shell-tags');
    var home = el('span', 'tag-tab');
    home.appendChild(el('span', null, '原型入口'));
    home.addEventListener('click', function () { window.location.href = base + '/index.html'; });
    wrap.appendChild(home);

    var tab = el('span', 'tag-tab active');
    tab.appendChild(el('span', null, meta.tab || '页面'));
    var close = el('span', 'close', '✕');
    close.addEventListener('click', function () {
      toast('原型演示中页签不可关闭；真实系统里页签右键支持"关闭其他 / 关闭全部"。', 'info');
    });
    tab.appendChild(close);
    wrap.appendChild(tab);
    return wrap;
  }

  function buildDemoPanel() {
    var panel = el('div', 'demo-panel');
    panel.setAttribute('data-demo', 'role-switcher');
    var head = el('header');
    head.appendChild(el('span', null, '原型演示'));
    head.appendChild(el('span', 'tag', '展开'));
    panel.appendChild(head);

    var body = el('div', 'panel-body');
    body.appendChild(el('div', 'panel-label', '角色形态'));
    var roleRow = el('div', 'panel-row');
    roleRow.setAttribute('data-demo', 'role-switcher');
    roleRow.setAttribute('data-demo-target', 'role');
    ROLES.forEach(function (role) {
      var btn = el('button', 'pbtn' + (role.code === state.role ? ' on' : ''), role.label);
      btn.addEventListener('click', function () {
        applyRole(role.code);
        toast('已切换为「' + role.label + ' · ' + role.persona + '」，按钮与字段按该角色重新渲染。', 'info');
      });
      roleRow.appendChild(btn);
    });
    body.appendChild(roleRow);

    body.appendChild(el('div', 'panel-label', '页面状态'));
    var stateRow = el('div', 'panel-row');
    stateRow.setAttribute('data-demo', 'state-switcher');
    stateRow.setAttribute('data-demo-target', 'state');
    STATES.forEach(function (item) {
      var btn = el('button', 'pbtn' + (item.code === state.state ? ' on' : ''), item.label);
      btn.addEventListener('click', function () { applyState(item.code); });
      stateRow.appendChild(btn);
    });
    body.appendChild(stateRow);

    body.appendChild(el('div', 'panel-label', '异常与反馈形态'));
    var errorRow = el('div', 'panel-row');
    errorRow.setAttribute('data-demo', 'error-samples');
    [
      { label: '唯一性冲突', text: '证件号码已存在（330102********1234），请核对后重新提交。', type: 'error' },
      { label: '并发冲突', text: '该记录已被 邓丽娟 于 2026-09-30 10:12 更新，请刷新后重试。', type: 'error' },
      { label: '无权限', text: '你没有该操作的权限（person.student:create）。如需开通，请联系本校管理员。', type: 'error' },
      { label: '服务异常', text: '处理失败，请稍后重试。请求编号 REQ-20260930-000123。', type: 'error' },
      { label: '部分失败', text: '共 120 行，成功 118 行，失败 2 行。失败明细可下载。', type: 'warning' }
    ].forEach(function (item) {
      var btn = el('button', 'pbtn', item.label);
      btn.addEventListener('click', function () { toast(item.text, item.type); });
      errorRow.appendChild(btn);
    });
    body.appendChild(errorRow);

    body.appendChild(el('div', 'panel-hint', '提示：角色决定按钮与字段可见性；状态决定表格区域的形态，两者都会影响截图。'));
    body.style.display = 'none';
    panel.appendChild(body);
    head.style.cursor = 'pointer';
    head.title = '点标题栏展开或收起演示控件';
    head.addEventListener('click', function () {
      var expanded = body.style.display !== 'none';
      body.style.display = expanded ? 'none' : 'block';
      head.querySelector('.tag').textContent = expanded ? '展开' : '收起';
    });
    return panel;
  }

  /* ---------------------------------------------------------------- 角色与状态 */

  function applyRole(code) {
    state.role = code;
    var meta = roleLabel(code);
    document.body.setAttribute('data-current-role', code);

    // 超级管理员不受功能权限与字段可编辑性限制（BR-ORG-014）：
    // 所有 data-role-visible / data-role-editable / data-role-enabled 一律放行。
    var isSuperAdmin = code === 'super_admin';

    document.querySelectorAll('[data-role-visible]').forEach(function (node) {
      var allowed = node.getAttribute('data-role-visible').split(',').map(function (s) { return s.trim(); });
      node.classList.toggle('role-hidden', !isSuperAdmin && allowed.indexOf(code) === -1);
    });

    document.querySelectorAll('[data-role-editable]').forEach(function (node) {
      var editable = node.getAttribute('data-role-editable').split(',').map(function (s) { return s.trim(); });
      var can = isSuperAdmin || editable.indexOf(code) !== -1;
      node.classList.toggle('role-readonly', !can);
      node.querySelectorAll('input, select, textarea').forEach(function (input) {
        if (input.hasAttribute('data-keep-enabled')) return;
        input.disabled = !can;
        if (input.tagName === 'SELECT') input.setAttribute('data-locked-by-role', String(!can));
      });
    });

    // 控件可用性按角色控制（如"学校"筛选只有平台运营可切换）
    document.querySelectorAll('[data-role-enabled]').forEach(function (node) {
      var allowed = node.getAttribute('data-role-enabled').split(',').map(function (s) { return s.trim(); });
      var enabled = isSuperAdmin || allowed.indexOf(code) !== -1;
      node.disabled = !enabled;
      node.setAttribute('data-locked-by-role', String(!enabled));
    });

    document.querySelectorAll('[data-demo="role-switcher"] .pbtn').forEach(function (btn) {
      var label = meta.label;
      btn.classList.toggle('on', btn.textContent === label);
    });

    var nameNode = document.querySelector('[data-role="nav-user-name"]');
    if (nameNode) nameNode.textContent = meta.persona + '（' + meta.label + '）';
  }

  function applyState(code) {
    state.state = code;
    document.querySelectorAll('[data-demo-state-panel]').forEach(function (node) {
      node.classList.toggle('state-hidden', node.getAttribute('data-demo-state-panel') !== code);
    });
    document.querySelectorAll('[data-normal-view]').forEach(function (node) {
      node.classList.toggle('state-hidden', code !== 'normal' && code !== 'submitting');
    });
    document.querySelectorAll('[data-demo="state-switcher"] .pbtn').forEach(function (btn) {
      var label = (STATES.filter(function (s) { return s.code === code; })[0] || {}).label;
      btn.classList.toggle('on', btn.textContent === label);
    });

    document.querySelectorAll('[data-demo-state-target="toolbar"] .btn').forEach(function (btn) {
      if (code === 'submitting' && btn.hasAttribute('data-api')) {
        btn.classList.add('loading', 'is-disabled');
        btn.setAttribute('disabled', 'disabled');
      } else {
        btn.classList.remove('loading', 'is-disabled');
        btn.removeAttribute('disabled');
      }
    });

    if (code === 'submitting') {
      toast('已进入「提交中」：按钮置 loading 并禁用，禁止重复提交。', 'info');
    }
  }

  /* ---------------------------------------------------------------- 浮层 */

  function openPanel(pageId) {
    var panel = document.querySelector('[data-demo-panel="' + pageId + '"]');
    if (!panel) {
      toast('「' + (PAGE_NAME[pageId] || pageId) + '」在本批未提供页面片段（原型按批交付），入口与权限显隐仍可验收。', 'warning');
      return;
    }
    if (panel.classList.contains('open')) return; // 深链接重复触发时不重复入栈
    panel.classList.add('open');
    state.panels.push(pageId);
    var focusable = panel.querySelector('input, select, textarea, button');
    if (focusable) window.setTimeout(function () { focusable.focus(); }, 60);
  }

  function closePanel(pageId) {
    if (pageId === 'all') {
      var opened = document.querySelectorAll('.overlay.open');
      var last = state.panels[0];
      opened.forEach(function (node) { node.classList.remove('open'); });
      state.panels = [];
      if (last) toast('已关闭「' + (PAGE_NAME[last] || last) + '」，本次填写内容未保存。', 'info');
      return;
    }
    var target = pageId
      ? document.querySelector('[data-demo-panel="' + pageId + '"]')
      : document.querySelector('.overlay.open');
    if (!target) return;
    target.classList.remove('open');
    state.panels = state.panels.filter(function (id) { return id !== target.getAttribute('data-demo-panel'); });
    // 关闭浮层后回到来源页面：列表页未卸载，筛选条件与页码自然保留
    var firstField = document.querySelector('#page-root [data-role="filter"] input, #page-root [data-role="filter"] select');
    if (firstField) firstField.focus();
  }

  /* ---------------------------------------------------------------- 交互委托 */

  function simulateApi(node) {
    var text = node.textContent;
    node.classList.add('loading');
    node.setAttribute('disabled', 'disabled');
    node.textContent = '处理中';
    window.setTimeout(function () {
      node.classList.remove('loading');
      node.removeAttribute('disabled');
      node.textContent = text;
      var outcome = node.getAttribute('data-demo-outcome') || 'success';
      var message = node.getAttribute('data-demo-message');
      toast(message || '操作已完成。', outcome === 'error' ? 'error' : outcome === 'partial' ? 'warning' : 'success');
      if (outcome === 'success' && node.hasAttribute('data-after-success')) {
        var action = node.getAttribute('data-after-success');
        if (action === 'close-panel') closePanel();
      }
    }, 1000);
  }

  function validateForm(scope) {
    var ok = true;
    var firstBad = null;
    scope.querySelectorAll('[data-required], [data-validate]').forEach(function (input) {
      var wrapper = input.closest('.field') || input.closest('.form-item');
      var value = (input.value || '').trim();
      var message = '';
      if (input.hasAttribute('data-required') && !value) message = input.getAttribute('data-required-message') || '该项为必填';
      if (!message && value && input.hasAttribute('data-validate')) {
        var rule = input.getAttribute('data-validate');
        if (rule === 'phone11' && !/^1\d{10}$/.test(value)) message = '手机号格式不正确';
        if (rule === 'code32' && !/^[A-Za-z0-9-]{2,32}$/.test(value)) message = '长度须为 2–32 位字母、数字或连字符';
        if (rule === 'nation-student-no' && !/^[GL]\d+$/.test(value)) message = '全国学籍号须以 G 或 L 开头';
        if (rule === 'year4' && !/^(19|20)\d{2}$/.test(value)) message = '入学年份不合法';
      }
      if (wrapper) {
        wrapper.classList.toggle('has-error', Boolean(message));
        var errNode = wrapper.querySelector('.error-text');
        if (errNode && message) errNode.textContent = message;
      }
      if (message) {
        ok = false;
        if (!firstBad) firstBad = input;
      }
    });
    if (!ok) {
      // 校验汇总通常放在步骤内容之外（步骤条下方），因此到整个浮层里找，而不是只看当前步骤
      var summaryScope = scope.closest('[data-demo-panel]') || scope;
      var summary = summaryScope.querySelector('[data-validate-summary]');
      if (summary) summary.classList.remove('state-hidden');
      if (firstBad) firstBad.focus();
    } else {
      // 校验通过时收起上一次的汇总，避免"已经改好了但提示还挂着"
      var okScope = scope.closest('[data-demo-panel]') || scope;
      var okSummary = okScope.querySelector('[data-validate-summary]');
      if (okSummary) okSummary.classList.add('state-hidden');
    }
    return ok;
  }

  function setStep(scope, index) {
    var steps = scope.querySelectorAll('[data-step-content]');
    var max = steps.length;
    if (index < 1) index = 1;
    if (index > max) index = max;
    scope.setAttribute('data-current-step', String(index));
    steps.forEach(function (node) {
      node.classList.toggle('state-hidden', Number(node.getAttribute('data-step-content')) !== index);
    });
    scope.querySelectorAll('.steps .step').forEach(function (node, i) {
      var order = i + 1;
      node.classList.remove('on', 'done');
      if (order < index) node.classList.add('done');
      if (order === index) node.classList.add('on');
    });
    scope.querySelectorAll('[data-step-btn="prev"]').forEach(function (btn) { btn.toggleAttribute('disabled', index === 1); });
    scope.querySelectorAll('[data-step-btn="next"]').forEach(function (btn) { btn.classList.toggle('state-hidden', index === max); });
    scope.querySelectorAll('[data-step-btn="save"]').forEach(function (btn) { btn.classList.toggle('state-hidden', index !== max); });
  }

  function onDocumentClick(event) {
    var node = event.target;

    var closer = node.closest('[data-close-panel]');
    if (closer) {
      var value = closer.getAttribute('data-close-panel');
      closePanel(value === 'self' ? null : value || null);
      return;
    }

    var stepper = node.closest('[data-step-btn]');
    if (stepper) {
      var scope = stepper.closest('[data-demo-panel]');
      var current = Number(scope.getAttribute('data-current-step') || '1');
      var kind = stepper.getAttribute('data-step-btn');
      if (kind === 'prev') {
        setStep(scope, current - 1);
        return;
      }
      if (kind === 'next') {
        var active = scope.querySelector('[data-step-content="' + current + '"]');
        if (validateForm(active)) setStep(scope, current + 1);
        return;
      }
      // kind === 'save' 时不返回，继续走 data-api 的提交模拟分支
    }

    var demoBtn = node.closest('[data-demo-toast]');
    if (demoBtn) {
      toast(demoBtn.getAttribute('data-demo-toast'), demoBtn.getAttribute('data-demo-toast-type') || 'info');
      return;
    }

    // 只有"动作元素"才把 data-api 当作触发点。
    // 表格、卡片上的 data-api 只是接口标注（markup-contract 第 2 节要求 table 带 data-api），
    // 若把容器也算触发点，点击行、行内按钮、复选框都会被误判为"调接口"。
    var apiNode = node.closest('[data-api]');
    var apiTrigger = apiNode && (apiNode.tagName === 'BUTTON' || apiNode.tagName === 'A' ||
      apiNode.getAttribute('data-role') === 'action');
    if (apiTrigger && !apiNode.classList.contains('is-disabled')) {
      var host = apiNode.closest('[data-demo-panel]');
      if (host && apiNode.hasAttribute('data-validate-on-submit')) {
        var stepNode = host.querySelector('[data-step-content]:not(.state-hidden)') || host;
        if (!validateForm(stepNode)) return;
      }
      simulateApi(apiNode);
      return;
    }

    // 落在表单控件上的点击（复选框 / 下拉 / 输入框 / label）不触发所在行或卡片的跳转
    var navNode = node.closest('[data-nav]');
    if (navNode && !node.closest('input, select, textarea, label')) {
      var pageId = navNode.getAttribute('data-nav');
      var overlay = navNode.getAttribute('data-overlay');
      // 未显式标注 data-overlay 时：只要当前文档里存在该页面片段，就按浮层打开。
      // 这样数据行只写 data-nav="…-DETAIL" 就能打开详情抽屉，不需要逐行补 data-overlay，
      // 也不会再把"本页已有片段"误判成"后续批次交付"。
      if (!overlay && document.querySelector('[data-demo-panel="' + pageId + '"]')) overlay = 'drawer';
      if (overlay === 'drawer' || overlay === 'dialog' || overlay === 'block') {
        openPanel(pageId);
        return;
      }
      var delivered = null;
      if (EXTRA_PAGES[pageId]) delivered = EXTRA_PAGES[pageId].path;
      if (!delivered) {
        MENUS.forEach(function (g) {
          g.items.forEach(function (it) { if (it.id === pageId && it.delivered) delivered = it.delivered; });
        });
      }
      if (delivered) {
        window.location.href = base + '/' + delivered;
      } else {
        toast(PAGE_NAME[pageId]
          ? '「' + PAGE_NAME[pageId] + '」在批次 ' + PAGE_BATCH[pageId] + ' 交付（' + pageId + '），本批只验证入口与权限显隐。'
          : '「' + pageId + '」在后续批次交付，本批只验证入口与权限显隐。', 'warning');
      }
      return;
    }

    if (node.classList && node.classList.contains('overlay')) closePanel();
  }

  function onKeydown(event) {
    if (event.key !== 'Escape') return;
    var open = document.querySelector('.overlay.open');
    if (open) closePanel();
  }

  /* ---------------------------------------------------------------- 深链接参数 */

  // 支持 #role=homeroom&state=empty&panel=PAGE-STU-CREATE，也支持 ?role=... 形式。
  // 页面自己的筛选参数（如 school / enroll / stage）由页面监听 prototype:params 事件处理。
  function parseParams() {
    var raw = (window.location.search.replace(/^\?/, '') + '&' + window.location.hash.replace(/^#/, '')).replace(/^&|&$/g, '');
    var params = {};
    raw.split('&').forEach(function (pair) {
      var kv = pair.split('=');
      if (kv[0]) params[kv[0]] = decodeURIComponent(kv[1] || '');
    });
    return params;
  }

  function applyParams(params) {
    if (params.role) applyRole(params.role);
    if (params.state) applyState(params.state);
    if (params.panel) openPanel(params.panel);
    // 交给页面处理自己的参数（筛选值等）。放在最后，保证页面看到的是已应用角色 / 状态之后的局面。
    document.dispatchEvent(new CustomEvent('prototype:params', { detail: params }));
  }

  /* ---------------------------------------------------------------- 初始化 */

  function init() {
    var body = document.body;
    base = body.getAttribute('data-base') || '.';
    // 页面自身的编号用于 `data-page` 回查；左菜单高亮允许单独指定（如独立的任教关系页高亮"教师管理"）
    var activePage = body.getAttribute('data-menu-page') || body.getAttribute('data-page-id') || '';
    var meta = {
      breadcrumb: body.getAttribute('data-breadcrumb') || '',
      tab: body.getAttribute('data-tab') || '',
      tenant: body.getAttribute('data-tenant') || '云溪实验学校（云溪教育集团）'
    };

    var mount = document.querySelector('#app-shell');
    var root = document.querySelector('#page-root');
    if (!mount || !root) return;

    var shell = el('div', 'shell');
    shell.appendChild(buildSidebar(activePage));
    var main = el('div', 'shell-main');
    main.appendChild(buildNavbar(meta));
    main.appendChild(buildTabs(meta));
    var content = el('main', 'shell-content');
    content.appendChild(root);
    main.appendChild(content);
    shell.appendChild(main);
    mount.appendChild(shell);

    var toastHost = el('div', 'toast-host');
    toastHost.setAttribute('data-demo', 'toast');
    document.body.appendChild(toastHost);
    document.body.appendChild(buildDemoPanel());

    applyRole(state.role);
    applyState('normal');
    // 深链接：便于截图与评审时直接定位到指定角色 / 状态 / 浮层
    applyParams(parseParams());
    // 在已打开的页面上直接改地址栏 hash 也应当生效（同文档导航不会重新加载页面）
    window.addEventListener('hashchange', function () { applyParams(parseParams()); });
    // 列级显隐：表头登记 data-col-hide-role，同一列的数据单元格自动继承
    document.querySelectorAll('table.el-table').forEach(function (table) {
      Array.prototype.forEach.call(table.querySelectorAll('thead th'), function (th, index) {
        var hide = th.getAttribute('data-col-hide-role');
        if (!hide) return;
        Array.prototype.forEach.call(table.querySelectorAll('tbody tr'), function (tr) {
          var td = tr.children[index];
          if (td) td.setAttribute('data-col-hide-role', hide);
        });
      });
    });
    document.addEventListener('click', onDocumentClick);
    document.addEventListener('keydown', onKeydown);

    // 浮层片段可能挂在 #page-root 之外（列表页的抽屉与弹窗），因此按整篇文档初始化步骤条
    document.querySelectorAll('[data-demo-panel]').forEach(function (panel) {
      if (panel.querySelector('[data-step-content]')) setStep(panel, 1);
    });
  }

  window.PrototypeShell = {
    roles: ROLES,
    init: init,
    toast: toast,
    applyRole: applyRole,
    applyState: applyState,
    openPanel: openPanel,
    closePanel: closePanel,
    parseParams: parseParams,
    applyParams: applyParams,
    // 深链接与截图辅助：把某个浮层内的步骤条直接切到第 index 步（向导类页面复用）
    step: function (pageId, index) {
      var panel = document.querySelector('[data-demo-panel="' + pageId + '"]');
      if (panel && panel.querySelector('[data-step-content]')) setStep(panel, index);
    },
    currentRole: function () { return state.role; },
    currentState: function () { return state.state; }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
