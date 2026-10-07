import { createWebHistory, createRouter, RouteRecordRaw } from 'vue-router';
/* Layout */
import Layout from '@/layout/index.vue';

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/social-callback',
    hidden: true,
    component: () => import('@/layout/components/SocialCallback/index.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/login.vue'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register.vue'),
    hidden: true
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401.vue'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index.vue'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  }
];

// 动态路由，基于用户权限动态去加载
//
// 教育域页面在阶段 6 逐批交付，交付一批在此登记一条静态路由，便于开发与交互对照；
// 阶段 8 由后端菜单（sys_menu）下发同名路由后，本处条目拆除（见 CR-044）。
export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: '/edu/student/list',
    component: Layout,
    hidden: true,
    permissions: ['person.student:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/student/stu_list/index.vue'),
        name: 'EduStudentList',
        meta: { title: '学生管理', icon: 'user' }
      }
    ]
  },
  {
    path: '/edu/student/cross-transfer',
    component: Layout,
    hidden: true,
    permissions: ['enrollment.transfer:create'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/student/stu_cross_transfer/index.vue'),
        name: 'EduStudentCrossTransfer',
        meta: { title: '跨校转学', icon: 'swap' }
      }
    ]
  },
  {
    path: '/edu/promotion/transfer',
    component: Layout,
    hidden: true,
    permissions: ['enrollment.transfer:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_transfer/index.vue'),
        name: 'EduPromotionTransfer',
        meta: { title: '转学接收', icon: 'swap' }
      }
    ]
  },
  {
    path: '/edu/class/list',
    component: Layout,
    hidden: true,
    permissions: ['org.class:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/class/cls_list/index.vue'),
        name: 'EduClassList',
        meta: { title: '班级管理', icon: 'tree' }
      }
    ]
  },
  {
    path: '/edu/student/import',
    component: Layout,
    hidden: true,
    permissions: ['data.import:import'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/student/stu_import/index.vue'),
        name: 'EduStudentImport',
        meta: { title: '学生批量导入', icon: 'upload' }
      }
    ]
  },
  {
    path: '/edu/grade/list',
    component: Layout,
    hidden: true,
    permissions: ['org.grade:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/grade/grd_list/index.vue'),
        name: 'EduGradeList',
        meta: { title: '年级管理', icon: 'tree' }
      }
    ]
  },
  {
    path: '/edu/teacher/list',
    component: Layout,
    hidden: true,
    permissions: ['person.teacher:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/teacher/tch_list/index.vue'),
        name: 'EduTeacherList',
        meta: { title: '教师管理', icon: 'user' }
      }
    ]
  },
  {
    path: '/edu/subject/list',
    component: Layout,
    hidden: true,
    permissions: ['org.subject:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/subject/sub_list/index.vue'),
        name: 'EduSubjectList',
        meta: { title: '学科与配置', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/term/list',
    component: Layout,
    hidden: true,
    permissions: ['org.term:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/term/term_list/index.vue'),
        name: 'EduTermList',
        meta: { title: '学年学期', icon: 'date' }
      }
    ]
  },
  {
    path: '/edu/school/list',
    component: Layout,
    hidden: true,
    permissions: ['org.school:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/school/sch_list/index.vue'),
        name: 'EduSchoolList',
        meta: { title: '学校管理', icon: 'tree' }
      }
    ]
  },
  {
    path: '/edu/audit/log/list',
    component: Layout,
    hidden: true,
    permissions: ['audit.log:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/audit/audit_log_list/index.vue'),
        name: 'EduAuditLogList',
        meta: { title: '操作日志', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/audit/operator-access',
    component: Layout,
    hidden: true,
    permissions: ['audit.log:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/audit/audit_ops_access/index.vue'),
        name: 'EduAuditOpsAccess',
        meta: { title: '运营访问记录', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/audit/sensitive-access',
    component: Layout,
    hidden: true,
    permissions: ['audit.log:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/audit/audit_sensitive_access/index.vue'),
        name: 'EduAuditSensitiveAccess',
        meta: { title: '敏感数据访问', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/audit/security-event',
    component: Layout,
    hidden: true,
    permissions: ['audit.log:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/audit/audit_security_event/index.vue'),
        name: 'EduAuditSecurityEvent',
        meta: { title: '登录与安全事件', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/audit/archive',
    component: Layout,
    hidden: true,
    permissions: ['audit.log:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/audit/audit_archive/index.vue'),
        name: 'EduAuditArchive',
        meta: { title: '归档管理', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/async-task/list',
    component: Layout,
    hidden: true,
    permissions: ['data.async_task:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/import-export/imp_task_list/index.vue'),
        name: 'EduAsyncTaskList',
        meta: { title: '异步任务', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/async-task/dead-letter',
    component: Layout,
    hidden: true,
    permissions: ['data.async_task:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/import-export/imp_deadletter/index.vue'),
        name: 'EduAsyncDeadLetter',
        meta: { title: '死信任务', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/promotion/history',
    component: Layout,
    hidden: true,
    permissions: ['enrollment.status:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_history/index.vue'),
        name: 'EduPromotionHistory',
        meta: { title: '异动历史', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/promotion/list',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_list/index.vue'),
        name: 'EduPromotionTaskList',
        meta: { title: '升班任务列表', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/promotion/create',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:create'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_create/index.vue'),
        name: 'EduPromotionCreate',
        meta: { title: '新建升班任务', icon: 'form' }
      }
    ]
  },
  {
    path: '/edu/promotion/preview',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_preview/index.vue'),
        name: 'EduPromotionPreview',
        meta: { title: '升班预览与调整', icon: 'form' }
      }
    ]
  },
  {
    path: '/edu/promotion/validate',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_validate/index.vue'),
        name: 'EduPromotionValidate',
        meta: { title: '升班校验', icon: 'check' }
      }
    ]
  },
  {
    path: '/edu/promotion/execute',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_execute/index.vue'),
        name: 'EduPromotionExecute',
        meta: { title: '执行与进度', icon: 'time' }
      }
    ]
  },
  {
    path: '/edu/promotion/result',
    component: Layout,
    hidden: true,
    permissions: ['promotion.batch:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/promotion/prm_result/index.vue'),
        name: 'EduPromotionResult',
        meta: { title: '执行结果与重试', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/school/campus',
    component: Layout,
    hidden: true,
    permissions: ['org.school:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/school/sch_campus/index.vue'),
        name: 'EduSchoolCampus',
        meta: { title: '校区管理', icon: 'tree' }
      }
    ]
  },
  {
    path: '/edu/class/roster/add',
    component: Layout,
    hidden: true,
    permissions: ['org.class:update'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/class/cls_roster_add/index.vue'),
        name: 'EduClassRosterAdd',
        meta: { title: '添加学生', icon: 'user' }
      }
    ]
  },
  {
    path: '/edu/class/move-students',
    component: Layout,
    hidden: true,
    permissions: ['org.class:update'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/class/cls_move/index.vue'),
        name: 'EduClassMove',
        meta: { title: '批量迁学生', icon: 'swap' }
      }
    ]
  },
  {
    path: '/edu/class/import-roster',
    component: Layout,
    hidden: true,
    permissions: ['data.import:import'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/class/cls_roster_import/index.vue'),
        name: 'EduClassRosterImport',
        meta: { title: '编班表导入', icon: 'upload' }
      }
    ]
  },
  {
    path: '/edu/class/teaching',
    component: Layout,
    hidden: true,
    permissions: ['org.teaching_class:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/class/cls_teaching/index.vue'),
        name: 'EduClassTeaching',
        meta: { title: '教学班管理', icon: 'list' }
      }
    ]
  },
  {
    path: '/edu/school/init',
    component: Layout,
    hidden: true,
    permissions: ['org.school:update'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/school/sch_init/index.vue'),
        name: 'EduSchoolInit',
        meta: { title: '开通初始化', icon: 'tree' }
      }
    ]
  },
  {
    path: '/edu/term/terms',
    component: Layout,
    hidden: true,
    permissions: ['org.term:read'],
    children: [
      {
        path: '',
        component: () => import('@/views/edu/term/term_terms/index.vue'),
        name: 'EduTermTerms',
        meta: { title: '学期管理', icon: 'date' }
      }
    ]
  }
];

/**
 * 创建路由
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_CONTEXT_PATH),
  routes: constantRoutes,
  // 刷新时，滚动条位置还原
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

export default router;
