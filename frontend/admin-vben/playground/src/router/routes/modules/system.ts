import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'ion:settings-outline',
      order: 9997,
      title: $t('system.title'),
    },
    name: 'System',
    path: '/system',
    children: [
      {
        path: '/system/user',
        name: 'SystemUser',
        meta: {
          icon: 'mdi:user',
          title: $t('system.user.title'),
        },
        component: () => import('#/views/system/user/list.vue'),
      },
      {
        path: '/system/role',
        name: 'SystemRole',
        meta: {
          icon: 'mdi:account-group',
          title: $t('system.role.title'),
        },
        component: () => import('#/views/system/role/list.vue'),
      },
      {
        path: '/system/menu',
        name: 'SystemMenu',
        meta: {
          icon: 'mdi:menu',
          title: $t('system.menu.title'),
        },
        component: () => import('#/views/system/menu/list.vue'),
      },
      {
        path: '/system/dept',
        name: 'SystemDept',
        meta: {
          icon: 'charm:organisation',
          title: $t('system.dept.title'),
        },
        component: () => import('#/views/system/dept/list.vue'),
      },
      {
        path: '/system/config',
        name: 'SystemConfig',
        meta: {
          icon: 'ion:options-outline',
          title: $t('system.config.title'),
        },
        component: () => import('#/views/system/config/list.vue'),
      },
      {
        path: '/system/login-log',
        name: 'SystemLoginLog',
        meta: {
          icon: 'ion:log-in-outline',
          title: $t('system.loginLog.title'),
        },
        component: () => import('#/views/system/loginLog/list.vue'),
      },
      {
        path: '/system/update-log',
        name: 'SystemUpdateLog',
        meta: {
          icon: 'ion:newspaper-outline',
          title: $t('system.updateLog.title'),
        },
        component: () => import('#/views/system/updateLog/list.vue'),
      },
    ],
  },
];

export default routes;
