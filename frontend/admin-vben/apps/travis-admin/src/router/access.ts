import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
  RouteRecordStringComponent,
} from '@vben/types';

import {generateAccessible} from '@vben/access';
import {preferences} from '@vben/preferences';

import {message} from 'antdv-next';

import {getAllMenusApi} from '#/api';
import {BasicLayout, IFrameView} from '#/layouts';
import {$t} from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

/**
 * 不在后端菜单中的隐藏路由，固定注入到前端路由表中
 */
const hiddenRoutes: RouteRecordStringComponent[] = [
  {
    name: 'Profile',
    path: '/profile',
    component: '_core/profile/index',
    meta: {
      hideInMenu: true,
      title: $t('page.auth.profile'),
    },
  },
  {
    name: 'Message',
    path: '/message',
    component: '_core/message/list',
    meta: {
      hideInMenu: true,
      title: $t('page.message.title'),
    },
  },
];


async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
  };

  return await generateAccessible(preferences.app.accessMode, {
    ...options,
    fetchMenuListAsync: async () => {
      const closeLoading = message.loading({
        content: `${$t('common.loadingMenu')}...`,
        duration: 0,
      });
      try {
        const menus = await getAllMenusApi();
        return [...menus, ...hiddenRoutes];
      } finally {
        closeLoading();
      }
    },
    // 可以指定没有权限跳转403页面
    forbiddenComponent,
    // 如果 route.meta.menuVisibleWithForbidden = true
    layoutMap,
    pageMap,
  });
}

export {generateAccess};
