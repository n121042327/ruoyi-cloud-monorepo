/* ===========================================================================
   阶段 3 高保真原型外壳（prototypes/high-fidelity/v1）
   - 与阶段 2 的 prototype-shell.js 同构：同样的 data-* 约定与同一个 API 形状，
     阶段 6 可用同一套映射规则把两者一起替换成 Element Plus 组件
   - 不引第三方库、不需构建；纯静态 HTML 直接打开即可
   - 暴露 window.HiFiShell（与阶段 2 的 PrototypeShell 方法名一致，便于复用 harness）
   =========================================================================== */
(function () {
  'use strict';

  var ROLES = [
    { code: 'super_admin', label: '超级管理员', persona: '系统内置账号' },
    { code: 'tenant_admin', label: '租户管理员', persona: '钱嘉禾' },
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

  // 与阶段 2 的 navigation.yaml 菜单结构一致；delivered 为高保真阶段已交付页面
  var MENUS = [
    {
      group: '教育管理', items: [
        { id: 'PAGE-STU-LIST', name: '学生管理', batch: '3-1', delivered: 'pages/student-list.html' },
        { id: 'PAGE-TCH-LIST', name: '教师管理', batch: '3-2', delivered: 'pages/teacher-list.html' },
        { id: 'PAGE-CLS-LIST', name: '班级管理', batch: '3-3', delivered: 'pages/class-list.html' },
        { id: 'PAGE-GRD-LIST', name: '年级管理', batch: '3-2', delivered: 'pages/grade-list.html' },
        { id: 'PAGE-PRM-LIST', name: '升班与学籍', batch: '3-3', delivered: 'pages/promotion-list.html' },
        { id: 'PAGE-STR-LIST', name: '选科与教学班', batch: '3-7', delivered: 'pages/stream-list.html' }
      ]
    },
    {
      group: '组织与配置', items: [
        { id: 'PAGE-SCH-LIST', name: '学校管理', batch: '3-6', delivered: 'pages/school-list.html' },
        { id: 'PAGE-TERM-LIST', name: '学年学期', batch: '3-6', delivered: 'pages/term-list.html' },
        { id: 'PAGE-SUB-LIST', name: '学科与配置', batch: '3-6', delivered: 'pages/subject-list.html' }
      ]
    },
    {
      group: '平台与运维', items: [
        { id: 'PAGE-IMP-WIZARD', name: '导入导出', batch: '3-4', delivered: 'pages/import-wizard.html' },
        { id: 'PAGE-IMP-TASK-LIST', name: '异步任务', batch: '3-9', delivered: 'pages/async-task-list.html' },
        { id: 'PAGE-AUDIT-LOG-LIST', name: '审计日志', batch: '3-8', delivered: 'pages/audit-log-list.html' }
      ]
    }
  ];

  var state = { role: 'academic_director', stateCode: 'normal', panels: [] };
  var base = '..';

  function el(tag, cls, text) {
    var node = document.createElement(tag);
    if (cls) node.className = cls;
    if (text !== undefined && text !== null) node.textContent = text;
    return node;
  }
  function roleMeta(code) { return ROLES.filter(function (r) { return r.code === code; })[0] || { label: code, persona: code }; }
  function stateMeta(code) { return STATES.filter(function (s) { return s.code === code; })[0] || { label: code }; }

  function parseParams() {
    var raw = (window.location.search.replace(/^\?/, '') + '&' + window.location.hash.replace(/^#/, '')).replace(/^&|&$/g, '');
    var params = {};
    raw.split('&').forEach(function (pair) {
      var kv = pair.split('=');
      if (kv[0]) params[kv[0]] = decodeURIComponent(kv[1] || '');
    });
    return params;
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

  function applyRole(code) {
    state.role = code;
    var meta = roleMeta(code);
    document.body.setAttribute('data-current-role', code);
    var isSuper = code === 'super_admin';
    document.querySelectorAll('[data-role-visible]').forEach(function (node) {
      var allowed = node.getAttribute('data-role-visible').split(',').map(function (s) { return s.trim(); });
      node.classList.toggle('role-hidden', !isSuper && allowed.indexOf(code) === -1);
    });
    document.querySelectorAll('[data-role-editable]').forEach(function (node) {
      var editable = node.getAttribute('data-role-editable').split(',').map(function (s) { return s.trim(); });
      var can = isSuper || editable.indexOf(code) !== -1;
      node.classList.toggle('role-readonly', !can);
      node.querySelectorAll('input, select, textarea').forEach(function (input) {
        if (input.hasAttribute('data-keep-enabled')) return;
        input.disabled = !can;
      });
    });
    document.querySelectorAll('[data-demo="role-switcher"] .pbtn').forEach(function (btn) {
      btn.classList.toggle('on', btn.textContent.trim() === meta.label);
    });
    var user = document.querySelector('.hi-user');
    if (user) user.innerHTML = '<span class="hi-avatar">' + meta.label.slice(0, 1) + '</span>' + meta.persona + '（' + meta.label + '）';
  }

  function applyState(code) {
    state.stateCode = code;
    document.querySelectorAll('[data-demo-state-panel]').forEach(function (node) {
      node.classList.toggle('state-hidden', node.getAttribute('data-demo-state-panel') !== code);
    });
    document.querySelectorAll('[data-normal-view]').forEach(function (node) {
      node.classList.toggle('state-hidden', code !== 'normal' && code !== 'submitting');
    });
    document.querySelectorAll('[data-demo="state-switcher"] .pbtn').forEach(function (btn) {
      btn.classList.toggle('on', btn.textContent.trim() === stateMeta(code).label);
    });
  }

  function openPanel(pageId) {
    var panel = document.querySelector('[data-demo-panel="' + pageId + '"]');
    if (!panel) { toast('「' + pageId + '」在高保真阶段尚未交付（本阶段按批交付）。', 'warning'); return; }
    if (panel.classList.contains('open')) return;
    panel.classList.add('open');
    state.panels.push(pageId);
    var focusable = panel.querySelector('input, select, textarea, button');
    if (focusable) window.setTimeout(function () { focusable.focus(); }, 60);
  }

  function closePanel(pageId) {
    if (pageId === 'all') {
      document.querySelectorAll('.overlay.open').forEach(function (n) { n.classList.remove('open'); });
      state.panels = [];
      return;
    }
    var panel = pageId
      ? document.querySelector('[data-demo-panel="' + pageId + '"]')
      : document.querySelector('.overlay.open');
    if (panel) panel.classList.remove('open');
    state.panels = state.panels.filter(function (p) { return p !== pageId; });
  }

  function validateForm(scope) {
    var ok = true;
    var firstBad = null;
    scope.querySelectorAll('[data-required], [data-validate]').forEach(function (input) {
      var wrapper = input.closest('.field');
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
      if (message) { ok = false; if (!firstBad) firstBad = input; }
    });
    var scopePanel = scope.closest('[data-demo-panel]') || scope;
    var summary = scopePanel.querySelector('[data-validate-summary]');
    if (summary) summary.classList.toggle('state-hidden', ok);
    if (!ok && firstBad) firstBad.focus();
    return ok;
  }

  function simulateApi(node) {
    var original = node.textContent;
    node.classList.add('loading');
    node.setAttribute('disabled', 'disabled');
    node.textContent = '处理中';
    window.setTimeout(function () {
      node.classList.remove('loading');
      node.removeAttribute('disabled');
      node.textContent = original;
      var outcome = node.getAttribute('data-demo-outcome') || 'success';
      var message = node.getAttribute('data-demo-message') || '操作已完成。';
      toast(message, outcome === 'error' ? 'error' : outcome === 'partial' ? 'warning' : 'success');
      if (outcome === 'success' && node.getAttribute('data-after-success') === 'close-panel') closePanel();
    }, 900);
  }

  function onDocumentClick(event) {
    var node = event.target;

    var closer = node.closest('[data-close-panel]');
    if (closer) { closePanel(closer.getAttribute('data-close-panel') || null); return; }

    var apiNode = node.closest('[data-api]');
    var apiTrigger = apiNode && (apiNode.tagName === 'BUTTON' || apiNode.tagName === 'A' ||
      apiNode.getAttribute('data-role') === 'action');
    if (apiTrigger && !apiNode.classList.contains('is-disabled')) {
      var host = apiNode.closest('[data-demo-panel]');
      if (host && apiNode.hasAttribute('data-validate-on-submit')) {
        if (!validateForm(host)) return;
      }
      simulateApi(apiNode);
      return;
    }

    var navNode = node.closest('[data-nav]');
    if (navNode && !node.closest('input, select, textarea, label')) {
      var pageId = navNode.getAttribute('data-nav');
      var overlay = navNode.getAttribute('data-overlay');
      if (!overlay && document.querySelector('[data-demo-panel="' + pageId + '"]')) overlay = 'drawer';
      if (overlay === 'drawer' || overlay === 'dialog' || overlay === 'block') { openPanel(pageId); return; }
      var target = document.querySelector('[data-delivery="' + pageId + '"]');
      if (target) { window.location.href = target.getAttribute('data-href'); return; }
      toast('「' + pageId + '」在高保真阶段尚未交付（本阶段按批交付），入口与权限显隐仍可验收。', 'warning');
    }
  }

  function onKeydown(event) { if (event.key === 'Escape') closePanel(); }

  function buildDemoPanel() {
    var panel = el('div', 'demo-panel');
    panel.setAttribute('data-demo', 'demo-panel');
    var label = el('div', 'panel-label');
    label.appendChild(el('span', null, '▸'));
    label.appendChild(el('span', null, '高保真演示 · 角色与状态（仅原型演示）'));
    label.addEventListener('click', function () {
      panel.classList.toggle('open');
      label.firstChild.textContent = panel.classList.contains('open') ? '▾' : '▸';
    });
    var body = el('div', 'panel-body');
    body.appendChild(el('div', 'panel-label', '角色'));
    var roleRow = el('div', 'panel-row');
    roleRow.setAttribute('data-demo', 'role-switcher');
    ROLES.forEach(function (r) {
      var b = el('button', 'pbtn', r.label);
      b.addEventListener('click', function () { applyRole(r.code); });
      roleRow.appendChild(b);
    });
    body.appendChild(roleRow);
    body.appendChild(el('div', 'panel-label', '页面状态'));
    var stateRow = el('div', 'panel-row');
    stateRow.setAttribute('data-demo', 'state-switcher');
    STATES.forEach(function (s) {
      var b = el('button', 'pbtn', s.label);
      b.addEventListener('click', function () { applyState(s.code); });
      stateRow.appendChild(b);
    });
    body.appendChild(stateRow);
    body.appendChild(el('div', 'panel-hint', '深链接：#role=school_leader #state=forbidden #panel=PAGE-STU-DETAIL'));
    panel.appendChild(label);
    panel.appendChild(body);
    return panel;
  }

  function buildSidebar(activePage) {
    var aside = el('aside', 'hi-sidebar');
    var logo = el('div', 'hi-logo');
    logo.appendChild(el('span', 'mark', 'K'));
    logo.appendChild(el('span', null, 'K12 教育平台'));
    aside.appendChild(logo);
    var menu = el('nav', 'hi-menu');
    MENUS.forEach(function (g) {
      menu.appendChild(el('div', 'hi-menu-group', g.group));
      g.items.forEach(function (item) {
        var node = el('div', 'hi-menu-item' + (item.id === activePage ? ' on' : ''));
        node.appendChild(el('span', null, item.name));
        node.appendChild(el('span', 'badge', item.delivered ? '高保真' : '3-2 起'));
        if (item.delivered) {
          node.setAttribute('data-delivery', item.id);
          node.setAttribute('data-href', base + '/' + item.delivered);
          node.addEventListener('click', function () { window.location.href = base + '/' + item.delivered; });
        } else {
          node.addEventListener('click', function () {
            toast('「' + item.name + '」的高保真页面在后续批次交付（阶段 3 按批推进）。', 'warning');
          });
        }
        menu.appendChild(node);
      });
    });
    aside.appendChild(menu);
    return aside;
  }

  function init() {
    var body = document.body;
    base = body.getAttribute('data-base') || '..';
    var mount = document.querySelector('#app-shell');
    var root = document.querySelector('#page-root');
    if (!mount || !root) return;
    var activePage = body.getAttribute('data-menu-page') || body.getAttribute('data-page-id') || '';

    var shell = el('div', 'hi-shell');
    shell.appendChild(buildSidebar(activePage));
    var main = el('div', 'hi-main');
    var navbar = el('header', 'hi-navbar');
    navbar.appendChild(el('span', 'hi-breadcrumb', body.getAttribute('data-breadcrumb') || ''));
    navbar.appendChild(el('span', 'spacer'));
    navbar.appendChild(el('span', 'hi-tenant', body.getAttribute('data-tenant') || '云溪实验学校（云溪教育集团）'));
    var user = el('span', 'hi-user', '');
    navbar.appendChild(user);
    main.appendChild(navbar);
    var tabbar = el('div', 'hi-tabbar');
    tabbar.appendChild(el('span', 'hi-tab on', body.getAttribute('data-tab') || '页面'));
    main.appendChild(tabbar);
    var content = el('main', 'hi-content');
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
    applyParams(parseParams());
    window.addEventListener('hashchange', function () { applyParams(parseParams()); });
    document.addEventListener('click', onDocumentClick);
    document.addEventListener('keydown', onKeydown);
  }

  function applyParams(params) {
    if (params.role) applyRole(params.role);
    if (params.state) applyState(params.state);
    if (params.panel) openPanel(params.panel);
    document.dispatchEvent(new CustomEvent('prototype:params', { detail: params }));
  }

  window.HiFiShell = {
    roles: ROLES,
    init: init,
    toast: toast,
    applyRole: applyRole,
    applyState: applyState,
    openPanel: openPanel,
    closePanel: closePanel,
    parseParams: parseParams,
    applyParams: applyParams,
    currentRole: function () { return state.role; },
    currentState: function () { return state.stateCode; }
  };
  // 与阶段 2 同名，便于 harness 复用同一段脚本
  window.PrototypeShell = window.HiFiShell;

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
