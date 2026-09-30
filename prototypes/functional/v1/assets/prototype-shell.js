/* 业务原型演示引擎（阶段 2）
   依据：markup-contract.md 第 10 节（原型可交互约定）、layout-spec.yaml 的 system_shell
   职责：注入完整外壳、角色切换、状态切换、浮层打开/关闭、提交模拟、表单校验、轻提示。
   约束：不引第三方库、不访问后端、不依赖构建；页面级差异仍写在每个页面底部的内联 script 里。 */
(function () {
  'use strict';

  /* ---------------------------------------------------------------- 静态定义 */

  // 与 content-samples.json 的 prototype_roles 保持一致
  var ROLES = [
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
        { id: 'PAGE-TCH-LIST', name: '教师管理', batch: '2-2' },
        { id: 'PAGE-CLS-LIST', name: '班级管理', batch: '2-3' },
        { id: 'PAGE-GRD-LIST', name: '年级管理', batch: '2-2' },
        { id: 'PAGE-PRM-LIST', name: '升班与学籍', batch: '2-3' },
        { id: 'PAGE-STR-LIST', name: '选科与教学班', batch: '2-7' }
      ]
    },
    {
      group: '组织与配置',
      items: [
        { id: 'PAGE-SCH-LIST', name: '学校管理', batch: '2-6' },
        { id: 'PAGE-TERM-LIST', name: '学年学期', batch: '2-6' },
        { id: 'PAGE-SUB-LIST', name: '学科与配置', batch: '2-6' }
      ]
    },
    {
      group: '平台与运维',
      items: [
        { id: 'PAGE-IMP-WIZARD', name: '导入导出', batch: '2-4' },
        { id: 'PAGE-IMP-TASK-LIST', name: '异步任务', batch: '2-9' },
        { id: 'PAGE-AUDIT-LOG-LIST', name: '审计日志', batch: '2-8' }
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

    document.querySelectorAll('[data-role-visible]').forEach(function (node) {
      var allowed = node.getAttribute('data-role-visible').split(',').map(function (s) { return s.trim(); });
      node.classList.toggle('role-hidden', allowed.indexOf(code) === -1);
    });

    document.querySelectorAll('[data-role-editable]').forEach(function (node) {
      var editable = node.getAttribute('data-role-editable').split(',').map(function (s) { return s.trim(); });
      var can = editable.indexOf(code) !== -1;
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
      node.disabled = allowed.indexOf(code) === -1;
      node.setAttribute('data-locked-by-role', String(allowed.indexOf(code) === -1));
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
      toast('未找到页面片段 ' + pageId, 'error');
      return;
    }
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
      var summary = scope.querySelector('[data-validate-summary]');
      if (summary) summary.classList.remove('state-hidden');
      if (firstBad) firstBad.focus();
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

    var apiNode = node.closest('[data-api]');
    if (apiNode && !apiNode.classList.contains('is-disabled')) {
      var host = apiNode.closest('[data-demo-panel]');
      if (host && apiNode.hasAttribute('data-validate-on-submit')) {
        var stepNode = host.querySelector('[data-step-content]:not(.state-hidden)') || host;
        if (!validateForm(stepNode)) return;
      }
      simulateApi(apiNode);
      return;
    }

    var navNode = node.closest('[data-nav]');
    if (navNode) {
      var pageId = navNode.getAttribute('data-nav');
      var overlay = navNode.getAttribute('data-overlay');
      if (overlay === 'drawer' || overlay === 'dialog' || overlay === 'block') {
        openPanel(pageId);
        return;
      }
      var delivered = null;
      MENUS.forEach(function (g) {
        g.items.forEach(function (it) { if (it.id === pageId && it.delivered) delivered = it.delivered; });
      });
      if (delivered) {
        window.location.href = base + '/' + delivered;
      } else {
        toast('「' + (PAGE_NAME[pageId] || pageId) + '」在批次 ' + (PAGE_BATCH[pageId] || '后续') + ' 交付（' + pageId + '），本批只验证入口与权限显隐。', 'warning');
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

  /* ---------------------------------------------------------------- 初始化 */

  function init() {
    var body = document.body;
    base = body.getAttribute('data-base') || '.';
    var activePage = body.getAttribute('data-page-id') || '';
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
    // 深链接：支持 #role=homeroom&state=empty&panel=PAGE-STU-CREATE（也支持 ?role=... 形式），
    // 便于截图与评审时直接定位到指定角色 / 状态 / 浮层
    var raw = (window.location.search.replace(/^\?/, '') + '&' + window.location.hash.replace(/^#/, '')).replace(/^&|&$/g, '');
    var params = {};
    raw.split('&').forEach(function (pair) {
      var kv = pair.split('=');
      if (kv[0]) params[kv[0]] = decodeURIComponent(kv[1] || '');
    });
    if (params.role) applyRole(params.role);
    if (params.state) applyState(params.state);
    if (params.panel) openPanel(params.panel);
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

    root.querySelectorAll('[data-demo-panel]').forEach(function (panel) {
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
    currentRole: function () { return state.role; },
    currentState: function () { return state.state; }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
